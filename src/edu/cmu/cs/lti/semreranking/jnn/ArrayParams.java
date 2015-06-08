package edu.cmu.cs.lti.semreranking.jnn;

/**
 * TODO: how to fix these? Just set to 50 (Chris Dyer)
 * 
 * @author sswayamd
 *
 */
public class ArrayParams {

    public final int tokenInpDim;
    public final int frameIdInpDim;
    public final int frameArgInpDim;

    public final int semScoreDim = 2; // Ideally, should be 1 but the library won't allow me a param
                                      // of that size
    public final int synScoreDim = 2;

    public final int argResultDim;
    public final int frameResultDim;
    public final int resultDim;

    public ArrayParams(int dim) {
        super();
        this.tokenInpDim = dim;
        this.frameIdInpDim = dim;
        this.frameArgInpDim = dim;
        this.argResultDim = dim;
        this.frameResultDim = dim;// + 1;
        this.resultDim = dim;// + 1;
    }

}
