public class FormatData_en extends ListResourceBundle {
    protected final Object[][] getContents() {
        return new Object[][] {
            { "NumberPatterns",
                new String[] {
                    "#,##0.###;-#,##0.###", 
                    "\u00A4#,##0.00;-\u00A4#,##0.00", 
                    "#,##0%" 
                }
            },
            { "DateTimePatternChars", "GyMdkHmsSEDFwWahKzZ" },
        };
    }
}
