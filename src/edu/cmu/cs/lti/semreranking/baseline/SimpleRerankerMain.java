package edu.cmu.cs.lti.semreranking.baseline;

import java.util.TreeSet;

import com.google.common.collect.Sets;
import com.google.common.collect.TreeMultiset;

public class SimpleRerankerMain {

    public static void main(String[] args) {
        // TODO:implement
        TreeMultiset<Integer> multiset = TreeMultiset.create();
        multiset.add(2);
        multiset.add(2);

        multiset.add(52);
        multiset.add(22);
        System.err.println(multiset.size());

        TreeSet<Integer> set = Sets.newTreeSet();
        set.add(2);
        set.add(2);
        set.add(42);
        set.add(21);
        System.err.println(set.size());
    }

    public static SimpleTrainData readTrainData(String fscoreFileName, String turboScoreFileName) {
        // TODO:implement
        return null;
    }

    public static SimpleTrainData readTestData(String fscoreFileName, String turboScoreFileName) {
        // TODO:implement
        return null;
    }
}
