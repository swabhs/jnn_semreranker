package edu.cmu.cs.lti.semreranking;

public class Scored<T> implements Comparable<Scored<T>> {
    public T entity;
    public Double fscore;
    public double rNum;
    public double rDenom;
    public double pNum;
    public double pDenom;

    public Scored(T fsp, Double fscore) {
        this.entity = fsp;
        this.fscore = fscore;
    }

    public Scored(T entity, Double fscore, double rNum, double rDenom, double pNum, double pDenom) {
        this.entity = entity;
        this.fscore = fscore;
        this.rNum = rNum;
        this.rDenom = rDenom;
        this.pNum = pNum;
        this.pDenom = pDenom;
    }

    @Override
    public int compareTo(Scored<T> o) {
        // Sort in descending order of scores
        return -1 * fscore.compareTo(o.fscore);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((entity == null) ? 0 : entity.hashCode());
        result = prime * result + ((fscore == null) ? 0 : fscore.hashCode());
        long temp;
        temp = Double.doubleToLongBits(pDenom);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(pNum);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(rDenom);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(rNum);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        Scored other = (Scored) obj;
        if (entity == null) {
            if (other.entity != null)
                return false;
        } else if (!entity.equals(other.entity))
            return false;
        if (fscore == null) {
            if (other.fscore != null)
                return false;
        } else if (!fscore.equals(other.fscore))
            return false;
        if (Double.doubleToLongBits(pDenom) != Double.doubleToLongBits(other.pDenom))
            return false;
        if (Double.doubleToLongBits(pNum) != Double.doubleToLongBits(other.pNum))
            return false;
        if (Double.doubleToLongBits(rDenom) != Double.doubleToLongBits(other.rDenom))
            return false;
        if (Double.doubleToLongBits(rNum) != Double.doubleToLongBits(other.rNum))
            return false;
        return true;
    }

}