package edu.cmu.cs.lti.semreranking.jnn;

import java.util.Map;

import com.google.common.collect.Maps;

/**
 * TODO: how to fix these? Just set to 50 (Chris Dyer)
 * 
 * @author sswayamd
 *
 */
public class ArrayParams {

    public final int tokenInpDim;
    public final int posInpDim;
    public final int frameIdInpDim;// = 150;
    public final int frameArgInpDim;// = 200;

    public final int semScoreDim = 2; // Ideally, should be 1 but the library won't allow me
    public final int synScoreDim = 2;

    public static final Map<Integer, Integer> spanMap = Maps.newTreeMap();
    public final int spanSizeDim;
    public static final Map<Integer, Integer> numArgsMap = Maps.newTreeMap();
    public final int numArgsDim;

    public final int argResultDim;
    public final int frameResultDim;
    public final int resultDim;

    public ArrayParams(int tokenInpDim) {
        this.tokenInpDim = tokenInpDim;
        this.posInpDim = tokenInpDim;

        this.frameIdInpDim = tokenInpDim;
        this.frameArgInpDim = tokenInpDim;

        this.argResultDim = tokenInpDim;
        this.frameResultDim = tokenInpDim;
        this.resultDim = tokenInpDim;

        spanMap.put(0, 0);
        spanMap.put(1, 1);
        spanMap.put(2, 2);
        spanMap.put(3, 3);
        spanMap.put(4, 4);
        spanMap.put(5, 5);
        spanMap.put(6, 8);
        spanMap.put(7, 10);
        spanMap.put(8, 20);
        this.spanSizeDim = spanMap.size();

        numArgsMap.put(0, 0);
        numArgsMap.put(1, 1);
        numArgsMap.put(2, 2);
        numArgsMap.put(3, 3);
        numArgsMap.put(4, 4);
        numArgsMap.put(5, 10);
        this.numArgsDim = numArgsMap.size();

    }

}
