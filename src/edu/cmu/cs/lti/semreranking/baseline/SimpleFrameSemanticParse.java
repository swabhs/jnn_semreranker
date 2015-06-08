package edu.cmu.cs.lti.semreranking.baseline;

public class SimpleFrameSemanticParse {

    public double[] synsemscore;
    public double semscore; // sum of scores by SEMAFOR, given to all frames in the sentence

    public SimpleFrameSemanticParse(double synscore, double semscore) {
        synsemscore = new double[2];
        synsemscore[0] = synscore;
        synsemscore[1] = semscore;
    }

}
