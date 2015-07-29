package edu.cmu.cs.lti.semreranking;

public abstract class NewData {

    public int size;
    public double avgNumRanks;
    public int numTotParses;

    public NewData(int size, int numTotParses) {
        this.size = size;
        this.numTotParses = numTotParses;
    }

    public abstract NewDataInstance getInstance(int exNum, int fspNum);

}
