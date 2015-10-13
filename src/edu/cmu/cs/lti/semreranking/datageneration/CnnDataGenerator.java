package edu.cmu.cs.lti.semreranking.datageneration;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.internal.Sets;
import com.google.common.collect.Lists;

import edu.cmu.cs.lti.nlp.swabha.fileutils.BasicFileReader;
import edu.cmu.cs.lti.nlp.swabha.fileutils.BasicFileWriter;
import edu.cmu.cs.lti.semreranking.Data;
import edu.cmu.cs.lti.semreranking.datastructs.Argument;
import edu.cmu.cs.lti.semreranking.datastructs.FrameSemParse;
import edu.cmu.cs.lti.semreranking.datastructs.FrameSemParse.FrameIdentifier;
import edu.cmu.cs.lti.semreranking.datastructs.Scored;
import edu.cmu.cs.lti.semreranking.datastructs.SemevalScore;
import edu.cmu.cs.lti.semreranking.evaluation.Result;

public class CnnDataGenerator {

    @Parameter(names = "-semhome", description = "SEMAFOR home")
    public static String semHome = "/Users/sswayamd/Documents/workspace/jnn/SemanticReranker/data/";

    @Parameter(names = "-mini", arity = 1, description = "use a mini corpus to test")
    public static boolean useMini = true;

    @Parameter(names = "-ranks", description = "number of ranks")
    public static int numRanks = 50;

    public static NumberFormat formatter = new DecimalFormat("#0.00000");

    // private FrequencySet labelSet = new FrequencySet(0);

    private static final String NOT_AN_ARG = "*";
    private static final String TARGET = "TARGET";
    private static final String ARG = "ARG";
    private final String separator = "~";

    public static void main(String[] args) {
        // new JCommander(new CnnDataGenerator(), args);
        // String trainFile = "corpus.fsp";
        // String testFile = "dev.fsp";
        //
        // AllRerankingData data = DataFilesReader.readAllRerankingData(useMini);
        //
        // CnnDataGenerator gen = new CnnDataGenerator();
        // // gen.prepareTagListForCnn(data.trainData);
        // // gen.prepareTagListForCnn(data.devData);
        // // gen.labelSet.addKeyIfNotFrozen(NOT_AN_ARG);
        // // gen.labelSet.freeze();
        // gen.writeCnnForCorpus(data.trainData, trainFile);
        // gen.writeCnnForCorpus(data.devData, testFile);
        //
        // SemRerankerMain.printOracle(data);
        // Oracle.rankWiseAnalysis(data.devData);
        //
        // System.err.println("oracle at 1 = " + Oracle.getMicroCorpusAvg(data.devData, 1));
        // System.err.println("oracle at 2 = " + Oracle.getMicroCorpusAvg(data.devData, 2));
        // System.err.println("oracle at 3 = " + Oracle.getMicroCorpusAvg(data.devData, 3));
        // System.err.println("oracle at 4 = " + Oracle.getMicroCorpusAvg(data.devData, 4));
        // System.err.println("oracle at 5 = " + Oracle.getMicroCorpusAvg(data.devData, 5));
        // System.err.println("\nOracle tests on DEV data in CNN format: ");
        // seqLabelOracle(testFile);
        //
        // analyseLabelset(trainFile, testFile);
        String ele = "2.2";
        System.err.println(ele.matches("^\\d+([\\.,/:]\\d+)?$"));
    }

    // public void prepareTagListForCnn(Data data) {
    // for (int ex = 0; ex < data.tokens.size(); ex++) {
    // for (FrameIdentifier id : data.getFramesInEx(ex)) {
    // int numRanks = data.getInstance(ex, id).numUniqueParses;
    // for (int rank = 0; rank < numRanks; rank++) {
    // Scored<FrameSemParse> parse = data.getInstance(ex, id).getParseAtRank(rank);
    // addLabelsForParseToSet(parse);
    // }
    // }
    // }
    // }

