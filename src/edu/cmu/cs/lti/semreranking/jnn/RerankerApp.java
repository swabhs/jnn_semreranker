package edu.cmu.cs.lti.semreranking.jnn;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jnn.functions.composite.LookupTable;
import jnn.functions.nonparametrized.LogisticSigmoidLayer;
import jnn.mapping.OutputMappingDenseToDense;
import jnn.mapping.OutputMappingStringArrayToDenseArray;
import jnn.neuron.DenseNeuronArray;
import jnn.training.GraphInference;
import vocab.Vocab;

import com.beust.jcommander.internal.Sets;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;

import edu.cmu.cs.lti.nlp.swabha.basic.Pair;
import edu.cmu.cs.lti.semreranking.FrameSemanticParse;
import edu.cmu.cs.lti.semreranking.FrameSemanticParse.Argument;
import edu.cmu.cs.lti.semreranking.FrameSemanticParse.Frame;
import edu.cmu.cs.lti.semreranking.TrainingInstance;
import edu.cmu.cs.lti.semreranking.lossfunctions.PairwiseLoss;
import edu.cmu.cs.lti.semreranking.utils.Scored;
import edu.cmu.cs.lti.semreranking.utils.StringUtils;

public class RerankerApp {

    public static final int maxEpochs = 10;

    private LookupTable initializeVocabularies(Set<String> vocabItems, int outputDim) {
        Vocab vocab = new Vocab();
        for (String vocabItem : vocabItems) {
            vocab.addWordToVocab(vocabItem);
        }
        vocab.sortVocabByCount();
        vocab.generateHuffmanCodes();
        return new LookupTable(vocab, outputDim);
    }

    /***
     * 
     * @param instance
     * @param ap
     *            array parameters - sizes of all the vectors needed
     * @param tokenTable
     * @param frameTable
     * @param argIdTable
     * @param network
     *            parameters of the network
     */
    public static void learning(List<TrainingInstance> instances, ArrayParams ap,
            LookupTable tokenTable, LookupTable frameTable, LookupTable argIdTable,
            FrameNetwork network) {

        for (int epoch = 0; epoch < maxEpochs; epoch++) {
            GraphInference inference = new GraphInference(0, true);

            double finalscores[][] = new double[instances.size()][instances.get(0).numParses]; // TODO
            DenseNeuronArray scoreArray = new DenseNeuronArray(1);

            int exNum = 0;
            for (TrainingInstance instance : instances) {

                /* adding all inputs to inference */
                DenseNeuronArray[] tokenInpArray = DenseNeuronArray.asArray(instance.size,
                        ap.tokenInpDim);
                for (int i = 0; i < instance.size; i++) {
                    tokenInpArray[i].setName("word rep for " + instance.tokens[i]);
                }

                inference.addNeurons(tokenInpArray);
                inference.addMapping(new OutputMappingStringArrayToDenseArray(instance.tokens,
                        tokenInpArray, tokenTable));

                int rank = 0;
                for (Scored<FrameSemanticParse> scoredFsp : instance.sortedParses) {

                    System.err.println("consider next " + instance.sortedParses.indexOf(scoredFsp));
                    int numFrames = scoredFsp.entity.numFrames;
                    int numArgs = scoredFsp.entity.numFrameArgs;
                    DenseNeuronArray[] frameIdInpArray = DenseNeuronArray.asArray(numFrames,
                            ap.frameIdInpDim);
                    DenseNeuronArray[] argIdInpArray = DenseNeuronArray.asArray(numArgs,
                            ap.frameArgInpDim);
                    String[] frameIds = new String[numFrames];
                    String[] frameArgIds = new String[numArgs];
                    int i = 0;
                    int j = 0;

                    // System.err.println(scoredFsp.entity.frames.size() + " "
                    // + scoredFsp.entity.numFrames);

                    for (Frame frame : scoredFsp.entity.frames) {
                        frameIdInpArray[i].setName(frame.id);
                        frameIds[i] = frame.id;

                        for (Argument arg : frame.arguments) {
                            String frameArgId = StringUtils.makeFrameArgId(frame.id, arg.id);
                            argIdInpArray[j].setName(frameArgId);
                            frameArgIds[j] = frameArgId;
                            j++;
                        }
                        i++;
                    }
                    inference.addNeurons(frameIdInpArray);
                    inference.addMapping(new OutputMappingStringArrayToDenseArray(frameIds,
                            frameIdInpArray, frameTable));

                    inference.addNeurons(argIdInpArray);
                    inference.addMapping(new OutputMappingStringArrayToDenseArray(frameArgIds,
                            argIdInpArray, argIdTable));

                    /* computation graph */

                    DenseNeuronArray sentenceArray = new DenseNeuronArray(ap.resultDim);
                    sentenceArray.setName("all frames");
                    inference.addNeurons(sentenceArray);

                    i = 0;
                    j = 0;
                    for (Frame frame : scoredFsp.entity.frames) {

                        DenseNeuronArray frameOutArray = new DenseNeuronArray(ap.frameResultDim);
                        frameOutArray.setName(frame.id + ":output");
                        inference.addNeurons(frameOutArray);
                        inference.addMapping(new OutputMappingDenseToDense(
                                tokenInpArray[frame.predStartPos], frameOutArray, network
                                        .getPredLayer()));
                        inference.addMapping(new OutputMappingDenseToDense(
                                frameIdInpArray[i], frameOutArray, network.getFrameLayer()));

                        for (Argument arg : frame.arguments) {

                            DenseNeuronArray argOutArray = new DenseNeuronArray(ap.argResultDim);
                            argOutArray.setName(frame.id + ":" + arg.id + ":output");
                            inference.addNeurons(argOutArray);
                            inference.addMapping(new OutputMappingDenseToDense(
                                    argIdInpArray[j], argOutArray, network.getArgLayer()));
                            for (int pos = arg.start; pos <= arg.end; pos++) {
                                inference.addMapping(new OutputMappingDenseToDense(
                                        tokenInpArray[pos], argOutArray, network.getTokenLayer()));
                            }

                            DenseNeuronArray argOutSig = new DenseNeuronArray(ap.argResultDim);
                            argOutSig.setName(frame.id + ":" + arg.id + "_sigmoid");
                            inference.addNeurons(argOutSig);
                            inference.addMapping(new OutputMappingDenseToDense(
                                    argOutArray, argOutSig, LogisticSigmoidLayer.singleton));

                            inference.addMapping(new OutputMappingDenseToDense(
                                    argOutSig, frameOutArray, network.getAllArgsLayer()));
                            j++;
                        }

                        DenseNeuronArray frameOutSig = new DenseNeuronArray(ap.frameResultDim);
                        frameOutSig.setName(frame.id + ":output_sigmoid");
                        inference.addNeurons(frameOutSig);
                        inference.addMapping(new OutputMappingDenseToDense(frameOutArray,
                                frameOutSig,
                                LogisticSigmoidLayer.singleton));

                        inference.addMapping(new OutputMappingDenseToDense(frameOutSig,
                                sentenceArray,
                                network.getAllFramesLayer()));
                        i++;
                    }

                    DenseNeuronArray sentOutSig = new DenseNeuronArray(ap.resultDim);
                    sentOutSig.setName("all frames + sigmoid");
                    inference.addNeurons(sentOutSig);
                    inference.addMapping(new OutputMappingDenseToDense(
                            sentenceArray, sentOutSig, LogisticSigmoidLayer.singleton));

                    DenseNeuronArray score = new DenseNeuronArray(1);
                    inference.addNeurons(score);
                    inference.addMapping(new OutputMappingDenseToDense(
                            sentenceArray, score, network.getScoreLayer()));

                    finalscores[exNum][rank] = score.getNeuron(0); // TODO(Wang Ling): throws a
                                                                   // runtime error here
                    rank++;
                    // DenseNeuronArray scoreTanh = new DenseNeuronArray(1);
                    // inference.addNeurons(scoreTanh);
                    // inference.addMapping(new OutputMappingDenseToDense(
                    // score, scoreTanh, TanSigmoidLayer.singleton));
                }
                exNum++;
            }

            inference.init();
            inference.forward();
            double error = PairwiseLoss.getLoss(finalscores, 1.0); // TODO: f-score instead of
                                                                   // margin
            scoreArray.addError(0, error); // TODO(Wang Ling): - this does not seem right

            inference.backward();
            inference.commit(0);

            System.err.println("error in iteration " + epoch + " : " + error);
        }
    }

