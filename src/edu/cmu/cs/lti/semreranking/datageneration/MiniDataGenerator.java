package edu.cmu.cs.lti.semreranking.datageneration;

import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Map;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.internal.Maps;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import edu.cmu.cs.lti.nlp.swabha.fileutils.BasicFileWriter;
import edu.cmu.cs.lti.semreranking.DataPaths;
import edu.cmu.cs.lti.semreranking.datastructs.FrameSemAnalysis;
import edu.cmu.cs.lti.semreranking.datastructs.FsaScore;
import edu.cmu.cs.lti.semreranking.datastructs.Scored;
import edu.cmu.cs.lti.semreranking.utils.FeReader;
import edu.cmu.cs.lti.semreranking.utils.FileUtils;

/**
 * Given the semafor k-best results directory, creates a list of fsps sorted by f-score and writes
 * them to a file.
 * 
 * @author sswayamd
 */
public class MiniDataGenerator {

    @Parameter(names = "-semhome", description = "SEMAFOR home")
    public static String semhome = "/Users/sswayamd/Documents/workspace/jnn/SemanticReranker/data/";

    @Parameter(names = "-trainsz", description = "trainSize")
    public static int trainSize = 100;
    @Parameter(names = "-devsz", description = "devSize")
    public static int devSize = 25;
    @Parameter(names = "-testsz", description = "testSize")
    public static int testSize = 30;
    @Parameter(names = "-ranks", description = "number of ranks")
    public static int numRanks = 50;

    public static NumberFormat formatter = new DecimalFormat("#0.00000");

    static void makeMiniDataSet(
            Map<Integer, Multimap<Integer, Scored<FrameSemAnalysis>>> allFsps,
            String feDir,
            String semaforResultsDir,
            int numExamples) {

        System.err.println("\nMaking mini dataset with " + numExamples + " examples with "
                + numRanks + " ranks here...");

        createDirectory(feDir);
        createDirectory(semaforResultsDir);

        for (int col = 0; col < numRanks; col++) {
            List<String> feLines = Lists.newArrayList();
            List<String> fscoreLines = Lists.newArrayList();
            fscoreLines.add("Sentence ID\tFscore\tFscore\tFscore");

            int exNum = 0;
            for (int row : allFsps.keySet()) {
                if (allFsps.get(row).containsKey(col)) {
                    for (Scored<FrameSemAnalysis> scoFsp : allFsps.get(row).get(col)) {
                        feLines.add(scoFsp.entity.toString(row));
                        // TODO: BUGS AHOY, division by a possible 0.0
                        fscoreLines.add(row
                                + "\t"
                                + formatter.format(scoFsp.detailedFspScore.rnum
                                        / scoFsp.detailedFspScore.rdenom)
                                + "("
                                + (scoFsp.detailedFspScore.rnum)
                                + "/"
                                + (scoFsp.detailedFspScore.rdenom)
                                + ")\t"
                                + formatter.format(scoFsp.detailedFspScore.pnum
                                        / scoFsp.detailedFspScore.pdenom) + "("
                                + (scoFsp.detailedFspScore.pnum) + "/"
                                + (scoFsp.detailedFspScore.pdenom) + ")\t"
                                + scoFsp.fscore);
                    }
                    exNum++;
                }
                if (exNum == numExamples) {
                    break;
                }
            }
            BasicFileWriter.writeStrings(feLines, feDir + col + DataPaths.FE_FILE_EXTN);
            BasicFileWriter.writeStrings(fscoreLines, semaforResultsDir + col
                    + DataPaths.RESULTS_FILE_EXTN);
        }
        System.err.println();
    }

    public static void main(String[] args) {
        new JCommander(new MiniDataGenerator(), args);

        Map<String, Integer> dataSetSizes = Maps.newHashMap();
        dataSetSizes.put("train", trainSize);
        dataSetSizes.put("dev", devSize);
        dataSetSizes.put("test", testSize);

        for (String dataset : dataSetSizes.keySet()) {

            DataPaths paths = new DataPaths(false, dataset);
            Map<Integer, Multimap<Integer, Scored<FrameSemAnalysis>>> scoredFsps = readScoredFsps(
                    paths.semaforResultsDir,
                    paths.semaforOutFEDir,
                    paths.synScoresDir);

            DataPaths outpaths = new DataPaths(true, dataset);
            makeMiniDataSet(
                    scoredFsps,
                    outpaths.semaforOutFEDir,
                    outpaths.semaforResultsDir,
                    dataSetSizes.get(dataset));
        }

    }

    public static void createDirectory(String fileName) {
        System.err.println("Creating directory : " + fileName);
        File feDirFile = new File(fileName);
        if (feDirFile.exists()) {
            feDirFile.delete();
        }
        feDirFile.mkdir();
    }

    public static Map<Integer, Multimap<Integer, Scored<FrameSemAnalysis>>> readScoredFsps(
            String xmlDir, String feDir, String synDir) {

        System.err.println("Reading data from...");
        System.err.println(feDir);
        System.err.println(xmlDir);

        Map<Integer, Multimap<Integer, Scored<FrameSemAnalysis>>> exRanksMap = Maps.newHashMap();
        int numRanks = new File(feDir).listFiles().length;

        for (int rank = 0; rank < numRanks; rank++) {
            String xmlFileName = xmlDir + rank + DataPaths.RESULTS_FILE_EXTN;
            String feFileName = feDir + rank + DataPaths.FE_FILE_EXTN;
            String synFileName = synDir + rank + DataPaths.SYNSCORE_FILE_EXTN;

            Multimap<Integer, FrameSemAnalysis> fsps = new FeReader().readFeFile(feFileName);
            Map<Integer, FsaScore> framescores = FileUtils.readFscoreFile(xmlFileName);
            Map<Integer, Double> synScores = FileUtils.readSynScoreFile(synFileName);

            for (int exNum : fsps.keySet()) {
                Multimap<Integer, Scored<FrameSemAnalysis>> allRanks = null;
                if (exRanksMap.containsKey(exNum) == false) {
                    allRanks = HashMultimap.create();
                    exRanksMap.put(exNum, allRanks);
                }
                allRanks = exRanksMap.get(exNum);
                for (FrameSemAnalysis fsp : fsps.get(exNum)) {
                    Scored<FrameSemAnalysis> scoFsp = null;
                    if (framescores.containsKey(exNum) == false) {
                        scoFsp = new Scored<FrameSemAnalysis>(
                                fsp, new FsaScore(), synScores.get(exNum), rank);
                    } else {
                        scoFsp = new Scored<FrameSemAnalysis>(
                                fsp, framescores.get(exNum), synScores.get(exNum), rank);
                    }
                    allRanks.put(rank, scoFsp);
                }
                exRanksMap.put(exNum, allRanks);
            }
        }

        System.err.println("Number of examples read = " + exRanksMap.keySet().size());
        return exRanksMap;
    }
}
