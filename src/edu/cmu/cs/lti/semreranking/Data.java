package edu.cmu.cs.lti.semreranking;

public abstract class Data {

    public int size;
    public double avgNumRanks;
    public int numTotParses;

    public Data(int size, int numTotParses) {
        this.size = size;
        this.numTotParses = numTotParses;
    }

    public abstract DataInstance getInstance(int exNum);

}
