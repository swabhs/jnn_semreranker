package edu.cmu.cs.lti.semreranking;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Table;

import edu.cmu.cs.lti.nlp.swabha.fileutils.BasicFileReader;
import edu.cmu.cs.lti.nlp.swabha.fileutils.BasicFileWriter;
import edu.cmu.cs.lti.semreranking.utils.FileUtils;
import edu.cmu.cs.lti.semreranking.utils.Scored;

/**
 * Given the semafor k-best results directory, creates a list of fsps sorted by f-score and writes
 * them to a file.
 * 
 * @author sswayamd
 */
public class TrainingDataGenerator {

    public static final String SEMHOME = "/Users/sswayamd/Documents/workspace/jnn/SemanticReranker/data/";

    public static final String EXPDIR = SEMHOME + "experiments/basic_tbps/semreranker_train/";
    public static final String RESDIR = EXPDIR + "results/partial/";
    public static final String FEDIR = EXPDIR + "output/frameElements/";
    public static final String tokfile = SEMHOME
            + "/training/data/naacl2012/cv.train.sentences.tokenized";

    public static final String xmlFileExtn = "thBest.argid.predict.xml";
    public static final String feFileExtn = "thBest.argid.predict.frame.elements";

    public static final String destDir = FEDIR + "sorted/";
    public static final String destFeFileExtn = "thBest.SORTED.frame.elements";

    public static final int RANKS = 10;

    public TrainingDataGenerator() {
        Table<Integer, Integer, Scored<FrameSemanticParse>> allTrainInstances = FileUtils
                .readKBestSemaforOutput(RESDIR, xmlFileExtn, FEDIR, feFileExtn);
        sortTrainInstancesByFscores(allTrainInstances);
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
    void sortTrainInstancesByFscores(
            Table<Integer, Integer, Scored<FrameSemanticParse>> allTrainInstances) {

        for (Integer row : allTrainInstances.rowKeySet()) {
            List<Scored<FrameSemanticParse>> instances = Lists.newArrayList();
            instances.addAll(allTrainInstances.row(row).values());
            Collections.sort(instances);
            for (int col = 0; col < RANKS; col++) {
                allTrainInstances.put(row, col, instances.get(col));
                col++;
            }
        }

    }

    void writeSortedInstancesToFile(
            Table<Integer, Integer, Scored<FrameSemanticParse>> allTrainInstances) {
        List<String> toklines = BasicFileReader.readFile(tokfile);

        for (Integer col : allTrainInstances.columnKeySet()) {
            List<String> lines = Lists.newArrayList();
            for (Integer row : allTrainInstances.rowKeySet()) {
                if (allTrainInstances.contains(row, col)) {
                    Scored<FrameSemanticParse> instance = allTrainInstances.get(row, col);
                    lines.add(instance.entity.toString(toklines.get(row).split("\t"), row));
                }
            }
            BasicFileWriter.writeStrings(lines, destDir + col.toString() + destFeFileExtn);
        }
    }

    public static void main(String[] args) {
        new TrainingDataGenerator();
    }

}
