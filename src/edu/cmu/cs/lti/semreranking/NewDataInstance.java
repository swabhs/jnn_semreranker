package edu.cmu.cs.lti.semreranking;

import edu.cmu.cs.lti.semreranking.datastructs.FrameSemParse;
import edu.cmu.cs.lti.semreranking.datastructs.Scored;

public abstract class NewDataInstance {
    public int numUniqueParses;

    public String[] tokens;
    public String[] posTags;
    public int size;

    public NewDataInstance(int numParses, String[] tokens, String[] posTags) {
        this.numUniqueParses = numParses;
        this.tokens = tokens;
        this.posTags = posTags;
        this.size = tokens.length;
    }

    public abstract Scored<FrameSemParse> getParseAtRank(int rank);

}
