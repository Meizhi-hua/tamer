public class NameLookupBuilderTest extends TestCase {
    private static class TestNameLookupBuilder extends NameLookupBuilder {
        StringBuilder sb = new StringBuilder();
        public TestNameLookupBuilder(NameSplitter splitter) {
            super(splitter);
        }
        @Override
        protected String normalizeName(String name) {
            return name;
        }
        @Override
        protected String[] getCommonNicknameClusters(String normalizedName) {
            if (normalizedName.equals("Bill")) {
                return new String[] {"*William"};
            } else if (normalizedName.equals("Al")) {
                return new String[] {"*Alex", "*Alice"};
            }
            return null;
        }
        public String inserted() {
            return sb.toString();
        }
        @Override
        protected void insertNameLookup(long rawContactId, long dataId, int lookupType,
                String string) {
            sb.append("(").append(lookupType).append(":").append(string).append(")");
        }
    }
    private TestNameLookupBuilder mBuilder;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mBuilder = new TestNameLookupBuilder(
                new NameSplitter("Mr", "", "", "", Locale.getDefault()));
    }
    public void testEmptyName() {
        mBuilder.insertNameLookup(0, 0, "", FullNameStyle.UNDEFINED);
        assertEquals("", mBuilder.inserted());
    }
    public void testSingleUniqueName() {
        mBuilder.insertNameLookup(0, 0, "Foo", FullNameStyle.UNDEFINED);
        assertEquals("(0:Foo)(2:Foo)", mBuilder.inserted());
    }
    public void testSingleUniqueNameWithPrefix() {
        mBuilder.insertNameLookup(0, 0, "Mr. Foo", FullNameStyle.UNDEFINED);
        assertEquals("(0:Foo)(2:Foo)", mBuilder.inserted());
    }
    public void testTwoUniqueNames() {
        mBuilder.insertNameLookup(0, 0, "Foo Bar", FullNameStyle.UNDEFINED);
        assertEquals("(0:Foo.Bar)(2:FooBar)(1:Bar.Foo)(2:BarFoo)", mBuilder.inserted());
    }
    public void testThreeUniqueNames() {
        mBuilder.insertNameLookup(0, 0, "Foo Bar Baz", FullNameStyle.UNDEFINED);
        assertEquals(
                "(0:Foo.Bar.Baz)(2:FooBarBaz)" +
                "(1:Foo.Baz.Bar)(2:FooBazBar)" +
                "(1:Bar.Foo.Baz)(2:BarFooBaz)" +
                "(1:Bar.Baz.Foo)(2:BarBazFoo)" +
                "(1:Baz.Bar.Foo)(2:BazBarFoo)" +
                "(1:Baz.Foo.Bar)(2:BazFooBar)", mBuilder.inserted());
    }
    public void testFourUniqueNames() {
        mBuilder.insertNameLookup(0, 0, "Foo Bar Baz Biz", FullNameStyle.UNDEFINED);
        assertEquals(
                "(0:Foo.Bar.Baz.Biz)(2:FooBarBazBiz)" +
                "(1:Foo.Bar.Biz.Baz)(2:FooBarBizBaz)" +
                "(1:Foo.Baz.Bar.Biz)(2:FooBazBarBiz)" +
                "(1:Foo.Baz.Biz.Bar)(2:FooBazBizBar)" +
                "(1:Foo.Biz.Baz.Bar)(2:FooBizBazBar)" +
                "(1:Foo.Biz.Bar.Baz)(2:FooBizBarBaz)" +
                "(1:Bar.Foo.Baz.Biz)(2:BarFooBazBiz)" +
                "(1:Bar.Foo.Biz.Baz)(2:BarFooBizBaz)" +
                "(1:Bar.Baz.Foo.Biz)(2:BarBazFooBiz)" +
                "(1:Bar.Baz.Biz.Foo)(2:BarBazBizFoo)" +
                "(1:Bar.Biz.Baz.Foo)(2:BarBizBazFoo)" +
                "(1:Bar.Biz.Foo.Baz)(2:BarBizFooBaz)" +
                "(1:Baz.Bar.Foo.Biz)(2:BazBarFooBiz)" +
                "(1:Baz.Bar.Biz.Foo)(2:BazBarBizFoo)" +
                "(1:Baz.Foo.Bar.Biz)(2:BazFooBarBiz)" +
                "(1:Baz.Foo.Biz.Bar)(2:BazFooBizBar)" +
                "(1:Baz.Biz.Foo.Bar)(2:BazBizFooBar)" +
                "(1:Baz.Biz.Bar.Foo)(2:BazBizBarFoo)" +
                "(1:Biz.Bar.Baz.Foo)(2:BizBarBazFoo)" +
                "(1:Biz.Bar.Foo.Baz)(2:BizBarFooBaz)" +
                "(1:Biz.Baz.Bar.Foo)(2:BizBazBarFoo)" +
                "(1:Biz.Baz.Foo.Bar)(2:BizBazFooBar)" +
                "(1:Biz.Foo.Baz.Bar)(2:BizFooBazBar)" +
                "(1:Biz.Foo.Bar.Baz)(2:BizFooBarBaz)", mBuilder.inserted());
    }
    public void testSingleNickname() {
        mBuilder.insertNameLookup(0, 0, "Bill", FullNameStyle.UNDEFINED);
        assertEquals("(0:Bill)(2:Bill)(1:*William)", mBuilder.inserted());
    }
    public void testSingleNameWithTwoNicknames() {
        mBuilder.insertNameLookup(0, 0, "Al", FullNameStyle.UNDEFINED);
        assertEquals("(0:Al)(2:Al)(1:*Alex)(1:*Alice)", mBuilder.inserted());
    }
    public void testTwoNamesOneOfWhichIsNickname() {
        mBuilder.insertNameLookup(0, 0, "Foo Al", FullNameStyle.UNDEFINED);
        assertEquals(
                "(0:Foo.Al)(2:FooAl)" +
                "(1:Al.Foo)(2:AlFoo)" +
                "(1:Foo.*Alex)(1:*Alex.Foo)" +
                "(1:Foo.*Alice)(1:*Alice.Foo)", mBuilder.inserted());
    }
    public void testTwoNamesBothNickname() {
        mBuilder.insertNameLookup(0, 0, "Bill Al", FullNameStyle.UNDEFINED);
        assertEquals(
                "(0:Bill.Al)(2:BillAl)" +
                "(1:Al.Bill)(2:AlBill)" +
                "(1:*William.Al)(1:Al.*William)" +
                "(1:*William.*Alex)(1:*Alex.*William)" +
                "(1:*William.*Alice)(1:*Alice.*William)" +
                "(1:Bill.*Alex)(1:*Alex.Bill)" +
                "(1:Bill.*Alice)(1:*Alice.Bill)", mBuilder.inserted());
    }
    public void testChineseName() {
        if (!Arrays.asList(Collator.getAvailableLocales()).contains(Locale.CHINA)) {
            return;
        }
        mBuilder.insertNameLookup(0, 0, "\u695A\u8FAD", FullNameStyle.CHINESE);
        assertEquals(
                "(0:\u695A\u8FAD)" +
                "(2:\u695A\u8FAD)" +
                "(6:\u695A\u8FAD)" +
                "(6:CI)" +
                "(6:\u8FAD)" +
                "(6:CHUCI)" +
                "(6:CC)" +
                "(6:C)", mBuilder.inserted());
    }
    public void testMultiwordName() {
        mBuilder.insertNameLookup(0, 0, "Jo Jeffrey John Jessy Longname", FullNameStyle.UNDEFINED);
        String actual = mBuilder.inserted();
        assertTrue(actual.contains("(0:Jo.Jeffrey.John.Jessy.Longname)"));
        assertTrue(actual.contains("(2:JoJeffreyJohnJessyLongname)"));
        assertTrue(actual.contains("(1:Longname.Jeffrey.Jessy.John)"));
        assertTrue(actual.contains("(2:Jo"));
        assertTrue(actual.contains("(2:Jeffrey"));
        assertTrue(actual.contains("(2:John"));
        assertTrue(actual.contains("(2:Jessy"));
        assertTrue(actual.contains("(2:Longname"));
    }
}
