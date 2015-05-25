package edu.cmu.cs.lti.semreranking;

import java.util.List;

import edu.cmu.cs.lti.semreranking.jnn.FrameNetVocabs;

public class TrainData {
    public List<TrainInstance> trainInstances;
    // public List<DataInstance> testInstances;
    public FrameNetVocabs vocabs;

    public TrainData(
            List<TrainInstance> instances,
            // List<DataInstance> testInstances,
            FrameNetVocabs vocabs) {
        this.trainInstances = instances;
        // this.testInstances = testInstances;
        this.vocabs = vocabs;
    }
}
