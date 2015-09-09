package edu.cmu.cs.lti.semreranking.utils;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.cmu.cs.lti.nlp.swabha.fileutils.BasicFileReader;
import edu.cmu.cs.lti.semreranking.DataPaths;

public class SynScoreReader {

    private static Map<Integer, List<Double>> readSynScoreFile(String turboScoreFileName,
            Map<Integer, List<Double>> exScoresMap) {
        List<String> synScoreLines = BasicFileReader.readFile(turboScoreFileName);

        for (int lineNum = 1; lineNum < synScoreLines.size(); lineNum++) {
            String[] ele = synScoreLines.get(lineNum).trim().split("\t");
            int exNum = Integer.parseInt(ele[0]);

            List<Double> scores = null;
            if (exScoresMap.containsKey(exNum)) {
                scores = exScoresMap.get(exNum);
            } else {
                scores = Lists.newArrayList();
            }
            scores.add(Double.parseDouble(ele[1]));
            exScoresMap.put(exNum, scores);
        }
        return exScoresMap;
    }

    public static Map<Integer, List<Double>> readAllSynScores(String synScoreDir) {
        Map<Integer, List<Double>> synScoreMap = Maps.newTreeMap();
        int numRanks = new File(synScoreDir).listFiles().length;
        for (int rank = 0; rank < numRanks; rank++) {
            String synScoreFileName = synScoreDir + rank + DataPaths.SYNSCORE_FILE_EXTN;
            synScoreMap = readSynScoreFile(synScoreFileName, synScoreMap);
        }
        return synScoreMap;
    }

}
