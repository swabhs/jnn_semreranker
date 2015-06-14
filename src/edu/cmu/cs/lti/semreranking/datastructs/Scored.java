package edu.cmu.cs.lti.semreranking.datastructs;

// TODO: can be made better
public class Scored<T> implements Comparable<Scored<T>> {
    public T entity;
    public Double fscore;

    public FspScore detailedFspScore;

    public double synScore;
    public int origRank; // rank as given by syntactic/semantic parser, NOT based on fscore

    public Scored(T entity, FspScore fspScore, double synScore, int origRank) {
        this.entity = entity;
        this.fscore = fspScore.fscore;
        this.detailedFspScore = fspScore;
        this.synScore = synScore;
        this.origRank = origRank;
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
        result = prime * result + ((detailedFspScore == null) ? 0 : detailedFspScore.hashCode());
        result = prime * result + ((entity == null) ? 0 : entity.hashCode());
        result = prime * result + ((fscore == null) ? 0 : fscore.hashCode());
        long temp;
        temp = Double.doubleToLongBits(origRank);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(synScore);
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
        Scored other = (Scored) obj;
        if (detailedFspScore == null) {
            if (other.detailedFspScore != null)
                return false;
        } else if (!detailedFspScore.equals(other.detailedFspScore))
            return false;
        if (entity == null) {
            if (other.entity != null)
                return false;
        } else if (!entity.equals(other.entity))
            return false;
        return true;
    }

}