    public void run(FrameNetVocabs vocabs, List<TrainingInstance> instances, ArrayParams ap) {
        /** Inputs */
        LookupTable tokenTable = initializeVocabularies(vocabs.tokens, ap.tokenInpDim);
        LookupTable frameTable = initializeVocabularies(vocabs.frameIds, ap.frameIdInpDim);
        LookupTable argTable = initializeVocabularies(vocabs.frameArguments, ap.frameArgInpDim);

        /** Defining the network structure below */
        FrameNetwork network = new FrameNetwork(ap.tokenInpDim, ap.frameArgInpDim,
                ap.frameIdInpDim, ap.argResultDim, ap.frameResultDim, ap.resultDim);

        /** Learning */
        learning(instances, ap, tokenTable, frameTable, argTable, network);
    }

    public void simpleExample() {
        Set<String> tokens = Sets.newHashSet();
        List<String> inps = Arrays.asList("the", "rich", "banker", "purchased", "stocks");
        tokens.addAll(inps);

        Set<String> frameIds = Sets.newHashSet();
        inps = Arrays.asList("BUY");
        frameIds.addAll(inps);

        Set<String> frameArguments = Sets.newHashSet();
        inps = Arrays.asList("BUY_buyer", "BUY_goods");
        frameArguments.addAll(inps);

        FrameNetVocabs vocabs = new FrameNetVocabs(tokens, frameIds, frameArguments);

        String words[] = new String[]{"the", "rich", "banker", "purchased", "stocks"};
        String frame = "BUY";

        Map<String, Integer> predStartMap = Maps.newHashMap();
        predStartMap.put(frame, 3);
        Map<String, Integer> predEndMap = Maps.newHashMap();
        predEndMap.put(frame, 3);

        Table<String, String, Pair<Integer, Integer>> frameMap = HashBasedTable.create();
        frameMap.put(frame, "BUY_buyer", Pair.of(0, 2));
        frameMap.put(frame, "BUY_goods", Pair.of(4, 4));

        FrameSemanticParse fsp = new FrameSemanticParse(
                predStartMap, predEndMap, frameMap, null);
        Scored<FrameSemanticParse> scoredFsp = new Scored<FrameSemanticParse>(fsp, 1.0);
        TrainingInstance instance = new TrainingInstance(words, Arrays.asList(scoredFsp));
        run(vocabs, instance, new ArrayParams());
    }

}
