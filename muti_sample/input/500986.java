public class JSONArrayTest extends TestCase {
    public void testEmptyArray() throws JSONException {
        JSONArray array = new JSONArray();
        assertEquals(0, array.length());
        assertEquals("", array.join(" AND "));
        try {
            array.get(0);
            fail();
        } catch (JSONException e) {
        }
        try {
            array.getBoolean(0);
            fail();
        } catch (JSONException e) {
        }
        assertEquals("[]", array.toString());
        assertEquals("[]", array.toString(4));
        assertTrue(array.isNull(0));
        assertNull(array.opt(0));
        assertFalse(array.optBoolean(0));
        assertTrue(array.optBoolean(0, true));
        assertNull(array.toJSONObject(new JSONArray()));
    }
    public void testEqualsAndHashCode() throws JSONException {
        JSONArray a = new JSONArray();
        JSONArray b = new JSONArray();
        assertTrue(a.equals(b));
        assertEquals("equals() not consistent with hashCode()", a.hashCode(), b.hashCode());
        a.put(true);
        a.put(false);
        b.put(true);
        b.put(false);
        assertTrue(a.equals(b));
        assertEquals(a.hashCode(), b.hashCode());
        b.put(true);
        assertFalse(a.equals(b));
        assertTrue(a.hashCode() != b.hashCode());
    }
    public void testBooleans() throws JSONException {
        JSONArray array = new JSONArray();
        array.put(true);
        array.put(false);
        array.put(2, false);
        array.put(3, false);
        array.put(2, true);
        assertEquals("[true,false,true,false]", array.toString());
        assertEquals(4, array.length());
        assertEquals(Boolean.TRUE, array.get(0));
        assertEquals(Boolean.FALSE, array.get(1));
        assertEquals(Boolean.TRUE, array.get(2));
        assertEquals(Boolean.FALSE, array.get(3));
        assertFalse(array.isNull(0));
        assertFalse(array.isNull(1));
        assertFalse(array.isNull(2));
        assertFalse(array.isNull(3));
        assertEquals(true, array.optBoolean(0));
        assertEquals(false, array.optBoolean(1, true));
        assertEquals(true, array.optBoolean(2, false));
        assertEquals(false, array.optBoolean(3));
        assertEquals("true", array.getString(0));
        assertEquals("false", array.getString(1));
        assertEquals("true", array.optString(2));
        assertEquals("false", array.optString(3, "x"));
        assertEquals("[\n     true,\n     false,\n     true,\n     false\n]", array.toString(5));
        JSONArray other = new JSONArray();
        other.put(true);
        other.put(false);
        other.put(true);
        other.put(false);
        assertTrue(array.equals(other));
        other.put(true);
        assertFalse(array.equals(other));
        other = new JSONArray();
        other.put("true");
        other.put("false");
        other.put("truE");
        other.put("FALSE");
        assertFalse(array.equals(other));
        assertFalse(other.equals(array));
        assertEquals(true, other.getBoolean(0));
        assertEquals(false, other.optBoolean(1, true));
        assertEquals(true, other.optBoolean(2));
        assertEquals(false, other.getBoolean(3));
    }
    public void testNulls() throws JSONException {
        JSONArray array = new JSONArray();
        array.put(3, null);
        array.put(0, JSONObject.NULL);
        assertEquals(4, array.length());
        assertEquals("[null,null,null,null]", array.toString());
        assertEquals(JSONObject.NULL, array.get(0));
        try {
            array.get(1);
            fail();
        } catch (JSONException e) {
        }
        try {
            array.get(2);
            fail();
        } catch (JSONException e) {
        }
        try {
            array.get(3);
            fail();
        } catch (JSONException e) {
        }
        assertEquals(JSONObject.NULL, array.opt(0));
        assertEquals(null, array.opt(1));
        assertEquals(null, array.opt(2));
        assertEquals(null, array.opt(3));
        assertTrue(array.isNull(0));
        assertTrue(array.isNull(1));
        assertTrue(array.isNull(2));
        assertTrue(array.isNull(3));
        assertEquals("null", array.optString(0));
        assertEquals("", array.optString(1));
        assertEquals("", array.optString(2));
        assertEquals("", array.optString(3));
    }
    public void testParseNullYieldsJSONObjectNull() throws JSONException {
        JSONArray array = new JSONArray("[\"null\",null]");
        array.put(null);
        assertEquals("null", array.get(0));
        assertEquals(JSONObject.NULL, array.get(1));
        try {
            array.get(2);
            fail();
        } catch (JSONException e) {
        }
        assertEquals("null", array.getString(0));
        assertEquals("null", array.getString(1));
        try {
            array.getString(2);
            fail();
        } catch (JSONException e) {
        }
    }
    public void testNumbers() throws JSONException {
        JSONArray array = new JSONArray();
        array.put(Double.MIN_VALUE);
        array.put(9223372036854775806L);
        array.put(Double.MAX_VALUE);
        array.put(-0d);
        assertEquals(4, array.length());
        assertEquals("[4.9E-324,9223372036854775806,1.7976931348623157E308,-0]", array.toString());
        assertEquals(Double.MIN_VALUE, array.get(0));
        assertEquals(9223372036854775806L, array.get(1));
        assertEquals(Double.MAX_VALUE, array.get(2));
        assertEquals(-0d, array.get(3));
        assertEquals(Double.MIN_VALUE, array.getDouble(0));
        assertEquals(9.223372036854776E18, array.getDouble(1));
        assertEquals(Double.MAX_VALUE, array.getDouble(2));
        assertEquals(-0d, array.getDouble(3));
        assertEquals(0, array.getLong(0));
        assertEquals(9223372036854775806L, array.getLong(1));
        assertEquals(Long.MAX_VALUE, array.getLong(2));
        assertEquals(0, array.getLong(3));
        assertEquals(0, array.getInt(0));
        assertEquals(-2, array.getInt(1));
        assertEquals(Integer.MAX_VALUE, array.getInt(2));
        assertEquals(0, array.getInt(3));
        assertEquals(Double.MIN_VALUE, array.opt(0));
        assertEquals(Double.MIN_VALUE, array.optDouble(0));
        assertEquals(0, array.optLong(0, 1L));
        assertEquals(0, array.optInt(0, 1));
        assertEquals("4.9E-324", array.getString(0));
        assertEquals("9223372036854775806", array.getString(1));
        assertEquals("1.7976931348623157E308", array.getString(2));
        assertEquals("-0.0", array.getString(3));
        JSONArray other = new JSONArray();
        other.put(Double.MIN_VALUE);
        other.put(9223372036854775806L);
        other.put(Double.MAX_VALUE);
        other.put(-0d);
        assertTrue(array.equals(other));
        other.put(0, 0L);
        assertFalse(array.equals(other));
    }
    public void testStrings() throws JSONException {
        JSONArray array = new JSONArray();
        array.put("true");
        array.put("5.5");
        array.put("9223372036854775806");
        array.put("null");
        array.put("5\"8' tall");
        assertEquals(5, array.length());
        assertEquals("[\"true\",\"5.5\",\"9223372036854775806\",\"null\",\"5\\\"8' tall\"]",
                array.toString());
        assertEquals("\"true\" \"5.5\" \"9223372036854775806\" \"null\" \"5\\\"8' tall\"",
                array.join(" "));
        assertEquals("true", array.get(0));
        assertEquals("null", array.getString(3));
        assertEquals("5\"8' tall", array.getString(4));
        assertEquals("true", array.opt(0));
        assertEquals("5.5", array.optString(1));
        assertEquals("9223372036854775806", array.optString(2, null));
        assertEquals("null", array.optString(3, "-1"));
        assertFalse(array.isNull(0));
        assertFalse(array.isNull(3));
        assertEquals(true, array.getBoolean(0));
        assertEquals(true, array.optBoolean(0));
        assertEquals(true, array.optBoolean(0, false));
        assertEquals(0, array.optInt(0));
        assertEquals(-2, array.optInt(0, -2));
        assertEquals(5.5d, array.getDouble(1));
        assertEquals(5L, array.getLong(1));
        assertEquals(5, array.getInt(1));
        assertEquals(5, array.optInt(1, 3));
        assertEquals(9223372036854775807L, array.getLong(2));
        assertEquals(9.223372036854776E18, array.getDouble(2));
        assertEquals(Integer.MAX_VALUE, array.getInt(2));
        assertFalse(array.isNull(3));
        try {
            array.getDouble(3);
            fail();
        } catch (JSONException e) {
        }
        assertEquals(Double.NaN, array.optDouble(3));
        assertEquals(-1.0d, array.optDouble(3, -1.0d));
    }
    public void testJoin() throws JSONException {
        JSONArray array = new JSONArray();
        array.put(null);
        assertEquals("null", array.join(" & "));
        array.put("\"");
        assertEquals("null & \"\\\"\"", array.join(" & "));
        array.put(5);
        assertEquals("null & \"\\\"\" & 5", array.join(" & "));
        array.put(true);
        assertEquals("null & \"\\\"\" & 5 & true", array.join(" & "));
        array.put(new JSONArray(Arrays.asList(true, false)));
        assertEquals("null & \"\\\"\" & 5 & true & [true,false]", array.join(" & "));
        array.put(new JSONObject(Collections.singletonMap("x", 6)));
        assertEquals("null & \"\\\"\" & 5 & true & [true,false] & {\"x\":6}", array.join(" & "));
    }
    public void testJoinWithNull() throws JSONException {
        JSONArray array = new JSONArray(Arrays.asList(5, 6));
        assertEquals("5null6", array.join(null));
    }
    public void testJoinWithSpecialCharacters() throws JSONException {
        JSONArray array = new JSONArray(Arrays.asList(5, 6));
        assertEquals("5\"6", array.join("\""));
    }
    public void testToJSONObject() throws JSONException {
        JSONArray keys = new JSONArray();
        keys.put("a");
        keys.put("b");
        JSONArray values = new JSONArray();
        values.put(5.5d);
        values.put(false);
        JSONObject object = values.toJSONObject(keys);
        assertEquals(5.5d, object.get("a"));
        assertEquals(false, object.get("b"));
        keys.put(0, "a");
        values.put(0, 11.0d);
        assertEquals(5.5d, object.get("a"));
    }
    public void testToJSONObjectWithNulls() throws JSONException {
        JSONArray keys = new JSONArray();
        keys.put("a");
        keys.put("b");
        JSONArray values = new JSONArray();
        values.put(5.5d);
        values.put(null);
        JSONObject object = values.toJSONObject(keys);
        assertEquals(1, object.length());
        assertFalse(object.has("b"));
        assertEquals("{\"a\":5.5}", object.toString());
    }
    public void testToJSONObjectMoreNamesThanValues() throws JSONException {
        JSONArray keys = new JSONArray();
        keys.put("a");
        keys.put("b");
        JSONArray values = new JSONArray();
        values.put(5.5d);
        JSONObject object = values.toJSONObject(keys);
        assertEquals(1, object.length());
        assertEquals(5.5d, object.get("a"));
    }
    public void testToJSONObjectMoreValuesThanNames() throws JSONException {
        JSONArray keys = new JSONArray();
        keys.put("a");
        JSONArray values = new JSONArray();
        values.put(5.5d);
        values.put(11.0d);
        JSONObject object = values.toJSONObject(keys);
        assertEquals(1, object.length());
        assertEquals(5.5d, object.get("a"));
    }
    public void testToJSONObjectNullKey() throws JSONException {
        JSONArray keys = new JSONArray();
        keys.put(JSONObject.NULL);
        JSONArray values = new JSONArray();
        values.put(5.5d);
        JSONObject object = values.toJSONObject(keys);
        assertEquals(1, object.length());
        assertEquals(5.5d, object.get("null"));
    }
    public void testPutUnsupportedNumbers() throws JSONException {
        JSONArray array = new JSONArray();
        try {
            array.put(Double.NaN);
            fail();
        } catch (JSONException e) {
        }
        try {
            array.put(0, Double.NEGATIVE_INFINITY);
            fail();
        } catch (JSONException e) {
        }
        try {
            array.put(0, Double.POSITIVE_INFINITY);
            fail();
        } catch (JSONException e) {
        }
    }
    public void testPutUnsupportedNumbersAsObject() throws JSONException {
        JSONArray array = new JSONArray();
        array.put(Double.valueOf(Double.NaN));
        array.put(Double.valueOf(Double.NEGATIVE_INFINITY));
        array.put(Double.valueOf(Double.POSITIVE_INFINITY));
        assertEquals(null, array.toString());
    }
    public void testCreateWithUnsupportedNumbers() throws JSONException {
        JSONArray array = new JSONArray(Arrays.asList(5.5, Double.NaN));
        assertEquals(2, array.length());
        assertEquals(5.5, array.getDouble(0));
        assertEquals(Double.NaN, array.getDouble(1));
    }
    public void testToStringWithUnsupportedNumbers() throws JSONException {
        JSONArray array = new JSONArray(Arrays.asList(5.5, Double.NaN));
        assertNull(array.toString());
    }
    public void testListConstructorCopiesContents() throws JSONException {
        List<Object> contents = Arrays.<Object>asList(5);
        JSONArray array = new JSONArray(contents);
        contents.set(0, 10);
        assertEquals(5, array.get(0));
    }
    public void testTokenerConstructor() throws JSONException {
        JSONArray object = new JSONArray(new JSONTokener("[false]"));
        assertEquals(1, object.length());
        assertEquals(false, object.get(0));
    }
    public void testTokenerConstructorWrongType() throws JSONException {
        try {
            new JSONArray(new JSONTokener("{\"foo\": false}"));
            fail();
        } catch (JSONException e) {
        }
    }
    public void testTokenerConstructorNull() throws JSONException {
        try {
            new JSONArray((JSONTokener) null);
            fail();
        } catch (NullPointerException e) {
        }
    }
    public void testTokenerConstructorParseFail() {
        try {
            new JSONArray(new JSONTokener("["));
            fail();
        } catch (JSONException e) {
        } catch (StackOverflowError e) {
            fail("Stack overflowed on input: \"[\"");
        }
    }
    public void testStringConstructor() throws JSONException {
        JSONArray object = new JSONArray("[false]");
        assertEquals(1, object.length());
        assertEquals(false, object.get(0));
    }
    public void testStringConstructorWrongType() throws JSONException {
        try {
            new JSONArray("{\"foo\": false}");
            fail();
        } catch (JSONException e) {
        }
    }
    public void testStringConstructorNull() throws JSONException {
        try {
            new JSONArray((String) null);
            fail();
        } catch (NullPointerException e) {
        }
    }
    public void testStringConstructorParseFail() {
        try {
            new JSONArray("[");
            fail();
        } catch (JSONException e) {
        } catch (StackOverflowError e) {
            fail("Stack overflowed on input: \"[\"");
        }
    }
    public void testCreate() throws JSONException {
        JSONArray array = new JSONArray(Arrays.asList(5.5, true));
        assertEquals(2, array.length());
        assertEquals(5.5, array.getDouble(0));
        assertEquals(true, array.get(1));
        assertEquals("[5.5,true]", array.toString());
    }
    public void testAccessOutOfBounds() throws JSONException {
        JSONArray array = new JSONArray();
        array.put("foo");
        assertEquals(null, array.opt(3));
        assertEquals(null, array.opt(-3));
        assertEquals("", array.optString(3));
        assertEquals("", array.optString(-3));
        try {
            array.get(3);
            fail();
        } catch (JSONException e) {
        }
        try {
            array.get(-3);
            fail();
        } catch (JSONException e) {
        }
        try {
            array.getString(3);
            fail();
        } catch (JSONException e) {
        }
        try {
            array.getString(-3);
            fail();
        } catch (JSONException e) {
        }
    }
}
