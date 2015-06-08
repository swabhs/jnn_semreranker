package edu.cmu.cs.lti.semreranking;

import java.util.Map;

import com.google.common.collect.Maps;
import com.google.common.collect.TreeMultiset;

import edu.cmu.cs.lti.semreranking.datastructs.FrameSemanticParse;
import edu.cmu.cs.lti.semreranking.datastructs.Scored;

public class TrainInstance extends DataInstance {

    public Map<Integer, Scored<FrameSemanticParse>> kbestParses;

    public TrainInstance(
            String[] tokens,
            String[] posTags,
            TreeMultiset<Scored<FrameSemanticParse>> sortedParses) {
        super(sortedParses.size(), tokens, posTags);

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
}
