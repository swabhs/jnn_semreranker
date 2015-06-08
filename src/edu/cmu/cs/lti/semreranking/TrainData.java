package edu.cmu.cs.lti.semreranking;

import java.util.List;

public class TrainData {
    public List<TrainInstance> trainInstances;
    public int totalUniqueFsps = 0;

    public TrainData(List<TrainInstance> instances) {
        this.trainInstances = instances;

        for (TrainInstance inst : instances) {
            totalUniqueFsps += (inst.kbestParses.size());
        }
        System.err.println("Unique train instances: " + totalUniqueFsps);
    }
}
