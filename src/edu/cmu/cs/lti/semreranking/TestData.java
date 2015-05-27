package edu.cmu.cs.lti.semreranking;

import java.util.Map;

public class TestData {

    public Map<Integer, TestInstance> testInstances;
    public int numRanks;

    public TestData(Map<Integer, TestInstance> testInstances) {
        super();
        this.testInstances = testInstances;
        this.numRanks = testInstances.entrySet().iterator().next().getValue().numParses;
    }

}
