package edu.cmu.cs.lti.semreranking.jnn;

import java.util.Set;

import jnn.functions.composite.LookupTable;
import util.IOUtils;
import vocab.Vocab;
import edu.cmu.cs.lti.semreranking.DataPaths;
import edu.cmu.cs.lti.semreranking.datastructs.FrameNetVocabs;

public class FspLookupTables {

    public LookupTable tokenTable;
    public LookupTable posTable;
    // public LookupTable goldFNPosTable;
    public LookupTable frameTable;
    public LookupTable frameArgTable;

    public FspLookupTables(boolean usePretrained, ArrayParams ap, FrameNetVocabs vocabs) {
        if (usePretrained) {
            Vocab vocab = new Vocab();
            for (String vocabItem : vocabs.tokens) {
                vocab.addWordToVocab(vocabItem);
            }
            vocab.sortVocabByCount();
            vocab.generateHuffmanCodes();

            tokenTable = new LookupTable(vocab, ap.tokenInpDim);
            FspLookupTables.preinitialize(DataPaths.WV_FILENAME, vocab, tokenTable);
        } else {
            tokenTable = initializeVocabularies(vocabs.tokens, ap.tokenInpDim);
        }

        posTable = initializeVocabularies(vocabs.posTags, ap.tokenInpDim);
        // goldFNPosTable = initializeVocabularies(vocabs.goldFNPosTags, ap.tokenInpDim);
        frameTable = initializeVocabularies(vocabs.frameIds, ap.frameIdInpDim);
        frameArgTable = initializeVocabularies(vocabs.frameArguments, ap.frameArgInpDim);
    }

    private LookupTable initializeVocabularies(Set<String> vocabItems, int outputDim) {
        Vocab vocab = new Vocab();
        for (String vocabItem : vocabItems) {
            vocab.addWordToVocab(vocabItem);
        }
        vocab.sortVocabByCount();
        vocab.generateHuffmanCodes();

        LookupTable table = new LookupTable(vocab, outputDim);
        return table;
    }

    /** use pretrained vectors for initialization instead of one-hot */
    public static void preinitialize(String wvFile, final Vocab vocab, final LookupTable table) {
        IOUtils.iterateFiles(new String[]{wvFile}, new IOUtils.iterateFilesCallback() {
            @Override
            public void cb(String[] lines, int lineNumber) {
                String line = lines[0];
                String params[] = line.split("\\s+");
                if (params.length == 2 && lineNumber == 0)
                    return;

                String word = params[0];
                double[] embeddings = new double[params.length - 1];
                for (int i = 1; i < params.length; i++) {
                    embeddings[i - 1] = Double.parseDouble(params[i]);
                }
                if (vocab.getEntry(word) != null) {
                    int key = vocab.getEntry(word).id;
                    table.setPretrainedWeight(key, embeddings);
                }
            }
        });
        System.err.println("Using word vectors ...");
    }

}
