package edu.cmu.cs.lti.semreranking.jnn;

import java.text.NumberFormat;
import java.util.Map;

import jnn.functions.nonparametrized.LogisticSigmoidLayer;
import jnn.mapping.OutputMappingDenseToDense;
import jnn.mapping.OutputMappingStringArrayToDenseArray;
import jnn.neuron.DenseNeuronArray;
import jnn.training.GraphInference;

import com.google.common.collect.Maps;

import edu.cmu.cs.lti.semreranking.Data;
import edu.cmu.cs.lti.semreranking.DataInstance;
import edu.cmu.cs.lti.semreranking.SemRerankerMain;
import edu.cmu.cs.lti.semreranking.TestData;
import edu.cmu.cs.lti.semreranking.TrainData;
import edu.cmu.cs.lti.semreranking.TrainInstance;
import edu.cmu.cs.lti.semreranking.datastructs.Argument;
import edu.cmu.cs.lti.semreranking.datastructs.Frame;
import edu.cmu.cs.lti.semreranking.datastructs.FrameSemanticParse;
import edu.cmu.cs.lti.semreranking.datastructs.Scored;
import edu.cmu.cs.lti.semreranking.evaluation.Evaluator;
import edu.cmu.cs.lti.semreranking.evaluation.Result;
import edu.cmu.cs.lti.semreranking.lossfunctions.Loss;
import edu.cmu.cs.lti.semreranking.utils.FileUtils.AllRerankingData;

public class FspRerankerApp {

    private final int maxEpochs = SemRerankerMain.numIter;
    private final int batchSize = 10;

    private ArrayParams ap; // sizes of all the vectors needed
    private FspNetworks network; // parameters of the network
    private FspLookupTables lookupTables; // inputs

    private NumberFormat formatter = SemRerankerMain.formatter;

    public FspRerankerApp(AllRerankingData allData, Loss loss) {
        ap = new ArrayParams(SemRerankerMain.paramDim);

        if (SemRerankerMain.useInitModel) {
            network = new FspNetworks(SemRerankerMain.initmodel);
        } else {
            network = new FspNetworks(ap);
        }

        lookupTables = new FspLookupTables(SemRerankerMain.usePretrained, ap, allData.vocabs);

        System.err.println("Parameter dimension = " + ap.argResultDim + "\n");

        /** Learning */
        System.err.println();
        doDeepLearning(allData.trainData, allData.testData, allData.devData, loss);
    }

