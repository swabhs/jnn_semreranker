package edu.cmu.cs.lti.semreranking;

public class RerankingData {

    public TrainData trainData;
    public TestData testData;
    public TestData devData;
    public RerankingData(TrainData trainData, TestData testData, TestData devData) {
        super();
        this.trainData = trainData;
        this.testData = testData;
        this.devData = devData;
    }

}
