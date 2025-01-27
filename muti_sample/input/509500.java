public class TestCoverageDoclet {
    public static final String[] COLORS = {
            "#a0ffa0", "#ffffa0", "#ff8080"
    };
    private static final boolean DEBUG = false;
    private File directory;
    private boolean _ignoreInterfacesAndAbstractMethods = false;
    private boolean _doIncludeDisabledTests = false;
    private boolean _acceptCompleteWithOtherStatus = false;
    private Map<ExecutableMemberDoc, AnnotationPointer> resolved =
        new HashMap<ExecutableMemberDoc, AnnotationPointer>(8192);
    private class DocComparator implements Comparator<Doc> {
        public int compare(Doc elem1, Doc elem2) {
            return elem1.name().compareTo(elem2.name());
        }
        public boolean equals(Doc elem) {
            return this == elem;
        }
    }
    private class MemberComparator implements Comparator<ExecutableMemberDoc> {
        public int compare(ExecutableMemberDoc mem1, ExecutableMemberDoc mem2) {
            return mem1.toString().compareTo(mem2.toString());
        }
    }
    private DocComparator classComparator = new DocComparator();
    private MemberComparator memberComparator = new MemberComparator();
    private Map<ClassDoc, List<TestTargetNew>> classToSpecialTargets =
        new HashMap<ClassDoc, List<TestTargetNew>>();
    public TestCoverageDoclet(String directory) {
        this.directory = new File(directory);
    }
    private PrintWriter openFile(String filename, String title) {
        File file = new File(directory, filename);
        File parent = file.getParentFile();
        parent.mkdirs();
        PrintWriter printer;
        try {
            printer = new PrintWriter(new FileOutputStream(file));
        } catch (FileNotFoundException e) {
            throw new RuntimeException("file not found:" + e.getMessage());
        }
        printer.println("<html>");
        printer.println("  <head>");
        printer.println("    <title>" + title + "</title>");
        printer.println("<style type=\"text/css\">\n"
                + "body, body table tr td { font-size:10pt;font-family: "
                + " sans-serif; }\n" + "li { padding-bottom:2px }\n"
                + "table { width:100%; border-width: 0px; border: solid; "
                + "border-collapse: collapse;}\n"
                + "table tr td { vertical-align:top; padding:3px; border: "
                + "1px solid black;}\n" + "</style>");
        printer.println("  </head>");
        printer.println("  <body>");
        printer.println("    <h1>" + title + "</h1>");
        return printer;
    }
    private void closeFile(PrintWriter printer) {
        printer.println("  </body>");
        printer.println("</html>");
        printer.flush();
        printer.close();
    }
    private class TablePrinter {
        private PrintWriter pr;
        public TablePrinter(PrintWriter pr) {
            this.pr = pr;
        }
        public void printRow(String... columns) {
            pr.print("<tr style=\"background-color:white\">");
            for (String col : columns) {
                pr.print("<td>" + col + "</td>");
            }
            pr.print("</tr>");
        }
        public void printPlain(String val) {
            pr.print(val);
        }
        public void printTableStart() {
            pr.print("<table>");
        }
        public void printTableEnd() {
            pr.print("</table>");
        }
        public void printPlainLn(String val) {
            printPlain(val);
            pr.print("<br>");
        }
    }
    private void process(RootDoc root) {
        System.out.println("V 0.9a");
        String mode = getOption(root, "-f", 1, "dummy-see bash script");
        System.out.println("mode: " + mode);
        _doIncludeDisabledTests = mode.contains("countdisabled");
        _acceptCompleteWithOtherStatus = mode.contains("acceptcandx");
        if (_doIncludeDisabledTests) {
            System.out.println("- including disabled tests");
        } else {
            System.out.println("- excluding disabled tests");
        }
        if (_acceptCompleteWithOtherStatus) {
            System.out.println("- accepting complete tests with partial tests");
        } else {
            System.out.println("- not accepting complete tests with partial "
                    + "tests");
        }
        System.out.println("stage 1 - get targets from all junit test methods");
        PrintWriter pr = openFile("testcoverage/test-annotation.html",
                "test class annotation coverage");
        TablePrinter printer = new TablePrinter(pr);
        printer.printTableStart();
        printer.printRow("Test package name", "JUnit classes", "Meth", "Unt",
                "Part", "Compl", "Disab", "Broken", "ToBeFixed", "KnownFail");
        ColorStat totalStats = new ColorStat("All test packages", null);
        PackageDoc[] packages = root.specifiedPackages();
        Arrays.sort(packages, classComparator);
        for (PackageDoc pack : packages) {
            if (pack.allClasses().length != 0) {
                ColorStat packageStat = processTestPackage(pack);
                if (!packageStat.ignored) {
                    printTestStats(printer, packageStat, true);
                    totalStats.add(packageStat);
                }
            }
        }
        printer.printTableEnd();
        printer.printPlainLn("<h2>Summary of all test packages</h2>");
        printer.printTableStart();
        printTestStats(printer, totalStats, false);
        printer.printTableEnd();
        closeFile(pr);
        System.out.println("stage 2 - proxy test targets to abstract classes"
                + "and interfaces");
        ClassDoc[] classes = root.classes();
        for (ClassDoc classDoc : classes) {
            if (!extendsJUnitTestCase(classDoc)) {
                MethodDoc[] methods = classDoc.methods();
                for (MethodDoc methodDoc : methods) {
                    AnnotationPointer ap = getAnnotationPointer(methodDoc,
                            false);
                    if (ap != null) {
                        List<MethodDoc> impls = implementingMethods(methodDoc);
                        for (MethodDoc impl : impls) {
                            AnnotationPointer apImpl = getAnnotationPointer(
                                    impl, true);
                            apImpl.addProxiesFrom(ap);
                        }
                    }
                }
            }
        }
        System.out.println("stage 3 - generating report for target api");
        pr = openFile("index.html", "All target api packages");
        printer = new TablePrinter(pr);
        printer.printPlainLn("Generated " + new Date().toString()
                + " - V0.9a<br>");
        printer.printPlainLn("<a href=\"testcoverage/test-annotation.html\">"
                + "annotation progress of test classes</a><br><br>");
        printer.printTableStart();
        printer.printRow("Package", "Classes", "Methods", "Untested",
                "Partial", "Complete", "Disabled", "Broken", "ToBeFixed", "KnownFail");
        totalStats = new ColorStat("All target packages", null);
        packages = root.specifiedPackages();
        Arrays.sort(packages, classComparator);
        int classCount = 0;
        for (PackageDoc pack : packages) {
            if (pack.allClasses().length != 0) {
                ColorStat packageStat = processPackage(pack);
                if (!packageStat.ignored) {
                    printStats(printer, packageStat, true);
                    totalStats.add(packageStat);
                    classCount += Integer.parseInt(packageStat.getExtra());
                }
            }
        }
        printer.printTableEnd();
        totalStats.setExtra("" + classCount);
        printer.printPlainLn("<h2>Summary of all target packages</h2>");
        printer.printTableStart();
        printStats(printer, totalStats, false);
        printer.printTableEnd();
        closeFile(pr);
    }
    private List<MethodDoc> implementingMethods(MethodDoc doc) {
        List<MethodDoc> resultmethods = new ArrayList<MethodDoc>();
        ClassDoc clazz = doc.containingClass();
        implementedMethod0(resultmethods, doc, clazz, false);
        return resultmethods;
    }
    private void implementedMethod0(List<MethodDoc> resultmethods,
            MethodDoc doc, ClassDoc testClass, boolean testMethods) {
        if (testMethods) {
            MethodDoc[] methods = testClass.methods();
            for (int j = 0; j < methods.length; j++) {
                MethodDoc methodDoc = methods[j];
                if ((methodDoc.isAbstract() || testClass.isInterface())
                        && doc.overrides(methodDoc)) {
                    resultmethods.add(methodDoc);
                }
            }
        }
        ClassDoc[] ifs = testClass.interfaces();
        for (int i = 0; i < ifs.length; i++) {
            ClassDoc iface = ifs[i];
            implementedMethod0(resultmethods, doc, iface, true);
        }
        ClassDoc superclass = testClass.superclass();
        if (superclass != null) {
            implementedMethod0(resultmethods, doc, superclass, true);
        }
    }
    private ColorStat processTestPackage(PackageDoc packageDoc) {
        ColorStat stats = new ColorStat(packageDoc.name(),
                getPackageBaseLink(packageDoc) + "/package.html");
        if (hasHideFlag(packageDoc)) {
            stats.ignored = true;
            return stats;
        }
        String file = getPackageDir("testcoverage", packageDoc)
                + "/package.html";
        PrintWriter pr = openFile(file, "Test package " + packageDoc.name());
        TablePrinter printer = new TablePrinter(pr);
        printer.printTableStart();
        printer.printRow("Class", "Extra", "Meth", "Unt", "Part", "Compl",
                "Disab", "Broken", "ToBeFixed", "KnownFail");
        ClassDoc[] classes = packageDoc.allClasses();
        Arrays.sort(classes, classComparator);
        int junitCnt = 0;
        for (ClassDoc clazz : classes) {
            if (extendsJUnitTestCase(clazz)) {
                junitCnt++;
                ColorStat subStats = processTestClass(clazz);
                printTestStats(printer, subStats, true);
                stats.add(subStats);
            } else {
                printer.printRow(clazz.name() + " ignored (no junit class): ",
                        "", "", "", "", "", "", "", "");
            }
        }
        printer.printTableEnd();
        printer.printPlainLn("<h2>Test package summary</h2>");
        printer.printTableStart();
        printStats(printer, stats, false);
        printer.printTableEnd();
        closeFile(pr);
        if (junitCnt == 0) {
            if ((packageDoc.name().contains("tests.")
                    || packageDoc.name().contains("junit.") || packageDoc
                    .name().contains(".testframework"))
                    && !(packageDoc.name().equals("junit.framework"))) {
                System.err.println("warning!: no junit classes in package '"
                        + packageDoc.name() + "' even though package name "
                        + "contains tests.,junit. or .testframework");
            }
            stats = new ColorStat(packageDoc.name(),
                    getPackageBaseLink(packageDoc) + "/package.html");
            stats.incColor(TestMethodInformation.Color.GREEN);
            stats.setExtra("Ignored since no Junit test and suites");
            stats.ignored = true;
        } else {
            stats.setExtra("" + junitCnt);
        }
        return stats;
    }
    private ColorStat processPackage(PackageDoc packageDoc) {
        ColorStat stats = new ColorStat(packageDoc.name(),
                getPackageBaseLink(packageDoc) + "/package.html");
        if (hasHideFlag(packageDoc)) {
            stats.ignored = true;
            return stats;
        }
        String file = getPackageDir("", packageDoc) + "/package.html";
        PrintWriter pr = openFile(file, "Package " + packageDoc.name());
        TablePrinter printer = new TablePrinter(pr);
        printer.printTableStart();
        printer.printRow("Class", "Extra", "Meth", "Unt", "Part", "Compl",
                "Disab", "Broken", "ToBeFixed", "KnownFail");
        ClassDoc[] classes = packageDoc.allClasses();
        Arrays.sort(classes, classComparator);
        int cnt = 0;
        int junitCnt = 0;
        for (ClassDoc clazz : classes) {
            cnt++;
            if (hasHideFlag(clazz)) {
            } else if (extendsJUnitTestCase(clazz)) {
                printer.printRow("ignored (junit class): " + clazz.name());
                junitCnt++;
            } else if (clazz.name().equals("AllTests")) {
                printer.printRow("ignored (junit test suite class): "
                        + clazz.name());
                junitCnt++;
            } else {
                ColorStat subStats = processClass(clazz);
                printStats(printer, subStats, true);
                stats.add(subStats);
            }
        }
        printer.printTableEnd();
        printer.printPlainLn("<h2>Target package summary</h2>");
        printer.printTableStart();
        printStats(printer, stats, false);
        printer.printTableEnd();
        closeFile(pr);
        if (junitCnt == cnt || packageDoc.name().contains("tests.")
                || packageDoc.name().contains("junit.")
                || packageDoc.name().contains(".testframework")
                || packageDoc.name().endsWith(".cts")) {
            stats = new ColorStat(packageDoc.name(),
                    getPackageBaseLink(packageDoc) + "/package.html");
            stats.incColor(TestMethodInformation.Color.GREEN);
            stats
                    .setExtra(junitCnt == cnt ? "Ignored since only Junit test and "
                            + "suites"
                            : "Ignored since \"tests.\" in name - recheck");
            stats.ignored = true;
        } else {
            stats.setExtra("" + cnt);
        }
        return stats;
    }
    private boolean hasHideFlag(Doc doc) {
        if (doc instanceof PackageDoc) {
            String comment = doc.getRawCommentText();
            return comment != null && comment.contains("@hide");
        } else {
            Tag[] hideTags = doc.tags("hide");
            return hideTags.length > 0;
        }
    }
    private ColorStat processTestClass(ClassDoc clazz) {
        String file = getPackageDir("testcoverage", clazz.containingPackage())
                + "/" + clazz.name() + ".html";
        PrintWriter pr = openFile(file, "Test class " + clazz.qualifiedName());
        TablePrinter printer = new TablePrinter(pr);
        ColorStat classStat = new ColorStat(clazz.name(), clazz.name()
                + ".html");
        TestTargetClass testTargetClass = getTargetClass(clazz);
        ClassDoc targetClass = testTargetClass.targetClass;
        String note = "Note:";
        if (targetClass == null) {
            note += "<br>targetClass annotation missing!<br>";
        } else {
            ClassOriginator co = new ClassOriginator(clazz, null);
            AnnotationDesc[] annotsC = testTargetClass.untestedMethods
                    .toArray(new AnnotationDesc[] {});
            if (annotsC.length > 0) {
                ColorStat classLevelStat = new ColorStat(clazz.name(), null);
                TestMethodInformation tminfo = new TestMethodInformation(co,
                        annotsC, targetClass);
                if (tminfo.getError() != null) {
                    printer.printPlainLn("<b>Error:</b>" + tminfo.getError());
                    classLevelStat.incColor(Color.RED);
                } else {
                    linkTargets(tminfo.getTargets());
                    classLevelStat.incColor(Color.GREEN);
                }
                classStat.add(classLevelStat);
            }
        }
        printer.printPlainLn(note);
        printer.printTableStart();
        printer.printRow("Method", "Note", "Meth", "Unt", "Part", "Compl",
                "Disab", "Broken", "ToBeFixed", "KnownFail");
        int methodCnt = 0;
        List<MethodDoc> testMethods = collectAllTestMethods(clazz);
        Collections.sort(testMethods, memberComparator);
        for (MethodDoc testMethod : testMethods) {
            methodCnt++;
            boolean disTest = testMethod.name().startsWith("_test");
            ColorStat methodStat = new ColorStat(testMethod.name(), null);
            if (disTest) {
                methodStat.incDisabledTestsCnt();
            }
            String comments = disTest ? "<b><span style=\"background:red\">"
                    + "DISABLED</span></b>" : null;
            MethodOriginator mo = new MethodOriginator(testMethod, clazz,
                    comments);
            AnnotationDesc[] annots = testMethod.annotations();
            TestMethodInformation minfo = new TestMethodInformation(mo, annots,
                    targetClass);
            linkTargets(minfo.getTargets());
            String extra = null;
            if (comments != null) {
                if (extra == null)
                    extra = "";
                extra += comments;
            }
            if (minfo.getError() != null) { 
                if (extra == null)
                    extra = "";
                extra += "<b>Error:</b> " + minfo.getError() + "<br>";
                methodStat.addMethodInfo(minfo);
            } else {
                if (mo.getKnownFailure() != null) {
                    methodStat.incKnownFailureCnt();
                    if (extra == null)
                        extra = "";
                    extra += mo.getKnownFailure();
                }
                if (mo.getBrokenTest() != null) {
                    methodStat.incBrokenTestCnt();
                    methodStat.incColor(Color.YELLOW);
                    if (extra == null)
                        extra = "";
                    extra += mo.getBrokenTest();
                }
                if (mo.getToBeFixed() != null) {
                    methodStat.incToBeFixedCnt();
                    methodStat.incColor(Color.YELLOW);
                    if (extra == null) {
                        extra = "";
                    }
                    extra += mo.getToBeFixed();
                } else { 
                    methodStat.addMethodInfo(minfo);
                }
            }
            if (extra != null) {
                methodStat.setExtra(extra);
            }
            printTestStats(printer, methodStat, false);
            classStat.add(methodStat);
        }
        printer.printTableEnd();
        printer.printPlainLn("<h2>Test class summary</h2>");
        printer.printTableStart();
        printStats(printer, classStat, false);
        printer.printTableEnd();
        closeFile(pr);
        classStat.setExtra("#methods: " + testMethods.size());
        return classStat;
    }
    private void linkTargets(List<TestTargetNew> targets) {
        for (TestTargetNew ttn : targets) {
            if (ttn.getTargetMethod() != null) {
                AnnotationPointer tar = getAnnotationPointer(ttn
                        .getTargetMethod(), true);
                tar.addTestTargetNew(ttn);
            } else if (ttn.getTargetClass() != null) {
                addToClassTargets(ttn.getTargetClass(), ttn);
            }
        }
    }
    private boolean isGreen(TestMethodInformation.Level level) {
        boolean lComplete = level == TestMethodInformation.Level.COMPLETE;
        boolean lSufficient = level == TestMethodInformation.Level.SUFFICIENT;
        boolean lPartialOk = level == TestMethodInformation.Level.PARTIAL_COMPLETE;
        boolean lPartial = level == TestMethodInformation.Level.PARTIAL;
        boolean lTodo = level == TestMethodInformation.Level.TODO;
        boolean lNotFeasible = level == TestMethodInformation.Level.NOT_FEASIBLE;
        boolean lNotNecessary = level == TestMethodInformation.Level.NOT_NECESSARY;
        return lComplete || lPartialOk || lSufficient || lNotFeasible
                || lNotNecessary;
    }
    private ColorStat processClass(ClassDoc clazz) {
        String file = getPackageDir("", clazz.containingPackage()) + "/"
                + clazz.name() + ".html";
        String classDesc = getClassString(clazz);
        PrintWriter pr = openFile(file, classDesc);
        TablePrinter printer = new TablePrinter(pr);
        printer.printPlain("<b>package " + clazz.containingPackage() + "</b>");
        ColorStat classStats = new ColorStat(classDesc, clazz.name() + ".html");
        List<TestTargetNew> classTargets = getTargetsFor(clazz);
        if (classTargets != null) {
            printer.printPlain("<h3>Class level tests</h3>");
            printer.printPlain("<ul>");
            for (TestTargetNew ttn : classTargets) {
                String line = "<li>" + ttn.getOriginator().asString();
                Level lev = ttn.getLevel();
                line += " <font color=\""
                        + (isGreen(lev) ? "green" : "red")
                        + "\"><b>"
                        + lev.name()
                        + "</b></font>"
                        + (ttn.getNotes() != null ? "<br>Notes: "
                                + ttn.getNotes() : "") + "</li>";
                printer.printPlain(line);
            }
            printer.printPlainLn("</ul>");
        }
        printer.printPlain("<h3>Method level tests</h3>");
        printer.printTableStart();
        printer.printRow("Method", "Tested by", "Meth", "Unt", "Part", "Compl",
                "Disab", "Broken", "ToBeFixed", "KnownFail");
        ConstructorDoc[] constructors = clazz.constructors();
        Arrays.sort(constructors, classComparator);
        int cnt = 0;
        for (ConstructorDoc constructor : constructors) {
            if (!hasHideFlag(constructor) && !hasHideFlag(clazz)) {
                cnt++;
                ColorStat memberStat = processElement(constructor);
                printStats(printer, memberStat, false);
                classStats.add(memberStat);
            }
        }
        MethodDoc[] methods = clazz.methods();
        Arrays.sort(methods, classComparator);
        for (MethodDoc method : methods) {
            if (!hasHideFlag(method) && !hasHideFlag(clazz)) {
                cnt++;
                ColorStat subStat = processElement(method);
                printStats(printer, subStat, false);
                classStats.add(subStat);
            }
        }
        printer.printTableEnd();
        printer.printPlainLn("<h2>Target class summary</h2>");
        printer.printTableStart();
        printStats(printer, classStats, false);
        printer.printTableEnd();
        closeFile(pr);
        classStats.setExtra("#methods: " + cnt);
        if (_ignoreInterfacesAndAbstractMethods && clazz.isInterface()) {
            classStats = new ColorStat(clazz.name()
                    + (clazz.isInterface() ? " (Interface)" : ""), clazz.name()
                    + ".html");
            int mcnt = clazz.methods().length;
            for (int i = 0; i < mcnt; i++) {
                classStats.incColor(TestMethodInformation.Color.GREEN);
            }
            classStats.setExtra("Ignored since interface");
        }
        return classStats;
    }
    private class TestTargetClass {
        ClassDoc targetClass;
        List<AnnotationDesc> untestedMethods = new ArrayList<AnnotationDesc>();
    }
    private TestTargetClass getTargetClass(ClassDoc classDoc) {
        TestTargetClass ttc = new TestTargetClass();
        ClassDoc targetClass = null;
        AnnotationDesc[] cAnnots = classDoc.annotations();
        for (AnnotationDesc cAnnot : cAnnots) {
            AnnotationTypeDoc atype = cAnnot.annotationType();
            if (atype.toString().equals("dalvik.annotation.TestTargetClass")) {
                ElementValuePair[] cpairs = cAnnot.elementValues();
                for (int i = 0; i < cpairs.length; i++) {
                    ElementValuePair ev = cpairs[i];
                    String elName = ev.element().name();
                    if (elName.equals("value")) {
                        AnnotationValue av = ev.value();
                        Object obj = av.value();
                        if (obj instanceof ClassDoc) {
                            targetClass = (ClassDoc)obj;
                        } else if (obj instanceof ParameterizedType) {
                            targetClass = ((ParameterizedType)obj).asClassDoc();
                        } else
                            throw new RuntimeException(
                                    "annotation elem value is of type "
                                            + obj.getClass().getName());
                    } else if (elName.equals("untestedMethods")) {
                        AnnotationValue[] targets = (AnnotationValue[])ev
                                .value().value();
                        for (AnnotationValue ttn : targets) {
                            AnnotationDesc ttnd = (AnnotationDesc)ttn.value();
                            ttc.untestedMethods.add(ttnd);
                        }
                    }
                }
            }
        }
        ttc.targetClass = targetClass;
        return ttc;
    }
    private List<MethodDoc> collectAllTestMethods(ClassDoc classDoc) {
        List<MethodDoc> m = new ArrayList<MethodDoc>();
        ClassDoc curCl = classDoc;
        do {
            m.addAll(getJunitTestMethods(curCl));
        } while ((curCl = curCl.superclass()) != null
                && !curCl.qualifiedName().equals("junit.framework.TestCase"));
        return m;
    }
    private List<MethodDoc> getJunitTestMethods(ClassDoc classDoc) {
        List<MethodDoc> cl = new ArrayList<MethodDoc>();
        for (MethodDoc methodDoc : classDoc.methods()) {
            if (methodDoc.isPublic()
                    && (methodDoc.name().startsWith("test") || methodDoc.name()
                            .startsWith("_test"))) {
                cl.add(methodDoc);
            }
        }
        return cl;
    }
    private class ColorStat {
        private String name;
        private String link;
        private String extra;
        public boolean ignored;
        private int[] cntCol = new int[4];
        private int disabledTestsCnt = 0;
        private int brokenTestCnt = 0;
        private int toBeFixedCnt = 0;
        private int knownFailureCnt = 0;
        public String getName() {
            return name;
        }
        public String getLink() {
            return link;
        }
        public ColorStat(String name, String link) {
            this.name = name;
            this.link = link;
        }
        public void add(ColorStat subStat) {
            for (int i = 0; i < cntCol.length; i++) {
                cntCol[i] += subStat.cntCol[i];
            }
            disabledTestsCnt += subStat.disabledTestsCnt;
            brokenTestCnt += subStat.brokenTestCnt;
            toBeFixedCnt += subStat.toBeFixedCnt;
            knownFailureCnt += subStat.knownFailureCnt;
        }
        public void incDisabledTestsCnt() {
            disabledTestsCnt++;
        }
        public void incBrokenTestCnt() {
            brokenTestCnt++;
        }
        public void incToBeFixedCnt() {
            toBeFixedCnt++;
        }
        public void incKnownFailureCnt() {
            knownFailureCnt++;
        }
        public void incColor(TestMethodInformation.Color color) {
            cntCol[color.ordinal()]++;
        }
        public int getColorCnt(TestMethodInformation.Color color) {
            return cntCol[color.ordinal()];
        }
        public void addMethodInfo(TestMethodInformation minfo) {
            TestMethodInformation.Color c = minfo.getColor();
            int ord = c.ordinal();
            cntCol[ord]++;
        }
        public void setExtra(String extra) {
            this.extra = extra;
        }
        public String getExtra() {
            return extra;
        }
        public int getDisabledTestsCnt() {
            return disabledTestsCnt;
        }
        public int getBrokenTestCnt() {
            return brokenTestCnt;
        }
        public int getToBeFixedCnt() {
            return toBeFixedCnt;
        }
        public int getKnownFailureCnt() {
            return knownFailureCnt;
        }
    }
    private AnnotationPointer getAnnotationPointer(
            ExecutableMemberDoc targetMethod, boolean create) {
        AnnotationPointer ap = resolved.get(targetMethod);
        if (create && ap == null) {
            ap = new AnnotationPointer(targetMethod);
            resolved.put(targetMethod, ap);
        }
        return ap;
    }
    private void addToClassTargets(ClassDoc targetClass, TestTargetNew ttn) {
        List<TestTargetNew> targets = classToSpecialTargets.get(targetClass);
        if (targets == null) {
            targets = new ArrayList<TestTargetNew>();
            classToSpecialTargets.put(targetClass, targets);
        }
        targets.add(ttn);
    }
    private List<TestTargetNew> getTargetsFor(ClassDoc targetClass) {
        return classToSpecialTargets.get(targetClass);
    }
    private boolean extendsJUnitTestCase(ClassDoc classDoc) {
        ClassDoc curClass = classDoc;
        while ((curClass = curClass.superclass()) != null) {
            if (curClass.toString().equals("junit.framework.TestCase")) {
                return true;
            }
        }
        return false;
    }
    private ColorStat processElement(ExecutableMemberDoc method) {
        if (DEBUG) System.out.println("Processing " + method);
        ColorStat memberStats = new ColorStat(getMethodString(method), null);
        TestMethodInformation.Color c = TestMethodInformation.Color.RED;
        if (_ignoreInterfacesAndAbstractMethods && method instanceof MethodDoc
                && ((MethodDoc)method).isAbstract()) {
            c = TestMethodInformation.Color.GREEN;
            memberStats.setExtra("ignored since abstract");
        } else {
            AnnotationPointer ap = getAnnotationPointer(method, false);
            int testedByCnt = 0;
            if (ap != null) {
                List<TestTargetNew> targets = ap.getTargets();
                testedByCnt = targets.size();
                if (testedByCnt == 0) {
                    throw new RuntimeException(
                            "existing annotation pointer with no entries!, "
                                    + "method:" + method);
                }
                String by = "<ul>";
                int completeTestCnt = 0;
                int partialOkTestCnt = 0;
                int partialTestCnt = 0;
                int todoTestCnt = 0;
                int notFeasableTestCnt = 0;
                int notNecessaryTestCnt = 0;
                int sufficientTestCnt = 0;
                for (TestTargetNew target : targets) {
                    Originator originator = target.getOriginator();
                    boolean disabledTest = originator.isDisabled();
                    boolean brokenTest = originator.getBrokenTest() != null;
                    boolean toBeFixed = originator.getToBeFixed() != null;
                    boolean knownFailure = originator.getKnownFailure() != null;
                    by += "<li>" + originator.asString();
                    TestMethodInformation.Level lev;
                    if (target.isHavingProblems()) {
                        lev = TestMethodInformation.Level.TODO;
                    } else {
                        lev = target.getLevel();
                    }
                    if (disabledTest) {
                        memberStats.incDisabledTestsCnt();
                    }
                    if (brokenTest) {
                        memberStats.incBrokenTestCnt();
                    }
                    if (toBeFixed) {
                        memberStats.incToBeFixedCnt();
                    }
                    if (knownFailure) {
                        memberStats.incKnownFailureCnt();
                    }
                    boolean lComplete = lev == TestMethodInformation.Level.COMPLETE;
                    boolean lSufficient = lev == TestMethodInformation.Level.SUFFICIENT;
                    boolean lPartialOk = lev == TestMethodInformation.Level.PARTIAL_COMPLETE;
                    boolean lPartial = lev == TestMethodInformation.Level.PARTIAL;
                    boolean lTodo = lev == TestMethodInformation.Level.TODO;
                    boolean lNotFeasible = lev == TestMethodInformation.Level.NOT_FEASIBLE;
                    boolean lNotNecessary = lev == TestMethodInformation.Level.NOT_NECESSARY;
                    by += " <font color=\""
                            + (lComplete || lPartialOk || lSufficient
                                    || lNotFeasible || lNotNecessary ? "green"
                                    : "red")
                            + "\"><b>"
                            + lev.name()
                            + "</b></font>"
                            + (target.getNotes() != null ? "<br>Notes: "
                                    + target.getNotes() : "");
                    if ((_doIncludeDisabledTests || !disabledTest)
                            && (!brokenTest) && (!toBeFixed)) {
                        if (lComplete) {
                            completeTestCnt++;
                        } else if (lPartialOk) {
                            partialOkTestCnt++;
                        } else if (lPartial) {
                            partialTestCnt++;
                        } else if (lTodo) {
                            todoTestCnt++;
                        } else if (lSufficient) {
                            sufficientTestCnt++;
                        } else if (lNotFeasible) {
                            notFeasableTestCnt++;
                        } else if (lNotNecessary) {
                            notNecessaryTestCnt++;
                        }
                    }
                    if (toBeFixed) {
                        partialTestCnt++;
                    }
                    if (DEBUG) {
                        System.out.println("completeTestCnt: " + completeTestCnt
                            + ", partialOkTestCnt: " + partialOkTestCnt
                            + ", partialTestCnt: " + partialTestCnt
                            + ", todoTestCnt: " + todoTestCnt
                            + ", sufficientTestCnt: " + sufficientTestCnt
                            + ", notFeasableTestCnt: " + notFeasableTestCnt
                            + ", notNecessaryTestCnt: " + notNecessaryTestCnt);
                    }
                    by += "</li>";
                }
                String warnings = "";
                int singularTestCnt = notFeasableTestCnt + notNecessaryTestCnt;
                boolean isAbstract = (method.containingClass().isInterface() ||
                        (method instanceof MethodDoc) && ((MethodDoc)method).isAbstract());
                if (_acceptCompleteWithOtherStatus
                        && (completeTestCnt > 0 || sufficientTestCnt > 0)) {
                    c = TestMethodInformation.Color.GREEN;
                } else if (_acceptCompleteWithOtherStatus
                        && (partialOkTestCnt > 1)) {
                    c = TestMethodInformation.Color.GREEN;
                } else {
                    if (singularTestCnt > 0) {
                        if (targets.size() > singularTestCnt) {
                            c = TestMethodInformation.Color.RED;
                            warnings += "<b>WARNING:</b>NOT_FEASIBLE or "
                                    + "NOT_NECESSARY together with other "
                                    + "status!<br>";
                        } else {
                            if (notNecessaryTestCnt > 0
                                    && notFeasableTestCnt > 0) {
                                warnings += "<b>WARNING:</b>both NOT_FEASIBLE "
                                        + "and NOT_NECESSARY together!<br>";
                                c = TestMethodInformation.Color.RED;
                            } else { 
                                c = TestMethodInformation.Color.GREEN;
                            }
                        }
                    } else if (todoTestCnt > 0) {
                        c = TestMethodInformation.Color.RED;
                    } else if (partialTestCnt > 0) {
                        c = TestMethodInformation.Color.YELLOW;
                        if (completeTestCnt > 0 || sufficientTestCnt > 0) {
                            if (_acceptCompleteWithOtherStatus) {
                                c = TestMethodInformation.Color.GREEN;
                            } else if (completeTestCnt > 0) {
                                warnings += "<b>WARNING</b>: mixed PARTIAL "
                                        + "and COMPLETE status<br>";
                            }
                        }
                    } else if (partialOkTestCnt > 0 || sufficientTestCnt > 0) {
                        c = TestMethodInformation.Color.GREEN;
                        if (partialOkTestCnt == 1) {
                            warnings += "<b>WARNING</b>: only one "
                                    + "PARTIAL_COMPLETE status<br>";
                            c = TestMethodInformation.Color.YELLOW;
                        }
                    } else if (completeTestCnt > 0 || singularTestCnt == 1) {
                        c = TestMethodInformation.Color.GREEN;
                    }
                    if (completeTestCnt > 1 && !isAbstract
                            && !_acceptCompleteWithOtherStatus) {
                        warnings += "<b>WARNING</b>: more than one "
                                + "COMPLETE status<br>";
                        if (c != TestMethodInformation.Color.RED) {
                            c = TestMethodInformation.Color.YELLOW;
                        }
                    }
                }
                by = warnings + by;
                memberStats.setExtra(by);
            } else { 
                if (method.isConstructor() && method.signature().equals("()")) {
                    if (method.position() != null) {
                        String constPos = method.position().toString();
                        String classPos = method.containingClass().position()
                                .toString();
                        if (constPos.equals(classPos)) {
                            c = TestMethodInformation.Color.GREEN;
                            memberStats
                                    .setExtra("automatically marked green "
                                            + "since implicit default "
                                            + "constructor");
                        }
                    } else {
                        System.err.println("warning: doclet returned null for "
                                + "source position: method:" + method);
                    }
                } else if (method.containingClass().superclass() != null
                        && method.containingClass().superclass()
                                .qualifiedName().equals("java.lang.Enum")) {
                    String sig = method.name() + method.signature();
                    if (sig.equals("valueOf(java.lang.String)")
                            || sig.equals("values()")) {
                        c = TestMethodInformation.Color.GREEN;
                        memberStats
                                .setExtra("automatically marked green since "
                                        + "generated by compiler for enums");
                    }
                }
            }
        }
        memberStats.incColor(c);
        return memberStats;
    }
    private String getMethodString(ExecutableMemberDoc method) {
        String methodDesc = (method.isPublic() ? "public " : method
                .isProtected() ? "protected " : method.isPrivate() ? "private "
                : "");
        return methodDesc + "<b>" + method.name() + "</b> "
                + method.signature();
    }
    private String getClassString(ClassDoc clazz) {
        return (clazz.isPublic() ? "public "
                : clazz.isProtected() ? "protected "
                        : clazz.isPrivate() ? "private " : "")
                + (clazz.isInterface() ? "interface" : "class")
                + " "
                + clazz.name();
    }
    private void printTestStats(TablePrinter printer, ColorStat stat,
            boolean wantLink) {
        printStats(printer, stat, wantLink);
    }
    private void printStats(TablePrinter printer, ColorStat stat,
            boolean wantLink) {
        int redCnt = stat.getColorCnt(TestMethodInformation.Color.RED);
        int yellowCnt = stat.getColorCnt(TestMethodInformation.Color.YELLOW);
        int greenCnt = stat.getColorCnt(TestMethodInformation.Color.GREEN);
        int disabledCnt = stat.getDisabledTestsCnt();
        int brokenTestCnt = stat.getBrokenTestCnt();
        int toBeFixedCnt = stat.getToBeFixedCnt();
        int knownFailureCnt = stat.getKnownFailureCnt();
        int total = redCnt + yellowCnt + greenCnt;
        String link = stat.getLink();
        String namePart;
        if (wantLink && link != null) {
            namePart = "<a href=\"" + link + "\">" + stat.getName() + "</a>";
        } else {
            namePart = stat.getName();
        }
        String extra = stat.getExtra() == null ? "" : stat.getExtra();
        int totalDots = 120;
        float toP = total == 0 ? 0 : (((float)totalDots) / total);
        int redD = (int)(toP * redCnt);
        if (redD == 0 && redCnt > 0) {
            redD = 1;
        }
        int yellowD = (int)(toP * yellowCnt);
        if (yellowD == 0 && yellowCnt > 0) {
            yellowD = 1;
        }
        int greenD = totalDots - redD - yellowD; 
        printer.printRow(namePart, extra, "" + total, "" + redCnt, ""
                + yellowCnt, "" + greenCnt, "" + disabledCnt, ""
                + brokenTestCnt, "" + toBeFixedCnt, "" + knownFailureCnt, ""
                + (redCnt == 0 ? "" : "<span style=\"background:"
                        + COLORS[TestMethodInformation.Color.RED.ordinal()]
                        + "\">" + getDots(redD) + "</span>")
                + (yellowCnt == 0 ? "" : "<span style=\"background:"
                        + COLORS[TestMethodInformation.Color.YELLOW.ordinal()]
                        + "\">" + getDots(yellowD) + "</span>")
                + (greenCnt == 0 && total > 0 ? ""
                        : "<span style=\"background:"
                                + COLORS[TestMethodInformation.Color.GREEN
                                        .ordinal()] + "\">" + getDots(greenD)
                                + "</span>")
                + "&nbsp;&nbsp;&nbsp;<span style=\"background:blue\">"
                + getDots(total / 10) + "</span>");
    }
    private String getDots(int cnt) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < cnt; i++) {
            sb.append("&nbsp;");
        }
        return sb.toString();
    }
    private String getPackageBaseLink(PackageDoc pack) {
        return pack.name().replace('.', '/');
    }
    private File getPackageDir(String prefix, PackageDoc pack) {
        if (pack == null || pack.name() == null || "".equals(pack.name())) {
            return new File(prefix + "/" + ".");
        } else {
            return new File(prefix + "/" + pack.name().replace('.', '/'));
        }
    }
    public static int optionLength(String option) {
        if ("-d".equals(option)) {
            return 2;
        }
        if ("-f".equals(option)) {
            return 2;
        } else {
            return 0;
        }
    }
    private static String getOption(RootDoc root, String option, int index,
            String defValue) {
        String[][] allOptions = root.options();
        for (int i = 0; i < allOptions.length; i++) {
            if (allOptions[i][0].equals(option)) {
                return allOptions[i][index];
            }
        }
        return defValue;
    }
    public static LanguageVersion languageVersion() {
        return LanguageVersion.JAVA_1_5;
    }
    public static boolean start(RootDoc root) {
        try {
            String target = getOption(root, "-d", 1, ".");
            TestCoverageDoclet doclet = new TestCoverageDoclet(target);
            doclet.process(root);
            File file = new File(target, "index.html");
            System.out.println("Please see complete report in " + 
                    file.getAbsolutePath());
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }
}
