package edu.cmu.cs.lti.semreranking;

public class SemRerankerMain {

    public static void readTrainingData() {
        System.err.println("reading training data - DONE");
    }

    public static void runLbfgs() {
        System.err.println("implemented log loss and pairwise loss");
        System.err.println("running lbfgs");
    }

    public static void decode() {
        System.err.println("decoding the test data and calculating the accuracy");
    }

    public static void main(String[] args) {
        readTrainingData();
        runLbfgs();
        decode();
    }

}
