class ReferenceIDTypeNode extends ReferenceTypeNode {
    String javaType() {
        return "long";
    }
    String debugValue(String label) {
        return "\"ref=\"+" + label;
    }
    public void genJavaWrite(PrintWriter writer, int depth,
                             String writeLabel) {
        genJavaDebugWrite(writer, depth, writeLabel);
        indent(writer, depth);
        writer.println("ps.writeClassRef(" + writeLabel + ");");
    }
    String javaRead() {
        return "ps.readClassRef()";
    }
}
