package edu.cmu.cs.lti.semreranking;

import java.util.List;

import edu.cmu.cs.lti.semreranking.utils.Scored;

public class TrainingInstance {

    public List<Scored<FrameSemanticParse>> sortedParses;
    public int numParses;

    public String[] tokens;
    public int size;

    public TrainingInstance(String[] tokens, List<Scored<FrameSemanticParse>> parses) {
        this.sortedParses = parses;
        this.numParses = parses.size();
        this.tokens = tokens;
        this.size = tokens.length;
    }
}
