package edu.cmu.cs.lti.semreranking.jnn;

import jnn.functions.composite.LookupTable;
import jnn.functions.nonparametrized.LogisticSigmoidLayer;
import jnn.functions.nonparametrized.TanSigmoidLayer;
import jnn.functions.parametrized.DenseFullyConnectedLayer;
import jnn.mapping.OutputMappingDenseToDense;
import jnn.mapping.OutputMappingStringArrayToDenseArray;
import jnn.mapping.OutputMappingStringToDense;
import jnn.neuron.DenseNeuronArray;
import jnn.training.GlobalParameters;
import jnn.training.GraphInference;
import vocab.Vocab;

public class SimpleRerankerApp {

    public static void main(String[] args) {

        GlobalParameters.learningRateDefault = 0.01;

        String inputSentence = "the rich banker buys stocks";
        String[] inputTokens = inputSentence.split("\\s+");

        Vocab tokenVocab = new Vocab();
        // adding all tokens to the vocabulary
        for (int i = 0; i < inputTokens.length; i++) {
            tokenVocab.addWordToVocab(inputTokens[i]);
        }

        tokenVocab.sortVocabByCount(); // TODO: what is this?
        tokenVocab.generateHuffmanCodes();// TODO: what is this?

        int tokenInpDim = 5; // dimension of the word vectors for the tokens of the sentence
        LookupTable tokenTable = new LookupTable(tokenVocab, tokenInpDim);

        // frame_arg parameters
        Vocab argIdVocab = new Vocab();
        argIdVocab.addWordToVocab("BUY_buyer");
        argIdVocab.addWordToVocab("BUY_goods");

        argIdVocab.sortVocabByCount();
        argIdVocab.generateHuffmanCodes();

        int argIdInpDim = 2;
        LookupTable argIdTable = new LookupTable(argIdVocab, argIdInpDim);

        // frame parameters
        Vocab frameIdVocab = new Vocab();
        frameIdVocab.addWordToVocab("BUY");

        frameIdVocab.sortVocabByCount();
        frameIdVocab.generateHuffmanCodes();

        int frameIdInpDim = 3;
        LookupTable frameTable = new LookupTable(frameIdVocab, frameIdInpDim);

        // span and argId combiner (y_a)
        int argResultDim = 6;
        DenseFullyConnectedLayer tokenLayer = new DenseFullyConnectedLayer(tokenInpDim,
                argResultDim);
        DenseFullyConnectedLayer argLayer = new DenseFullyConnectedLayer(argIdInpDim, argResultDim);

        // args and frameId and predicate combiner (y_f)
        int frameResultDim = 5;
        DenseFullyConnectedLayer allArgsLayer = new DenseFullyConnectedLayer(argResultDim,
                frameResultDim);
        DenseFullyConnectedLayer frameLayer = new DenseFullyConnectedLayer(frameIdInpDim,
                frameResultDim);
        DenseFullyConnectedLayer predLayer = new DenseFullyConnectedLayer(tokenInpDim,
                frameResultDim);

        // frames combiner(y)
        int resultDim = 4;
        DenseFullyConnectedLayer allFramesLayer = new DenseFullyConnectedLayer(frameResultDim,
                resultDim);
        // for now we are ignoring the syntactic and semantic scores

        // score for y
        DenseFullyConnectedLayer scoreLayer = new DenseFullyConnectedLayer(resultDim, 1);

        final int maxEpochs = 1000;
        for (int epoch = 0; epoch < maxEpochs; epoch++) {

            GraphInference inference = new GraphInference(0, true);
            DenseNeuronArray[] tokenInpArray = DenseNeuronArray.asArray(inputTokens.length,
                    tokenInpDim); // random initialization?
            for (int i = 0; i < tokenInpArray.length; i++) {
                tokenInpArray[i].setName("word rep for " + inputTokens[i]);
            }

            inference.addNeurons(tokenInpArray); // TODO: what exactly does this do?
            inference.addMapping(new OutputMappingStringArrayToDenseArray(inputTokens,
                    tokenInpArray, tokenTable));

            DenseNeuronArray arg0InpArray = new DenseNeuronArray(argIdInpDim);
            DenseNeuronArray arg1InpArray = new DenseNeuronArray(argIdInpDim);
            inference.addNeurons(arg0InpArray);
            inference.addNeurons(arg1InpArray);
            inference.addMapping(new OutputMappingStringToDense("BUY_buyer", arg0InpArray,
                    argIdTable));
            inference.addMapping(new OutputMappingStringToDense("BUY_goods", arg1InpArray,
                    argIdTable));

            DenseNeuronArray frameInpArray = new DenseNeuronArray(frameIdInpDim);
            inference.addNeurons(frameInpArray);
            inference
                    .addMapping(new OutputMappingStringToDense("BUY", frameInpArray, frameTable));

            // combine "the rich banker" + BUY_buyer
            DenseNeuronArray arg0OutArray = new DenseNeuronArray(argResultDim);
            arg0OutArray.setName("the rich banker (BUY_buyer)");
            inference.addNeurons(arg0OutArray);
            inference.addMapping(new OutputMappingDenseToDense(tokenInpArray[0], arg0OutArray,
                    tokenLayer));
            inference.addMapping(new OutputMappingDenseToDense(tokenInpArray[1], arg0OutArray,
                    tokenLayer));
            inference.addMapping(new OutputMappingDenseToDense(tokenInpArray[2], arg0OutArray,
                    tokenLayer));
            inference.addMapping(new OutputMappingDenseToDense(arg0InpArray, arg0OutArray,
                    argLayer));

            DenseNeuronArray arg0OutSig = new DenseNeuronArray(argResultDim);
            arg0OutSig.setName("the rich banker (BUY_buyer) + sigmoid");
            inference.addNeurons(arg0OutSig);
            inference.addMapping(new OutputMappingDenseToDense(arg0OutArray, arg0OutSig,
                    LogisticSigmoidLayer.singleton));

            // combine "stocks" + BUY_goods
            DenseNeuronArray arg1OutArray = new DenseNeuronArray(argResultDim);
            arg1OutArray.setName("stocks (BUY_goods)");
            inference.addNeurons(arg1OutArray);
            inference.addMapping(new OutputMappingDenseToDense(tokenInpArray[4], arg1OutArray,
                    tokenLayer));
            inference.addMapping(new OutputMappingDenseToDense(arg1InpArray, arg1OutArray,
                    argLayer));

            DenseNeuronArray arg1OutSig = new DenseNeuronArray(argResultDim);
            arg1OutSig.setName("stocks (BUY_goods) + sigmoid");
            inference.addNeurons(arg1OutSig);
            inference.addMapping(new OutputMappingDenseToDense(arg1OutArray, arg1OutSig,
                    LogisticSigmoidLayer.singleton));

            // combine the frame id, predicate and the args
            DenseNeuronArray frameOutArray = new DenseNeuronArray(frameResultDim);
            frameOutArray.setName("buys (BUY)");
            inference.addNeurons(frameOutArray);
            inference.addMapping(new OutputMappingDenseToDense(tokenInpArray[3], frameOutArray,
                    predLayer));
            inference.addMapping(new OutputMappingDenseToDense(frameInpArray, frameOutArray,
                    frameLayer));
            inference.addMapping(new OutputMappingDenseToDense(arg0OutSig, frameOutArray,
                    allArgsLayer));
            inference.addMapping(new OutputMappingDenseToDense(arg1OutSig, frameOutArray,
                    allArgsLayer));

            DenseNeuronArray frameOutSig = new DenseNeuronArray(frameResultDim);
            frameOutSig.setName("buys (BUY) + sigmoid");
            inference.addNeurons(frameOutSig);
            inference.addMapping(new OutputMappingDenseToDense(frameOutArray, frameOutSig,
                    LogisticSigmoidLayer.singleton));

            // combine all frames (//TODO: add also non-framewords)
            DenseNeuronArray sentenceArray = new DenseNeuronArray(resultDim);
            sentenceArray.setName("all frames");
            inference.addNeurons(sentenceArray);
            inference.addMapping(new OutputMappingDenseToDense(frameOutSig, sentenceArray,
                    allFramesLayer));

            DenseNeuronArray sentOutSig = new DenseNeuronArray(resultDim);
            sentOutSig.setName("all frames + sigmoid");
            inference.addNeurons(sentOutSig);
            inference.addMapping(new OutputMappingDenseToDense(sentenceArray, sentOutSig,
                    LogisticSigmoidLayer.singleton));

            DenseNeuronArray score = new DenseNeuronArray(1);
            inference.addNeurons(score);
            inference.addMapping(new OutputMappingDenseToDense(sentenceArray, score, scoreLayer));

            DenseNeuronArray scoreTanh = new DenseNeuronArray(1);
            inference.addNeurons(scoreTanh);
            inference.addMapping(new OutputMappingDenseToDense(score, scoreTanh,
                    TanSigmoidLayer.singleton));

            inference.init();
            inference.forward();
            double error = 1 - scoreTanh.getNeuron(0);
            scoreTanh.addError(0, error);

            inference.backward();
            inference.commit(0);

            // for(int i = 0; i < inputReps.length; i++) {System.err.println(inputReps[i]);};
            // System.err.println(combined);
            // System.err.println(combinedSig);
            // System.err.println(score);
            // System.err.println(scoreTanh);

            System.err.println("error in iteration " + epoch + " : " + error);
        }
    }
}
