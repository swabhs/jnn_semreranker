package edu.cmu.cs.lti.semreranking.evaluation;

import java.text.NumberFormat;

import edu.cmu.cs.lti.semreranking.SemRerankerMain;

public class Result implements Comparable<Result> {

    public double pre;
    public double rec;
    public Double f1;

    public double macroF1;

    public Result(double prec, double rec, double f1, double macroF1) {
        this.pre = prec;
        this.rec = rec;
        this.f1 = f1;
        this.macroF1 = macroF1;
    }

    public Result() {
        this.pre = 0.0;
        this.rec = 0.0;
        this.f1 = 0.0;
    }

    @Override
    public int compareTo(Result o) {
        return f1.compareTo(o.f1);
    }

    @Override
    public String toString() {
        NumberFormat formatter = SemRerankerMain.formatter;
        return "P: " + formatter.format(pre)
                + "\tR: " + formatter.format(rec)
                + "\tF1: " + formatter.format(f1);
    }

    public static double getFscore(double precision, double recall) {
        if (precision == 0.0 && recall == 0.0) {
            return 0.0;
        }
        return 2.0 * precision * recall / (precision + recall);
    }

    public static double getFscore(double pNum, double pDenom, double rNum, double rDenom) {
        double precision = 0.0;
        double recall = 0.0;
        if (pDenom > 0.0) {
            precision = pNum / pDenom;
        }
        if (rDenom > 0.0) {
            recall = rNum / rDenom;
        }
        if (precision == 0.0 && recall == 0.0) {
            return 0.0;
        }
        return 2.0 * precision * recall / (precision + recall);
    }

}
