package edu.cmu.cs.lti.semreranking.datageneration;

import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.internal.Maps;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import edu.cmu.cs.lti.nlp.swabha.basic.Conll;
import edu.cmu.cs.lti.nlp.swabha.basic.Pair;
import edu.cmu.cs.lti.nlp.swabha.fileutils.BasicFileReader;
import edu.cmu.cs.lti.nlp.swabha.fileutils.BasicFileWriter;
import edu.cmu.cs.lti.semreranking.DataPaths;
import edu.cmu.cs.lti.semreranking.datastructs.FrameSemParse;
import edu.cmu.cs.lti.semreranking.datastructs.FrameSemParse.FrameIdentifier;
import edu.cmu.cs.lti.semreranking.datastructs.SemevalScore;
import edu.cmu.cs.lti.semreranking.utils.FeReader;
import edu.cmu.cs.lti.semreranking.utils.ResultsFileUtils;
import edu.cmu.cs.lti.semreranking.utils.SynScoreReader;

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

    public static void main(String[] args) {
        new JCommander(new MiniDataGenerator(), args);

        Map<String, Integer> dataSetSizes = Maps.newHashMap();
        dataSetSizes.put("train", trainSize);
        dataSetSizes.put("dev", devSize);
        // dataSetSizes.put("test", testSize);

        for (String dataset : dataSetSizes.keySet()) {
            DataPaths inPaths = new DataPaths(false, dataset);
            DataPaths outpaths = new DataPaths(true, dataset);
            makeMiniDataSet(inPaths, outpaths, dataSetSizes.get(dataset));
        }
    }

    static void makeMiniDataSet(DataPaths inPaths, DataPaths outPaths, int numExamples) {
        System.err.println("Reading data from...\n" + inPaths.toString());
        System.err.println("\nMaking mini dataset with " + numExamples + " examples with "
                + numRanks + " ranks here...");

        List<Integer> selectedEx = makeMiniTokenizedFile(inPaths, outPaths, numExamples);
        System.out.println(selectedEx);
        makeMiniConllFile(inPaths, outPaths, selectedEx);
        makeMiniSynScoreFiles(inPaths, outPaths, selectedEx);

        makeMiniFEs(inPaths, outPaths, selectedEx);
        makeMiniResults(inPaths, outPaths, selectedEx);
    }

    static void makeMiniFEs(DataPaths inPaths, DataPaths outPaths, List<Integer> selectEx) {
        // read all the FEs
        Map<Integer, Map<FrameIdentifier, List<FrameSemParse>>> kbestFspsMap = new FeReader()
                .readAllFeFiles(inPaths.semaforOutFEDir);
        createDirectory(outPaths.semaforOutFEDir);

        Map<Integer, Multimap<Integer, FrameSemParse>> rankedParseLists = Maps.newHashMap();

        int writtenEx = 0;
        for (int ex : selectEx) {
            if (kbestFspsMap.containsKey(ex) == false) {
                ++writtenEx;
                continue;
            }
            for (FrameIdentifier id : kbestFspsMap.get(ex).keySet()) {
                for (int rank = 0; rank < numRanks; rank++) {
                    Multimap<Integer, FrameSemParse> parses = null;
                    if (rankedParseLists.containsKey(rank)) {
                        parses = rankedParseLists.get(rank);
                    } else {
                        parses = HashMultimap.create();
                    }
                    parses.put(writtenEx, kbestFspsMap.get(ex).get(id).get(rank));
                    rankedParseLists.put(rank, parses);
                }
            }
            ++writtenEx;
        }

        for (int rank : rankedParseLists.keySet()) {
            List<String> lines = Lists.newArrayList();
            for (int exNum : rankedParseLists.get(rank).keySet()) {
                for (FrameSemParse parse : rankedParseLists.get(rank).get(exNum)) {
                    lines.add(parse.toString(exNum));
                }
            }
            BasicFileWriter.writeStrings(lines, outPaths.semaforOutFEDir + "/" + rank
                    + DataPaths.FE_FILE_EXTN);
        }
        System.err.println("Wrote " + writtenEx + " FEs");
    }

    public static void makeMiniResults(DataPaths inPaths, DataPaths outPaths, List<Integer> selectEx) {
        Map<Integer, Map<FrameIdentifier, List<SemevalScore>>> kbestFscoMap = ResultsFileUtils
                .readAllFscores(inPaths.tokFile, inPaths.semaforResultsDir);

        createDirectory(outPaths.semaforResultsDir);
        Map<Integer, Multimap<Integer, Pair<FrameIdentifier, SemevalScore>>> rankedResultLists = Maps
                .newHashMap();

        int writtenEx = 0;
        for (int ex : selectEx) {
            if (kbestFscoMap.containsKey(ex) == false) {
                ++writtenEx;
                continue;
            }
            for (FrameIdentifier id : kbestFscoMap.get(ex).keySet()) {
                for (int rank = 0; rank < numRanks; rank++) {
                    Multimap<Integer, Pair<FrameIdentifier, SemevalScore>> resultList = null;
                    if (rankedResultLists.containsKey(rank)) {
                        resultList = rankedResultLists.get(rank);
                    } else {
                        resultList = HashMultimap.create();
                    }
                    resultList.put(writtenEx,
                            new Pair<FrameIdentifier, SemevalScore>(
                                    id, kbestFscoMap.get(ex).get(id).get(rank)));
                    rankedResultLists.put(rank, resultList);
                }
            }
            ++writtenEx;
        }

        for (int rank : rankedResultLists.keySet()) {
            List<String> lines = Lists.newArrayList();
            lines.add("Sent#\tFrame-ID\tRecall\tPrecision\tFscore");
            for (int ex : rankedResultLists.get(rank).keySet()) {
                for (Pair<FrameIdentifier, SemevalScore> pair : rankedResultLists.get(rank).get(ex)) {
                    lines.add(ResultsFileUtils.getFscoreLine(pair.first, pair.second, ex));
                }
            }
            BasicFileWriter.writeStrings(lines, outPaths.semaforResultsDir + "/" + rank
                    + DataPaths.RESULTS_FILE_EXTN);
        }
        System.err.println("Wrote " + writtenEx + " results");
    }

    public static void makeMiniSynScoreFiles(DataPaths inPaths, DataPaths outPaths,
            List<Integer> selectedEx) {
        Map<Integer, List<Double>> kbestSynScoresMap = SynScoreReader.readAllSynScores(
                inPaths.synScoresDir);

        createDirectory(outPaths.synScoresDir);
        Map<Integer, Map<Integer, Double>> rankedSynscoreList = Maps.newHashMap();

        int writtenEx = 0;
        for (int ex : selectedEx) {
            if (kbestSynScoresMap.containsKey(ex) == false) {
                ++writtenEx;
                continue;
            }
            for (int rank = 0; rank < numRanks; rank++) {
                Map<Integer, Double> synScoreMap = null;
                if (rankedSynscoreList.containsKey(rank)) {
                    synScoreMap = rankedSynscoreList.get(rank);
                } else {
                    synScoreMap = Maps.newHashMap();
                }
                synScoreMap.put(writtenEx, kbestSynScoresMap.get(ex).get(rank));
                rankedSynscoreList.put(rank, synScoreMap);
            }
            writtenEx++;
        }

        for (int rank : rankedSynscoreList.keySet()) {
            List<String> lines = Lists.newArrayList();
            lines.add("Sentence ID\tTurboScore");
            for (int ex : rankedSynscoreList.get(rank).keySet()) {
                lines.add(ex + "\t" + rankedSynscoreList.get(rank).get(ex));
            }
            BasicFileWriter.writeStrings(lines, outPaths.synScoresDir + "/" + rank
                    + DataPaths.SYNSCORE_FILE_EXTN);
        }
        System.err.println("Wrote " + writtenEx + " syn scores");
    }

    static List<Integer> makeMiniTokenizedFile(DataPaths inPaths, DataPaths outPaths, int numEx) {
        List<String> fullDataSetSents = BasicFileReader.readFile(inPaths.tokFile);
        List<Integer> selectedEx = createRandomSubset(fullDataSetSents.size(), numEx);

        List<String> selectedSents = Lists.newArrayList();
        for (int idx : selectedEx) {
            selectedSents.add(fullDataSetSents.get(idx));
        }
        BasicFileWriter.writeStrings(selectedSents, outPaths.tokFile);
        return selectedEx;
    }

    static void makeMiniConllFile(DataPaths inPaths, DataPaths outPaths,
            List<Integer> selectedEx) {
        List<Conll> inConlls = BasicFileReader.readConllFile(inPaths.conllFile);
        List<Conll> selectedConlls = Lists.newArrayList();
        for (int i : selectedEx) {
            selectedConlls.add(inConlls.get(i));
        }
        BasicFileWriter.writeConll(selectedConlls, outPaths.conllFile);
    }

    static List<Integer> createRandomSubset(int maxSize, int sizeWanted) {
        List<Integer> fullSet = Lists.newArrayList();
        for (int i = 0; i < maxSize; i++) {
            fullSet.add(i);
        }
        Collections.shuffle(fullSet);
        return fullSet.subList(0, sizeWanted);
    }

    static void createDirectory(String fileName) {
        System.err.println("Creating directory : " + fileName);
        File feDirFile = new File(fileName);
        if (feDirFile.exists()) {
            feDirFile.delete();
        }
        feDirFile.mkdir();
    }
}
