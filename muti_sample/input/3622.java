public class TypeVariableSignature implements FieldTypeSignature {
    private String identifier;
    private TypeVariableSignature(String id) {identifier = id;}
    public static TypeVariableSignature make(String id) {
        return new TypeVariableSignature(id);
    }
    public String getIdentifier(){return identifier;}
    public void accept(TypeTreeVisitor<?> v){
        v.visitTypeVariableSignature(this);
    }
}
