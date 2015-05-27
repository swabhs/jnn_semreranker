package edu.cmu.cs.lti.semreranking.utils;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import edu.cmu.cs.lti.nlp.swabha.fileutils.BasicFileReader;
import edu.cmu.cs.lti.semreranking.DataPaths;
import edu.cmu.cs.lti.semreranking.FrameSemanticParse;
import edu.cmu.cs.lti.semreranking.Scored;

public class CodeGrave {

    public static Table<Integer, Integer, Scored<FrameSemanticParse>> readScoredFsps(
            String xmlDir, String feDir, FeReader reader) {

        System.err.println("Reading data from...");
        System.err.println(feDir);
        System.err.println(xmlDir);

        Table<Integer, Integer, Scored<FrameSemanticParse>> allFsps = HashBasedTable.create();
        int numRanks = new File(feDir).listFiles().length;

        for (int rank = 0; rank < numRanks; rank++) {
            String xmlFileName = xmlDir + rank + DataPaths.XML_FILE_EXTN;
            String feFileName = feDir + rank + DataPaths.FE_FILE_EXTN;

            Map<Integer, FrameSemanticParse> fsps = reader.readFeFile(feFileName);
            List<String> fscoreLines = BasicFileReader.readFile(xmlFileName);

            for (int lineNum = 1; lineNum < fscoreLines.size(); lineNum++) {
                String[] ele = fscoreLines.get(lineNum).trim().split("\t");
                int exNum = Integer.parseInt(ele[0]);
                if (fsps.containsKey(exNum)) { // read only relevant f-scores
                    String rfrac = ele[1].split("\\(")[1];
                    String rNum = rfrac.split("/")[0];
                    String rDenom = rfrac.split("/")[1].split("\\)")[0];
                    String pfrac = ele[2].split("\\(")[1];
                    String pNum = pfrac.split("/")[0];
                    String pDenom = pfrac.split("/")[1].split("\\)")[0];
                    Scored<FrameSemanticParse> instance = new Scored<FrameSemanticParse>(
                            fsps.get(exNum),
                            Double.parseDouble(ele[3]),
                            Double.parseDouble(rNum),
                            Double.parseDouble(rDenom),
                            Double.parseDouble(pNum),
                            Double.parseDouble(pDenom));
                    allFsps.put(exNum, rank, instance);
                }
                else {
                    allFsps.put(exNum, rank, new Scored<FrameSemanticParse>(
                            fsps.get(exNum), 0.0, 0.0, 0.0, 0.0, 0.0));
                }
            }
        }

        System.err.println("Number of examples read = " + allFsps.rowKeySet().size());
        System.err
                .println("Number of ranked parses per example = " + allFsps.columnKeySet().size());
        return allFsps;
    }
}
