package edu.cmu.cs.lti.semreranking.jnn;

import jnn.mapping.OutputMappingStringArrayToDenseArray;
import jnn.neuron.DenseNeuronArray;
import jnn.training.GraphInference;
import edu.cmu.cs.lti.semreranking.datastructs.Argument;
import edu.cmu.cs.lti.semreranking.datastructs.Frame;
import edu.cmu.cs.lti.semreranking.datastructs.FrameSemanticParse;
import edu.cmu.cs.lti.semreranking.datastructs.Scored;
import edu.cmu.cs.lti.semreranking.utils.StringUtils;

public class FspInputNeuronArrays {

    public DenseNeuronArray[] posArray;
    public DenseNeuronArray[] frameIdsArray;
    public DenseNeuronArray[] frameArgIdsArray;

    public FspInputNeuronArrays(FspLookupTables lookupTables, ArrayParams ap,
            GraphInference inference, Scored<FrameSemanticParse> scoredFsp) {

        int numFrames = scoredFsp.entity.numFrames;
        int numArgs = scoredFsp.entity.numFrameArgs;

        posArray = DenseNeuronArray.asArray(numFrames, ap.tokenInpDim);
        frameIdsArray = DenseNeuronArray.asArray(numFrames, ap.frameIdInpDim);
        frameArgIdsArray = DenseNeuronArray.asArray(numArgs, ap.frameArgInpDim);

        String[] predPostags = new String[numFrames];
        String[] frameIds = new String[numFrames];
        String[] frameArgIds = new String[numArgs];

        int i = 0;
        int j = 0;

        for (Frame frame : scoredFsp.entity.frames) {
            frameIdsArray[i].setName(frame.id);
            frameIds[i] = frame.id;
            posArray[i].setName("pos" + frame.id);
            predPostags[i] = frame.predPosTag;

            for (Argument arg : frame.arguments) {
                String frameArgId = StringUtils.makeFrameArgId(frame.id, arg.id);
                frameArgIdsArray[j].setName(frameArgId);
                frameArgIds[j] = frameArgId;
                j++;
            }
            i++;
        }

        /* adding all pos tags to inference */
        inference.addNeurons(posArray);
        inference.addMapping(new OutputMappingStringArrayToDenseArray(predPostags, posArray,
                lookupTables.goldFNPosTable));

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
