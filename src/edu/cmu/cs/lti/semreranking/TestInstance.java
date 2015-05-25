package edu.cmu.cs.lti.semreranking;

import java.util.List;

public class TestInstance extends DataInstance {

    public List<Scored<FrameSemanticParse>> unsortedParses;

    public TestInstance(String[] tokens, List<Scored<FrameSemanticParse>> parses) {
        super(parses.size(), tokens);
        this.unsortedParses = parses;

    }

}
