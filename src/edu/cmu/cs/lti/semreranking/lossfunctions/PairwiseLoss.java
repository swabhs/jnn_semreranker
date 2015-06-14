package edu.cmu.cs.lti.semreranking.lossfunctions;

import java.util.Map;

import jnn.neuron.DenseNeuronArray;
import edu.cmu.cs.lti.semreranking.TrainInstance;
import edu.cmu.cs.lti.semreranking.utils.MathUtils;

/**
 * Max margin pair-wise loss function
 * 
 * @author sswayamd
 *
 */
public class PairwiseLoss extends Loss {

    public int getNumComparisons(int numParses) {
        return numParses * (numParses + 1) / 2;
    }

    public double getLoss(
            Map<Integer, DenseNeuronArray> rankScoreMap, TrainInstance instance) {
        int numRanks = instance.numUniqueParses;

        double loss = 0.0;

        for (int i = 0; i < numRanks - 1; i++) {
            for (int j = i + 1; j < numRanks; j++) {
                double margin = instance.getFscoreAtRank(i) - instance.getFscoreAtRank(j);
                double better = rankScoreMap.get(i).getNeuron(0);
                double wworse = rankScoreMap.get(j).getNeuron(0);

                double iloss = MathUtils.hinge(margin - better + wworse);

                if (iloss > 0.0) {
                    double betterGrad = -1.0;
                    double worseGrad = 1.0;

                    double betterError = -betterGrad; // y_new - y_old = - grad * stepsize
                    double worseError = -worseGrad;
                    rankScoreMap.get(i).addError(0, betterError);
                    rankScoreMap.get(j).addError(0, worseError);
                }
                loss += iloss;

            }
        }
        return loss;
    }

}
