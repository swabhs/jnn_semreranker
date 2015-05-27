package edu.cmu.cs.lti.semreranking.jnn;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import jnn.functions.composite.LookupTable;
import jnn.functions.parametrized.DenseFullyConnectedLayer;
import jnn.mapping.OutputMappingDenseToDense;
import jnn.mapping.OutputMappingStringToDense;
import jnn.neuron.DenseNeuronArray;
import jnn.training.GlobalParameters;
import jnn.training.GraphInference;
import vocab.Vocab;

import com.beust.jcommander.internal.Sets;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import edu.cmu.cs.lti.semreranking.Argument;
import edu.cmu.cs.lti.semreranking.Frame;
import edu.cmu.cs.lti.semreranking.FrameSemanticParse;
import edu.cmu.cs.lti.semreranking.Scored;
import edu.cmu.cs.lti.semreranking.lossfunctions.PairwiseLoss;

public class RerankerAppTest {

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
        String frameId = "BUY";
        // List<Argument> arguments = Lists.newArrayList();
        Set<Argument> arguments = Sets.newHashSet();
        arguments.add(new Argument("buyer", 0, 2));
        arguments.add(new Argument("goods", 4, 4));

        Frame frame = new Frame(frameId, 3, 3, "purchased.v", "purchased", arguments, 2.98);
        List<Frame> frames = Arrays.asList(frame);

        // Table<String, String, Pair<Integer, Integer>> frameMap = HashBasedTable.create();
        // frameMap.put(frame, "BUY_buyer", Pair.of(0, 2));
        // frameMap.put(frame, "BUY_goods", Pair.of(4, 4));

        FrameSemanticParse fsp = new FrameSemanticParse(frames);
        Scored<FrameSemanticParse> scoredFsp = new Scored<FrameSemanticParse>(fsp, 1.0);
        // TreeMultiset<TrainingInstance> instances = Arrays.asList(
        // new TrainingInstance(words, Arrays.asList(scoredFsp)));
        //
        // RerankerApp app = new RerankerApp(new TrainData(instances, null, vocabs));
        // System.err.println(app.network);
    }

    public static void main(String[] args) {
        GlobalParameters.learningRateDefault = 0.01;

        int inpDim = 2; // dimension of the word vectors for the tokens of the sentence

        // output label vocab
        Vocab labelVocab = new Vocab();
        labelVocab.addWordToVocab("adj");
        labelVocab.addWordToVocab("mwe");

        labelVocab.sortVocabByCount();
        labelVocab.generateHuffmanCodes();

        LookupTable labelTable = new LookupTable(labelVocab, inpDim);

        int outpDim = 2;
        DenseFullyConnectedLayer params = new DenseFullyConnectedLayer(inpDim, outpDim);

        Table<Integer, Integer, DenseNeuronArray> scores = HashBasedTable.create();

        for (int epoch = 0; epoch < 100; epoch++) {
            GraphInference inference = new GraphInference(0, true);

            DenseNeuronArray labelArray = new DenseNeuronArray(outpDim);
            labelArray.setName("input");
            inference.addNeurons(labelArray);
            inference.addMapping(new OutputMappingStringToDense("adj", labelArray,
                    labelTable));

            DenseNeuronArray outArray = new DenseNeuronArray(outpDim);
            inference.addNeurons(outArray);
            inference.addMapping(new OutputMappingDenseToDense(labelArray, outArray,
                    params));

            scores.put(0, 0, outArray);

            labelArray = new DenseNeuronArray(outpDim);
            labelArray.setName("input");
            inference.addNeurons(labelArray);
            inference.addMapping(new OutputMappingStringToDense("mwe", labelArray,
                    labelTable));

            outArray = new DenseNeuronArray(outpDim);
            inference.addNeurons(outArray);
            inference.addMapping(new OutputMappingDenseToDense(labelArray, outArray,
                    params));
            scores.put(0, 1, outArray);

            inference.init();
            inference.forward();
            System.err.println("error in iteration " + epoch + " : "
                    + PairwiseLoss.getLossTest(scores, Arrays.asList(0.8, 0.6)));

            inference.backward();
            inference.commit(0);

        }
    }

}
