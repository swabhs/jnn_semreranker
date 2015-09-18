package edu.cmu.cs.lti.semreranking.utils;

import java.util.List;

import com.google.common.collect.Lists;

import edu.cmu.cs.lti.nlp.swabha.basic.Conll;
import edu.cmu.cs.lti.nlp.swabha.basic.ConllElement;
import edu.cmu.cs.lti.nlp.swabha.fileutils.BasicFileReader;

public class SentConllReader {

    public static class SentsAndToks {

        public List<String[]> allLemmas;
        public List<String[]> allPostags;
        public FrequencySet tokensVocab;
        public FrequencySet posVocab;

        public SentsAndToks(
                List<String[]> allLemmas,
                List<String[]> allPostags,
                FrequencySet tokensVocab,
                FrequencySet posVocab) {
            this.allLemmas = allLemmas;
            this.allPostags = allPostags;
            this.tokensVocab = tokensVocab;
            this.posVocab = posVocab;
        }
    }

    public static SentsAndToks readConlls(String fileName) {
        List<String[]> allToks = Lists.newArrayList();
        List<String[]> allPostags = Lists.newArrayList();
        FrequencySet tokensVocab = new FrequencySet();
        FrequencySet posVocab = new FrequencySet();

        List<Conll> sents = BasicFileReader.readConllFile(fileName);

        for (Conll sent : sents) {
            for (ConllElement element : sent.getElements()) {
                tokensVocab.addKeyIfNotFrozen(element.getLemma());
                posVocab.addKeyIfNotFrozen(element.getCoarsePosTag());
            }
        }
        tokensVocab.freeze();
        posVocab.freeze();

        for (Conll sent : sents) {
            String[] tokens = new String[sent.getElements().size()];
            String[] posTags = new String[sent.getElements().size()];
            int i = 0;
            for (ConllElement element : sent.getElements()) {
                tokens[i] = tokensVocab.returnKeyAfterFreezing(element.getLemma());
                posTags[i] = posVocab.returnKeyAfterFreezing(element.getCoarsePosTag());
                i++;
            }
            allToks.add(tokens);
            allPostags.add(posTags);
        }
        return new SentsAndToks(allToks, allPostags, tokensVocab, posVocab);
    }

    public static SentsAndToks readConlls(
            String fileName,
            FrequencySet tokensVocab,
            FrequencySet posVocab) {
        List<String[]> allToks = Lists.newArrayList();
        List<String[]> allPostags = Lists.newArrayList();
        List<Conll> sents = BasicFileReader.readConllFile(fileName);

        for (Conll sent : sents) {
            String[] tokens = new String[sent.getElements().size()];
            String[] posTags = new String[sent.getElements().size()];
            int i = 0;
            for (ConllElement element : sent.getElements()) {
                tokens[i] = tokensVocab.returnKeyAfterFreezing(element.getLemma());
                posTags[i] = posVocab.returnKeyAfterFreezing(element.getCoarsePosTag());
                i++;
            }
            allToks.add(tokens);
            allPostags.add(posTags);
        }
        return new SentsAndToks(allToks, allPostags, tokensVocab, posVocab);
    }

}
