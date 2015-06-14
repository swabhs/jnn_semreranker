package edu.cmu.cs.lti.semreranking;

import edu.cmu.cs.lti.semreranking.datastructs.FrameSemanticParse;
import edu.cmu.cs.lti.semreranking.datastructs.Scored;

public abstract class DataInstance {

    public int numUniqueParses;

    public String[] tokens;
    public String[] posTags;
    public int size;

    public DataInstance(int numParses, String[] tokens, String[] posTags) {
        this.numUniqueParses = numParses;
        this.tokens = tokens;
        this.posTags = posTags;
        this.size = tokens.length;
    }

    public abstract Scored<FrameSemanticParse> getParseAtRank(int rank);

}
