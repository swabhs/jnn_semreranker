package edu.cmu.cs.lti.semreranking.lossfunctions;

import java.util.List;

import jnn.neuron.DenseNeuronArray;

import com.google.common.collect.Lists;
import com.google.common.collect.Table;

import edu.cmu.cs.lti.semreranking.TrainInstance;
import edu.cmu.cs.lti.semreranking.utils.MathUtils;

public class LogLoss {

    public static double getLoss(
            Table<Integer, Integer, DenseNeuronArray> scores,
            List<TrainInstance> instances) {

        int numRanks = scores.columnKeySet().size();

        double loss = 0.0;
        for (int ex : scores.rowKeySet()) {
            double errorAtTop = scores.get(ex, 0).getNeuron(0);

            double dotProducts[] = new double[numRanks];
            double partition = 0.0;
            for (int rank = 0; rank < numRanks; rank++) {
                double errorAtRank = scores.get(ex, rank).getNeuron(0);

                dotProducts[rank] = Math.exp(errorAtRank);
                partition += dotProducts[rank];

                scores.get(ex, rank).addError(0, errorAtRank - errorAtTop);
            }

            loss -= dotProducts[0] / partition;
        }

        return loss;
    }

    public static double getSmartLoss(Table<Integer, Integer, DenseNeuronArray> scores,
            List<TrainInstance> instances) {
        int numRanks = scores.columnKeySet().size();

        double loss = 0.0;
        for (int ex : scores.rowKeySet()) {
            List<Double> dotProds = Lists.newArrayList();

            for (int rank = 0; rank < numRanks; rank++) {
                dotProds.add(scores.get(ex, rank).getNeuron(0));
                scores.get(ex, rank).addError(0, -dotProds.get(rank) + dotProds.get(0));
            }

            double partition = MathUtils.logSum(dotProds);

            loss -= dotProds.get(0) - partition;
        }
        return loss;
    }
}
