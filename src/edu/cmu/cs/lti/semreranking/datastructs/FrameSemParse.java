package edu.cmu.cs.lti.semreranking.datastructs;

import java.util.Set;

public class FrameSemParse {
    public final String id;
    public final int predStartPos;
    public final int predEndPos;

    public final String lexicalUnit;
    public final String predToken;
    public final String predPosTag;

    public Set<Argument> arguments;
    public final int numArgs;

    public double score;

    public FrameSemParse(
            String id,
            int predStartPos,
            int predEndPos,
            String lexicalUnit,
            String predToken,
            Set<Argument> arguments,
            double score) {
        this.id = id;
        this.predStartPos = predStartPos;
        this.predEndPos = predEndPos;
        this.lexicalUnit = lexicalUnit;
        this.predToken = predToken;
        this.predPosTag = lexicalUnit.split("\\.")[1];
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
        builder.append("1\t"); // 0
        builder.append(score); // 1
        builder.append("\t");
        if (numArgs == 1 && arguments.iterator().next().id.equals("NULL")) {
            builder.append(1); // 2
        } else {
            builder.append(numArgs + 1); // 2
        }
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
        builder.append(lexicalUnit); // 4
        builder.append("\t");
        builder.append(predPosBuilder.toString()); // 5
        builder.append("\t");
        builder.append(predToken); // 6
        builder.append("\t");

        builder.append(exNum); // 7

        for (Argument a : arguments) {
            if (a.id.equals("NULL")) {
                break;
            }
            builder.append("\t");
            builder.append(a.toString());
        }

        return builder.toString();
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FrameSemParse other = (FrameSemParse) obj;
        if (arguments == null) {
            if (other.arguments != null)
                return false;
        } else if (!arguments.equals(other.arguments))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (lexicalUnit == null) {
            if (other.lexicalUnit != null)
                return false;
        } else if (!lexicalUnit.equals(other.lexicalUnit))
            return false;
        if (numArgs != other.numArgs)
            return false;
        if (predEndPos != other.predEndPos)
            return false;
        if (predPosTag == null) {
            if (other.predPosTag != null)
                return false;
        } else if (!predPosTag.equals(other.predPosTag))
            return false;
        if (predStartPos != other.predStartPos)
            return false;
        if (predToken == null) {
            if (other.predToken != null)
                return false;
        } else if (!predToken.equals(other.predToken))
            return false;
        if (Double.doubleToLongBits(score) != Double.doubleToLongBits(other.score))
            return false;
        return true;
    }

}
