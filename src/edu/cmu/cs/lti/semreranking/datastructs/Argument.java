package edu.cmu.cs.lti.semreranking.datastructs;

public class Argument {
    public String id;
    public int start;
    public int end;

    public Argument(String id, int start, int end) {
        this.id = id;
        this.start = start;
        this.end = end;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (id == "NULL") {
            return builder.toString(); // empty string
        }
        builder.append(id);
        builder.append("\t");
        builder.append(start);
        if (start != end) {

            builder.append(":");
            builder.append(end);
        }

        return builder.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + end;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + start;
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
        Argument other = (Argument) obj;
        if (end != other.end)
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (start != other.start)
            return false;
        return true;
    }

}
