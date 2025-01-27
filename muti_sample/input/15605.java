class ReferenceTypeNode extends AbstractSimpleTypeNode {
    String docType() {
        return "referenceTypeID";
    }
    String javaType() {
        return "ReferenceTypeImpl";
    }
    String debugValue(String label) {
        return "(" + label + "==null?\"NULL\":\"ref=\"+" + label + ".ref())";
    }
    public void genJavaWrite(PrintWriter writer, int depth,
                             String writeLabel) {
        genJavaDebugWrite(writer, depth, writeLabel,
                          debugValue(writeLabel));
        indent(writer, depth);
        writer.println("ps.writeClassRef(" + writeLabel + ".ref());");
    }
    String javaRead() {
        error("--- should not gen ---");
        return null;
    }
}
