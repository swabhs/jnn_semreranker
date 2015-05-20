package edu.cmu.cs.lti.semreranking.utils;

public class Scored<T> implements Comparable<Scored<T>> {
    public T entity;
    public Double fscore;

    public Scored(T fsp, Double fscore) {
        this.entity = fsp;
        this.fscore = fscore;
    }

    @Override
    public int compareTo(Scored<T> o) {
        // Sort in descending order of scores
        return -1 * fscore.compareTo(o.fscore);
    }
}