    /** Algorithm */
    void doDeepLearning(TrainData trainData, TestData testData, TestData devData, Loss loss) {
        Result bestTestEval = new Result();
        Result bestTrainEval = new Result();

        for (int epoch = 0; epoch < maxEpochs; epoch++) {
            double totalLoss = 0.0;
            double avgRank = 0.0;
            System.err.print("it = " + epoch + "\t");

            for (int exNum = 0; exNum < trainData.size; exNum++) {
                TrainInstance trainInst = (TrainInstance) trainData.getInstance(exNum);
                Map<Integer, DenseNeuronArray> goldRankScoreMap = Maps.newHashMap();

                GraphInference inference = new GraphInference(0, true);
                inference.setNorm(1 / (double) (
                        batchSize * loss.getNumComparisons(trainInst.numUniqueParses)));

                DenseNeuronArray[] tokenInpArr = getInputsFromTokens(inference, trainInst);
                DenseNeuronArray[] posInpArr = getInputsFromPosTags(inference, trainInst);
                DenseNeuronArray[] prevPosInpArr = getInputsFromPosTags(inference, trainInst);
                DenseNeuronArray[] nextPosInpArr = getInputsFromPosTags(inference, trainInst);

                for (int goldRank = 0; goldRank < trainInst.numUniqueParses; goldRank++) {
                    Scored<FrameSemanticParse> fsp = trainInst.getParseAtRank(goldRank);
                    FspInputNeuronArrays fspInpArrs = new FspInputNeuronArrays(lookupTables, ap,
                            inference, fsp);
                    DenseNeuronArray score = buildFspRepresentation(tokenInpArr,
                            posInpArr, prevPosInpArr, nextPosInpArr, fspInpArrs, inference, fsp);
                    goldRankScoreMap.put(goldRank, score);
                }

                inference.init();
                inference.forward();

                totalLoss += loss.getLoss(goldRankScoreMap, trainInst);
                avgRank += loss.getPredictedTrainRankOfBestParse(goldRankScoreMap);
                inference.backward();

                if (exNum == trainData.trainInstances.size() - 1 || exNum % batchSize == 0) {
                    inference.commit(0);
                    // network.update();
                    // lookupTables.posTable.updateWeights(0.0, 0.0);
                    // lookupTables.frameArgTable.updateWeights(0.0, 0.0);
                    // lookupTables.frameTable.updateWeights(0.0, 0.0);
                }

            }

            System.err.print("loss = " + formatter.format(totalLoss) + "\t");
            System.err.print("avgrank = " + formatter.format(avgRank / trainData.size) + "\t");

            Map<Integer, Scored<FrameSemanticParse>> bestTrainRanks = doDeepDecoding(trainData);
            Result trainEval = Evaluator.getRerankedMicroAvg(bestTrainRanks);
            System.err.print("train : " + trainEval.toString() + "\t");

            // Map<Integer, Scored<FrameSemanticParse>> bestTestRanks = doDeepDecoding(testData);
            // Result testEval = Evaluator.getRerankedMicroAvg(bestTestRanks);
            // System.err.print("test = " + formatter.format(testEval.f1) + "\t");

            // Map<Integer, Scored<FrameSemanticParse>> bestDevRanks = doDeepDecoding(devData);
            // Result devEval = Evaluator.getRerankedMicroAvg(bestDevRanks);
            // System.err.print("dev = " + formatter.format(devEval.f1) + "\t");

            System.err.println();

            if (trainEval.compareTo(bestTrainEval) > 0) {
                // bestTestEval = testEval;
                bestTrainEval = trainEval;
                System.err.println("Writing down model for iteration " + epoch);
                network.saveAllParams("models/semrerank.epoch" + epoch + ".model");
            }
            Evaluator.writeRerankedBest(bestTrainRanks,
                    "logs/reranked.train." + epoch + ".frame.elements",
                    "logs/reranked.train." + epoch + ".ranks");
        }
        System.err.print("\nReport best:\t");
        System.err.println(bestTestEval.toString());
    }

