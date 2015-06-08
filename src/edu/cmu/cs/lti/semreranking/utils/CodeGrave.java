package edu.cmu.cs.lti.semreranking.utils;

import java.io.File;
import java.util.Map;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import edu.cmu.cs.lti.semreranking.DataPaths;
import edu.cmu.cs.lti.semreranking.datastructs.FrameSemanticParse;
import edu.cmu.cs.lti.semreranking.datastructs.FspScore;
import edu.cmu.cs.lti.semreranking.datastructs.Scored;

public class CodeGrave {

    public static Table<Integer, Integer, Scored<FrameSemanticParse>> readScoredFsps(
            String xmlDir, String feDir, String synDir, FeReader reader) {

        System.err.println("Reading data from...");
        System.err.println(feDir);
        System.err.println(xmlDir);

        Table<Integer, Integer, Scored<FrameSemanticParse>> allFsps = HashBasedTable.create();
        int numRanks = new File(feDir).listFiles().length;

        for (int rank = 0; rank < numRanks; rank++) {
            String xmlFileName = xmlDir + rank + DataPaths.XML_FILE_EXTN;
            String feFileName = feDir + rank + DataPaths.FE_FILE_EXTN;
            String synFileName = synDir + rank + DataPaths.TURBO_FILE_EXTN;

            Map<Integer, FrameSemanticParse> fsps = reader.readFeFile(feFileName);
            Map<Integer, FspScore> framescores = FileUtils.readFscoreFile(xmlFileName);
            Map<Integer, Double> synScores = FileUtils.readSynScoreFile(synFileName);

            for (int exNum : fsps.keySet()) {
                Scored<FrameSemanticParse> scoFsp = null;
                if (framescores.containsKey(exNum) == false) {
                    scoFsp = new Scored<FrameSemanticParse>(
                            fsps.get(exNum), 0.0, 0.0, 0.0, 0.0, synScores.get(exNum));
                } else {
                    scoFsp = new Scored<FrameSemanticParse>(
                            fsps.get(exNum), framescores.get(exNum), synScores.get(exNum));
                }
                allFsps.put(exNum, rank, scoFsp);

            }
        }

        System.err.println("Number of examples read = " + allFsps.rowKeySet().size());
        System.err
                .println("Number of ranked parses per example = " + allFsps.columnKeySet().size());
        return allFsps;
    }

    // public static void writeSortedInstancesToFile(
    // Map<Integer, TreeMultiset<Scored<FrameSemanticParse>>> sortedTrainInstances) {
    //
    // DataPaths dataPaths = new DataPaths(true, true, SemRerankerMain.model);
    //
    // int i = 0;
    // try {
    // PrintWriter outFe;
    // PrintWriter outXml;
    // for (int ex : sortedTrainInstances.keySet()) {
    // int rank = 0;
    // for (Scored<FrameSemanticParse> instance : sortedTrainInstances.get(ex)) {
    // String outFeFileName = dataPaths.feDir + rank + DataPaths.FE_FILE_EXTN;
    // String outXmlFileName = dataPaths.xmlDir + rank + DataPaths.XML_FILE_EXTN;
    //
    // if (i == 0) {
    // outFe = new PrintWriter(
    // new BufferedWriter(new FileWriter(outFeFileName, false)));
    //
    // outXml = new PrintWriter(
    // new BufferedWriter(new FileWriter(outXmlFileName, false)));
    // outXml.println("Sentence ID\tSORTED Recall\tSORTED Precision\tSORTED Fscore");
    // } else {
    // outFe = new PrintWriter(
    // new BufferedWriter(new FileWriter(outFeFileName, true)));
    //
    // outXml = new PrintWriter(
    // new BufferedWriter(new FileWriter(outXmlFileName, true)));
    // }
    //
    // outFe.println(instance.entity.toString(ex));
    //
    // outXml.println(ex + "\t"
    // + SemRerankerMain.formatter.format(instance.rNum / instance.rDenom)
    // + "("
    // + (instance.rNum) + "/"
    // + (instance.rDenom) + ")\t"
    // + SemRerankerMain.formatter.format(instance.pNum / instance.pDenom)
    // + "("
    // + (instance.pNum) + "/"
    // + (instance.pDenom) + ")\t"
    // + instance.fscore);
    // outFe.close();
    // outXml.close();
    // rank++;
    // }
    // i++;
    // }
    // } catch (IOException e) {
    // e.printStackTrace();
    // }
    //
    // }
    //
    // public void sortTrainingInsts() {
    // DataPaths dataPaths = new DataPaths(false, false, SemRerankerMain.model);
    // Map<Integer, TreeMultiset<Scored<FrameSemanticParse>>> sortedTrainInstances = FileUtils
    // .readAndSortTrain(dataPaths.xmlDir, dataPaths.feDir, new FeReader());
    // CodeGrave.writeSortedInstancesToFile(sortedTrainInstances);
    // }
}
