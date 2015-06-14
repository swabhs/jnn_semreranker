package edu.cmu.cs.lti.semreranking;

import java.util.List;

public class TestData extends Data {

    public List<TestInstance> testInstances;

    public TestData(List<TestInstance> testInstances, int totNumRanks) {
        super(testInstances.size(), totNumRanks);
        this.testInstances = testInstances;

        double totalUniqueFsps = 0.0;
        avgNumRanks = 0.0;
        for (TestInstance inst : testInstances) {
            totalUniqueFsps += (inst.numUniqueParses);
            avgNumRanks += inst.numUniqueParses;
        }
        avgNumRanks /= size;

        System.err.println("Unique test instances: " + totalUniqueFsps);
        System.err.println("Avg test size = " + avgNumRanks);
    }

    @Override
    public DataInstance getInstance(int exNum) {
        return testInstances.get(exNum);
    }

}
