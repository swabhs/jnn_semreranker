package edu.cmu.cs.lti.semreranking;

import java.util.List;
import java.util.Set;

public class NewTrainData extends NewData {

    public List<Set<NewTrainInstance>> trainInstances;

    public NewTrainData(List<Set<NewTrainInstance>> instances, int totNumParses) {
        super(instances.size(), totNumParses);
        this.trainInstances = instances;

        double totalUniqueFsps = 0.0;
        avgNumRanks = 0.0;
        for (Set<NewTrainInstance> allFspsOfEx : instances) {
            for (NewTrainInstance inst : allFspsOfEx) {
                totalUniqueFsps += (inst.numUniqueParses);
                avgNumRanks += inst.numUniqueParses;
            }
        }
        avgNumRanks /= size;

        System.err.println("Unique train instances: " + totalUniqueFsps);
        System.err.println("Avg train size = " + avgNumRanks);
    }

    @Override
    public NewDataInstance getInstance(int exNum, int fspNum) {
        return trainInstances.get(exNum).iterator().next(); // TODO:fix
    }
}
