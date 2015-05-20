package edu.cmu.cs.lti.semreranking.lossfunctions;

/**
 * Softmax margin pair-wise loss function
 * 
 * @author sswayamd
 *
 */
public class PairwiseLoss {

    public static double getLoss(double[][] scores, double margin) {
        int numEx = scores.length;
        int numRanks = scores[0].length;

        double loss = 0.0;
        for (int n = 0; n < numEx; n++) {
            for (int i = 0; i < numRanks - 1; i++) {
                for (int j = i + 1; j < numRanks; j++) {
                    loss += Math.exp(margin - scores[n][i] + scores[n][j]);
                }
            }
        }
        return loss;
    }

    public static double getLoss(double[][] scores, double[][] fscores) {
        int numEx = scores.length;
        int numRanks = scores[0].length;

        double loss = 0.0;
        for (int n = 0; n < numEx; n++) {
            for (int i = 0; i < numRanks - 1; i++) {
                for (int j = i + 1; j < numRanks; j++) {
                    loss += Math.exp(fscores[n][i] - fscores[n][j] - scores[n][i] + scores[n][j]);
                }
            }
        }
        return loss;
    }
}
