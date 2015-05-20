package edu.cmu.cs.lti.semreranking.jnn;

import java.util.Set;

public class FrameNetVocabs {

    public Set<String> tokens;
    public Set<String> frameIds;
    public Set<String> frameArguments;

    public FrameNetVocabs(Set<String> tokens, Set<String> frameIds, Set<String> frameArguments) {
        super();
        this.tokens = tokens;
        this.frameIds = frameIds;
        this.frameArguments = frameArguments;
    }
}
