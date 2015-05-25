package edu.cmu.cs.lti.semreranking;

import java.util.List;
import java.util.Set;

public class SentsAndToks {

    public List<String[]> allToks;
    public Set<String> tokensVocab;

    public SentsAndToks(List<String[]> allToks, Set<String> tokensVocab) {
        this.allToks = allToks;
        this.tokensVocab = tokensVocab;
    }
}
