package edu.cmu.cs.lti.semreranking.lossfunctions;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import jnn.neuron.DenseNeuronArray;

import com.google.common.collect.Maps;

import edu.cmu.cs.lti.semreranking.TrainInstance;

public abstract class Loss {

    public abstract double getLoss(Map<Integer, DenseNeuronArray> rankScoreMap,
            TrainInstance instance);

    public abstract int getNumComparisons(int numParses);

    public int getPredictedTrainRankOfBestParse(Map<Integer, DenseNeuronArray> rankScoreMap) {
        TreeMap<Double, Integer> scoreOrigRankMap = Maps.newTreeMap(Collections.reverseOrder());
        for (int rank : rankScoreMap.keySet()) {
            scoreOrigRankMap.put(rankScoreMap.get(rank).getNeuron(0), rank);
        }

        int predictedRank = 0;
        for (double key : scoreOrigRankMap.keySet()) {
            if (scoreOrigRankMap.get(key) == 0) {
                return predictedRank;
            }
            predictedRank++;
        }
        throw new IllegalArgumentException("could not find the best example!!!");
    }
}
