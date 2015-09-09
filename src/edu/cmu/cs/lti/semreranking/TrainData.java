package edu.cmu.cs.lti.semreranking;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.cmu.cs.lti.semreranking.datastructs.FrameSemParse;
import edu.cmu.cs.lti.semreranking.datastructs.FrameSemParse.FrameIdentifier;

public class TrainData extends Data {

    public Map<Integer, Map<FrameIdentifier, TrainInstance>> trainInstances;

    public TrainData(
            List<String[]> tokens,
            List<String[]> posTags,
            Map<Integer, Map<FrameIdentifier, TrainInstance>> trainInstances,
            int numTrainInstances,
            int numRanks) {
        super(tokens, posTags, numTrainInstances, numRanks);
        this.trainInstances = trainInstances;

        double totalUniqueFsps = 0.0;
        avgNumRanks = 0.0;
        for (int ex : trainInstances.keySet()) {
            for (FrameIdentifier identifier : trainInstances.get(ex).keySet()) {
                totalUniqueFsps += trainInstances.get(ex).get(identifier).numUniqueParses;
                avgNumRanks += trainInstances.get(ex).get(identifier).numUniqueParses;
            }
        }
        avgNumRanks /= numTrainInstances;

        System.err.println("Unique train instances: " + totalUniqueFsps);
        System.err.println("Avg train size = " + avgNumRanks);
    }

    @Override
    public DataInstance getInstance(int exNum, FrameIdentifier identifier) {
        return trainInstances.get(exNum).get(identifier);
    }

    @Override
    public Set<FrameIdentifier> getFramesInEx(int exNum) {
        if (trainInstances.containsKey(exNum) == false) { // TODO: when would this happen?
            return new HashSet<FrameSemParse.FrameIdentifier>();
        }
        return trainInstances.get(exNum).keySet();
    }
}
