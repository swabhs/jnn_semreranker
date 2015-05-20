package edu.cmu.cs.lti.semreranking.utils;

import gnu.trove.map.TIntIntMap;

import java.util.List;

public class MathUtils {

    public static double dotProd(double[] arr1, double[] arr2) {
        if (arr1.length != arr2.length) {
            throw new IllegalArgumentException("vector sizes unequal for dot product");
        }
        double sum = 0.0;
        for (int i = 0; i < arr1.length; i++) {
            sum += arr1[i] * arr2[i];
        }
        return sum;
    }

    public static double dotProd(TIntIntMap featFreqs, double[] params, int modelScoreId,
            double modelScore) {

        double result = params[modelScoreId] * modelScore;
        for (int featId : featFreqs.keys()) {
            result += featFreqs.get(featId) * params[featId];
        }
        return result;
    }

    /** takes log(x1), log(x2).... and returns log(x1 +x2+...) */
    public static double logSum(List<Double> logs) {
        double sum = logs.get(0);
        for (int i = 1; i < logs.size(); i++) {
            double logx = sum;
            double logy = logs.get(i);
            if (logx > logy) {
                sum += Math.log(1 + Math.exp(logy - logx));
            } else {
                sum += Math.log(1 + Math.exp(logx - logy));
            }
        }
        return sum;
    }
}
