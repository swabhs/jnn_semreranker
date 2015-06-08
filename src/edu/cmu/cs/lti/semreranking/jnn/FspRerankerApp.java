package edu.cmu.cs.lti.semreranking.jnn;

import java.text.NumberFormat;
import java.util.Map;
import java.util.Set;

import jnn.functions.nonparametrized.LogisticSigmoidLayer;
import jnn.mapping.OutputMappingDenseToDense;
import jnn.mapping.OutputMappingStringArrayToDenseArray;
import jnn.neuron.DenseNeuronArray;
import jnn.training.GraphInference;
import util.MathUtils;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import edu.cmu.cs.lti.semreranking.DataInstance;
import edu.cmu.cs.lti.semreranking.SemRerankerMain;
import edu.cmu.cs.lti.semreranking.TestData;
import edu.cmu.cs.lti.semreranking.TestInstance;
import edu.cmu.cs.lti.semreranking.TrainData;
import edu.cmu.cs.lti.semreranking.TrainInstance;
import edu.cmu.cs.lti.semreranking.datastructs.Argument;
import edu.cmu.cs.lti.semreranking.datastructs.Frame;
import edu.cmu.cs.lti.semreranking.datastructs.FrameNetVocabs;
import edu.cmu.cs.lti.semreranking.datastructs.FrameSemanticParse;
import edu.cmu.cs.lti.semreranking.datastructs.Scored;
import edu.cmu.cs.lti.semreranking.evaluation.Evaluator;
import edu.cmu.cs.lti.semreranking.evaluation.Result;
import edu.cmu.cs.lti.semreranking.lossfunctions.PairwiseLoss;

public class FspRerankerApp {

    private final int maxEpochs = SemRerankerMain.numIter;
    private final int batchSize = 10;

    private ArrayParams ap; // sizes of all the vectors needed
    private FspNetworks network; // parameters of the network
    private FspLookupTables lookupTables; // inputs

    private NumberFormat formatter = SemRerankerMain.formatter;

    public FspRerankerApp(TrainData data, TestData testData, TestData devData, FrameNetVocabs vocabs) {
        ap = new ArrayParams(SemRerankerMain.paramDim);
        network = new FspNetworks(ap);
        lookupTables = new FspLookupTables(SemRerankerMain.usePretrained, ap, vocabs);

        System.err.println("Parameter dimension = " + ap.argResultDim + "\n");

        /** Learning */
        System.err.println();
        doDeepLearning(data, testData, devData);
    }

    /** Algorithm */
    void doDeepLearning(TrainData trainData, TestData testData, TestData devData) {

        double errorPerEx[] = new double[trainData.trainInstances.size()];
        Map<Integer, DenseNeuronArray> finalScoresPerEx = Maps.newHashMap();

        Result bestDevEval = new Result();
        Result bestTestEval = new Result();
        for (int epoch = 0; epoch < maxEpochs; epoch++) {

            System.err.print("it = " + epoch + "\t");

            int exNum = 0;
            for (TrainInstance instance : trainData.trainInstances) {
                finalScoresPerEx = Maps.newHashMap();

                GraphInference inference = new GraphInference(0, true);
                int numComparisons = instance.numParses * (instance.numParses + 1) / 2;
                inference.setNorm(1 / (double) (batchSize * numComparisons));

                DenseNeuronArray[] tokenInpArr = getInputsFromTokens(inference, instance);
                DenseNeuronArray[] posInpArr = getInputsFromPosTags(inference, instance);

                for (int rank = 0; rank < instance.kbestParses.size(); rank++) {
                    Scored<FrameSemanticParse> semParse = instance.kbestParses.get(rank);
                    FspInputNeuronArrays fspInp = new FspInputNeuronArrays(lookupTables, ap,
                            inference, semParse);
                    DenseNeuronArray score = buildFspRepresentation(
                            tokenInpArr, posInpArr, fspInp, inference, semParse);
                    finalScoresPerEx.put(rank, score);
                }

                inference.init();
                inference.forward();
                errorPerEx[exNum] = PairwiseLoss.getLoss(finalScoresPerEx, instance);
                inference.backward();
                exNum++;
                if (exNum == trainData.trainInstances.size() || exNum % batchSize == 0) {
                    // inference.commit(0);
                    network.update();
                    lookupTables.posTable.updateWeights(0.0, 0.0);
                    lookupTables.goldFNPosTable.updateWeights(0.0, 0.0);
                    lookupTables.frameArgTable.updateWeights(0.0, 0.0);
                    lookupTables.frameTable.updateWeights(0.0, 0.0);
                }

            }

            double error = MathUtils.arraySum(errorPerEx);
            System.err.print("error = " + formatter.format(error) + "\t");

            Map<Integer, Integer> bestTrainRanks = doDeepDecoding(trainData);
            Result trainEval = Evaluator.getTrainRerankedMicroAvg(trainData.trainInstances,
                    bestTrainRanks);
            System.err.print("train = " + formatter.format(trainEval.f1) + "\t");

            Map<Integer, Integer> bestTestRanks = doDeepDecoding(testData);
            Result testEval = Evaluator.getRerankedMicroAvg(testData.testInstances,
                    bestTestRanks);
            System.err.print("test = " + formatter.format(testEval.f1) + "\t");

            Map<Integer, Integer> bestDevRanks = doDeepDecoding(devData);
            Result devEval = Evaluator.getRerankedMicroAvg(devData.testInstances, bestDevRanks);
            System.err.print("dev = " + formatter.format(devEval.f1) + "\t");

            System.err.println();

            if (devEval.compareTo(bestDevEval) > 0) {
                bestDevEval = devEval;
                bestTestEval = testEval;
                System.err.println("Writing down model for iteration " + epoch);
                network.saveAllParams("models/semrerank" + epoch + ".model");
            }

        }
        System.err.print("\nReport best:\t");
        bestTestEval.print();
    }

