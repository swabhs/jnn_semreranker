package edu.cmu.cs.lti.semreranking.datastructs;

import edu.cmu.cs.lti.semreranking.evaluation.Result;

/**
 * Score in terms of true and false positives and negatives given to a frame-semantic parse,
 * according to the SemEval 2007 scheme.
 * 
 * @author sswayamd
 *
 */
public class SemevalScore {
    public double pnum;
    public double pdenom;
    public double rnum;
    public double rdenom;
    public double fscore;

    public SemevalScore(double pnum, double pdenom, double rnum, double rdenom) {
        this.pnum = pnum;
        this.pdenom = pdenom;
        this.rnum = rnum;
        this.rdenom = rdenom;
        this.fscore = Result.getFscore(pnum, pdenom, rnum, rdenom);
    }

    public SemevalScore() {
        this(0.0, 0.0, 0.0, 0.0);
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
        SemevalScore other = (SemevalScore) obj;

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

    @Override
    public String toString() {
        return Result.getRecallOrPrec(rnum, rdenom) + "(" + rnum + "/" + rdenom + ")\t"
                + Result.getRecallOrPrec(pnum, pdenom) + "(" + pnum + "/" + pdenom + ")\t"
                + fscore;
    }

}