    /* computation graph */
    private DenseNeuronArray buildFspRepresentation(
            DenseNeuronArray[] tokenInpArray,
            DenseNeuronArray[] posInpArray,
            DenseNeuronArray[] prevPosInpArray,
            DenseNeuronArray[] nextPosInpArray,
            FspInputNeuronArrays inp,
            GraphInference inference,
            Scored<FrameSemanticParse> scoredFsp) {

        DenseNeuronArray sentLinear = new DenseNeuronArray(ap.resultDim);
        sentLinear.setName("sentence linear");
        inference.addNeurons(sentLinear);

        // Set<Integer> tokensParticipatingInFrames = Sets.newHashSet(); // don't add in null frame
        int i = 0;
        int j = 0;
        for (Frame frame : scoredFsp.entity.frames) {

            DenseNeuronArray frameLinear = new DenseNeuronArray(ap.frameResultDim);
            frameLinear.setName("frame linear");
            inference.addNeurons(frameLinear);

            for (int pos = frame.predStartPos; pos <= frame.predEndPos; pos++) {
                inference.addMapping(new OutputMappingDenseToDense(
                        tokenInpArray[pos], frameLinear, network.tokenLayer));
                inference.addMapping(new OutputMappingDenseToDense(
                        posInpArray[pos], frameLinear, network.posLayer));
                // tokensParticipatingInFrames.add(pos);
            }
            // inference.addMapping(new OutputMappingDenseToDense(inp.posArray[i], frameLinear,
            // network.goldFNPosLayer));

            inference.addMapping(new OutputMappingDenseToDense(
                    inp.frameIdsArray[i], frameLinear, network.frameLayer));

            for (Argument arg : frame.arguments) {

                DenseNeuronArray argLinear = new DenseNeuronArray(ap.argResultDim);
                argLinear.setName("arg linear");
                inference.addNeurons(argLinear);
                inference.addMapping(new OutputMappingDenseToDense(
                        inp.frameArgIdsArray[j], argLinear, network.argLayer));

                for (int pos = arg.start; pos <= arg.end; pos++) {
                    if (pos == -1) { // for frames with no arguments
                        continue;
                    }
                    // tokensParticipatingInFrames.add(pos);
                    inference.addMapping(new OutputMappingDenseToDense(
                            tokenInpArray[pos], argLinear, network.tokenLayer));
                    inference.addMapping(new OutputMappingDenseToDense(
                            posInpArray[pos], argLinear, network.posLayer));
                }

                // adding window of 2 prev tokens/pos tags
                for (int prev = arg.start - 1; prev > arg.start - 3; prev--) {
                    if (prev < 0) {
                        break;
                    }
                    inference.addMapping(new OutputMappingDenseToDense(
                            tokenInpArray[prev], argLinear, network.tokenLayer));
                    inference.addMapping(new OutputMappingDenseToDense(
                            prevPosInpArray[prev], argLinear, network.posLayer));
                }

                // adding window of 2 next tokens/pos tags
                for (int next = arg.end + 1; next < arg.start + 3; next++) {
                    if (next >= tokenInpArray.length) {
                        break;
                    }
                    inference.addMapping(new OutputMappingDenseToDense(
                            tokenInpArray[next], argLinear, network.tokenLayer));
                    inference.addMapping(new OutputMappingDenseToDense(
                            nextPosInpArray[next], argLinear, network.posLayer));
                }

                DenseNeuronArray spanSize = getSpanSizeBins(arg.end - arg.start);
                inference.addNeurons(spanSize);
                inference.addMapping(new OutputMappingDenseToDense(spanSize, argLinear,
                        network.spanSizeLayer));

                DenseNeuronArray argNonlinear = new DenseNeuronArray(ap.argResultDim);
                argNonlinear.setName("arg non-linear");
                inference.addNeurons(argNonlinear);
                inference.addMapping(new OutputMappingDenseToDense(
                        argLinear, argNonlinear, LogisticSigmoidLayer.singleton));

                inference.addMapping(new OutputMappingDenseToDense(
                        argNonlinear, frameLinear, network.allArgsLayer));
                j++;
            }

            DenseNeuronArray numArgs = getNumArgsBins(frame.numArgs);
            inference.addNeurons(numArgs);
            inference.addMapping(new OutputMappingDenseToDense(numArgs, frameLinear,
                    network.numArgsLayer));

            DenseNeuronArray frameSemScore = new DenseNeuronArray(2);
            frameSemScore.setName("frame sem score");
            frameSemScore.init();
            frameSemScore.addNeuron(0, frame.score);
            inference.addNeurons(frameSemScore);
            inference.addMapping(new OutputMappingDenseToDense(frameSemScore, frameLinear, network
                    .semScoreLayer));

            DenseNeuronArray frameNonlinear = new DenseNeuronArray(ap.frameResultDim);
            frameNonlinear.setName("frame non linear");
            inference.addNeurons(frameNonlinear);
            inference.addMapping(new OutputMappingDenseToDense(frameLinear,
                    frameNonlinear, LogisticSigmoidLayer.singleton));

            inference.addMapping(new OutputMappingDenseToDense(frameNonlinear,
                    sentLinear, network.allFramesLayer));
            i++;
        }

        // adding null-frame vectors
        // for (int pos = 0; pos < tokenInpArray.length; pos++) {
        // if (tokensParticipatingInFrames.contains(pos) == false) {
        // inference.addMapping(new OutputMappingDenseToDense(tokenInpArray[pos],
        // sentLinear, network.getTokenLayer()));
        // inference.addMapping(new OutputMappingDenseToDense(posInpArray[pos],
        // sentLinear, network.getTokenLayer()));
        // }
        // }

        DenseNeuronArray synScore = new DenseNeuronArray(2);
        synScore.setName("syntactic");
        synScore.init();
        synScore.addNeuron(0, scoredFsp.synScore);
        synScore.addNeuron(1, scoredFsp.entity.semaforScore); // full semantic score
        inference.addNeurons(synScore);
        inference.addMapping(new OutputMappingDenseToDense(synScore, sentLinear,
                network.synScoreLayer));

        DenseNeuronArray sentNonLinear = new DenseNeuronArray(ap.resultDim);
        sentNonLinear.setName("sentence non-linear");
        inference.addNeurons(sentNonLinear);
        inference.addMapping(new OutputMappingDenseToDense(
                sentLinear, sentNonLinear, LogisticSigmoidLayer.singleton));

        DenseNeuronArray sentScore = new DenseNeuronArray(1);
        sentScore.setName("sentence score");
        inference.addNeurons(sentScore);
        inference.addMapping(new OutputMappingDenseToDense(
                sentNonLinear, sentScore, network.scoreLayer));

        return sentScore;
    }

