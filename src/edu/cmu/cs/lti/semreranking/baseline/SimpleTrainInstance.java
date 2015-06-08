package edu.cmu.cs.lti.semreranking.baseline;

import java.util.Map;

import com.google.common.collect.Maps;
import com.google.common.collect.TreeMultiset;

import edu.cmu.cs.lti.semreranking.datastructs.Scored;

public class SimpleTrainInstance {

    public Map<Integer, Scored<SimpleFrameSemanticParse>> kBestParses;
    public int numRanks;

    public SimpleTrainInstance(TreeMultiset<Scored<SimpleFrameSemanticParse>> sortedParses) {
        kBestParses = Maps.newHashMap();
        int rank = 0;
        for (Scored<SimpleFrameSemanticParse> scoredParse : sortedParses.elementSet()) {
            kBestParses.put(rank, scoredParse);
            rank++;
        }
        numRanks = kBestParses.size();
    }

}
