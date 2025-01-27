@TestTargetClass(CollationElementIterator.class) 
public class CollationElementIteratorTest extends TestCase {
    private RuleBasedCollator coll;
    protected void setUp() {
        coll = (RuleBasedCollator) Collator.getInstance(Locale.US);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getOffset",
        args = {}
    )
    public void testGetOffset() {
        String text = "abc";
        CollationElementIterator iterator = coll
                .getCollationElementIterator(text);
        int[] offsets = { 0, 1, 2, 3 };
        int offset = iterator.getOffset();
        int i = 0;
        assertEquals(offsets[i++], offset);
        while (offset != text.length()) {
            iterator.next();
            offset = iterator.getOffset();
            assertEquals(offsets[i++], offset);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "next",
        args = {}
    )
    public void testNext() {
        String text = "abc";
        CollationElementIterator iterator = coll
                .getCollationElementIterator(text);
        int[] orders = new int[text.length()];
        int order = iterator.next();
        int i = 0;
        while (order != CollationElementIterator.NULLORDER) {
            orders[i++] = order;
            order = iterator.next();
        }
        int offset = iterator.getOffset();
        assertEquals(text.length(), offset);
        order = iterator.previous();
        while (order != CollationElementIterator.NULLORDER) {
            assertEquals(orders[--i], order);
            order = iterator.previous();
        }
        assertEquals(0, iterator.getOffset());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "previous",
        args = {}
    )
    public void testPrevious() {
        String text = "abc";
        CollationElementIterator iterator = coll
                .getCollationElementIterator(text);
        int[] orders = new int[text.length()];
        int order = iterator.next();
        int i = 0;
        while (order != CollationElementIterator.NULLORDER) {
            orders[i++] = order;
            order = iterator.next();
        }
        int offset = iterator.getOffset();
        assertEquals(text.length(), offset);
        order = iterator.previous();
        while (order != CollationElementIterator.NULLORDER) {
            assertEquals(orders[--i], order);
            order = iterator.previous();
        }
        assertEquals(0, iterator.getOffset());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "reset",
        args = {}
    )
    public void testReset() {
        String text = "abc";
        CollationElementIterator iterator = coll
                .getCollationElementIterator(text);
        int[] orders = new int[text.length()];
        int order = iterator.next();
        int i = 0;
        while (order != CollationElementIterator.NULLORDER) {
            orders[i++] = order;
            order = iterator.next();
        }
        int offset = iterator.getOffset();
        assertEquals(text.length(), offset);
        iterator.reset();
        assertEquals(0, iterator.getOffset());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getMaxExpansion",
        args = {int.class}
    )
    public void testGetMaxExpansion() {
        String text = "cha";
        RuleBasedCollator rbColl = (RuleBasedCollator) Collator
                .getInstance(new Locale("es", "", "TRADITIONAL"));
        CollationElementIterator iterator = rbColl
                .getCollationElementIterator(text);
        int order = iterator.next();
        while (order != CollationElementIterator.NULLORDER) {
            assertEquals(1, iterator.getMaxExpansion(order));
            order = iterator.next();
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "primaryOrder",
        args = {int.class}
    )
    public void testPrimaryOrder() {
        RuleBasedCollator rbColl = (RuleBasedCollator) Collator
                .getInstance(new Locale("de", "DE"));
        String text = "\u00e6";
        CollationElementIterator iterator = rbColl
                .getCollationElementIterator(text);
        int order = iterator.next();
        int pOrder = CollationElementIterator.primaryOrder(order);
        CollationElementIterator iterator2 = rbColl
                .getCollationElementIterator("ae");
        int order2 = iterator2.next();
        int pOrder2 = CollationElementIterator.primaryOrder(order2);
        assertEquals(pOrder, pOrder2);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "secondaryOrder",
        args = {int.class}
    )
    public void testSecondaryOrder() {
        RuleBasedCollator rbColl = (RuleBasedCollator) Collator
                .getInstance(new Locale("fr", "FR"));
        String text = "a\u00e0";
        CollationElementIterator iterator = rbColl
                .getCollationElementIterator(text);
        int order = iterator.next();
        int sOrder1 = CollationElementIterator.secondaryOrder(order);
        order = iterator.next();
        int sOrder2 = CollationElementIterator.secondaryOrder(order);
        assertEquals(sOrder1, sOrder2);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "tertiaryOrder",
        args = {int.class}
    )
    public void testTertiaryOrder() {
        RuleBasedCollator rbColl = (RuleBasedCollator) Collator
                .getInstance(new Locale("fr", "FR"));
        String text = "abAB";
        CollationElementIterator iterator = rbColl
                .getCollationElementIterator(text);
        int order = iterator.next();
        int tOrder1 = CollationElementIterator.tertiaryOrder(order);
        order = iterator.next();
        int tOrder2 = CollationElementIterator.tertiaryOrder(order);
        assertEquals(tOrder1, tOrder2);
        order = iterator.next();
        tOrder1 = CollationElementIterator.tertiaryOrder(order);
        order = iterator.next();
        tOrder2 = CollationElementIterator.tertiaryOrder(order);
        assertEquals(tOrder1, tOrder2);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setOffset",
        args = {int.class}
    )
    public void testSetOffset() {
        RuleBasedCollator rbColl = (RuleBasedCollator) Collator
                .getInstance(new Locale("es", "", "TRADITIONAL"));
        String text = "cha";
        CollationElementIterator iterator = rbColl
                .getCollationElementIterator(text);
        iterator.setOffset(1);
        assertEquals(1, iterator.getOffset());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setText",
        args = {java.lang.String.class}
    )
    public void testSetTextString() {
        RuleBasedCollator rbColl = (RuleBasedCollator) Collator
                .getInstance(new Locale("es", "", "TRADITIONAL"));
        String text = "caa";
        CollationElementIterator iterator = rbColl
                .getCollationElementIterator(text);
        iterator.setOffset(1);
        assertEquals(1, iterator.getOffset());
        iterator.setText("cha");
        iterator.setOffset(1);
        assertEquals(1, iterator.getOffset());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setText",
        args = {java.text.CharacterIterator.class}
    )
    public void testSetTextCharacterIterator() {
        RuleBasedCollator rbColl = (RuleBasedCollator) Collator
                .getInstance(new Locale("es", "", "TRADITIONAL"));
        String text = "caa";
        CollationElementIterator iterator = rbColl
                .getCollationElementIterator(text);
        iterator.setOffset(1);
        assertEquals(1, iterator.getOffset());
        iterator.setText(new StringCharacterIterator("cha"));
        iterator.setOffset(1);
        assertEquals(1, iterator.getOffset());
    }
}
