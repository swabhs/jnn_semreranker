package edu.cmu.cs.lti.semreranking.jnn;

import jnn.functions.parametrized.DenseFullyConnectedLayer;

public class FrameNetwork {

    DenseFullyConnectedLayer tokenLayer; // W_x
    DenseFullyConnectedLayer argLayer; // W_a

    DenseFullyConnectedLayer allArgsLayer; // V
    DenseFullyConnectedLayer frameLayer; // W_f
    DenseFullyConnectedLayer predLayer; // W_p

    DenseFullyConnectedLayer allFramesLayer; // U
    DenseFullyConnectedLayer synScoreLayer; // w_syn
    DenseFullyConnectedLayer semScoreLayer; // w_sem

    DenseFullyConnectedLayer scoreLayer;

    public FrameNetwork(int tokenInpDim, int frameArgInpDim, int frameIdInpDim,
            int argResultDim, int frameResultDim, int resultDim) {

        tokenLayer = new DenseFullyConnectedLayer(tokenInpDim, argResultDim);
        argLayer = new DenseFullyConnectedLayer(frameArgInpDim, argResultDim);

        allArgsLayer = new DenseFullyConnectedLayer(argResultDim, frameResultDim);
        frameLayer = new DenseFullyConnectedLayer(frameIdInpDim, frameResultDim);
        predLayer = new DenseFullyConnectedLayer(tokenInpDim, frameResultDim);

        allFramesLayer = new DenseFullyConnectedLayer(frameResultDim, resultDim);
        // synScoreLayer = new DenseFullyConnectedLayer(1, 1);
        // semScoreLayer = new DenseFullyConnectedLayer(1, 1);

        // scoreLayer = new DenseFullyConnectedLayer(resultDim + 2, 1); // TODO: how to?
        scoreLayer = new DenseFullyConnectedLayer(resultDim, 1);
    }

    public DenseFullyConnectedLayer getTokenLayer() {
        return tokenLayer;
    }

    public DenseFullyConnectedLayer getArgLayer() {
        return argLayer;
    }

    public DenseFullyConnectedLayer getAllArgsLayer() {
        return allArgsLayer;
    }

    public DenseFullyConnectedLayer getFrameLayer() {
        return frameLayer;
    }

    public DenseFullyConnectedLayer getPredLayer() {
        return predLayer;
    }

    public DenseFullyConnectedLayer getAllFramesLayer() {
        return allFramesLayer;
    }

    public DenseFullyConnectedLayer getSynScoreLayer() {
        return synScoreLayer;
    }

    public DenseFullyConnectedLayer getSemScoreLayer() {
        return semScoreLayer;
    }

    public DenseFullyConnectedLayer getScoreLayer() {
        return scoreLayer;
    }

}
