package edu.cmu.cs.lti.semreranking.lossfunctions;

import java.util.List;
import java.util.Map;

import jnn.neuron.DenseNeuronArray;

import com.google.common.collect.Table;

import edu.cmu.cs.lti.semreranking.TrainInstance;
import edu.cmu.cs.lti.semreranking.utils.MathUtils;

/**
 * Softmax margin pair-wise loss function
 * 
 * @author sswayamd
 *
 */
public class PairwiseLoss {

    public static double getLoss(
            Map<Integer, DenseNeuronArray> scores, TrainInstance instance) {
        int numRanks = instance.numParses;

        double loss = 0.0;
        final double stepSize = 1.0; // SemRerankerMain.learningRate;

        for (int i = 0; i < numRanks - 1; i++) {
            for (int j = i + 1; j < numRanks; j++) {
                double margin = instance.getFscoreAtRank(i) - instance.getFscoreAtRank(j);
                double better = scores.get(i).getNeuron(0);
                double wworse = scores.get(j).getNeuron(0);

                double iloss = MathUtils.hinge(margin - better + wworse);

                if (iloss > 0.0) {
                    double betterGrad = -1.0;
                    double worseGrad = 1.0;

                    double betterError = -betterGrad * stepSize; // y_new - y_old = - grad *
                                                                 // stepsize
                    double worseError = -worseGrad * stepSize;
                    scores.get(i).addError(0, betterError);
                    scores.get(j).addError(0, worseError);
                }
                loss += iloss;

            }
        }
        return loss;
    }

    public static double getLossTest(
            Table<Integer, Integer, DenseNeuronArray> scores, List<Double> fscores) {
        int numEx = scores.rowKeySet().size();
        int numRanks = scores.columnKeySet().size();

        double loss = 0.0;
        for (int n = 0; n < numEx; n++) {
            for (int i = 0; i < numRanks - 1; i++) {
                for (int j = i + 1; j < numRanks; j++) {
                    double margin = fscores.get(i) - fscores.get(j);
                    double better = scores.get(n, i).getNeuron(0);
                    double worse = scores.get(n, j).getNeuron(0);
                    double iloss = Math.abs(margin - better + worse);
                    loss += iloss;
                    // loss = max{0, margin - score(true) + score(false)}
                    if (iloss > 0.0) {
                        // dLoss / dscore(true) = -1
                        // dLoss / dscore(false) = +1
                        scores.get(n, i).addError(0, 1.0);
                        scores.get(n, j).addError(0, -1.0);
                    }
                }
            }
        }
        return loss;
    }
}
