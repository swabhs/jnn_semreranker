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
        for (Scored<FrameSemanticParse> parse : sortedParses) {
            kbestParses.put(rank, parse);
            rank++;
        }
    }

    public FrameSemanticParse getParseAtRank(int rank) {
        return kbestParses.get(rank).entity;
    }

    public double getFscoreAtRank(int rank) {
        return kbestParses.get(rank).fscore;
    }
}
