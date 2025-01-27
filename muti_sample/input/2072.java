public class TestHtmlTableTags extends JavadocTester {
    private static final String BUG_ID = "6786688";
    private static final String[] ARGS = new String[] {
        "-d", BUG_ID, "-sourcepath", SRC_DIR, "-use", "pkg1", "pkg2"
    };
    private static final String[][] TABLE_TAGS_TEST = {
        {BUG_ID + FS + "pkg1" + FS + "package-summary.html",
            "<table class=\"packageSummary\" border=\"0\" cellpadding=\"3\"" +
            " cellspacing=\"0\" summary=\"Class Summary table, " +
            "listing classes, and an explanation\">"
        },
        {BUG_ID + FS + "pkg1" + FS + "package-summary.html",
            "<table class=\"packageSummary\" border=\"0\" cellpadding=\"3\"" +
            " cellspacing=\"0\" summary=\"Interface Summary table, " +
            "listing interfaces, and an explanation\">"
        },
        {BUG_ID + FS + "pkg2" + FS + "package-summary.html",
            "<table class=\"packageSummary\" border=\"0\" cellpadding=\"3\"" +
            " cellspacing=\"0\" summary=\"Enum Summary table, " +
            "listing enums, and an explanation\">"
        },
        {BUG_ID + FS + "pkg2" + FS + "package-summary.html",
            "<table class=\"packageSummary\" border=\"0\" cellpadding=\"3\"" +
            " cellspacing=\"0\" summary=\"Annotation Types Summary table, " +
            "listing annotation types, and an explanation\">"
        },
        {BUG_ID + FS + "pkg1" + FS + "C1.html",
            "<table class=\"overviewSummary\" border=\"0\" cellpadding=\"3\" " +
            "cellspacing=\"0\" summary=\"Field Summary table, listing fields, " +
            "and an explanation\">"
        },
        {BUG_ID + FS + "pkg1" + FS + "C1.html",
            "<table class=\"overviewSummary\" border=\"0\" cellpadding=\"3\" " +
            "cellspacing=\"0\" summary=\"Method Summary table, listing methods, " +
            "and an explanation\">"
        },
        {BUG_ID + FS + "pkg2" + FS + "C2.html",
            "<table class=\"overviewSummary\" border=\"0\" cellpadding=\"3\" " +
            "cellspacing=\"0\" summary=\"Nested Class Summary table, listing " +
            "nested classes, and an explanation\">"
        },
        {BUG_ID + FS + "pkg2" + FS + "C2.html",
            "<table class=\"overviewSummary\" border=\"0\" cellpadding=\"3\" " +
            "cellspacing=\"0\" summary=\"Constructor Summary table, listing " +
            "constructors, and an explanation\">"
        },
        {BUG_ID + FS + "pkg2" + FS + "C2.ModalExclusionType.html",
            "<table class=\"overviewSummary\" border=\"0\" cellpadding=\"3\" " +
            "cellspacing=\"0\" summary=\"Enum Constant Summary table, listing " +
            "enum constants, and an explanation\">"
        },
        {BUG_ID + FS + "pkg2" + FS + "C3.html",
            "<table class=\"overviewSummary\" border=\"0\" cellpadding=\"3\" " +
            "cellspacing=\"0\" summary=\"Required Element Summary table, " +
            "listing required elements, and an explanation\">"
        },
        {BUG_ID + FS + "pkg2" + FS + "C4.html",
            "<table class=\"overviewSummary\" border=\"0\" cellpadding=\"3\" " +
            "cellspacing=\"0\" summary=\"Optional Element Summary table, " +
            "listing optional elements, and an explanation\">"
        },
        {BUG_ID + FS + "pkg1" + FS + "class-use" + FS + "I1.html",
            "<table border=\"0\" cellpadding=\"3\" cellspacing=\"0\" summary=\"Use " +
            "table, listing packages, and an explanation\">"
        },
        {BUG_ID + FS + "pkg1" + FS + "class-use" + FS + "C1.html",
            "<table border=\"0\" cellpadding=\"3\" cellspacing=\"0\" summary=\"Use " +
            "table, listing fields, and an explanation\">"
        },
        {BUG_ID + FS + "pkg1" + FS + "class-use" + FS + "C1.html",
            "<table border=\"0\" cellpadding=\"3\" cellspacing=\"0\" summary=\"Use " +
            "table, listing methods, and an explanation\">"
        },
        {BUG_ID + FS + "pkg2" + FS + "class-use" + FS + "C2.html",
            "<table border=\"0\" cellpadding=\"3\" cellspacing=\"0\" summary=\"Use " +
            "table, listing fields, and an explanation\">"
        },
        {BUG_ID + FS + "pkg2" + FS + "class-use" + FS + "C2.html",
            "<table border=\"0\" cellpadding=\"3\" cellspacing=\"0\" summary=\"Use " +
            "table, listing methods, and an explanation\">"
        },
        {BUG_ID + FS + "pkg2" + FS + "class-use" + FS + "C2.ModalExclusionType.html",
            "<table border=\"0\" cellpadding=\"3\" cellspacing=\"0\" summary=\"Use " +
            "table, listing packages, and an explanation\">"
        },
        {BUG_ID + FS + "pkg2" + FS + "class-use" + FS + "C2.ModalExclusionType.html",
            "<table border=\"0\" cellpadding=\"3\" cellspacing=\"0\" summary=\"Use " +
            "table, listing methods, and an explanation\">"
        },
        {BUG_ID + FS + "pkg1" + FS + "package-use.html",
            "<table border=\"0\" cellpadding=\"3\" cellspacing=\"0\" summary=\"Use " +
            "table, listing packages, and an explanation\">"
        },
        {BUG_ID + FS + "pkg1" + FS + "package-use.html",
            "<table border=\"0\" cellpadding=\"3\" cellspacing=\"0\" summary=\"Use " +
            "table, listing classes, and an explanation\">"
        },
        {BUG_ID + FS + "pkg2" + FS + "package-use.html",
            "<table border=\"0\" cellpadding=\"3\" cellspacing=\"0\" summary=\"Use " +
            "table, listing packages, and an explanation\">"
        },
        {BUG_ID + FS + "pkg2" + FS + "package-use.html",
            "<table border=\"0\" cellpadding=\"3\" cellspacing=\"0\" summary=\"Use " +
            "table, listing classes, and an explanation\">"
        },
        {BUG_ID + FS + "deprecated-list.html",
            "<table border=\"0\" cellpadding=\"3\" cellspacing=\"0\" " +
            "summary=\"Deprecated Fields table, listing deprecated fields, " +
            "and an explanation\">"
        },
        {BUG_ID + FS + "deprecated-list.html",
            "<table border=\"0\" cellpadding=\"3\" cellspacing=\"0\" " +
            "summary=\"Deprecated Methods table, listing deprecated methods, " +
            "and an explanation\">"
        },
        {BUG_ID + FS + "constant-values.html",
            "<table border=\"0\" cellpadding=\"3\" cellspacing=\"0\" " +
            "summary=\"Constant Field Values table, listing " +
            "constant fields, and values\">"
        },
        {BUG_ID + FS + "overview-summary.html",
            "<table class=\"overviewSummary\" border=\"0\" cellpadding=\"3\" " +
            "cellspacing=\"0\" summary=\"Packages table, " +
            "listing packages, and an explanation\">"
        },
        {BUG_ID + FS + "pkg1" + FS + "package-summary.html",
            "<caption><span>Class Summary</span><span class=\"tabEnd\">" +
            "&nbsp;</span></caption>"
        },
        {BUG_ID + FS + "pkg1" + FS + "package-summary.html",
            "<caption><span>Interface Summary</span><span class=\"tabEnd\">" +
            "&nbsp;</span></caption>"
        },
        {BUG_ID + FS + "pkg2" + FS + "package-summary.html",
            "<caption><span>Enum Summary</span><span class=\"tabEnd\">" +
            "&nbsp;</span></caption>"
        },
        {BUG_ID + FS + "pkg2" + FS + "package-summary.html",
            "<caption><span>Annotation Types Summary</span><span class=\"tabEnd\">" +
            "&nbsp;</span></caption>"
        },
        {BUG_ID + FS + "pkg1" + FS + "C1.html",
            "<caption><span>Fields</span><span class=\"tabEnd\">&nbsp;</span></caption>"
        },
        {BUG_ID + FS + "pkg1" + FS + "C1.html",
            "<caption><span>Methods</span><span class=\"tabEnd\">&nbsp;</span></caption>"
        },
        {BUG_ID + FS + "pkg2" + FS + "C2.html",
            "<caption><span>Nested Classes</span><span class=\"tabEnd\">&nbsp;</span></caption>"
        },
        {BUG_ID + FS + "pkg2" + FS + "C2.html",
            "<caption><span>Constructors</span><span class=\"tabEnd\">&nbsp;</span></caption>"
        },
        {BUG_ID + FS + "pkg2" + FS + "C2.ModalExclusionType.html",
            "<caption><span>Enum Constants</span><span class=\"tabEnd\">&nbsp;</span></caption>"
        },
        {BUG_ID + FS + "pkg2" + FS + "C3.html",
            "<caption><span>Required Elements</span><span class=\"tabEnd\">&nbsp;" +
            "</span></caption>"
        },
        {BUG_ID + FS + "pkg2" + FS + "C4.html",
            "<caption><span>Optional Elements</span><span class=\"tabEnd\">&nbsp;" +
            "</span></caption>"
        },
        {BUG_ID + FS + "pkg1" + FS + "class-use" + FS + "I1.html",
            "<caption><span>Packages that use <a href=\"../../pkg1/I1.html\" " +
            "title=\"interface in pkg1\">I1</a></span><span class=\"tabEnd\">" +
            "&nbsp;</span></caption>"
        },
        {BUG_ID + FS + "pkg1" + FS + "class-use" + FS + "C1.html",
            "<caption><span>Fields in <a href=\"../../pkg2/package-summary.html\">" +
            "pkg2</a> declared as <a href=\"../../pkg1/C1.html\" " +
            "title=\"class in pkg1\">C1</a></span><span class=\"tabEnd\">&nbsp;" +
            "</span></caption>"
        },
        {BUG_ID + FS + "pkg1" + FS + "class-use" + FS + "C1.html",
            "<caption><span>Methods in <a href=\"../../pkg2/package-summary.html\">" +
            "pkg2</a> that return <a href=\"../../pkg1/C1.html\" " +
            "title=\"class in pkg1\">C1</a></span><span class=\"tabEnd\">" +
            "&nbsp;</span></caption>"
        },
        {BUG_ID + FS + "pkg2" + FS + "class-use" + FS + "C2.html",
            "<caption><span>Fields in <a href=\"../../pkg1/package-summary.html\">" +
            "pkg1</a> declared as <a href=\"../../pkg2/C2.html\" " +
            "title=\"class in pkg2\">C2</a></span><span class=\"tabEnd\">" +
            "&nbsp;</span></caption>"
        },
        {BUG_ID + FS + "pkg2" + FS + "class-use" + FS + "C2.html",
            "<caption><span>Methods in <a href=\"../../pkg1/package-summary.html\">" +
            "pkg1</a> that return <a href=\"../../pkg2/C2.html\" " +
            "title=\"class in pkg2\">C2</a></span><span class=\"tabEnd\">" +
            "&nbsp;</span></caption>"
        },
        {BUG_ID + FS + "pkg2" + FS + "class-use" + FS + "C2.ModalExclusionType.html",
            "<caption><span>Methods in <a href=\"../../pkg2/package-summary.html\">" +
            "pkg2</a> that return <a href=\"../../pkg2/C2.ModalExclusionType.html\" " +
            "title=\"enum in pkg2\">C2.ModalExclusionType</a></span>" +
            "<span class=\"tabEnd\">&nbsp;</span></caption>"
        },
        {BUG_ID + FS + "pkg1" + FS + "package-use.html",
            "<caption><span>Packages that use <a href=\"../pkg1/package-summary.html\">" +
            "pkg1</a></span><span class=\"tabEnd\">&nbsp;</span></caption>"
        },
        {BUG_ID + FS + "pkg1" + FS + "package-use.html",
            "<caption><span>Classes in <a href=\"../pkg1/package-summary.html\">" +
            "pkg1</a> used by <a href=\"../pkg1/package-summary.html\">pkg1</a>" +
            "</span><span class=\"tabEnd\">&nbsp;</span></caption>"
        },
        {BUG_ID + FS + "pkg2" + FS + "package-use.html",
            "<caption><span>Packages that use <a href=\"../pkg2/package-summary.html\">" +
            "pkg2</a></span><span class=\"tabEnd\">&nbsp;</span></caption>"
        },
        {BUG_ID + FS + "pkg2" + FS + "package-use.html",
            "<caption><span>Classes in <a href=\"../pkg2/package-summary.html\">" +
            "pkg2</a> used by <a href=\"../pkg1/package-summary.html\">pkg1</a>" +
            "</span><span class=\"tabEnd\">&nbsp;</span></caption>"
        },
        {BUG_ID + FS + "deprecated-list.html",
            "<caption><span>Deprecated Fields</span><span class=\"tabEnd\">" +
            "&nbsp;</span></caption>"
        },
        {BUG_ID + FS + "deprecated-list.html",
            "<caption><span>Deprecated Methods</span><span class=\"tabEnd\">" +
            "&nbsp;</span></caption>"
        },
        {BUG_ID + FS + "constant-values.html",
            "<caption><span>pkg1.<a href=\"pkg1/C1.html\" title=\"class in pkg1\">" +
            "C1</a></span><span class=\"tabEnd\">&nbsp;</span></caption>"
        },
        {BUG_ID + FS + "overview-summary.html",
            "<caption><span>Packages</span><span class=\"tabEnd\">&nbsp;</span></caption>"
        },
        {BUG_ID + FS + "pkg1" + FS + "package-summary.html",
            "<th class=\"colFirst\" scope=\"col\">" +
            "Class</th>" + NL + "<th class=\"colLast\" scope=\"col\"" +
            ">Description</th>"
        },
        {BUG_ID + FS + "pkg1" + FS + "package-summary.html",
            "<th class=\"colFirst\" scope=\"col\">" +
            "Interface</th>" + NL + "<th class=\"colLast\" scope=\"col\"" +
            ">Description</th>"
        },
        {BUG_ID + FS + "pkg2" + FS + "package-summary.html",
            "<th class=\"colFirst\" scope=\"col\">" +
            "Enum</th>" + NL + "<th class=\"colLast\" scope=\"col\"" +
            ">Description</th>"
        },
        {BUG_ID + FS + "pkg2" + FS + "package-summary.html",
            "<th class=\"colFirst\" scope=\"col\">" +
            "Annotation Type</th>" + NL + "<th class=\"colLast\"" +
            " scope=\"col\">Description</th>"
        },
        {BUG_ID + FS + "pkg1" + FS + "C1.html",
            "<th class=\"colFirst\" scope=\"col\">Modifier and Type</th>" + NL +
            "<th class=\"colLast\" scope=\"col\">Field and Description</th>"
        },
        {BUG_ID + FS + "pkg1" + FS + "C1.html",
            "<th class=\"colFirst\" scope=\"col\">Modifier and Type</th>" + NL +
            "<th class=\"colLast\" scope=\"col\">Method and Description</th>"
        },
        {BUG_ID + FS + "pkg2" + FS + "C2.html",
            "<th class=\"colFirst\" scope=\"col\">Modifier and Type</th>" + NL +
            "<th class=\"colLast\" scope=\"col\">Class and Description</th>"
        },
        {BUG_ID + FS + "pkg2" + FS + "C2.html",
            "<th class=\"colOne\" scope=\"col\">Constructor and Description</th>"
        },
        {BUG_ID + FS + "pkg2" + FS + "C2.ModalExclusionType.html",
            "<th class=\"colOne\" scope=\"col\">Enum Constant and Description</th>"
        },
        {BUG_ID + FS + "pkg2" + FS + "C3.html",
            "<th class=\"colFirst\" scope=\"col\">Modifier and Type</th>" + NL +
            "<th class=\"colLast\" scope=\"col\">Required Element and Description</th>"
        },
        {BUG_ID + FS + "pkg2" + FS + "C4.html",
            "<th class=\"colFirst\" scope=\"col\">Modifier and Type</th>" + NL +
            "<th class=\"colLast\" scope=\"col\">Optional Element and Description</th>"
        },
        {BUG_ID + FS + "pkg1" + FS + "class-use" + FS + "I1.html",
            "<th class=\"colFirst\" scope=\"col\">Package</th>" + NL +
            "<th class=\"colLast\" scope=\"col\">Description</th>"
        },
        {BUG_ID + FS + "pkg1" + FS + "class-use" + FS + "C1.html",
            "<th class=\"colFirst\" scope=\"col\">Modifier and Type</th>" + NL +
            "<th class=\"colLast\" scope=\"col\">Field and Description</th>"
        },
        {BUG_ID + FS + "pkg1" + FS + "class-use" + FS + "C1.html",
            "<th class=\"colFirst\" scope=\"col\">Modifier and Type</th>" + NL +
            "<th class=\"colLast\" scope=\"col\">Method and Description</th>"
        },
        {BUG_ID + FS + "pkg2" + FS + "class-use" + FS + "C2.html",
            "<th class=\"colFirst\" scope=\"col\">Modifier and Type</th>" + NL +
            "<th class=\"colLast\" scope=\"col\">Field and Description</th>"
        },
        {BUG_ID + FS + "pkg2" + FS + "class-use" + FS + "C2.html",
            "<th class=\"colFirst\" scope=\"col\">Modifier and Type</th>" + NL +
            "<th class=\"colLast\" scope=\"col\">Method and Description</th>"
        },
        {BUG_ID + FS + "pkg2" + FS + "class-use" + FS + "C2.ModalExclusionType.html",
            "<th class=\"colFirst\" scope=\"col\">Package</th>" + NL +
            "<th class=\"colLast\" scope=\"col\">Description</th>"
        },
        {BUG_ID + FS + "pkg2" + FS + "class-use" + FS + "C2.ModalExclusionType.html",
            "<th class=\"colFirst\" scope=\"col\">Modifier and Type</th>" + NL +
            "<th class=\"colLast\" scope=\"col\">Method and Description</th>"
        },
        {BUG_ID + FS + "pkg1" + FS + "package-use.html",
            "<th class=\"colFirst\" scope=\"col\">Package</th>" + NL +
            "<th class=\"colLast\" scope=\"col\">Description</th>"
        },
        {BUG_ID + FS + "pkg1" + FS + "package-use.html",
            "<th class=\"colOne\" scope=\"col\">Class and Description</th>"
        },
        {BUG_ID + FS + "pkg2" + FS + "package-use.html",
            "<th class=\"colFirst\" scope=\"col\">Package</th>" + NL +
            "<th class=\"colLast\" scope=\"col\">Description</th>"
        },
        {BUG_ID + FS + "pkg2" + FS + "package-use.html",
            "<th class=\"colOne\" scope=\"col\">Class and Description</th>"
        },
        {BUG_ID + FS + "deprecated-list.html",
            "<th class=\"colOne\" scope=\"col\">Field and Description</th>"
        },
        {BUG_ID + FS + "deprecated-list.html",
            "<th class=\"colOne\" scope=\"col\">Method and Description</th>"
        },
        {BUG_ID + FS + "constant-values.html",
            "<th class=\"colFirst\" scope=\"col\">" +
            "Modifier and Type</th>" + NL + "<th" +
            " scope=\"col\">Constant Field</th>" + NL +
            "<th class=\"colLast\" scope=\"col\">Value</th>"
        },
        {BUG_ID + FS + "overview-summary.html",
            "<th class=\"colFirst\" scope=\"col\">" +
            "Package</th>" + NL + "<th class=\"colLast\" scope=\"col\"" +
            ">Description</th>"
        }
    };
    private static final String[][] NEGATED_TEST = NO_TEST;
    public static void main(String[] args) {
        TestHtmlTableTags tester = new TestHtmlTableTags();
        run(tester, ARGS, TABLE_TAGS_TEST, NEGATED_TEST);
        tester.printSummary();
    }
    public String getBugId() {
        return BUG_ID;
    }
    public String getBugName() {
        return getClass().getName();
    }
}
