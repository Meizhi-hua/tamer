public class TestByteType {
    public static void main(String[] args) {
        System.setSecurityManager(new SecurityManager());
        new TestEditor(Byte.TYPE);
    }
}