public class DescriptorSupportXMLTest {
    public static void main(String[] args) throws Exception {
        System.out.println("Testing that DescriptorSupport.toXMLString() " +
                           "can be used to reconstruct an equivalent " +
                           "DescriptorSupport");
        int failed = 0;
        final Object[] testValues = {
            "",
            "ok",
            "null",
            "(open",
            "close)",
            "(parens)",
            "quote\"quote",
            "a description with several words",
            "magic&\"\\<> \r\t\n\f;&;magic",
            "&lt;descriptor&gt;&&&&lt;/descriptor&gt;",
            "&lt;descriptor&gt;&&&&lt;/blahblahblah&gt;",
            null,
            new Integer(10),
            Boolean.TRUE,
            new Float(1.0f),
            new Character('!'),
            new java.util.HashMap(),
        };
        for (int i = 0; i < testValues.length; i++) {
            final Object v = testValues[i];
            final String what =
                (v == null) ? "null" :
                (v.getClass().getName() + "{" + v + "}");
            final DescriptorSupport in =
                new DescriptorSupport(new String[] {"bloo"}, new Object[] {v});
            final String xml;
            try {
                xml = in.toXMLString();
            } catch (RuntimeOperationsException e) {
                final Throwable cause = e.getCause();
                if (cause instanceof IllegalArgumentException) {
                    System.out.println("OK: " + what + ": got a " +
                                       "RuntimeOperationsException wrapping " +
                                       "an IllegalArgumentException: " +
                                       cause.getMessage());
                } else {
                    final String causeString =
                        (cause == null) ? "null" : cause.getClass().getName();
                    System.out.println("FAILED: " + what + ": got a " +
                                       "RuntimeOperationException wrapping " +
                                       causeString);
                    failed++;
                }
                continue;
            }
            System.out.println("Encoded " + what + " as " + xml);
            final DescriptorSupport out;
            try {
                out = new DescriptorSupport(xml);
            } catch (Exception e) {
                System.out.println("FAILED: " + what + ": got an exception:");
                e.printStackTrace(System.out);
                failed++;
                continue;
            }
            final String[] names = out.getFieldNames();
            if (names.length != 1 || !"bloo".equals(names[0])) {
                System.out.println("FAILED: decoded names wrong: " +
                                   Arrays.asList(names));
                failed++;
                continue;
            }
            final Object[] values = out.getFieldValues(names);
            if (values.length != 1) {
                System.out.println("FAILED: wrong number of values: " +
                                   Arrays.asList(values));
                failed++;
                continue;
            }
            final Object outValue = values[0];
            if (v == null) {
                if (outValue == null)
                    System.out.println("OK: decoded null value");
                else {
                    System.out.println("FAILED: decoded null value as " +
                                       outValue.getClass().getName() + "{" +
                                       outValue + "}");
                    failed++;
                }
                continue;
            }
            if (outValue == null) {
                System.out.println("FAILED: decoded non-null value as null");
                failed++;
                continue;
            }
            if (v.getClass() != outValue.getClass()) {
                System.out.println("FAILED: decoded value has class " +
                                   outValue.getClass().getName() + "{" +
                                   outValue + "}");
                failed++;
                continue;
            }
            if (v.equals(outValue))
                System.out.println("OK: decoded value is equal to original");
            else {
                System.out.println("FAILED: decoded value is different: {" +
                                   outValue + "}");
                failed++;
            }
        }
        if (failed == 0)
            System.out.println("OK: all tests passed");
        else {
            System.out.println("TEST FAILED: fail count: " + failed);
            System.exit(1);
        }
    }
}
