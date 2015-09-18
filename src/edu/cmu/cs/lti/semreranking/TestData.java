package edu.cmu.cs.lti.semreranking;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.cmu.cs.lti.semreranking.datastructs.FrameSemParse;
import edu.cmu.cs.lti.semreranking.datastructs.FrameSemParse.FrameIdentifier;

public class TestData extends Data {

    public Map<Integer, Map<FrameIdentifier, TestInstance>> testInstances;

    public TestData(List<String[]> tokens,
            List<String[]> posTags,
            Map<Integer, Map<FrameIdentifier, TestInstance>> testInstances,
            int numTestInstances,
            int totNumRanks) {
        super(tokens, posTags, numTestInstances, totNumRanks);
        this.testInstances = testInstances;

        int totalUniqueFsps = 0;
        avgNumRanks = 0.0;
        for (int ex : testInstances.keySet()) {
            for (FrameIdentifier identifier : testInstances.get(ex).keySet()) {
                totalUniqueFsps += testInstances.get(ex).get(identifier).numUniqueParses;
                avgNumRanks += testInstances.get(ex).get(identifier).numUniqueParses;
            }
        }
        avgNumRanks /= numTestInstances;

        System.err.println("Num of test instances = " + numTestInstances);
        System.err.println("Total unique test FSPs = " + totalUniqueFsps);
        System.err.println("Avg test k-best size = " + avgNumRanks + "\n");
    }

    @Override
    public DataInstance getInstance(int exNum, FrameIdentifier identifier) {
        return testInstances.get(exNum).get(identifier);
    }

    @Override
    public Set<FrameIdentifier> getFramesInEx(int exNum) {
        if (testInstances.containsKey(exNum) == false) { // TODO: when would this happen?
            return new HashSet<FrameSemParse.FrameIdentifier>();
        }
        return testInstances.get(exNum).keySet();
    }

}
