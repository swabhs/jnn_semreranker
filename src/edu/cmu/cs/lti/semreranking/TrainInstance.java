package edu.cmu.cs.lti.semreranking;

import java.util.Map;

import com.google.common.collect.Maps;
import com.google.common.collect.TreeMultiset;

public class TrainInstance extends DataInstance {

    public Map<Integer, Scored<FrameSemanticParse>> kbestParses;

    public TrainInstance(String[] tokens,
            TreeMultiset<Scored<FrameSemanticParse>> sortedParses) {
        super(sortedParses.size(), tokens);

        this.kbestParses = Maps.newHashMap();
        int rank = 0;
        for (Scored<FrameSemanticParse> parse : sortedParses.elementSet()) {
            kbestParses.put(rank, parse);
            rank++;
        }
        this.numParses = kbestParses.size();
    }

    public FrameSemanticParse getParseAtRank(int rank) {
        return kbestParses.get(rank).entity;
    }

    public double getFscoreAtRank(int rank) {
        return kbestParses.get(rank).fscore;
    }

    // public static class Xio implements Comparable<Xio> {
    // String s;
    // Double b;
    // public Xio(String s, double b) {
    // super();
    // this.s = s;
    // this.b = b;
    // }
    // @Override
    // public int compareTo(Xio o) {
    // // Sort in descending order of scores
    // return -1 * b.compareTo(o.b);
    // }
    //
    // }
    //
    // public static void main(String[] args) {
    // TreeMultiset<Xio> treeMultiset = TreeMultiset.create();
    // treeMultiset.add(new Xio("a", 89.0));
    // treeMultiset.add(new Xio("b", 23.0));
    // treeMultiset.add(new Xio("b2", 23.0));
    // treeMultiset.add(new Xio("b3", 23.0));
    // treeMultiset.add(new Xio("c", 55.0));
    // for (Xio x : treeMultiset.elementSet()) {
    // System.err.println(x.s + " " + x.b);
    // }
    // }
}
