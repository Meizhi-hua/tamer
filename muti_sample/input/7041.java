abstract class AbstractNamedNode extends Node {
    NameNode nameNode = null;
    String name;
    public String name() {
        return name;
    }
    void prune() {
        Iterator it = components.iterator();
        if (it.hasNext()) {
            Node nameNode = (Node)it.next();
            if (nameNode instanceof NameNode) {
                this.nameNode = (NameNode)nameNode;
                this.name = this.nameNode.text();
                it.remove();
            } else {
                error("Bad name: " + name);
            }
        } else {
            error("empty");
        }
        super.prune();
    }
    void constrain(Context ctx) {
        nameNode.constrain(ctx);
        super.constrain(ctx.subcontext(name));
    }
    void document(PrintWriter writer) {
        writer.println("<h4><a name=" + name + ">" + name +
                       " Command Set</a></h4>");
        for (Iterator it = components.iterator(); it.hasNext();) {
            ((Node)it.next()).document(writer);
        }
    }
    String javaClassName() {
        return name();
    }
    void genJavaClassSpecifics(PrintWriter writer, int depth) {
    }
    String javaClassImplements() {
        return ""; 
    }
    void genJavaClass(PrintWriter writer, int depth) {
        writer.println();
        genJavaComment(writer, depth);
        indent(writer, depth);
        if (depth != 0) {
            writer.print("static ");
        }
        writer.print("class " + javaClassName());
        writer.println(javaClassImplements() + " {");
        genJavaClassSpecifics(writer, depth+1);
        for (Iterator it = components.iterator(); it.hasNext();) {
            ((Node)it.next()).genJava(writer, depth+1);
        }
        indent(writer, depth);
        writer.println("}");
    }
    void genCInclude(PrintWriter writer) {
        if (nameNode instanceof NameValueNode) {
            writer.println("#define " + context.whereC +
                           " " + nameNode.value());
        }
        super.genCInclude(writer);
    }
}
