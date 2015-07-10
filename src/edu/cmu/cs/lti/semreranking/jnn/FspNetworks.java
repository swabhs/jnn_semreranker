package edu.cmu.cs.lti.semreranking.jnn;

import java.io.BufferedReader;
import java.io.PrintStream;

import jnn.functions.parametrized.DenseFullyConnectedLayer;
import util.IOUtils;

public class FspNetworks {

    DenseFullyConnectedLayer tokenLayer; // W_tok
    DenseFullyConnectedLayer posLayer; // W_pos
    DenseFullyConnectedLayer prevPosLayer;
    DenseFullyConnectedLayer nextPosLayer;

    DenseFullyConnectedLayer spanSizeLayer;
    DenseFullyConnectedLayer argLayer; // W_role

    DenseFullyConnectedLayer allArgsLayer; // V
    DenseFullyConnectedLayer numArgsLayer;
    DenseFullyConnectedLayer frameLayer; // W_frame

    DenseFullyConnectedLayer localScoresLayer; // alpha, beta
    DenseFullyConnectedLayer globalScoreLayer;

    // BLSTM argumentCombiner;

    public FspNetworks(ArrayParams ap) {

        tokenLayer = new DenseFullyConnectedLayer(ap.tokenInpDim, ap.argResultDim);
        posLayer = new DenseFullyConnectedLayer(ap.posInpDim, ap.argResultDim);
        prevPosLayer = new DenseFullyConnectedLayer(ap.posInpDim, ap.argResultDim);
        nextPosLayer = new DenseFullyConnectedLayer(ap.posInpDim, ap.argResultDim);

        spanSizeLayer = new DenseFullyConnectedLayer(ap.spanSizeDim, ap.argResultDim);
        argLayer = new DenseFullyConnectedLayer(ap.frameArgInpDim, ap.argResultDim);

        allArgsLayer = new DenseFullyConnectedLayer(ap.argResultDim, ap.frameResultDim);
        numArgsLayer = new DenseFullyConnectedLayer(ap.numArgsDim, ap.frameResultDim);
        frameLayer = new DenseFullyConnectedLayer(ap.frameIdInpDim, ap.frameResultDim);

        localScoresLayer = new DenseFullyConnectedLayer(ap.localScoresDim, 1);
        globalScoreLayer = new DenseFullyConnectedLayer(ap.frameResultDim, 1);
        // argumentCombiner = new BLSTM(ap.argResultDim, 150, ap.frameResultDim);// default is tanh
        // layer
    }

    public FspNetworks(String modelFileName) {
        BufferedReader in = IOUtils.getReader(modelFileName);
        tokenLayer = DenseFullyConnectedLayer.load(in);
        posLayer = DenseFullyConnectedLayer.load(in);
        prevPosLayer = DenseFullyConnectedLayer.load(in);
        nextPosLayer = DenseFullyConnectedLayer.load(in);

        spanSizeLayer = DenseFullyConnectedLayer.load(in);
        argLayer = DenseFullyConnectedLayer.load(in);

        allArgsLayer = DenseFullyConnectedLayer.load(in);
        numArgsLayer = DenseFullyConnectedLayer.load(in);
        frameLayer = DenseFullyConnectedLayer.load(in);

        localScoresLayer = DenseFullyConnectedLayer.load(in);
        globalScoreLayer = DenseFullyConnectedLayer.load(in);
    }

    public void saveAllParams(String modelFileName) {
        PrintStream out = util.IOUtils.getPrintStream(modelFileName);
        tokenLayer.save(out);
        posLayer.save(out);
        prevPosLayer.save(out);
        nextPosLayer.save(out);

        spanSizeLayer.save(out);
        argLayer.save(out);

        allArgsLayer.save(out);
        numArgsLayer.save(out);
        frameLayer.save(out);

        localScoresLayer.save(out);
        globalScoreLayer.save(out);
    }

    public void update() {
        tokenLayer.updateWeights(0.0, 0.0);
        posLayer.updateWeights(0.0, 0.0);
        prevPosLayer.updateWeights(0.0, 0.0);
        nextPosLayer.updateWeights(0.0, 0.0);

        spanSizeLayer.updateWeights(0.0, 0.0);
        argLayer.updateWeights(0.0, 0.0);

        allArgsLayer.updateWeights(0.0, 0.0);
        numArgsLayer.updateWeights(0.0, 0.0);
        frameLayer.updateWeights(0.0, 0.0);

        localScoresLayer.updateWeights(0.0, 0.0);
        globalScoreLayer.updateWeights(0.0, 0.0);
    }

}
