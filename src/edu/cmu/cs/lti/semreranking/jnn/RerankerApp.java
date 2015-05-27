package edu.cmu.cs.lti.semreranking.jnn;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Map;
import java.util.Set;

import jnn.functions.composite.LookupTable;
import jnn.functions.nonparametrized.LogisticSigmoidLayer;
import jnn.functions.nonparametrized.TanSigmoidLayer;
import jnn.mapping.OutputMappingDenseToDense;
import jnn.mapping.OutputMappingStringArrayToDenseArray;
import jnn.neuron.DenseNeuronArray;
import jnn.training.GraphInference;
import util.MathUtils;
import vocab.Vocab;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;

import edu.cmu.cs.lti.semreranking.Argument;
import edu.cmu.cs.lti.semreranking.DataInstance;
import edu.cmu.cs.lti.semreranking.Frame;
import edu.cmu.cs.lti.semreranking.FrameSemanticParse;
import edu.cmu.cs.lti.semreranking.Scored;
import edu.cmu.cs.lti.semreranking.TestData;
import edu.cmu.cs.lti.semreranking.TestInstance;
import edu.cmu.cs.lti.semreranking.TrainData;
import edu.cmu.cs.lti.semreranking.TrainInstance;
import edu.cmu.cs.lti.semreranking.evaluation.Evaluator;
import edu.cmu.cs.lti.semreranking.evaluation.Oracle;
import edu.cmu.cs.lti.semreranking.lossfunctions.PairwiseLoss;
import edu.cmu.cs.lti.semreranking.utils.StringUtils;

public class RerankerApp {

    public static final int maxEpochs = 30;
    public static final int batchSize = 10;

    private ArrayParams ap = new ArrayParams(); // sizes of all the vectors needed
    public FrameNetwork network; // parameters of the network

    private LookupTable tokenTable;
    private LookupTable frameTable;
    private LookupTable frameArgTable;

    public static NumberFormat formatter = new DecimalFormat("#0.00000");

    public RerankerApp(TrainData data, TestData testData, TestData devData) {

        /** Inputs */
        tokenTable = initializeVocabularies(data.vocabs.tokens, ap.tokenInpDim);
        frameTable = initializeVocabularies(data.vocabs.frameIds, ap.frameIdInpDim);
        frameArgTable = initializeVocabularies(data.vocabs.frameArguments,
                ap.frameArgInpDim);

        /** Defining the network structure below */
        network = new FrameNetwork(ap.tokenInpDim, ap.frameArgInpDim,
                ap.frameIdInpDim, ap.argResultDim, ap.frameResultDim, ap.resultDim);

        /** Learning */
        doDeepLearning(data, testData, devData);
    }

    private void doDeepLearning(TrainData data, TestData testData, TestData devData) {

        System.err.println("\n\n1-best = "
                + formatter.format(Oracle.getMicroCorpusAvg(testData.testInstances, 1))
                + "\t" + testData.numRanks + "-best = "
                + formatter.format(Oracle.getMicroCorpusAvg(testData.testInstances,
                        testData.numRanks)) + "\n");

        double errorPerEx[] = new double[data.trainInstances.size()];
        Map<Integer, DenseNeuronArray> finalScoresPerEx = Maps.newHashMap();
        for (int epoch = 0; epoch < maxEpochs; epoch++) {

            int exNum = 0;
            for (TrainInstance instance : data.trainInstances) {
                GraphInference inference = new GraphInference(0, true);
                DenseNeuronArray[] tokenInpArr = getInputsFromTokens(inference, instance);

                for (int rank = 0; rank < instance.kbestParses.size(); rank++) {
                    Scored<FrameSemanticParse> semParse = instance.kbestParses.get(rank);
                    InputFspNeuronArrays fspInp = getInputsFromLookupTables(inference, semParse);
                    DenseNeuronArray score = getFspRepresentation(tokenInpArr, fspInp, inference,
                            semParse);
                    finalScoresPerEx.put(rank, score);
                }

                // if (exNum % batchSize == 0 && epoch != 0) {
                // inference.commit(0);
                // System.err.println(Evaluator.getCorpusFscore(data.testInstances,
                // getBestRanks(data)));
                // }

                inference.init();
                inference.forward();
                errorPerEx[exNum] = PairwiseLoss.getLoss(finalScoresPerEx, instance);
                inference.backward();
                exNum++;
                if (exNum == data.trainInstances.size() || exNum % batchSize == 0) {
                    inference.commit(0);
                }

            }

            double error = MathUtils.arraySum(errorPerEx);
            System.err.println("error in iteration " + epoch + " : " + formatter.format(error));

            Map<Integer, Integer> bestRanks = getBestRanks(testData);
            double testScore = Evaluator.getRerankedMicroAvg(testData.testInstances, bestRanks);
            System.err.print("test = " + formatter.format(testScore) + "\t");

            Map<Integer, Integer> bestDevRanks = getBestRanks(devData);
            double devScore = Evaluator.getRerankedMicroAvg(devData.testInstances, bestDevRanks);
            System.err.println("dev = " + formatter.format(devScore) + "\n");

        }
    }

