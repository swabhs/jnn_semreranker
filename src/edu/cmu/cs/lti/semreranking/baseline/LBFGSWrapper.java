package edu.cmu.cs.lti.semreranking.baseline;

import java.util.Map;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;

import edu.cmu.cs.lti.nlp.swabha.basic.Pair;
import edu.cmu.cs.lti.nlp.swabha.fileutils.BasicFileWriter;
import edu.cmu.cs.lti.semreranking.baseline.riso.numerical.LBFGS;
import edu.cmu.cs.lti.semreranking.utils.MathUtils;

/**
 * Convenience functions for riso.numerical.LBFGS. Calls with reasonable defaults.
 * 
 * @author sthomson@cs.cmu.edu, swabha@cs.cmu.edu
 */
public class LBFGSWrapper {

    /** LBFGS constants */
    // number of corrections, between 3 and 7
    // a higher number means more computation per iteration,
    // but possibly less iterations until convergence
    public static int NUM_CORRECTIONS = 6;
    public static double STOPPING_THRESHOLD = 1.0e-4;
    public static double XTOL = calculateMachineEpsilon();
    // estimate of machine precision. ~= 2.220446049250313E-16
    public static int MAX_ITERATIONS = 2000;
    // we've converged when ||gradient step|| <= STOPPING_THRESHOLD * max(||parameters||, 1)

    /** regularization constant */
    private double lambda;

    public static boolean DEBUG = true;
    public static int SAVE_EVERY_K = 10;

    public LBFGSWrapper(double lambda) {
        this.lambda = lambda;
    }

    public double[] trainAndSaveModel(
            double[] startingParams,
            Features trainFeatures,
            String modelFilePrefix,
            final int msId) throws Exception {

        // parameters needed for lbfgs
        double[] params = startingParams.clone();
        int modelSize = params.length;

        int iter = 0;
        // Output every iteration:
        // iteration count, number of function evaluations,
        // function value, norm of the gradient, and step-length
        int[] iprint = new int[]{DEBUG ? 1 : -1, 0};
        // lbfgs sets this flag to zero when it has converged
        final int[] iflag = {0};
        // unused
        final double[] diag = new double[modelSize];
        final boolean diagco = false;
        // double initLoss = freq.size() * Math.log(freq.get(0).size());// assuming all params are
        // // initialized to 0
        // System.err.println("initial log loss = " + initLoss);
        try {
            do {
                Pair<double[], Double> valAndGrad = getGradientAndLoss(params, trainFeatures);
                double loss = valAndGrad.second;
                // System.err.println("iter = " + iter + " log loss = " + loss);
                double[] gradients = valAndGrad.first;
                // final long startTime = System.currentTimeMillis();
                LBFGS.lbfgs(modelSize, NUM_CORRECTIONS, params, loss, gradients, diagco,
                        diag, iprint, STOPPING_THRESHOLD, XTOL, iflag);
                // final long endTime = System.currentTimeMillis();
                // System.err.println(String
                // .format("took %s seconds.", (endTime - startTime) / 1000.0));
                iter++;

                if (iter % SAVE_EVERY_K == 0) {
                    final String modelFileName = String.format("%s_%05d", modelFilePrefix, iter);
                    BasicFileWriter.writeDoubles(LBFGS.solution_cache, modelFileName);
                }
            } while (iter <= MAX_ITERATIONS && iflag[0] != 0);
        } catch (LBFGS.ExceptionWithIflag e) {
            // these exceptions happen sometimes even though the training was successful
            // we still want to save the most recent model
            // TODO: separate out the ok exceptions from the bad exceptions
            e.printStackTrace();
            System.err.println("line search stopped.");
        }
        // final String modelFilename = String.format("%s_%05d", modelFilePrefix, iter);
        // BasicFileWriter.writeDoubles(LBFGS.solution_cache, modelFilename);
        return params;
    }

    /**
     * Calculates the logloss function and the gradient wrt parameters of the same. N = # training
     * data points, K = # unique best parses of a sentence S = # features, for us S=2
     * 
     * @param params
     *            vector of size S, containing the weight of the Sth feature.
     * @param trainFeats
     *            N X K X S matrix. May contain repetitions
     * @param lambda
     *            regularization constant
     * @return array of doubles which is the gradient for every parameter, value of loss function
     */
    public Pair<double[], Double> getGradientAndLoss(double[] params, Features trainFeats) {
        int N = trainFeats.numEx;
        int K = trainFeats.numRanks;
        int S = params.length;
        Table<Integer, Integer, Double> dotProds = HashBasedTable.create();
        Map<Integer, Double> sumExpDotProd = Maps.newHashMap();
        for (int i = 0; i < N; i++) {
            sumExpDotProd.put(i, 0.0);
            for (int k = 0; k < K; k++) {
                double dp = MathUtils.dotProd(params, trainFeats.get(i, k));
                dotProds.put(i, k, dp);
                sumExpDotProd.put(i, sumExpDotProd.get(i) + Math.exp(dp));
            }
        }

        double[] grad = new double[S];
        for (int s = 0; s < S; s++) {
            grad[s] = lambda * params[s];
            for (int i = 0; i < N; i++) {
                for (int k = 0; k < K; k++) {
                    grad[s] += Math.exp(dotProds.get(i, k)) * trainFeats.get(i, 0)[s];
                }
                grad[s] /= sumExpDotProd.get(i);
                grad[s] -= trainFeats.get(i, 0)[s];
            }
        }

        double loss = lambda * MathUtils.dotProd(params, params) / 2;
        for (int i = 0; i < N; i++) {
            loss += Math.log(sumExpDotProd.get(i)) - dotProds.get(i, 0);
        }
        return new Pair<double[], Double>(null, loss);
    }

    private static double calculateMachineEpsilon() {
        double machineEpsilon = 1.0;
        do {
            machineEpsilon /= 2.0;
        } while (1.0 + (machineEpsilon / 2.0) != 1.0);
        return machineEpsilon;
    }

}
