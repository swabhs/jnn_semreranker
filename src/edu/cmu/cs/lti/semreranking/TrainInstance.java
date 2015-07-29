package edu.cmu.cs.lti.semreranking;

import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import com.google.common.collect.Maps;

import edu.cmu.cs.lti.semreranking.datastructs.FrameSemAnalysis;
import edu.cmu.cs.lti.semreranking.datastructs.Scored;

public class TrainInstance extends DataInstance {

    /* parses ranked by SEMAFOR gold f-score */
    final private TreeMap<Integer, Scored<FrameSemAnalysis>> goldRankParseMap;

    /* parses ranked by syntactic score */
    final private TreeMap<Integer, Scored<FrameSemAnalysis>> rankParseMap;

    public TrainInstance(String[] tokens, String[] posTags, List<Scored<FrameSemAnalysis>> parses) {
        super(parses.size(), tokens, posTags);

        this.rankParseMap = Maps.newTreeMap();
        int rank = 0;
        for (Scored<FrameSemAnalysis> parse : parses) {
            rankParseMap.put(rank, parse);
            rank++;
        }

        /* sorting the parses by gold f-score */
        TreeSet<Scored<FrameSemAnalysis>> kbestParses = new TreeSet<Scored<FrameSemAnalysis>>();
        kbestParses.addAll(parses);

        this.goldRankParseMap = Maps.newTreeMap();
        int goldRank = 0;
        for (Scored<FrameSemAnalysis> parse : kbestParses) {
            parse.origRank = goldRank; // for debugging purposes, TODO: remove?
            goldRankParseMap.put(goldRank, parse);
            goldRank++;
        }

        this.numUniqueParses = kbestParses.size();
    }

    @Override
    public Scored<FrameSemAnalysis> getParseAtRank(int goldRank) {
        return goldRankParseMap.get(goldRank);
    }

    public double getFscoreAtRank(int goldRank) {
        if (goldRankParseMap.containsKey(goldRank) == false) {
            throw new IllegalArgumentException("rank not present!");
        }
        return goldRankParseMap.get(goldRank).fscore;
    }

    public Scored<FrameSemAnalysis> getUnsortedParseAtRank(int rank) {
        return rankParseMap.get(rank);
    }

}
