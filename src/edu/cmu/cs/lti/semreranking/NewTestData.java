package edu.cmu.cs.lti.semreranking;

import java.util.List;
import java.util.Set;

public class NewTestData extends NewData {

    public List<Set<NewTestInstance>> testInstances;

    public NewTestData(List<Set<NewTestInstance>> testInstances, int totNumRanks) {
        super(testInstances.size(), totNumRanks);
        this.testInstances = testInstances;

        double totalUniqueFsps = 0.0;
        avgNumRanks = 0.0;
        for (Set<NewTestInstance> allFspsForEx : testInstances) {
            for (NewTestInstance inst : allFspsForEx) {
                totalUniqueFsps += inst.numUniqueParses;
                avgNumRanks += inst.numUniqueParses;
            }
        }
        avgNumRanks /= size;

        System.err.println("Unique test instances: " + totalUniqueFsps);
        System.err.println("Avg test size = " + avgNumRanks);
    }

    @Override
    public NewDataInstance getInstance(int exNum, int fspNum) {
        return testInstances.get(exNum).iterator().next(); // TODO: fix
    }

}
