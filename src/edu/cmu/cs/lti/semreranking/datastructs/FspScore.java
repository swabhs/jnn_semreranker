package edu.cmu.cs.lti.semreranking.datastructs;

import edu.cmu.cs.lti.semreranking.evaluation.Result;

public class FspScore {
    public double pnum;
    public double pdenom;
    public double rnum;
    public double rdenom;
    public double fscore;

    public FspScore(double pnum, double pdenom, double rnum, double rdenom, double fscore) {
        super();
        this.pnum = pnum;
        this.pdenom = pdenom;
        this.rnum = rnum;
        this.rdenom = rdenom;
        this.fscore = fscore;
    }

    public FspScore(double pnum, double pdenom, double rnum, double rdenom) {
        super();
        this.pnum = pnum;
        this.pdenom = pdenom;
        this.rnum = rnum;
        this.rdenom = rdenom;
        this.fscore = Result.getFscore(pnum, pdenom, rnum, rdenom);
    }

    public FspScore() {
        this.pnum = 0.0;
        this.pdenom = 0.0;
        this.rnum = 0.0;
        this.rdenom = 0.0;
        this.fscore = Result.getFscore(pnum, pdenom, rnum, rdenom);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(fscore);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(pdenom);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(pnum);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(rdenom);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(rnum);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FspScore other = (FspScore) obj;

        if (Double.doubleToLongBits(pdenom) != Double.doubleToLongBits(other.pdenom))
            return false;
        if (Double.doubleToLongBits(pnum) != Double.doubleToLongBits(other.pnum))
            return false;
        if (Double.doubleToLongBits(rdenom) != Double.doubleToLongBits(other.rdenom))
            return false;
        if (Double.doubleToLongBits(rnum) != Double.doubleToLongBits(other.rnum))
            return false;
        return true;
    }

}
