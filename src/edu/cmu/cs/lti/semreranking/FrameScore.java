package edu.cmu.cs.lti.semreranking;

public class FrameScore {
    double pnum;
    double pdenom;
    double rnum;
    double rdenom;
    double fscore;
    public FrameScore(double pnum, double pdenom, double rnum, double rdenom, double fscore) {
        super();
        this.pnum = pnum;
        this.pdenom = pdenom;
        this.rnum = rnum;
        this.rdenom = rdenom;
        this.fscore = fscore;
    }
}
