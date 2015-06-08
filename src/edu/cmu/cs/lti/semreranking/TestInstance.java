package edu.cmu.cs.lti.semreranking;

import java.util.List;

import edu.cmu.cs.lti.semreranking.datastructs.FrameSemanticParse;
import edu.cmu.cs.lti.semreranking.datastructs.Scored;

public class TestInstance extends DataInstance {

    public List<Scored<FrameSemanticParse>> unsortedParses;

    public TestInstance(String[] tokens, String[] posTags, List<Scored<FrameSemanticParse>> parses) {
        super(parses.size(), tokens, posTags);
        this.unsortedParses = parses;
    }

}