    private LookupTable initializeVocabularies(Set<String> vocabItems, int outputDim) {
        Vocab vocab = new Vocab();
        for (String vocabItem : vocabItems) {
            vocab.addWordToVocab(vocabItem);
        }
        vocab.sortVocabByCount();
        vocab.generateHuffmanCodes();
        return new LookupTable(vocab, outputDim);
    }

    /**
     * TODO: use pretrained word vectors alternatively!
     * 
     * @return
     */
    private DenseNeuronArray[] getInputsFromTokens(
            GraphInference inference, DataInstance instance) {
        DenseNeuronArray[] tokenInpArray = DenseNeuronArray.asArray(instance.size,
                ap.tokenInpDim);
        inference.addNeurons(tokenInpArray);
        inference.addMapping(new OutputMappingStringArrayToDenseArray(instance.tokens,
                tokenInpArray, tokenTable));
        return tokenInpArray;
    }

    class InputFspNeuronArrays {
        DenseNeuronArray[] frameIdsArray;
        DenseNeuronArray[] frameArgIdsArray;

        public InputFspNeuronArrays(
                DenseNeuronArray[] frameIdInpArray, DenseNeuronArray[] frameArgIdInpArray) {
            this.frameIdsArray = frameIdInpArray;
            this.frameArgIdsArray = frameArgIdInpArray;
        }
    }

    private InputFspNeuronArrays getInputsFromLookupTables(
            GraphInference inference, Scored<FrameSemanticParse> scoredFsp) {

        int numFrames = scoredFsp.entity.numFrames;
        int numArgs = scoredFsp.entity.numFrameArgs;

        DenseNeuronArray[] frameIdsArray = DenseNeuronArray.asArray(numFrames,
                ap.frameIdInpDim);
        DenseNeuronArray[] frameArgIdInpArray = DenseNeuronArray.asArray(numArgs,
                ap.frameArgInpDim);
        String[] frameIds = new String[numFrames];
        String[] frameArgIds = new String[numArgs];
        int i = 0;
        int j = 0;

        for (Frame frame : scoredFsp.entity.frames) {
            frameIdsArray[i].setName(frame.id);
            frameIds[i] = frame.id;

            for (Argument arg : frame.arguments) {
                String frameArgId = StringUtils.makeFrameArgId(frame.id, arg.id);
                frameArgIdInpArray[j].setName(frameArgId);
                frameArgIds[j] = frameArgId;
                j++;
            }
            i++;
        }
        /* adding all inputs to inference */
        inference.addNeurons(frameIdsArray);
        inference.addMapping(new OutputMappingStringArrayToDenseArray(frameIds,
                frameIdsArray, frameTable));

        /* adding all inputs to inference */
        inference.addNeurons(frameArgIdInpArray);
        inference.addMapping(new OutputMappingStringArrayToDenseArray(frameArgIds,
                frameArgIdInpArray, frameArgTable));

        return new InputFspNeuronArrays(frameIdsArray, frameArgIdInpArray);
    }

