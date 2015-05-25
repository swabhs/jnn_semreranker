package edu.cmu.cs.lti.semreranking;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.beust.jcommander.Parameter;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import com.google.common.collect.TreeMultiset;

import edu.cmu.cs.lti.nlp.swabha.fileutils.BasicFileWriter;
import edu.cmu.cs.lti.semreranking.utils.FeReader;
import edu.cmu.cs.lti.semreranking.utils.FileUtils;

/**
 * Given the semafor k-best results directory, creates a list of fsps sorted by f-score and writes
 * them to a file.
 * 
 * @author sswayamd
 */
public class TrainingDataGenerator {

    @Parameter(names = "-mini", arity = 1, description = "use a mini corpus to test")
    public static boolean useMini = true;

    public static NumberFormat formatter = new DecimalFormat("#0.00000");

    public TrainingDataGenerator() {
        DataPaths dataPaths = new DataPaths(useMini, false, SemRerankerMain.model);
        // Table<Integer, Integer, Scored<FrameSemanticParse>> unsorted = FileUtils
        // .readScoredFsps(dataPaths.xmlDir, dataPaths.feDir, new FeReader());
        // System.err.println("1-best = " + Evaluator.getCorpusFscore(unsorted));
        // makeMiniDataSet(unsorted, DataPaths.TOKEN_FILE_TRAIN);
        // Table<Integer, Integer, Scored<FrameSemanticParse>> sorted =
        // sortTrainInstancesByFscores(unsorted);
        // writeSortedInstancesToFile(sorted);

        Map<Integer, TreeMultiset<Scored<FrameSemanticParse>>> sortedTrainInstances = FileUtils
                .readAndSortTrain(dataPaths.xmlDir, dataPaths.feDir, new FeReader());
        TrainingDataGenerator.writeSortedInstancesToFile(sortedTrainInstances);
    }

    // public void readOutputFrameElements(String ofeFileName) {
    // Map<Integer, Double> feScores = Maps.newHashMap();
    // List<String> lines = BasicFileReader.readFile(ofeFileName);
    // for (String line : lines) {
    // String[] toks = line.split("\t");
    // int exNum = Integer.parseInt(toks[7]);
    // if (feScores.containsKey(exNum)) {
    // feScores.put(exNum, feScores.get(exNum) + Double.parseDouble(toks[2]));
    // } else {
    // feScores.put(exNum, Double.parseDouble(toks[2]));
    // }
    // }
    // }

    /**
     * Given 100 frame.elements files and 100 partial results files, sorts them by ranks provided in
     * the results file.
     */
    private Table<Integer, Integer, Scored<FrameSemanticParse>> sortTrainInstancesByFscores(
            Table<Integer, Integer, Scored<FrameSemanticParse>> allTrainInstances) {
        System.err.println("Sorting training examples...");

        Table<Integer, Integer, Scored<FrameSemanticParse>> sorted = HashBasedTable.create();
        int i = 1;
        for (Integer row : allTrainInstances.rowKeySet()) {
            List<Scored<FrameSemanticParse>> instances = Lists.newArrayList();
            instances.addAll(allTrainInstances.row(row).values());
            Collections.sort(instances);
            for (int col = 0; col < allTrainInstances.columnKeySet().size(); col++) {
                sorted.put(row, col, instances.get(col));
            }
            if (i % 10 == 0) {
                System.err.print(i + "...");
            }
            i++;
        }
        System.err.println();
        return sorted;
    }

