package edu.cmu.cs.lti.semreranking.datastructs;

import java.util.Set;

import edu.cmu.cs.lti.nlp.swabha.basic.Pair;

public class FrameSemParse {
    public final String id;
    public final int tarStartPos;
    public final int tarEndPos;

    public final String lexicalUnit;
    public final String tarToken;
    public final String tarPosTag;

    public Set<Argument> arguments;
    public final int numArgs;

    public double semaforScore;

    public FrameSemParse(
            String id,
            int tarStartPos,
            int tarEndPos,
            String lexicalUnit,
            String tarToken,
            Set<Argument> arguments,
            double score) {
        this.id = id;
        this.tarStartPos = tarStartPos;
        this.tarEndPos = tarEndPos;
        this.lexicalUnit = lexicalUnit;
        this.tarToken = tarToken;
        this.tarPosTag = lexicalUnit.split("\\.")[1];
        this.arguments = arguments;
        this.semaforScore = score;
        this.numArgs = arguments.size();
    }

    public void print() {
        System.out.println("FRAME " + id + "\t" + tarStartPos + ":" + tarEndPos);
        System.out.println("---args---");
        for (Argument a : arguments) {
            System.out.println(a.id + "\t" + a.start + ":" + a.end);
        }
        System.out.println();
    }

    public String toString(int exNum) {
        StringBuilder builder = new StringBuilder();
        builder.append("1\t"); // 0
        builder.append(semaforScore); // 1
        builder.append("\t");
        if (numArgs == 1 && arguments.iterator().next().id.equals("NULL")) {
            builder.append(1); // 2
        } else {
            builder.append(numArgs + 1); // 2
        }
        builder.append("\t");
        builder.append(id); // 3
        builder.append("\t");

        StringBuilder tarPosBuilder = new StringBuilder();
        for (int i = tarStartPos; i <= tarEndPos; i++) {
            tarPosBuilder.append(i);
            if (i != tarEndPos) {
                tarPosBuilder.append("_");
            }
        }
        builder.append(lexicalUnit); // 4
        builder.append("\t");
        builder.append(tarPosBuilder.toString()); // 5
        builder.append("\t");
        builder.append(tarToken); // 6
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
        if (tarEndPos != other.tarEndPos)
            return false;
        if (tarPosTag == null) {
            if (other.tarPosTag != null)
                return false;
        } else if (!tarPosTag.equals(other.tarPosTag))
            return false;
        if (tarStartPos != other.tarStartPos)
            return false;
        if (tarToken == null) {
            if (other.tarToken != null)
                return false;
        } else if (!tarToken.equals(other.tarToken))
            return false;
        if (Double.doubleToLongBits(semaforScore) != Double.doubleToLongBits(other.semaforScore))
            return false;
        return true;
    }

    public static class FrameIdentifier {
        public String frameId;
        public int tarStartPos;
        public int tarEndPos;
        public Pair<Short, Short> sentIdx;

        public FrameIdentifier(String frameId, int tarStartPos, int tarEndPos) {
            this(frameId, tarStartPos, tarEndPos, new Pair<Short, Short>((short) -1, (short) -1));
        }

        public FrameIdentifier(String frameId, int tarStartPos, int tarEndPos,
                Pair<Short, Short> sentIdx) {
            this.frameId = frameId;
            this.tarStartPos = tarStartPos;
            this.tarEndPos = tarEndPos;
            this.sentIdx = sentIdx;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("frame id:\t");
            builder.append(frameId);
            builder.append("\nstart:\t");
            builder.append(tarStartPos);
            builder.append("\nend:\t");
            builder.append(tarEndPos);
            return builder.toString();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((frameId == null) ? 0 : frameId.hashCode());
            result = prime * result + tarEndPos;
            result = prime * result + tarStartPos;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            FrameIdentifier other = (FrameIdentifier) obj;
            if (frameId == null) {
                if (other.frameId != null)
                    return false;
            } else if (!frameId.equals(other.frameId))
                return false;
            if (tarEndPos != other.tarEndPos)
                return false;
            if (tarStartPos != other.tarStartPos)
                return false;
            return true;
        }
    }

    public FrameIdentifier getIdentifier() {
        return new FrameIdentifier(id, tarStartPos, tarEndPos);
    }

}
