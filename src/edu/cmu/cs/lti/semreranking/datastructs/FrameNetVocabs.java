package edu.cmu.cs.lti.semreranking.datastructs;

import java.util.Set;

public class FrameNetVocabs {

    public Set<String> tokens;
    public Set<String> posTags;
    public Set<String> goldFNPosTags; // from FrameNet
    public Set<String> frameIds;
    public Set<String> frameArguments;

    public FrameNetVocabs(
            Set<String> tokens,
            Set<String> posTags,
            Set<String> goldFNPosTags,
            Set<String> frameIds,
            Set<String> frameArguments) {
        this.tokens = tokens;
        this.posTags = posTags;
        this.goldFNPosTags = goldFNPosTags;
        this.frameIds = frameIds;
        this.frameArguments = frameArguments;
    }

}
