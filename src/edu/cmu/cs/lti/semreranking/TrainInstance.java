package edu.cmu.cs.lti.semreranking;

import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import com.google.common.collect.Maps;

import edu.cmu.cs.lti.semreranking.datastructs.FrameSemParse;
import edu.cmu.cs.lti.semreranking.datastructs.FrameSemParse.FrameIdentifier;
import edu.cmu.cs.lti.semreranking.datastructs.Scored;

public class TrainInstance extends DataInstance {

    /* parses ranked by SEMAFOR gold f-score: TODO check if completely true */
    final private TreeMap<Integer, Scored<FrameSemParse>> goldRankParseMap;

    /* parses ranked by syntactic score */
    final private TreeMap<Integer, Scored<FrameSemParse>> rankParseMap;

    public TrainInstance(
            int exID,
            FrameIdentifier identifier,
            List<Scored<FrameSemParse>> parses) {
        super(exID, identifier, parses.size());

        this.rankParseMap = Maps.newTreeMap();
        int rank = 0;
        for (Scored<FrameSemParse> parse : parses) {
            rankParseMap.put(rank, parse);
            rank++;
        }

        /* sorting the parses by gold f-score in descending order */
        TreeSet<Scored<FrameSemParse>> kbestParses = new TreeSet<Scored<FrameSemParse>>();
        kbestParses.addAll(parses);

        this.goldRankParseMap = Maps.newTreeMap();
        int goldRank = 0;
        for (Scored<FrameSemParse> parse : kbestParses) {
            parse.origRank = goldRank; // for debugging purposes, TODO: remove?
            goldRankParseMap.put(goldRank, parse);
            goldRank++;
        }

        this.numUniqueParses = kbestParses.size();
    }

    @Override
    public Scored<FrameSemParse> getParseAtRank(int goldRank) {
        return goldRankParseMap.get(goldRank);
    }

    public double getFscoreAtRank(int goldRank) {
        if (goldRankParseMap.containsKey(goldRank) == false) {
            throw new IllegalArgumentException("rank not present!");
        }
        return goldRankParseMap.get(goldRank).fscore;
    }

    public Scored<FrameSemParse> getUnsortedParseAtRank(int rank) {
        return rankParseMap.get(rank);
    }

}
