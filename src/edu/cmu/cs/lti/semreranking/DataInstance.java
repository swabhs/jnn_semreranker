package edu.cmu.cs.lti.semreranking;

import edu.cmu.cs.lti.semreranking.datastructs.FrameSemParse;
import edu.cmu.cs.lti.semreranking.datastructs.FrameSemParse.FrameIdentifier;
import edu.cmu.cs.lti.semreranking.datastructs.Scored;

/**
 * Represents a single FrameSemParse in a list of k-best parses.
 * 
 * @author sswayamd
 *
 */
public abstract class DataInstance {
    public int exNum;
    public FrameIdentifier identifier;
    public int numUniqueParses;

    public DataInstance(
            int exNum,
            FrameIdentifier identifier,
            int numUniqueParses) {
        this.exNum = exNum;
        this.identifier = identifier;

        this.numUniqueParses = numUniqueParses;
    }

    public abstract Scored<FrameSemParse> getParseAtRank(int rank);

}