    /* computation graph */
    private DenseNeuronArray buildFspRepresentation(
            DenseNeuronArray[] tokenInpArray,
            DenseNeuronArray[] posInpArray,
            FspInputNeuronArrays inp,
            GraphInference inference,
            Scored<FrameSemanticParse> scoredFsp) {

        DenseNeuronArray sentenceLinear = new DenseNeuronArray(ap.resultDim);
        sentenceLinear.setName("sentence linear");
        inference.addNeurons(sentenceLinear);

        Set<Integer> tokensParticipatingInFrames = Sets.newHashSet(); // don't add in null frame
        int i = 0;
        int j = 0;
        for (Frame frame : scoredFsp.entity.frames) {

            DenseNeuronArray frameLinear = new DenseNeuronArray(ap.frameResultDim);
            frameLinear.setName("frame linear");
            inference.addNeurons(frameLinear);

            for (int pos = frame.predStartPos; pos <= frame.predEndPos; pos++) {
                inference.addMapping(new OutputMappingDenseToDense(
                        tokenInpArray[pos], frameLinear, network.getTokenLayer()));
                inference.addMapping(new OutputMappingDenseToDense(
                        posInpArray[pos], frameLinear, network.getPosLayer()));
                tokensParticipatingInFrames.add(pos);
            }
            inference.addMapping(new OutputMappingDenseToDense(inp.posArray[i], frameLinear,
                    network.goldFNPosLayer));

            inference.addMapping(new OutputMappingDenseToDense(
                    inp.frameIdsArray[i], frameLinear, network.getFrameLayer()));

            for (Argument arg : frame.arguments) {

                DenseNeuronArray argLinear = new DenseNeuronArray(ap.argResultDim);
                argLinear.setName("arg linear");
                inference.addNeurons(argLinear);
                inference.addMapping(new OutputMappingDenseToDense(
                        inp.frameArgIdsArray[j], argLinear, network.getArgLayer()));

                for (int pos = arg.start; pos <= arg.end; pos++) {
                    if (pos == -1) { // for frames with no arguments
                        continue;
                    }
                    tokensParticipatingInFrames.add(pos);
                    inference.addMapping(new OutputMappingDenseToDense(
                            tokenInpArray[pos], argLinear, network.getTokenLayer()));
                    inference.addMapping(new OutputMappingDenseToDense(
                            posInpArray[pos], argLinear, network.getPosLayer()));
                }

                DenseNeuronArray argNonlinear = new DenseNeuronArray(ap.argResultDim);
                argNonlinear.setName("arg non-linear");
                inference.addNeurons(argNonlinear);
                inference.addMapping(new OutputMappingDenseToDense(
                        argLinear, argNonlinear, LogisticSigmoidLayer.singleton));

                inference.addMapping(new OutputMappingDenseToDense(
                        argNonlinear, frameLinear, network.getAllArgsLayer()));
                j++;
            }

            DenseNeuronArray frameSemScore = new DenseNeuronArray(2);
            frameSemScore.setName("frame sem score");
            frameSemScore.init();
            frameSemScore.addNeuron(0, frame.score);
            inference.addNeurons(frameSemScore);
            inference.addMapping(new OutputMappingDenseToDense(frameSemScore, frameLinear, network
                    .getSemScoreLayer()));

            DenseNeuronArray frameNonlinear = new DenseNeuronArray(ap.frameResultDim);
            frameNonlinear.setName("frame non linear");
            inference.addNeurons(frameNonlinear);
            inference.addMapping(new OutputMappingDenseToDense(frameLinear,
                    frameNonlinear, LogisticSigmoidLayer.singleton));

            inference.addMapping(new OutputMappingDenseToDense(frameNonlinear,
                    sentenceLinear, network.getAllFramesLayer()));
            i++;
        }

        // adding null-frame vectors
        for (int pos = 0; pos < tokenInpArray.length; pos++) {
            if (tokensParticipatingInFrames.contains(pos) == false) {
                inference.addMapping(new OutputMappingDenseToDense(tokenInpArray[pos],
                        sentenceLinear, network.getTokenLayer()));
            }
        }

        DenseNeuronArray synScore = new DenseNeuronArray(2);
        synScore.setName("syntactic");
        synScore.init();
        synScore.addNeuron(0, scoredFsp.synScore);
        synScore.addNeuron(1, scoredFsp.entity.semaforScore); // full semantic score
        inference.addNeurons(synScore);
        inference.addMapping(new OutputMappingDenseToDense(synScore, sentenceLinear,
                network.getSynSemScoreLayer()));

        DenseNeuronArray sentNonlinear = new DenseNeuronArray(ap.resultDim);
        sentNonlinear.setName("sentence non-linear");
        inference.addNeurons(sentNonlinear);
        inference.addMapping(new OutputMappingDenseToDense(
                sentenceLinear, sentNonlinear, LogisticSigmoidLayer.singleton));

        DenseNeuronArray sentScore = new DenseNeuronArray(1);
        sentScore.setName("sentence score");
        inference.addNeurons(sentScore);
        inference.addMapping(new OutputMappingDenseToDense(
                sentenceLinear, sentScore, network.getScoreLayer()));

        return sentScore;
    }

