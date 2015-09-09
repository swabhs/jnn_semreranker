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
import edu.cmu.cs.lti.semreranking.datastructs.FrameSemParse;
import edu.cmu.cs.lti.semreranking.datastructs.FrameSemParse.FrameIdentifier;
import edu.cmu.cs.lti.semreranking.datastructs.Scored;
import edu.cmu.cs.lti.semreranking.evaluation.Evaluator;
import edu.cmu.cs.lti.semreranking.evaluation.Result;
import edu.cmu.cs.lti.semreranking.lossfunctions.Loss;
import edu.cmu.cs.lti.semreranking.utils.DataFilesReader.AllRerankingData;

public class FspRerankerApp {

    private final int maxEpochs = SemRerankerMain.numIter;
    private final int batchSize = 10;

    private ArrayParams ap; // sizes of all the vectors needed
    private FspNetworks network; // parameters of the network
    private FspLookupTables lookupTables; // inputs

    private NumberFormat formatter = SemRerankerMain.formatter;

    public FspRerankerApp(AllRerankingData allData, Loss loss) {
        ap = new ArrayParams(SemRerankerMain.paramDim, SemRerankerMain.dimFile);

        if (SemRerankerMain.useInitModel) {
            network = new FspNetworks(SemRerankerMain.initmodel);
        } else {
            network = new FspNetworks(ap);
        }

        lookupTables = new FspLookupTables(SemRerankerMain.usePretrained, ap, allData.vocabs);

        System.err.println("Parameter dimension = " + ap.argResultDim + "\n");

        /** Learning */
        System.err.println();
        doDeepLearning(allData.trainData, allData.devData, loss);
    }

