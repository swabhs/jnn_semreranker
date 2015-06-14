package edu.cmu.cs.lti.semreranking.lossfunctions;

import java.util.Map;

import jnn.neuron.DenseNeuronArray;
import edu.cmu.cs.lti.semreranking.TrainInstance;

public class LogLoss extends Loss {

    @Override
    public double getLoss(
            Map<Integer, DenseNeuronArray> rankScoreMap, TrainInstance instance) {
        int numRanks = instance.numUniqueParses;

        double partition = 0.0;
        double highestScore = Double.NEGATIVE_INFINITY;
        for (int k = 0; k < numRanks; k++) {
            partition += Math.exp(rankScoreMap.get(k).getNeuron(0));
            if (rankScoreMap.get(k).getNeuron(0) > highestScore) {
                highestScore = rankScoreMap.get(k).getNeuron(0);
            }
        }

        double probFirst = rankScoreMap.get(0).getNeuron(0) / partition;
        if (rankScoreMap.get(0).getNeuron(0) < highestScore || probFirst < 0.5) {
            double firstGrad = -1.0 + Math.exp(rankScoreMap.get(0).getNeuron(0)) / partition;
            rankScoreMap.get(0).addError(0, -firstGrad);

            for (int k = 1; k < numRanks; k++) {
                double grad = Math.exp(rankScoreMap.get(k).getNeuron(0)) / partition;
                rankScoreMap.get(k).addError(0, -grad);
            }
        }

        double loss = -rankScoreMap.get(0).getNeuron(0) + Math.log(partition);
        return loss;
    }

    public int getNumComparisons(int numParses) {
        return 1;
    }

    // public static double getLossOld(Table<Integer, Integer, DenseNeuronArray> scores,
    // List<TrainInstance> instances) {
    // int numRanks = scores.columnKeySet().size();
    //
    // double loss = 0.0;
    // for (int ex : scores.rowKeySet()) {
    // List<Double> dotProds = Lists.newArrayList();
    //
    // for (int rank = 0; rank < numRanks; rank++) {
    // dotProds.add(scores.get(ex, rank).getNeuron(0));
    // scores.get(ex, rank).addError(0, -dotProds.get(rank) + dotProds.get(0));
    // }
    //
    // double partition = MathUtils.logSum(dotProds);
    //
    // loss -= dotProds.get(0) - partition;
    // }
    // return loss;
    // }

}
