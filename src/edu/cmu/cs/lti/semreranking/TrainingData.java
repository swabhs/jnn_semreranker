package edu.cmu.cs.lti.semreranking;

import java.util.List;

import edu.cmu.cs.lti.semreranking.jnn.FrameNetVocabs;

public class TrainingData {
    public List<TrainingInstance> instances;
    public FrameNetVocabs vocabs;
    public int size;

    public TrainingData(List<TrainingInstance> instances, FrameNetVocabs vocabs) {
        this.instances = instances;
        this.vocabs = vocabs;
        this.size = instances.size();
    }
}
