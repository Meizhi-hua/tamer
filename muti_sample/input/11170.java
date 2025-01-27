class ByteTypeNode extends AbstractSimpleTypeNode {
    String docType() {
        return "byte";
    }
    public void genJavaWrite(PrintWriter writer, int depth,
                             String writeLabel) {
        genJavaDebugWrite(writer, depth, writeLabel);
        indent(writer, depth);
        writer.println("ps.writeByte(" + writeLabel + ");");
    }
    String javaRead() {
        return "ps.readByte()";
    }
}
