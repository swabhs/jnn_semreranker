package edu.cmu.cs.lti.semreranking.jnn;

import jnn.mapping.OutputMappingStringArrayToDenseArray;
import jnn.neuron.DenseNeuronArray;
import jnn.training.GraphInference;
import edu.cmu.cs.lti.semreranking.datastructs.Argument;
import edu.cmu.cs.lti.semreranking.datastructs.FrameSemAnalysis;
import edu.cmu.cs.lti.semreranking.datastructs.FrameSemParse;
import edu.cmu.cs.lti.semreranking.datastructs.Scored;
import edu.cmu.cs.lti.semreranking.utils.FormatUtils;

public class FspInputNeuronArrays {

    // public DenseNeuronArray[] posArray;
    public DenseNeuronArray[] frameIdsArray;
    public DenseNeuronArray[] frameArgIdsArray;

    public FspInputNeuronArrays(FspLookupTables lookupTables, ArrayParams ap,
            GraphInference inference, FrameSemAnalysis fsa) {

        int numFrames = fsa.numFsps;
        int numArgs = fsa.numFrameArgs;

        // posArray = DenseNeuronArray.asArray(numFrames, ap.tokenInpDim);
        frameIdsArray = DenseNeuronArray.asArray(numFrames, ap.frameIdInpDim);
        frameArgIdsArray = DenseNeuronArray.asArray(numArgs, ap.frameArgInpDim);

        String[] predPostags = new String[numFrames];
        String[] frameIds = new String[numFrames];
        String[] frameArgIds = new String[numArgs];

        int i = 0;
        int j = 0;

        for (Scored<FrameSemParse> scoredFrame : fsa.frameSemParses) {
            FrameSemParse frame = scoredFrame.entity;
            frameIdsArray[i].setName(frame.id);
            frameIds[i] = frame.id;
            // posArray[i].setName("pos" + frame.id);
            predPostags[i] = frame.tarPosTag;

            for (Argument arg : frame.arguments) {
                String frameArgId = FormatUtils.makeFrameArgId(frame.id, arg.id);
                frameArgIdsArray[j].setName(frameArgId);
                frameArgIds[j] = frameArgId;
                j++;
            }
            i++;
        }

        /* adding all pos tags to inference */
        // inference.addNeurons(posArray);
        // inference.addMapping(new OutputMappingStringArrayToDenseArray(predPostags, posArray,
        // lookupTables.goldFNPosTable));

        /* adding all inputs to inference */
        inference.addNeurons(frameIdsArray);
        inference.addMapping(new OutputMappingStringArrayToDenseArray(frameIds,
                frameIdsArray, lookupTables.frameTable));

        /* adding all inputs to inference */
        inference.addNeurons(frameArgIdsArray);
        inference.addMapping(new OutputMappingStringArrayToDenseArray(frameArgIds,
                frameArgIdsArray, lookupTables.frameArgTable));

    }

}