    public void writeCnnForCorpus(Data data, String cnnFileName) {
        List<String> lines = Lists.newArrayList();
        for (int ex = 0; ex < data.tokens.size(); ex++) {
            String[] tokens = data.tokens.get(ex);
            for (FrameIdentifier id : data.getFramesInEx(ex)) {
                StringBuilder builder = new StringBuilder();
                joinTokenStrings(builder, tokens);
                int numRanks = data.getInstance(ex, id).numUniqueParses;
                for (int rank = 0; rank < numRanks; rank++) {
                    Scored<FrameSemParse> scoredParse = data.getInstance(ex, id).getParseAtRank(
                            rank);
                    String[] labels = generateLabelsForParse(scoredParse, tokens.length);
                    if (rank == numRanks - 1) {
                        joinParseStrings(builder, labels, scoredParse.semevalScore, true);
                        continue;
                    }
                    joinParseStrings(builder, labels, scoredParse.semevalScore, false);

                }
                joinPosStrings(builder, data.posTags.get(ex));
                lines.add(builder.toString());
            }
        }
        BasicFileWriter.writeStrings(lines, cnnFileName);
    }

    // private void addLabelsForParseToSet(Scored<FrameSemParse> scoredParse) {
    // labelSet.addKeyIfNotFrozen(scoredParse.entity.id + separator + TARGET);
    //
    // for (Argument arg : scoredParse.entity.arguments) {
    // if (arg.start == -1) { // frame with no arguments
    // continue;
    // }
    // labelSet.addKeyIfNotFrozen(scoredParse.entity.id + separator + ARG + separator + arg.id);
    // if (scoredParse.entity.tarStartPos >= arg.start
    // && scoredParse.entity.tarEndPos <= arg.end) {
    // labelSet.addKeyIfNotFrozen(scoredParse.entity.id + separator + TARGET + separator +
    // ARG + separator + arg.id);
    // }
    // }
    // }

    private String[] generateLabelsForParse(Scored<FrameSemParse> scoredParse, int sz) {
        String[] labels = new String[sz];
        String defaultLabel = scoredParse.entity.id + separator;
        // default case
        for (int pos = 0; pos < sz; pos++) {
            labels[pos] = defaultLabel;
        }

        // target tokens
        for (int pos = scoredParse.entity.tarStartPos; pos <= scoredParse.entity.tarEndPos; pos++) {
            // labels[pos] = labelSet.returnKeyAfterFreezing(labels[pos] + TARGET);
            labels[pos] = labels[pos] + TARGET;
        }

        // argument tokens
        for (Argument arg : scoredParse.entity.arguments) {
            if (arg.start == -1) { // frame with no arguments
                continue;
            }
            for (int pos = arg.start; pos <= arg.end; pos++) {
                // labels[pos] = labelSet.returnKeyAfterFreezing(labels[pos] + ARG + separator
                // + arg.id);
                labels[pos] = labels[pos] + ARG + separator
                        + arg.id;
            }
        }

        // token is not an argument for the frame
        for (int pos = 0; pos < sz; pos++) {
            if (labels[pos].equals(defaultLabel) == true) {
                // labels[pos] = labelSet.returnKeyAfterFreezing(NOT_AN_ARG);
                labels[pos] = NOT_AN_ARG;
            }
        }
        return labels;
    }

    public void joinTokenStrings(StringBuilder builder, String[] tokens) {
        builder.append("<s>\t");
        for (String ele : tokens) {
            if (ele.matches("^\\d+([\\.,/:]\\d+)?$")) { // "^(\\+|-)?([0-9]+(\\.[0-9]+))$"
                System.err.print(ele + "\t");
                builder.append("UNK_NUM\t");
                continue;
            }
            builder.append(ele);
            builder.append("\t");
        }
        builder.append("<\\s>\t||||\t");
    }

    public void joinParseStrings(
            StringBuilder builder,
            String[] tokens,
            SemevalScore score,
            boolean isLast) {
        builder.append("<s>\t");
        for (String ele : tokens) {
            builder.append(ele);
            builder.append("\t");
        }
        builder.append("<\\s>\t||\t");
        builder.append(score.pnum);
        builder.append("\t");
        builder.append(score.pdenom);
        builder.append("\t");
        builder.append(score.rnum);
        builder.append("\t");
        builder.append(score.rdenom);
        builder.append("\t");
        if (isLast == false) {
            builder.append("|||\t");
        }
    }

