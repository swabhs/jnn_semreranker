package edu.cmu.cs.lti.semreranking;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Map;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.internal.Maps;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import edu.cmu.cs.lti.nlp.swabha.fileutils.BasicFileWriter;
import edu.cmu.cs.lti.semreranking.datastructs.FrameSemanticParse;
import edu.cmu.cs.lti.semreranking.datastructs.Scored;
import edu.cmu.cs.lti.semreranking.utils.CodeGrave;
import edu.cmu.cs.lti.semreranking.utils.FeReader;

/**
 * Given the semafor k-best results directory, creates a list of fsps sorted by f-score and writes
 * them to a file.
 * 
 * @author sswayamd
 */
public class MiniDataGenerator {

    @Parameter(names = "-mini", arity = 1, description = "use a mini corpus to test")
    public static boolean useMini = true;

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
            Map<Integer, Multimap<Integer, Scored<FrameSemanticParse>>> allFsps,
            String feDir,
            String xmlDir,
            int numExamples,
            int numRanks) {

        System.err.println("\nMaking mini dataset with " + numExamples + " examples with "
                + numRanks + " ranks here...");

        System.err.println(feDir);
        System.err.println(xmlDir);

        for (int col = 0; col < numRanks; col++) {
            List<String> feLines = Lists.newArrayList();
            List<String> fscoreLines = Lists.newArrayList();
            fscoreLines.add("Sentence ID\tFscore\tFscore\tFscore");

            int exNum = 0;
            for (Integer row : allFsps.keySet()) {
                if (allFsps.get(row).containsKey(col)) {
                    for (Scored<FrameSemanticParse> scoFsp : allFsps.get(row).get(col)) {
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
            BasicFileWriter.writeStrings(fscoreLines, xmlDir + col + DataPaths.XML_FILE_EXTN);
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
            Map<Integer, Multimap<Integer, Scored<FrameSemanticParse>>> unsorted = CodeGrave
                    .readScoredFsps(paths.xmlDir, paths.feDir, paths.synDir, new FeReader());

            DataPaths outpaths = new DataPaths(true, dataset);
            makeMiniDataSet(unsorted, outpaths.feDir, outpaths.xmlDir, dataSetSizes.get(dataset),
                    50);
        }

    }
}
