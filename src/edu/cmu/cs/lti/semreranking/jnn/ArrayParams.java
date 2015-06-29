package edu.cmu.cs.lti.semreranking.jnn;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import edu.cmu.cs.lti.nlp.swabha.fileutils.BasicFileReader;

/**
 * TODO: how to fix these? Just set to 50 (Chris Dyer)
 * 
 * @author sswayamd
 *
 */
public class ArrayParams {

    public final int tokenInpDim;
    public final int posInpDim;// = 25;
    public final int frameIdInpDim;// = 150;
    public final int frameArgInpDim;// = 200;

    public final int semScoreDim = 2; // Ideally, should be 1 but the library won't allow me
    public final int synScoreDim = 2;

    public static final Map<Integer, Integer> spanMap = Maps.newTreeMap();
    {
        spanMap.put(0, 0);
        spanMap.put(1, 1);
        spanMap.put(2, 2);
        spanMap.put(3, 3);
        spanMap.put(4, 4);
        spanMap.put(5, 5);
        spanMap.put(6, 8);
        spanMap.put(7, 10);
        spanMap.put(8, 20);
    }
    public final int spanSizeDim;

    public static final Map<Integer, Integer> numArgsMap = Maps.newTreeMap();
    {
        numArgsMap.put(0, 0);
        numArgsMap.put(1, 1);
        numArgsMap.put(2, 2);
        numArgsMap.put(3, 3);
        numArgsMap.put(4, 4);
        numArgsMap.put(5, 10);
    }
    public final int numArgsDim;

    public final int argResultDim;// = 75;
    public final int frameResultDim;// = 100;
    public final int resultDim;// = 100;

    public ArrayParams(int tokenInpDim, String paramDimFile) {
        this.tokenInpDim = tokenInpDim;

        Map<String, Integer> paramDims = readParamDimFile(paramDimFile);
        this.posInpDim = paramDims.get("pos");
        this.frameIdInpDim = paramDims.get("frame");
        this.frameArgInpDim = paramDims.get("framearg");
        this.argResultDim = paramDims.get("argresult");
        this.frameResultDim = argResultDim; // these two are the same bcz tokens, pos depend on both
        this.resultDim = paramDims.get("result");

        this.spanSizeDim = spanMap.size();
        this.numArgsDim = numArgsMap.size();
    }

    public Map<String, Integer> readParamDimFile(String paramDimFile) {
        List<String> lines = BasicFileReader.readFile(paramDimFile);
        Map<String, Integer> paramDims = Maps.newHashMap();
        for (String line : lines) {
            String ele[] = line.split("\t");
            paramDims.put(ele[0], Integer.parseInt(ele[1]));
        }
        return paramDims;
    }

}