    public void joinPosStrings(StringBuilder builder, String[] posTags) {
        builder.append("|\t<s>\t");
        for (int pos = 0; pos < posTags.length; pos++) {
            builder.append(posTags[pos]);
            builder.append("\t");
        }
        builder.append("<\\s>");
    }

    public static void seqLabelOracle(String fileName) {
        List<String> cnnLines = BasicFileReader.readFile(fileName);
        List<List<Double>> oneBests = Lists.newArrayList();
        List<List<Double>> bestPerEx = Lists.newArrayList();
        for (String line : cnnLines) {
            line = line.trim();
            String kbestParses[] = line.split("\\|\\|\\|\\|")[1].split("\\|\\|\\|");

            double bestfscore = Double.NEGATIVE_INFINITY;
            List<Double> bestRes = null;

            int rank = 0;
            for (String parseSeq : kbestParses) {
                String evaluation[] = parseSeq.split("\\|\\|")[1].split("\\|")[0].split("\t");
                if (evaluation.length != 5) {
                    throw new IllegalArgumentException("results not read right");
                }
                List<Double> curRes = Lists.newArrayList();
                for (int i = 1; i < evaluation.length; i++) {
                    curRes.add(Double.parseDouble(evaluation[i]));
                }
                double fscore = Result.getFscore(curRes.get(0), curRes.get(1), curRes.get(2),
                        curRes.get(3));
                if (fscore > bestfscore) {
                    bestRes = curRes;
                    bestfscore = fscore;
                }
                if (rank == 0) {
                    oneBests.add(curRes);
                }
                ++rank;
            }
            bestPerEx.add(bestRes);
        }

        List<Double> orRes = Lists.newArrayList();
        List<Double> oneBest = Lists.newArrayList();
        for (int i = 0; i < 4; i++) {
            orRes.add(0.0);
            oneBest.add(0.0);
        }
        for (int ex = 0; ex < bestPerEx.size(); ex++) {
            for (int i = 0; i < bestPerEx.get(ex).size(); i++) {
                orRes.set(i, orRes.get(i) + bestPerEx.get(ex).get(i));
                oneBest.set(i, oneBest.get(i) + oneBests.get(ex).get(i));
            }
        }
        System.err.println("1-best = "
                + formatter.format(Result.getFscore(oneBest.get(0), oneBest.get(1), oneBest.get(2),
                        oneBest.get(3))));
        System.err.println("Oracle = "
                + formatter.format(Result.getFscore(orRes.get(0), orRes.get(1), orRes.get(2),
                        orRes.get(3))));
    }

    public static void analyseLabelset(String trainfile, String testfile) {
        List<String> trainLines = BasicFileReader.readFile(trainfile);

        Set<String> trainLabels = Sets.newHashSet();
        for (String line : trainLines) {
            String kbest[] = line.trim().split("\\|\\|\\|\\|")[1].split("\\|\\|\\|");

            for (String parseSeq : kbest) {
                String[] labels = parseSeq.split("\\|\\|")[0].split("\\|")[0].split("\t");
                trainLabels.addAll(Arrays.asList(labels));
            }
        }

        List<String> testLines = BasicFileReader.readFile(testfile);
        Set<String> unseenLabels = Sets.newHashSet();
        for (String line : testLines) {
            String kbest[] = line.trim().split("\\|\\|\\|\\|")[1].split("\\|\\|\\|");

            for (String parseSeq : kbest) {
                String[] labels = parseSeq.split("\\|\\|")[0].split("\t");
                for (String testLabel : labels) {
                    if (trainLabels.contains(testLabel) == false) {
                        unseenLabels.add(testLabel);
                    }
                }
            }
        }

        int totalLabels = trainLabels.size() + unseenLabels.size();
        System.err.println("Total number of labels = " + totalLabels);
        System.err.println("Train labels = " + trainLabels.size());
        System.err.println("Test labels unseen in train = " + unseenLabels.size());
        double unseenPercent = unseenLabels.size() * 100.0 / totalLabels;
        System.err.println(formatter.format(unseenPercent)
                + "% of all labels are not in training data");
    }
}