    public static void writeSortedInstancesToFile(
            Map<Integer, TreeMultiset<Scored<FrameSemanticParse>>> sortedTrainInstances) {

        SentsAndToks sentsAndToks = FileUtils.readTokFile(DataPaths.TOKEN_FILE_TRAIN);
        DataPaths dataPaths = new DataPaths(useMini, true, SemRerankerMain.model);

        int i = 0;
        try {
            PrintWriter outFe;
            PrintWriter outXml;
            for (int ex : sortedTrainInstances.keySet()) {
                int rank = 0;
                for (Scored<FrameSemanticParse> instance : sortedTrainInstances.get(ex)) {
                    String outFeFileName = dataPaths.feDir + rank + DataPaths.FE_FILE_EXTN;
                    String outXmlFileName = dataPaths.xmlDir + rank + DataPaths.XML_FILE_EXTN;

                    if (i == 0) {
                        outFe = new PrintWriter(
                                new BufferedWriter(new FileWriter(outFeFileName, false)));

                        outXml = new PrintWriter(
                                new BufferedWriter(new FileWriter(outXmlFileName, false)));
                        outXml.println("Sentence ID\tSORTED Recall\tSORTED Precision\tSORTED Fscore");
                    } else {
                        outFe = new PrintWriter(
                                new BufferedWriter(new FileWriter(outFeFileName, true)));

                        outXml = new PrintWriter(
                                new BufferedWriter(new FileWriter(outXmlFileName, true)));
                    }

                    outFe.println(instance.entity.toString(sentsAndToks.allToks.get(ex), ex));

                    outXml.println(ex + "\t"
                            + formatter.format(instance.rNum / instance.rDenom) + "("
                            + (instance.rNum) + "/"
                            + (instance.rDenom) + ")\t"
                            + formatter.format(instance.pNum / instance.pDenom) + "("
                            + (instance.pNum) + "/"
                            + (instance.pDenom) + ")\t"
                            + instance.fscore);
                    outFe.close();
                    outXml.close();
                    rank++;
                }
                i++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    void writeSortedInstancesToFile(
            Table<Integer, Integer, Scored<FrameSemanticParse>> sortedTrainInstances) {

        SentsAndToks sentsAndToks = FileUtils.readTokFile(DataPaths.TOKEN_FILE_TRAIN);
        DataPaths dataPaths = new DataPaths(useMini, true, SemRerankerMain.model);

        for (Integer col : sortedTrainInstances.columnKeySet()) {
            List<String> feLines = Lists.newArrayList();
            List<String> fscoreLines = Lists.newArrayList();
            fscoreLines.add("Sentence ID\tSORTED Fscore\tSORTED Fscore\tSORTED Fscore");
            for (Integer row : sortedTrainInstances.rowKeySet()) {
                if (sentsAndToks.allToks.size() > row) {
                    Scored<FrameSemanticParse> instance = sortedTrainInstances.get(row, col);
                    feLines.add(instance.entity.toString(sentsAndToks.allToks.get(row), row));
                    fscoreLines.add(row + "\t" + instance.fscore + "\t" + instance.fscore + "\t"
                            + instance.fscore);
                }
            }
            BasicFileWriter.writeStrings(feLines, dataPaths.feDir + col.toString()
                    + DataPaths.FE_FILE_EXTN);
            BasicFileWriter.writeStrings(fscoreLines, dataPaths.xmlDir + col.toString()
                    + DataPaths.XML_FILE_EXTN);
        }
    }

    static void makeMiniDataSet(
            Table<Integer, Integer, Scored<FrameSemanticParse>> allTrain,
            String feDir,
            String xmlDir,
            String tokFileName) {
        SentsAndToks sentsAndToks = FileUtils.readTokFile(tokFileName);
        // DataPaths dataPaths = new DataPaths(true, false, SemRerankerMain.model);
        // String feDir = dataPaths.feDir;
        // String xmlDir = dataPaths.xmlDir;

        int numRanks = 20;
        int numExamples = 15;

        System.err.println("Making mini dataset. Writing data to...");

        System.err.println(feDir);
        System.err.println(xmlDir);

        for (int col = 0; col < numRanks; col++) {
            List<String> feLines = Lists.newArrayList();
            List<String> fscoreLines = Lists.newArrayList();
            fscoreLines.add("Sentence ID\tFscore\tFscore\tFscore");

            int exNum = 0;
            for (Integer row : allTrain.rowKeySet()) {
                if (allTrain.contains(row, col) && sentsAndToks.allToks.size() > row) {
                    Scored<FrameSemanticParse> instance = allTrain.get(row, col);
                    feLines.add(instance.entity.toString(sentsAndToks.allToks.get(row), row));
                    fscoreLines.add(row + "\t"
                            + formatter.format(instance.rNum / instance.rDenom) + "("
                            + (instance.rNum) + "/"
                            + (instance.rDenom) + ")\t"
                            + formatter.format(instance.pNum / instance.pDenom) + "("
                            + (instance.pNum) + "/"
                            + (instance.pDenom) + ")\t"
                            + instance.fscore);
                    exNum++;
                }
                if (exNum == numExamples) {
                    break;
                }
            }
            BasicFileWriter.writeStrings(feLines, feDir + col + DataPaths.FE_FILE_EXTN);
            BasicFileWriter.writeStrings(fscoreLines, xmlDir + col + DataPaths.XML_FILE_EXTN);
        }
    }

    public static void main(String[] args) {
        // new TrainingDataGenerator();
        String semhome = "/Users/sswayamd/Documents/workspace/jnn/SemanticReranker/data/experiments/basic_tbps/";
        String inp = "semreranker_dev";
        String outp = "semreranker_dev_mini";
        Table<Integer, Integer, Scored<FrameSemanticParse>> unsorted = FileUtils
                .readScoredFsps(semhome + "/results/" + inp + "/partial/",
                        semhome + "/output/" + inp + "/frameElements/",
                        new FeReader());
        makeMiniDataSet(unsorted, semhome + "/output/" + outp + "/frameElements/", semhome
                + "/results/" + outp + "/partial/", DataPaths.TOKEN_FILE_DEV);
    }
}
