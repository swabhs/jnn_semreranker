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

    DenseFullyConnectedLayer allFramesLayer; // U
    DenseFullyConnectedLayer synScoreLayer; // alpha
    DenseFullyConnectedLayer semScoreLayer; // beta

    DenseFullyConnectedLayer scoreLayer;

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

        allFramesLayer = new DenseFullyConnectedLayer(ap.frameResultDim, ap.resultDim);
        synScoreLayer = new DenseFullyConnectedLayer(ap.synScoreDim, ap.resultDim);
        semScoreLayer = new DenseFullyConnectedLayer(ap.semScoreDim, ap.frameResultDim);

        scoreLayer = new DenseFullyConnectedLayer(ap.resultDim, 1);
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

        allFramesLayer = DenseFullyConnectedLayer.load(in);
        synScoreLayer = DenseFullyConnectedLayer.load(in);
        semScoreLayer = DenseFullyConnectedLayer.load(in);

        scoreLayer = DenseFullyConnectedLayer.load(in);
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

        allFramesLayer.save(out);
        synScoreLayer.save(out);
        semScoreLayer.save(out);
        scoreLayer.save(out);
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

        allFramesLayer.updateWeights(0.0, 0.0);
        synScoreLayer.updateWeights(0.0, 0.0);
        semScoreLayer.updateWeights(0.0, 0.0);
        scoreLayer.updateWeights(0.0, 0.0);
    }

}