    public Map<Integer, Scored<FrameSemanticParse>> doDeepDecoding(Data data) {
        Map<Integer, Scored<FrameSemanticParse>> bestRanks = Maps.newTreeMap(); // preserve ex order

        for (int exNum = 0; exNum < data.size; exNum++) {
            DataInstance instance = data.getInstance(exNum);

            GraphInference inference = new GraphInference(0, false);
            DenseNeuronArray[] tokenInpArray = getInputsFromTokens(inference, instance);
            DenseNeuronArray[] posInpArray = getInputsFromTokens(inference, instance);
            DenseNeuronArray[] prevPosInpArray = getInputsFromTokens(inference, instance);
            DenseNeuronArray[] nextPosInpArray = getInputsFromTokens(inference, instance);

            Map<Scored<FrameSemanticParse>, DenseNeuronArray> fspScoreMap = Maps.newHashMap();
            Map<Integer, DenseNeuronArray> rankScoreMap = Maps.newHashMap();
            for (int rank = 0; rank < instance.numUniqueParses; rank++) {
                Scored<FrameSemanticParse> scoredFsp = instance.getParseAtRank(rank);

                FspInputNeuronArrays fspInp = new FspInputNeuronArrays(lookupTables, ap, inference,
                        scoredFsp);
                DenseNeuronArray score = buildFspRepresentation(tokenInpArray, posInpArray,
                        prevPosInpArray, nextPosInpArray, fspInp, inference, scoredFsp);
                fspScoreMap.put(scoredFsp, score);
                rankScoreMap.put(rank, score);
            }
            inference.init();
            inference.forward();

            double max = Double.NEGATIVE_INFINITY;
            Scored<FrameSemanticParse> argmax = null;
            for (Scored<FrameSemanticParse> candidate : fspScoreMap.keySet()) {
                if (fspScoreMap.get(candidate).getNeuron(0) > max) {
                    max = fspScoreMap.get(candidate).getNeuron(0);
                    argmax = candidate;
                }
            }

            if (argmax == null) {
                System.err.println("STOP: TEST scores are not real numbers any more " + max);
                System.err.println(exNum);
                inference.printNeurons();
            }
            bestRanks.put(exNum, argmax);
        }
        return bestRanks;
    }

    private DenseNeuronArray[] getInputsFromTokens(
            GraphInference inference, DataInstance instance) {
        DenseNeuronArray[] tokenInpArray = DenseNeuronArray.asArray(instance.size,
                ap.tokenInpDim);
        inference.addNeurons(tokenInpArray);

        inference.addMapping(new OutputMappingStringArrayToDenseArray(instance.tokens,
                tokenInpArray, lookupTables.tokenTable));

        return tokenInpArray;
    }

    private DenseNeuronArray[] getInputsFromPosTags(
            GraphInference inference, DataInstance instance) {
        DenseNeuronArray[] posInpArray = DenseNeuronArray.asArray(instance.size,
                ap.posInpDim); // TODO: what about the dimensions?
        inference.addNeurons(posInpArray);

        inference.addMapping(new OutputMappingStringArrayToDenseArray(instance.posTags,
                posInpArray, lookupTables.posTable));

        return posInpArray;
    }

    private DenseNeuronArray getSpanSizeBins(int spanSz) {
        DenseNeuronArray spanSizeNeuronArray = new DenseNeuronArray(ap.spanSizeDim);
        spanSizeNeuronArray.setName("span size");
        spanSizeNeuronArray.init();
        for (int index : ArrayParams.spanMap.keySet()) {
            if (spanSz >= ArrayParams.spanMap.get(index)) {
                spanSizeNeuronArray.addNeuron(0, 1);
            }
        }
        return spanSizeNeuronArray;
    }

    private DenseNeuronArray getNumArgsBins(int numArgs) {
        DenseNeuronArray numArgsNeuronArray = new DenseNeuronArray(ap.numArgsDim);
        numArgsNeuronArray.setName("# args");
        numArgsNeuronArray.init();
        for (int index : ArrayParams.numArgsMap.keySet()) {
            if (numArgs >= ArrayParams.numArgsMap.get(index)) {
                numArgsNeuronArray.addNeuron(0, 1);
            }
        }
        return numArgsNeuronArray;
    }

}
