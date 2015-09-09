package edu.cmu.cs.lti.semreranking.utils;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import edu.cmu.cs.lti.nlp.swabha.basic.Conll;
import edu.cmu.cs.lti.nlp.swabha.basic.ConllElement;
import edu.cmu.cs.lti.nlp.swabha.fileutils.BasicFileReader;

public class SentConllReader {

    public static class SentsAndToks {

        public List<String[]> allLemmas;
        public List<String[]> allPostags;
        public Set<String> tokensVocab;
        public Set<String> posVocab;

        public SentsAndToks(
                List<String[]> allLemmas,
                List<String[]> allPostags,
                Set<String> tokensVocab,
                Set<String> posVocab) {
            this.allLemmas = allLemmas;
            this.allPostags = allPostags;
            this.tokensVocab = tokensVocab;
            this.posVocab = posVocab;
        }
    }

    public static SentsAndToks readConlls(String fileName) {
        List<String[]> allToks = Lists.newArrayList();
        List<String[]> allPostags = Lists.newArrayList();
        Set<String> tokensVocab = Sets.newHashSet();
        Set<String> posVocab = Sets.newHashSet();

        List<Conll> sents = BasicFileReader.readConllFile(fileName);

        for (Conll sent : sents) {
            String[] tokens = new String[sent.getElements().size()];
            String[] posTags = new String[sent.getElements().size()];
            int i = 0;
            for (ConllElement element : sent.getElements()) {
                tokens[i] = element.getLemma();
                tokensVocab.add(element.getLemma());
                posTags[i] = element.getCoarsePosTag();
                posVocab.add(element.getCoarsePosTag());
                i++;
            }
            allToks.add(tokens);
            allPostags.add(posTags);
        }
        return new SentsAndToks(allToks, allPostags, tokensVocab, posVocab);
    }

}
