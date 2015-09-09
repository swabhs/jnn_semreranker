package edu.cmu.cs.lti.semreranking.jnn;

import jnn.mapping.OutputMappingStringArrayToDenseArray;
import jnn.mapping.OutputMappingStringToDense;
import jnn.neuron.DenseNeuronArray;
import jnn.training.GraphInference;
import edu.cmu.cs.lti.semreranking.datastructs.Argument;
import edu.cmu.cs.lti.semreranking.datastructs.FrameSemParse;
import edu.cmu.cs.lti.semreranking.utils.FormatUtils;

public class NewFspInputNeuronArrays {

    public DenseNeuronArray frameIdArray;
    public DenseNeuronArray[] argIdsArray;

    public NewFspInputNeuronArrays(FspLookupTables lookupTables, ArrayParams ap,
            GraphInference inference, FrameSemParse frame) {

        int numArgs = frame.numArgs;

        frameIdArray = new DenseNeuronArray(ap.frameIdInpDim);
        argIdsArray = DenseNeuronArray.asArray(numArgs, ap.frameArgInpDim);

        String[] frameArgIds = new String[numArgs];

        int j = 0;

        String frameIds = frame.id;

        frameIdArray.setName(frame.id);

        for (Argument arg : frame.arguments) {
            String frameArgId = FormatUtils.makeFrameArgId(frame.id, arg.id);
            argIdsArray[j].setName(frameArgId);
            frameArgIds[j] = frameArgId;
            j++;
        }

        /* adding all inputs to inference */
        inference.addNeurons(frameIdArray);
        inference.addMapping(new OutputMappingStringToDense(frameIds,
                frameIdArray, lookupTables.frameTable));

        /* adding all inputs to inference */
        inference.addNeurons(argIdsArray);
        inference.addMapping(new OutputMappingStringArrayToDenseArray(frameArgIds,
                argIdsArray, lookupTables.frameArgTable));

    }

}
