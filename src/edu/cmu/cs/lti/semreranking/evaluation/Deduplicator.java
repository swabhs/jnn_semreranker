package edu.cmu.cs.lti.semreranking.evaluation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import edu.cmu.cs.lti.nlp.swabha.fileutils.BasicFileWriter;
import edu.cmu.cs.lti.semreranking.DataPaths;
import edu.cmu.cs.lti.semreranking.FrameSemanticParse;
import edu.cmu.cs.lti.semreranking.SemRerankerMain;
import edu.cmu.cs.lti.semreranking.utils.FeReader;

public class Deduplicator {

    public static void main(String[] args) {
        DataPaths dp = new DataPaths(false, SemRerankerMain.model);
        String feFile = dp.feDir + "0" + dp.FE_FILE_EXTN;
        System.err.println(feFile);
        FeReader reader = new FeReader();
        Map<Integer, FrameSemanticParse> instances = reader.readFeFile(feFile);
        TreeMap<Integer, String> orderFes = new TreeMap<Integer, String>();
        for (int numEx : instances.keySet()) {
            FrameSemanticParse inst = instances.get(numEx);

            orderFes.put(numEx, inst.toString(numEx));
        }

        List<String> lines = new ArrayList<String>(instances.size());
        for (int numEx : orderFes.keySet()) {
            lines.add(orderFes.get(numEx));
        }
        System.err.println(orderFes.size());

        BasicFileWriter.writeStrings(lines, "concise.frame.elements");
    }

}
