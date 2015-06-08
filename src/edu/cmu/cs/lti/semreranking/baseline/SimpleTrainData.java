package edu.cmu.cs.lti.semreranking.baseline;

import java.util.List;

public class SimpleTrainData {

    public List<SimpleTrainInstance> trainInstances;
    public int numEx;

    public SimpleTrainData(List<SimpleTrainInstance> trainInstances, int numEx) {
        super();
        this.trainInstances = trainInstances;
        this.numEx = numEx;
    }

}
