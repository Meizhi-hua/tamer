public class SignatureSourcerTest {
    private SignatureSourcer mSourcer;
    @Before
    public void setUp() throws Exception {
        mSourcer = new SignatureSourcer();
    }
    @After
    public void tearDown() throws Exception {
    }
    @Test
    public void testReturnMapNoArgs() {
        SignatureReader reader = new SignatureReader(
                "()Ljava/util/Map<Ljava/util/ArrayList<TT;>;Ljava/util/Map<Ljava/lang/String;Ljava/util/ArrayList<TU;>;>;>;");
        reader.accept(mSourcer);
        String result = mSourcer.getReturnType().toString();
        Assert.assertEquals(
                "java.util.Map<java.util.ArrayList<T>, java.util.Map<java.lang.String, java.util.ArrayList<U>>>",
                result);
    }
    @Test
    public void testReturnVoid() {
        SignatureReader reader = new SignatureReader(
                "(Ljava/util/List<+Lorg/w3c/dom/css/Rect;>;)V");
        reader.accept(mSourcer);
        String result = mSourcer.getReturnType().toString();
        Assert.assertEquals(
                "void",
                result);
    }
    @Test
    public void testSimpleArg() {
        SignatureReader reader = new SignatureReader(
                "(Ljava/util/List<+Lorg/w3c/dom/css/Rect;>;)V");
        reader.accept(mSourcer);
        ArrayList<SignatureSourcer> params = mSourcer.getParameters();
        Assert.assertNotNull(params);
        String[] array = toStringArray(params);
        Assert.assertArrayEquals(
                new String[] { "java.util.List<? extends org.w3c.dom.css.Rect>" },
                array);
    }
    @Test
    public void testFormalParameters1() {
        SignatureReader reader = new SignatureReader("<X:TT;Y:Ljava/lang/Object;>()V");
        reader.accept(mSourcer);
        Assert.assertTrue(mSourcer.hasFormalsContent());
        String result = mSourcer.formalsToString();
        Assert.assertEquals(
                "<X extends T, Y extends java.lang.Object>",
                result);
    }
    @Test
    public void testFormalParameters2() {
        SignatureReader reader = new SignatureReader("<T::Ljava/lang/Comparable<-TT;>;>(Ljava/util/List<TT;>;)V");
        reader.accept(mSourcer);
        Assert.assertTrue(mSourcer.hasFormalsContent());
        String result = mSourcer.formalsToString();
        Assert.assertEquals(
                "<T extends java.lang.Comparable<? super T>>",
                result);
    }
    @Test
    public void testManyArgs() {
        SignatureReader reader = new SignatureReader(
                "<X:TT;Y:Ljava/lang/Object;>(Ljava/util/List<TT;>;Ljava/util/Map<TT;TU;>;Ljava/util/Map<TX;Ljava/util/Set<-TY;>;>;)V");
        reader.accept(mSourcer);
        Assert.assertTrue(mSourcer.hasFormalsContent());
        String formals = mSourcer.formalsToString();
        Assert.assertEquals(
                "<X extends T, Y extends java.lang.Object>",
                formals);
        String result = mSourcer.getReturnType().toString();
        Assert.assertEquals(
                "void",
                result);
        ArrayList<SignatureSourcer> params = mSourcer.getParameters();
        Assert.assertNotNull(params);
        String[] array = toStringArray(params);
        Assert.assertArrayEquals(
                new String[] { "java.util.List<T>",
                               "java.util.Map<T, U>",
                               "java.util.Map<X, java.util.Set<? super Y>>" },
                array);
    }
    private String[] toStringArray(ArrayList<?> params) {
        String[] array = new String[params.size()];
        for (int i = 0; i < params.size(); i++) {
            array[i] = params.get(i).toString();
        }
        return array;
    }
}
