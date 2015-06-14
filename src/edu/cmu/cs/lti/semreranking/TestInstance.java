package edu.cmu.cs.lti.semreranking;

import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import edu.cmu.cs.lti.semreranking.datastructs.FrameSemanticParse;
import edu.cmu.cs.lti.semreranking.datastructs.Scored;

public class TestInstance extends DataInstance {

    /* parses ranked by syntactic score */
    final private TreeMap<Integer, Scored<FrameSemanticParse>> rankParseMap;

    public TestInstance(String[] tokens, String[] posTags, List<Scored<FrameSemanticParse>> parses) {
        super(parses.size(), tokens, posTags);
        this.rankParseMap = Maps.newTreeMap();

        // uniquing all the parses:
        Set<Scored<FrameSemanticParse>> uniqueParses = Sets.newHashSet();
        int rank = 0;
        for (Scored<FrameSemanticParse> parse : parses) {
            if (uniqueParses.contains(parse)) {
                continue;
            }
            uniqueParses.add(parse);
            rankParseMap.put(rank, parse);
            rank++;
        }
        this.numUniqueParses = uniqueParses.size();
    }

    @Override
    public Scored<FrameSemanticParse> getParseAtRank(int rank) {
        return rankParseMap.get(rank);
    }

}