    /* computation graph */
    private DenseNeuronArray getFspRepresentation(
            DenseNeuronArray[] tokenInpArray,
            InputFspNeuronArrays inp,
            GraphInference inference,
            Scored<FrameSemanticParse> scoredFsp) {

        DenseNeuronArray sentenceLinear = new DenseNeuronArray(ap.resultDim);
        inference.addNeurons(sentenceLinear);

        Set<Integer> tokensParticipatingInFrames = Sets.newHashSet(); // don't add in null frame
        int i = 0;
        int j = 0;
        for (Frame frame : scoredFsp.entity.frames) {

            DenseNeuronArray frameLinear = new DenseNeuronArray(ap.frameResultDim);
            inference.addNeurons(frameLinear);

            for (int pos = frame.predStartPos; pos <= frame.predEndPos; pos++) {
                inference.addMapping(new OutputMappingDenseToDense(
                        tokenInpArray[pos], frameLinear, network.getPredLayer()));
                tokensParticipatingInFrames.add(pos);
            }
            inference.addMapping(new OutputMappingDenseToDense(
                    inp.frameIdsArray[i], frameLinear, network.getFrameLayer()));

            for (Argument arg : frame.arguments) {

                DenseNeuronArray argLinear = new DenseNeuronArray(ap.argResultDim);
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
                }

                DenseNeuronArray argNonlinear = new DenseNeuronArray(ap.argResultDim);
                inference.addNeurons(argNonlinear);
                inference.addMapping(new OutputMappingDenseToDense(
                        argLinear, argNonlinear, LogisticSigmoidLayer.singleton));

                inference.addMapping(new OutputMappingDenseToDense(
                        argNonlinear, frameLinear, network.getAllArgsLayer()));
                j++;
            }

            DenseNeuronArray frameNonlinear = new DenseNeuronArray(ap.frameResultDim);
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

        DenseNeuronArray sentNonlinear = new DenseNeuronArray(ap.resultDim);
        inference.addNeurons(sentNonlinear);
        inference.addMapping(new OutputMappingDenseToDense(
                sentenceLinear, sentNonlinear, LogisticSigmoidLayer.singleton));

        DenseNeuronArray sentScore = new DenseNeuronArray(1);
        inference.addNeurons(sentScore);
        inference.addMapping(new OutputMappingDenseToDense(
                sentenceLinear, sentScore, network.getScoreLayer()));

        DenseNeuronArray sentScoreTanh = new DenseNeuronArray(1); // TODO: this seems weird on paper
        inference.addNeurons(sentScoreTanh);
        inference.addMapping(new OutputMappingDenseToDense(
                sentScore, sentScoreTanh, TanSigmoidLayer.singleton));

        return sentScoreTanh;
    }

    private Table<Integer, Integer, DenseNeuronArray> doDeepDecoding(TestData data) {
        Table<Integer, Integer, DenseNeuronArray> scoreNeuronArray = HashBasedTable.create();

        GraphInference inference = new GraphInference(0, true);

        for (int exNum : data.testInstances.keySet()) {
            TestInstance instance = data.testInstances.get(exNum);
            DenseNeuronArray[] tokenInpArray = getInputsFromTokens(inference, instance);

            int rank = 0;
            for (Scored<FrameSemanticParse> scoredFsp : instance.unsortedParses) {
                InputFspNeuronArrays fspInp = getInputsFromLookupTables(inference, scoredFsp);
                DenseNeuronArray score = getFspRepresentation(
                        tokenInpArray, fspInp, inference, scoredFsp);
                scoreNeuronArray.put(exNum, rank, score);
                rank++;
            }
        }
        inference.init();
        inference.forward();

        return scoreNeuronArray;
    }

    public Map<Integer, Integer> getBestRanks(TestData data) {
        Table<Integer, Integer, DenseNeuronArray> finalScoreNeurons = doDeepDecoding(data);
        Map<Integer, Integer> bestRanks = Maps.newHashMap();
        for (int ex : finalScoreNeurons.rowKeySet()) {
            double max = Double.NEGATIVE_INFINITY;
            int argmax = -1;
            for (int rank = 0; rank < finalScoreNeurons.columnKeySet().size(); rank++) {
                if (finalScoreNeurons.get(ex, rank).getNeuron(0) > max) {
                    max = finalScoreNeurons.get(ex, rank).getNeuron(0);
                    argmax = rank;
                }
            }
            bestRanks.put(ex, argmax);
        }

        return bestRanks;
    }

}