    public Map<Integer, Integer> doDeepDecoding(TestData data) {
        Map<Integer, Integer> bestRanks = Maps.newHashMap();
        for (int exNum : data.testInstances.keySet()) {
            Map<Integer, DenseNeuronArray> scoreNeuronArray = Maps.newHashMap();

            GraphInference inference = new GraphInference(0, true);
            TestInstance instance = data.testInstances.get(exNum);
            DenseNeuronArray[] tokenInpArray = getInputsFromTokens(inference, instance);
            DenseNeuronArray[] posInpArray = getInputsFromTokens(inference, instance);

            int rank = 0;
            for (Scored<FrameSemanticParse> scoredFsp : instance.unsortedParses) {

                FspInputNeuronArrays fspInp = new FspInputNeuronArrays(lookupTables, ap, inference,
                        scoredFsp);
                DenseNeuronArray score = buildFspRepresentation(
                        tokenInpArray, posInpArray, fspInp, inference, scoredFsp);
                scoreNeuronArray.put(rank, score);
                rank++;

            }
            inference.init();
            inference.forward();

            double max = Double.NEGATIVE_INFINITY;
            int argmax = -1;
            for (int r : scoreNeuronArray.keySet()) {
                if (scoreNeuronArray.get(r).getNeuron(0) > max) {
                    max = scoreNeuronArray.get(r).getNeuron(0);
                    argmax = r;
                }
            }
            if (argmax == -1) {
                System.err.println("STOP: TEST scores are not real numbers any more " + max);
                System.err.println(exNum + " " + rank);
                inference.printNeurons();
            }
            bestRanks.put(exNum, argmax);
        }
        return bestRanks;
    }

    private Map<Integer, Integer> doDeepDecoding(TrainData data) {
        Map<Integer, Integer> bestRanks = Maps.newHashMap();
        int exNum = 0;
        for (TrainInstance instance : data.trainInstances) {
            Map<Integer, DenseNeuronArray> scoreNeuronArray = Maps.newHashMap();

            GraphInference inference = new GraphInference(0, true);
            DenseNeuronArray[] tokenInpArray = getInputsFromTokens(inference, instance);
            DenseNeuronArray[] posInpArray = getInputsFromTokens(inference, instance);

            for (int rank : instance.kbestParses.keySet()) {
                Scored<FrameSemanticParse> scoredFsp = instance.kbestParses.get(rank);
                FspInputNeuronArrays fspInp = new FspInputNeuronArrays(lookupTables, ap, inference,
                        scoredFsp);
                DenseNeuronArray score = buildFspRepresentation(tokenInpArray, posInpArray, fspInp,
                        inference, scoredFsp);
                scoreNeuronArray.put(rank, score);
            }
            inference.init();
            inference.forward();

            double max = Double.NEGATIVE_INFINITY;
            int argmax = -1;
            for (int rank : scoreNeuronArray.keySet()) {
                if (scoreNeuronArray.get(rank).getNeuron(0) > max) {
                    max = scoreNeuronArray.get(rank).getNeuron(0);
                    argmax = rank;
                }
            }
            if (argmax == -1) {
                System.err.println("STOP: TRAIN scores are not real numbers any more" + max);
                inference.printNeurons();
            }
            bestRanks.put(exNum, argmax);
            exNum++;
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
                ap.tokenInpDim); // TODO: what about the dimensions?
        inference.addNeurons(posInpArray);

        inference.addMapping(new OutputMappingStringArrayToDenseArray(instance.posTags,
                posInpArray, lookupTables.posTable));

        return posInpArray;
    }

}
