package edu.cmu.cs.lti.semreranking;

import java.util.List;

public class TrainData extends Data {

    public List<TrainInstance> trainInstances;

    public TrainData(List<TrainInstance> instances, int totNumParses) {
        super(instances.size(), totNumParses);
        this.trainInstances = instances;

        double totalUniqueFsps = 0.0;
        avgNumRanks = 0.0;
        for (TrainInstance inst : instances) {
            totalUniqueFsps += (inst.numUniqueParses);
            avgNumRanks += inst.numUniqueParses;
        }
        avgNumRanks /= size;

        System.err.println("Unique train instances: " + totalUniqueFsps);
        System.err.println("Avg train size = " + avgNumRanks);
    }

    @Override
    public DataInstance getInstance(int exNum) {
        return trainInstances.get(exNum);
    }
}
