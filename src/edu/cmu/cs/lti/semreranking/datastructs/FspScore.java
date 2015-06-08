package edu.cmu.cs.lti.semreranking.datastructs;

public class FspScore {
    double pnum;
    double pdenom;
    double rnum;
    double rdenom;
    double fscore;
    
    public FspScore(double pnum, double pdenom, double rnum, double rdenom, double fscore) {
        super();
        this.pnum = pnum;
        this.pdenom = pdenom;
        this.rnum = rnum;
        this.rdenom = rdenom;
        this.fscore = fscore;
    }
}