    /** Algorithm */
    void doDeepLearning(TrainData trainData, TestData devData, Loss loss) {
        Result bestTestEval = new Result();
        Result bestDevEval = new Result();

        for (int epoch = 0; epoch < maxEpochs; epoch++) {
            double totalLoss = 0.0;
            // double avgRank = 0.0;
            System.err.print("it = " + epoch + "\t");

            for (int exNum : trainData.trainInstances.keySet()) {
                for (FrameIdentifier identifier : trainData.getFramesInEx(exNum)) {
                    TrainInstance trainInst = (TrainInstance) trainData.getInstance(exNum,
                            identifier);
                    Map<Integer, DenseNeuronArray> goldRankScoreMap = Maps.newHashMap();

                    GraphInference inference = new GraphInference(0, true);
                    inference.setNorm(1 / (double) (
                            batchSize * loss.getNumComparisons(trainInst.numUniqueParses)));

                    DenseNeuronArray[] tokenInpArr = getInputsFromTokens(inference, trainInst,
                            trainData.tokens.get(exNum));
                    DenseNeuronArray[] posInpArr = getInputsFromPosTags(inference, trainInst,
                            trainData.posTags.get(exNum));

                    for (int goldRank = 0; goldRank < trainInst.numUniqueParses; goldRank++) {
                        Scored<FrameSemParse> fsp = trainInst.getParseAtRank(goldRank);
                        NewFspInputNeuronArrays fspInpArrs = new NewFspInputNeuronArrays(
                                lookupTables,
                                ap,
                                inference, fsp.entity);

                        DenseNeuronArray score = buildFspRepresentation(
                                tokenInpArr,
                                posInpArr,
                                fspInpArrs,
                                inference,
                                fsp);
                        goldRankScoreMap.put(goldRank, score); // TODO : means nothing yet
                    }

                    inference.init();
                    inference.forward();

                    totalLoss += loss.getLoss(goldRankScoreMap, trainInst);
                    // avgRank += loss.getPredictedTrainRankOfBestParse(goldRankScoreMap);
                    inference.backward();

                    if (exNum == trainData.trainInstances.size() - 1 || exNum % batchSize == 0) {
                        // inference.commit(0); // if you want to overfit to train. if u want to
                        // generalize, uncomment and use below...
                        network.update();
                        lookupTables.posTable.updateWeights(0.0, 0.0);
                        lookupTables.frameArgTable.updateWeights(0.0, 0.0);
                        lookupTables.frameTable.updateWeights(0.0, 0.0);
                    }
                }
            }

            System.err.print("loss = " + formatter.format(totalLoss) + "\t");
            // System.err.print("avgrank = " + formatter.format(avgRank / trainData.size) + "\t");

            Map<Integer, Map<FrameIdentifier, Scored<FrameSemParse>>> bestTrainRanks = doDeepDecoding(trainData);
            Result trainEval = Evaluator.getRerankedMicroAvgNew(bestTrainRanks);
            System.err.print("train = " + formatter.format(trainEval.f1) + "\t");

            Map<Integer, Map<FrameIdentifier, Scored<FrameSemParse>>> bestDevRanks = doDeepDecoding(devData);
            Result devEval = Evaluator.getRerankedMicroAvgNew(bestDevRanks);
            System.err.print("dev = " + formatter.format(devEval.f1) + "\t");

            if (devEval.compareTo(bestDevEval) > 0) {
                bestDevEval = devEval;

                System.err.println("\nWriting down model for iteration " + epoch);
                network.saveAllParams("models/semrerank.epoch" + epoch + ".model");
            } else {
                System.err.println();
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
            NewFspInputNeuronArrays inp,
            GraphInference inference,
            Scored<FrameSemParse> scoredFsp) {

        int argNumber = 0;

        DenseNeuronArray frameLinear = new DenseNeuronArray(ap.frameResultDim);
        frameLinear.setName("frame linear");
        inference.addNeurons(frameLinear);

        FrameSemParse fsp = scoredFsp.entity;
        for (int pos = fsp.predStartPos; pos <= fsp.predEndPos; pos++) {
            inference.addMapping(new OutputMappingDenseToDense(
                    tokenInpArray[pos], frameLinear, network.tokenLayer));
            inference.addMapping(new OutputMappingDenseToDense(
                    posInpArray[pos], frameLinear, network.posLayer));
        }

        inference.addMapping(new OutputMappingDenseToDense(
                inp.frameIdArray, frameLinear, network.frameLayer));

        for (Argument arg : fsp.arguments) {

            DenseNeuronArray argLinear = new DenseNeuronArray(ap.argResultDim);
            argLinear.setName("arg linear");
            inference.addNeurons(argLinear);
            inference.addMapping(new OutputMappingDenseToDense(
                    inp.argIdsArray[argNumber], argLinear, network.argLayer));

            for (int pos = arg.start; pos <= arg.end; pos++) {
                if (pos == -1) { // for frames with no arguments
                    continue;
                }
                inference.addMapping(new OutputMappingDenseToDense(
                        tokenInpArray[pos], argLinear, network.tokenLayer));
                inference.addMapping(new OutputMappingDenseToDense(
                        posInpArray[pos], argLinear, network.posLayer));
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
            argNumber++;
        }

        DenseNeuronArray numArgs = getNumArgsBins(fsp.numArgs);
        inference.addNeurons(numArgs);
        inference.addMapping(new OutputMappingDenseToDense(numArgs, frameLinear,
                network.numArgsLayer));

        DenseNeuronArray frameNonlinear = new DenseNeuronArray(ap.frameResultDim);
        frameNonlinear.setName("frame non linear");
        inference.addNeurons(frameNonlinear);
        inference.addMapping(new OutputMappingDenseToDense(frameLinear,
                frameNonlinear, LogisticSigmoidLayer.singleton));

        DenseNeuronArray predictedFrameScore = new DenseNeuronArray(1);
        predictedFrameScore.setName("frame global predicted score");
        inference.addNeurons(predictedFrameScore);
        inference.addMapping(new OutputMappingDenseToDense(
                frameNonlinear, predictedFrameScore, network.globalScoreLayer));

        DenseNeuronArray localScores = new DenseNeuronArray(2);
        localScores.setName("local scores by semafor and input syntax score by turboparser");
        localScores.init();
        localScores.addNeuron(0, scoredFsp.synScore);
        localScores.addNeuron(1, fsp.semaforScore); // local score assigned by semafor
        inference.addNeurons(localScores);
        inference.addMapping(new OutputMappingDenseToDense(localScores, predictedFrameScore,
                network.localScoresLayer));

        return predictedFrameScore;
    }

    public Map<Integer, Map<FrameIdentifier, Scored<FrameSemParse>>> doDeepDecoding(Data data) {
        Map<Integer, Map<FrameIdentifier, Scored<FrameSemParse>>> bestRanks = Maps.newTreeMap();
        for (int exNum = 0; exNum < data.numInstances; exNum++) {
            Map<FrameIdentifier, Scored<FrameSemParse>> bestParsesForAnalysis = Maps.newHashMap();
            for (FrameIdentifier identifier : data.getFramesInEx(exNum)) {
                DataInstance instance = data.getInstance(exNum, identifier);

                GraphInference inference = new GraphInference(0, false);
                DenseNeuronArray[] tokenInpArr = getInputsFromTokens(inference, instance,
                        data.tokens.get(exNum));
                DenseNeuronArray[] posInpArr = getInputsFromPosTags(inference, instance,
                        data.posTags.get(exNum));

                Map<Scored<FrameSemParse>, DenseNeuronArray> fspScoreMap = Maps.newHashMap();
                Map<Integer, DenseNeuronArray> rankScoreMap = Maps.newHashMap();
                for (int rank = 0; rank < instance.numUniqueParses; rank++) {
                    Scored<FrameSemParse> fsp = instance.getParseAtRank(rank);

                    NewFspInputNeuronArrays fspInpArrs = new NewFspInputNeuronArrays(lookupTables,
                            ap,
                            inference,
                            fsp.entity);

                    DenseNeuronArray score = buildFspRepresentation(
                            tokenInpArr,
                            posInpArr,
                            fspInpArrs,
                            inference,
                            fsp);
                    fspScoreMap.put(fsp, score); // TODO : means nothing yet
                    rankScoreMap.put(rank, score); // TODO : means nothing yet
                }
                inference.init();
                inference.forward();

                double max = Double.NEGATIVE_INFINITY;
                Scored<FrameSemParse> argmax = null;
                for (Scored<FrameSemParse> candidate : fspScoreMap.keySet()) {
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
                bestParsesForAnalysis.put(identifier, argmax);
            }

            bestRanks.put(exNum, bestParsesForAnalysis);
        }
        return bestRanks;
    }

    private DenseNeuronArray[] getInputsFromTokens(
            GraphInference inference, DataInstance instance, String[] tokens) {
        DenseNeuronArray[] tokenInpArray = DenseNeuronArray.asArray(tokens.length, ap.tokenInpDim);
        inference.addNeurons(tokenInpArray);

        inference.addMapping(new OutputMappingStringArrayToDenseArray(tokens,
                tokenInpArray, lookupTables.tokenTable));
        return tokenInpArray;
    }

    private DenseNeuronArray[] getInputsFromPosTags(
            GraphInference inference, DataInstance instance, String[] posTags) {
        DenseNeuronArray[] posInpArray = DenseNeuronArray.asArray(posTags.length,
                ap.posInpDim); // TODO: what about the dimensions?
        inference.addNeurons(posInpArray);

        inference.addMapping(new OutputMappingStringArrayToDenseArray(posTags,
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
