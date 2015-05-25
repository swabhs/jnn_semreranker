package edu.cmu.cs.lti.semreranking;

import java.util.Map;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;

import edu.cmu.cs.lti.nlp.swabha.basic.Pair;

public class FrameSemanticParse {

    public class Frame {
        public final String id;
        public final int predStartPos;
        public final int predEndPos;

        public Set<Argument> arguments;
        public final int numArgs;

        public Double score;

        public Frame(String id, int predStartPos, int predEndPos,
                Set<Argument> arguments, Double score) {
            this.id = id;
            this.score = score;
            this.predStartPos = predStartPos;
            this.predEndPos = predEndPos;
            this.arguments = arguments;
            this.numArgs = arguments.size();
        }

        public void print() {
            System.out.println("FRAME " + id + "\t" + predStartPos + ":" + predEndPos);
            System.out.println("---args---");
            for (Argument a : arguments) {
                System.out.println(a.id + "\t" + a.start + ":" + a.end);
            }
            System.out.println();
        }

        public String toString(String[] tokens, int exNum) {
            StringBuilder builder = new StringBuilder();
            builder.append("0\t"); // 0
            builder.append(score); // 1
            builder.append("\t");
            builder.append(numArgs + 1); // 2
            builder.append("\t");
            builder.append(id); // 3
            builder.append("\t");

            StringBuilder predBuilder = new StringBuilder();
            StringBuilder predPosBuilder = new StringBuilder();
            for (int i = predStartPos; i <= predEndPos; i++) {
                predBuilder.append(tokens[i]);
                predPosBuilder.append(i);
                if (i != predEndPos) {
                    predBuilder.append("_");
                    predPosBuilder.append("_");
                }
            }
            builder.append(predBuilder.toString()); // 4
            builder.append("\t");
            builder.append(predPosBuilder.toString()); // 5
            builder.append("\t");
            builder.append(predBuilder.toString()); // 6
            builder.append("\t");

            builder.append(exNum);

            for (Argument a : arguments) {
                builder.append("\t");
                builder.append(a.toString());
            }

            return builder.toString();
        }
    }

    public class Argument {
        public String id;
        public int start;
        public int end;

        public Argument(String id, int start, int end) {
            this.id = id.split("___")[0];
            this.start = start;
            this.end = end;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(id);
            builder.append("\t");
            builder.append(start);
            if (start != end) {

                builder.append(":");
                builder.append(end);
            }

            return builder.toString();
        }
    }

    public Set<Frame> frames;

    public int numFrames;
    public int numFrameArgs;

    public FrameSemanticParse(
            Map<String, Integer> predStartMap,
            Map<String, Integer> predEndMap,
            Table<String, String, Pair<Integer, Integer>> frameMap,
            Optional<Map<String, Double>> frameScores) {

        Set<Frame> frames = Sets.newHashSet();
        for (String frame : predStartMap.keySet()) {
            Set<Argument> arguments = Sets.newHashSet();
            for (String arg : frameMap.row(frame).keySet()) {
                arguments.add(new Argument(arg,
                        frameMap.get(frame, arg).first, frameMap.get(frame, arg).second));
            }

            frames.add(new Frame(frame, predStartMap.get(frame), predEndMap.get(frame), arguments,
                    null));
        }

        this.frames = frames;
        numFrames = frames.size();
        numFrameArgs = 0;
        for (Frame f : frames) {
            numFrameArgs += f.numArgs;
        }
    }

    public void print() {
        for (Frame frame : frames) {
            frame.print();
        }
    }

    public String toString(String[] tokens, int exNum) {
        StringBuilder builder = new StringBuilder();
        int i = 0;
        for (Frame frame : frames) {
            builder.append(frame.toString(tokens, exNum));
            i++;
            if (i != frames.size()) {
                builder.append("\n");
            }
        }
        return builder.toString();
    }
}
