package edu.cmu.cs.lti.semreranking.datastructs;

import java.util.Set;

import edu.cmu.cs.lti.semreranking.utils.FrequencySet;

public class FrameNetVocabs {

    public Set<String> tokens;
    public Set<String> posTags;
    // public Set<String> goldFNPosTags; // from FrameNet
    public Set<String> frameIds;
    public Set<String> frameArguments;

    public FrameNetVocabs(
            FrequencySet tokens,
            FrequencySet posTags,
            // Set<String> goldFNPosTags,
            Set<String> frameIds,
            Set<String> frameArguments) {
        this.tokens = tokens.keySet();
        this.posTags = posTags.keySet();
        // this.goldFNPosTags = goldFNPosTags;
        this.frameIds = frameIds;
        this.frameArguments = frameArguments;
    }

}
