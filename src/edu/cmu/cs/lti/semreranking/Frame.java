package edu.cmu.cs.lti.semreranking;

import java.util.Set;

public class Frame {
    public final String id;
    public final int predStartPos;
    public final int predEndPos;

    public final String predType;
    public final String predToken;

    public Set<Argument> arguments;
    public final int numArgs;

    public double score;

    public Frame(
            String id,
            int predStartPos,
            int predEndPos,
            String predType,
            String predToken,
            Set<Argument> arguments,
            double score) {
        this.id = id;
        this.predStartPos = predStartPos;
        this.predEndPos = predEndPos;
        this.predType = predType;
        this.predToken = predToken;
        this.arguments = arguments;
        this.score = score;
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

    public String toString(int exNum) {
        StringBuilder builder = new StringBuilder();
        builder.append("0\t"); // 0
        builder.append(score); // 1
        builder.append("\t");
        builder.append(numArgs + 1); // 2
        builder.append("\t");
        builder.append(id); // 3
        builder.append("\t");

        StringBuilder predPosBuilder = new StringBuilder();
        for (int i = predStartPos; i <= predEndPos; i++) {
            predPosBuilder.append(i);
            if (i != predEndPos) {
                predPosBuilder.append("_");
            }
        }
        builder.append(predType); // 4
        builder.append("\t");
        builder.append(predPosBuilder.toString()); // 5
        builder.append("\t");
        builder.append(predToken); // 6
        builder.append("\t");

        builder.append(exNum); // 7

        for (Argument a : arguments) {
            builder.append("\t");
            builder.append(a.toString());
        }

        return builder.toString();
    }
}
