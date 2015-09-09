package edu.cmu.cs.lti.semreranking;

import java.util.List;
import java.util.Set;

import edu.cmu.cs.lti.semreranking.datastructs.FrameSemParse.FrameIdentifier;

public abstract class Data {

    public List<String[]> tokens;
    public List<String[]> posTags;

    public int numInstances; // num of IID examples in the data
    public int numRanks; // typically k=100 for our experiments
    public double avgNumRanks; // set in the inherited classes

    public Data(List<String[]> tokens, List<String[]> posTags, int numInstances, int numRanks) {
        this.tokens = tokens;
        this.posTags = posTags;
        this.numInstances = numInstances;
        this.numRanks = numRanks;
    }

    public abstract Set<FrameIdentifier> getFramesInEx(int exNum);
    public abstract DataInstance getInstance(int exNum, FrameIdentifier identifier);
}
