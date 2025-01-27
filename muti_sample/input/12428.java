public class PathOps {
    static final java.io.PrintStream out = System.out;
    private String input;
    private Path path;
    private Exception exc;
    private PathOps(String first, String... more) {
        out.println();
        input = first;
        try {
            path = FileSystems.getDefault().getPath(first, more);
            out.format("%s -> %s", first, path);
        } catch (Exception x) {
            exc = x;
            out.format("%s -> %s", first, x);
        }
        out.println();
    }
    Path path() {
        return path;
    }
    void fail() {
        throw new RuntimeException("PathOps failed");
    }
    void checkPath() {
        if (path == null) {
            throw new InternalError("path is null");
        }
    }
    void check(Object result, String expected) {
        out.format("\tExpected: %s\n", expected);
        out.format("\tActual: %s\n",  result);
        if (result == null) {
            if (expected == null) return;
        } else {
            if (expected != null && result.toString().equals(expected.toString()))
                return;
        }
        fail();
    }
    void check(Object result, boolean expected) {
        check(result, Boolean.toString(expected));
    }
    PathOps root(String expected) {
        out.println("check root");
        checkPath();
        check(path.getRoot(), expected);
        return this;
    }
    PathOps parent(String expected) {
        out.println("check parent");
        checkPath();
        check(path.getParent(), expected);
        return this;
    }
    PathOps name(String expected) {
        out.println("check name");
        checkPath();
        check(path.getFileName(), expected);
        return this;
    }
    PathOps element(int index, String expected) {
        out.format("check element %d\n", index);
        checkPath();
        check(path.getName(index), expected);
        return this;
    }
    PathOps subpath(int startIndex, int endIndex, String expected) {
        out.format("test subpath(%d,%d)\n", startIndex, endIndex);
        checkPath();
        check(path.subpath(startIndex, endIndex), expected);
        return this;
    }
    PathOps starts(String prefix) {
        out.format("test startsWith with %s\n", prefix);
        checkPath();
        Path s = FileSystems.getDefault().getPath(prefix);
        check(path.startsWith(s), true);
        return this;
    }
    PathOps notStarts(String prefix) {
        out.format("test not startsWith with %s\n", prefix);
        checkPath();
        Path s = FileSystems.getDefault().getPath(prefix);
        check(path.startsWith(s), false);
        return this;
    }
    PathOps ends(String suffix) {
        out.format("test endsWith %s\n", suffix);
        checkPath();
        Path s = FileSystems.getDefault().getPath(suffix);
        check(path.endsWith(s), true);
        return this;
    }
    PathOps notEnds(String suffix) {
        out.format("test not endsWith %s\n", suffix);
        checkPath();
        Path s = FileSystems.getDefault().getPath(suffix);
        check(path.endsWith(s), false);
        return this;
    }
    PathOps absolute() {
        out.println("check path is absolute");
        checkPath();
        check(path.isAbsolute(), true);
        return this;
    }
    PathOps notAbsolute() {
        out.println("check path is not absolute");
        checkPath();
        check(path.isAbsolute(), false);
        return this;
    }
    PathOps resolve(String other, String expected) {
        out.format("test resolve %s\n", other);
        checkPath();
        check(path.resolve(other), expected);
        return this;
    }
    PathOps resolveSibling(String other, String expected) {
        out.format("test resolveSibling %s\n", other);
        checkPath();
        check(path.resolveSibling(other), expected);
        return this;
    }
    PathOps relativize(String other, String expected) {
        out.format("test relativize %s\n", other);
        checkPath();
        Path that = FileSystems.getDefault().getPath(other);
        check(path.relativize(that), expected);
        return this;
    }
    PathOps normalize(String expected) {
        out.println("check normalized path");
        checkPath();
        check(path.normalize(), expected);
        return this;
    }
    PathOps string(String expected) {
        out.println("check string representation");
        checkPath();
        check(path, expected);
        return this;
    }
    PathOps invalid() {
        if (!(exc instanceof InvalidPathException)) {
            out.println("InvalidPathException not thrown as expected");
            fail();
        }
        return this;
    }
    static PathOps test(String first, String... more) {
        return new PathOps(first, more);
    }
    static void header(String s) {
        out.println();
        out.println();
        out.println("-- " + s + " --");
    }
    static void doWindowsTests() {
        header("Windows specific tests");
        test("C:\\")
            .string("C:\\");
        test("C:\\", "")
            .string("C:\\");
        test("C:\\", "foo")
            .string("C:\\foo");
        test("C:\\", "\\foo")
            .string("C:\\foo");
        test("C:\\", "foo\\")
            .string("C:\\foo");
        test("foo", "bar", "gus")
            .string("foo\\bar\\gus");
        test("")
            .string("");
        test("", "C:\\")
            .string("C:\\");
        test("", "foo", "", "bar", "", "\\gus")
            .string("foo\\bar\\gus");
        test("C:\\a\\b\\c")
            .root("C:\\")
            .parent("C:\\a\\b")
            .name("c");
        test("C:a\\b\\c")
            .root("C:")
            .parent("C:a\\b")
            .name("c");
        test("\\\\server\\share\\a")
            .root("\\\\server\\share\\")
            .parent("\\\\server\\share\\")
            .name("a");
        test("C:\\")
            .root("C:\\")
            .parent(null)
            .name(null);
        test("C:")
            .root("C:")
            .parent(null)
            .name(null);
        test("\\\\server\\share\\")
            .root("\\\\server\\share\\")
            .parent(null)
            .name(null);
        test("a\\b")
            .root(null)
            .parent("a")
            .name("b");
        test("foo")
            .root(null)
            .parent(null)
            .name("foo");
        test("")
            .root(null)
            .parent(null)
            .name("");
        test("C:\\")
            .starts("C:\\")
            .starts("c:\\")
            .notStarts("C")
            .notStarts("C:")
            .notStarts("");
        test("C:")
            .starts("C:")
            .starts("c:")
            .notStarts("C")
            .notStarts("");
        test("\\")
            .starts("\\");
        test("C:\\foo\\bar")
            .starts("C:\\")
            .starts("C:\\foo")
            .starts("C:\\FOO")
            .starts("C:\\foo\\bar")
            .starts("C:\\Foo\\Bar")
            .notStarts("C:")
            .notStarts("C")
            .notStarts("C:foo")
            .notStarts("");
        test("\\foo\\bar")
            .starts("\\")
            .starts("\\foo")
            .starts("\\foO")
            .starts("\\foo\\bar")
            .starts("\\fOo\\BaR")
            .notStarts("foo")
            .notStarts("foo\\bar")
            .notStarts("");
        test("foo\\bar")
            .starts("foo")
            .starts("foo\\bar")
            .notStarts("\\")
            .notStarts("");
        test("\\\\server\\share")
            .starts("\\\\server\\share")
            .starts("\\\\server\\share\\")
            .notStarts("\\")
            .notStarts("");
        test("")
            .starts("")
            .notStarts("\\");
        test("C:\\")
            .ends("C:\\")
            .ends("c:\\")
            .notEnds("\\")
            .notEnds("");
        test("C:")
            .ends("C:")
            .ends("c:")
            .notEnds("");
        test("\\")
            .ends("\\")
            .notEnds("");
        test("C:\\foo\\bar")
            .ends("bar")
            .ends("BAR")
            .ends("foo\\bar")
            .ends("Foo\\Bar")
            .ends("C:\\foo\\bar")
            .ends("c:\\foO\\baR")
            .notEnds("r")
            .notEnds("\\foo\\bar")
            .notEnds("");
        test("\\foo\\bar")
            .ends("bar")
            .ends("BaR")
            .ends("foo\\bar")
            .ends("foO\\baR")
            .ends("\\foo\\bar")
            .ends("\\Foo\\Bar")
            .notEnds("oo\\bar")
            .notEnds("");
        test("foo\\bar")
            .ends("bar")
            .ends("BAR")
            .ends("foo\\bar")
            .ends("Foo\\Bar")
            .notEnds("ar")
            .notEnds("");
        test("\\\\server\\share")
            .ends("\\\\server\\share")
            .ends("\\\\server\\share\\")
            .notEnds("shared")
            .notEnds("\\")
            .notEnds("");
        test("")
            .ends("")
            .notEnds("\\");
        test("C:\\a\\b\\c")
            .element(0, "a")
            .element(1, "b")
            .element(2, "c");
        test("foo.bar\\gus.alice")
            .element(0, "foo.bar")
            .element(1, "gus.alice");
        test("")
            .element(0, "");
        test("C:\\foo")
            .subpath(0, 1, "foo");
        test("C:foo")
            .subpath(0, 1, "foo");
        test("foo")
            .subpath(0, 1, "foo");
        test("C:\\foo\\bar\\gus")
            .subpath(0, 1, "foo")
            .subpath(0, 2, "foo\\bar")
            .subpath(0, 3, "foo\\bar\\gus")
            .subpath(1, 2, "bar")
            .subpath(1, 3, "bar\\gus")
            .subpath(2, 3, "gus");
        test("\\\\server\\share\\foo")
            .subpath(0, 1, "foo");
        test("")
            .subpath(0, 1, "");
        test("foo").notAbsolute();
        test("C:").notAbsolute();
        test("C:\\").absolute();
        test("C:\\abc").absolute();
        test("\\\\server\\share\\").absolute();
        test("").notAbsolute();
        test("C:\\")
            .resolve("foo", "C:\\foo")
            .resolve("D:\\bar", "D:\\bar")
            .resolve("\\\\server\\share\\bar", "\\\\server\\share\\bar")
            .resolve("C:foo", "C:\\foo")
            .resolve("D:foo", "D:foo")
            .resolve("", "C:\\");
        test("\\")
            .resolve("foo", "\\foo")
            .resolve("D:bar", "D:bar")
            .resolve("C:\\bar", "C:\\bar")
            .resolve("\\\\server\\share\\bar", "\\\\server\\share\\bar")
            .resolve("\\foo", "\\foo")
            .resolve("", "\\");
        test("\\foo")
            .resolve("bar", "\\foo\\bar")
            .resolve("D:bar", "D:bar")
            .resolve("C:\\bar", "C:\\bar")
            .resolve("\\\\server\\share\\bar", "\\\\server\\share\\bar")
            .resolve("\\bar", "\\bar")
            .resolve("", "\\foo");
        test("foo")
            .resolve("bar", "foo\\bar")
            .resolve("D:\\bar", "D:\\bar")
            .resolve("\\\\server\\share\\bar", "\\\\server\\share\\bar")
            .resolve("C:bar", "C:bar")
            .resolve("D:foo", "D:foo")
            .resolve("", "foo");
        test("C:")
            .resolve("foo", "C:foo")
            .resolve("", "C:");
        test("\\\\server\\share\\foo")
            .resolve("bar", "\\\\server\\share\\foo\\bar")
            .resolve("\\bar", "\\\\server\\share\\bar")
            .resolve("D:\\bar", "D:\\bar")
            .resolve("\\\\other\\share\\bar", "\\\\other\\share\\bar")
            .resolve("D:bar", "D:bar")
            .resolve("", "\\\\server\\share\\foo");
        test("")
            .resolve("", "")
            .resolve("foo", "foo")
            .resolve("C:\\", "C:\\")
            .resolve("C:foo", "C:foo")
            .resolve("\\\\server\\share\\bar", "\\\\server\\share\\bar");
        test("foo")
            .resolveSibling("bar", "bar")
            .resolveSibling("D:\\bar", "D:\\bar")
            .resolveSibling("\\\\server\\share\\bar", "\\\\server\\share\\bar")
            .resolveSibling("C:bar", "C:bar")
            .resolveSibling("D:foo", "D:foo")
            .resolveSibling("", "");
        test("foo\\bar")
            .resolveSibling("gus", "foo\\gus")
            .resolveSibling("D:\\bar", "D:\\bar")
            .resolveSibling("\\\\server\\share\\bar", "\\\\server\\share\\bar")
            .resolveSibling("C:bar", "C:bar")
            .resolveSibling("D:foo", "D:foo")
            .resolveSibling("", "foo");
        test("C:\\foo")
            .resolveSibling("gus", "C:\\gus")
            .resolveSibling("D:\\bar", "D:\\bar")
            .resolveSibling("\\\\server\\share\\bar", "\\\\server\\share\\bar")
            .resolveSibling("C:bar", "C:\\bar")
            .resolveSibling("D:foo", "D:foo")
            .resolveSibling("", "C:\\");
        test("C:\\foo\\bar")
            .resolveSibling("gus", "C:\\foo\\gus")
            .resolveSibling("D:\\bar", "D:\\bar")
            .resolveSibling("\\\\server\\share\\bar", "\\\\server\\share\\bar")
            .resolveSibling("C:bar", "C:\\foo\\bar")
            .resolveSibling("D:foo", "D:foo")
            .resolveSibling("", "C:\\foo");
        test("\\\\server\\share\\foo")
            .resolveSibling("bar", "\\\\server\\share\\bar")
            .resolveSibling("\\bar", "\\\\server\\share\\bar")
            .resolveSibling("D:\\bar", "D:\\bar")
            .resolveSibling("\\\\other\\share\\bar", "\\\\other\\share\\bar")
            .resolveSibling("D:bar", "D:bar")
            .resolveSibling("", "\\\\server\\share\\");
        test("")
            .resolveSibling("", "")
            .resolveSibling("foo", "foo")
            .resolveSibling("C:\\", "C:\\");
        test("foo\\bar")
            .relativize("foo\\bar", "")
            .relativize("foo", "..");
        test("C:\\a\\b\\c")
            .relativize("C:\\a", "..\\..")
            .relativize("C:\\a\\b\\c", "");
        test("\\\\server\\share\\foo")
            .relativize("\\\\server\\share\\bar", "..\\bar")
            .relativize("\\\\server\\share\\foo", "");
        test("")
            .relativize("", "");
        test("C:\\")
            .normalize("C:\\");
        test("C:\\.")
            .normalize("C:\\");
        test("C:\\..")
            .normalize("C:\\");
        test("\\\\server\\share")
            .normalize("\\\\server\\share\\");
        test("\\\\server\\share\\.")
            .normalize("\\\\server\\share\\");
        test("\\\\server\\share\\..")
            .normalize("\\\\server\\share\\");
        test("C:")
            .normalize("C:");
        test("C:.")
            .normalize("C:");
        test("C:..")
            .normalize("C:..");
        test("\\")
            .normalize("\\");
        test("\\.")
            .normalize("\\");
        test("\\..")
            .normalize("\\");
        test("foo")
            .normalize("foo");
        test("foo\\.")
            .normalize("foo");
        test("foo\\..")
            .normalize("");
        test("C:\\foo")
            .normalize("C:\\foo");
        test("C:\\foo\\.")
            .normalize("C:\\foo");
        test("C:\\.\\foo")
            .normalize("C:\\foo");
        test("C:\\foo\\..")
            .normalize("C:\\");
        test("C:\\..\\foo")
            .normalize("C:\\foo");
        test("\\\\server\\share\\foo")
            .normalize("\\\\server\\share\\foo");
        test("\\\\server\\share\\foo\\.")
            .normalize("\\\\server\\share\\foo");
        test("\\\\server\\share\\.\\foo")
            .normalize("\\\\server\\share\\foo");
        test("\\\\server\\share\\foo\\..")
            .normalize("\\\\server\\share\\");
        test("\\\\server\\share\\..\\foo")
            .normalize("\\\\server\\share\\foo");
        test("C:foo")
            .normalize("C:foo");
        test("C:foo\\.")
            .normalize("C:foo");
        test("C:.\\foo")
            .normalize("C:foo");
        test("C:foo\\..")
            .normalize("C:");
        test("C:..\\foo")
            .normalize("C:..\\foo");
        test("\\foo")
            .normalize("\\foo");
        test("\\foo\\.")
            .normalize("\\foo");
        test("\\.\\foo")
            .normalize("\\foo");
        test("\\foo\\..")
            .normalize("\\");
        test("\\..\\foo")
            .normalize("\\foo");
        test(".")
            .normalize("");
        test("..")
            .normalize("..");
        test("\\..\\..")
            .normalize("\\");
        test("..\\..\\foo")
            .normalize("..\\..\\foo");
        test("foo\\bar\\..")
            .normalize("foo");
        test("foo\\bar\\.\\..")
            .normalize("foo");
        test("foo\\bar\\gus\\..\\..")
            .normalize("foo");
        test(".\\foo\\.\\bar\\.\\gus\\..\\.\\..")
            .normalize("foo");
        test("")
            .normalize("");
        test("\\\\server\\share\\")
            .root("\\\\server\\share\\")
            .parent(null)
            .name(null);
        test("\\\\server")
            .invalid();
        test("\\\\server\\")
            .invalid();
        test("\\\\server\\share")
            .root("\\\\server\\share\\")
            .parent(null)
            .name(null);
        test(":\\foo")
            .invalid();
        test("C::")
            .invalid();
        test("C:\\?")           
            .invalid();
        test("C:\\*")           
            .invalid();
        test("C:\\abc\u0001\\foo")
            .invalid();
        test("C:\\\u0019\\foo")
            .invalid();
        test("\\\\server\u0019\\share")
            .invalid();
        test("\\\\server\\share\u0019")
            .invalid();
        test("foo\u0000\bar")
            .invalid();
        test("C:\\foo ")                
             .invalid();
        test("C:\\foo \\bar")
            .invalid();
        test("C:/a/b/c")
            .string("C:\\a\\b\\c")
            .root("C:\\")
            .parent("C:\\a\\b");
        test("C:
            .string("C:\\a\\b\\c")
            .root("C:\\")
            .parent("C:\\a\\b");
        header("hashCode");
        int h1 = test("C:\\foo").path().hashCode();
        int h2 = test("c:\\FOO").path().hashCode();
        if (h1 != h2)
            throw new RuntimeException("PathOps failed");
    }
    static void doUnixTests() {
        header("Unix specific tests");
        test("/")
            .string("/");
        test("/", "")
            .string("/");
        test("/", "foo")
            .string("/foo");
        test("/", "/foo")
            .string("/foo");
        test("/", "foo/")
            .string("/foo");
        test("foo", "bar", "gus")
            .string("foo/bar/gus");
        test("")
            .string("");
        test("", "/")
            .string("/");
        test("", "foo", "", "bar", "", "/gus")
            .string("foo/bar/gus");
        test("/a/b/c")
            .root("/")
            .parent("/a/b")
            .name("c");
        test("/")
            .root("/")
            .parent(null)
            .name(null);
        test("a/b")
            .root(null)
            .parent("a")
            .name("b");
        test("foo")
            .root(null)
            .parent(null)
            .name("foo");
        test("")
             .root(null)
             .parent(null)
             .name("");
        test("/")
            .starts("/")
            .notStarts("")
            .notStarts("/foo");
        test("/foo")
            .starts("/")
            .starts("/foo")
            .notStarts("/f");
        test("/foo/bar")
            .starts("/")
            .starts("/foo")
            .starts("/foo/bar")
            .notStarts("/f")
            .notStarts("foo")
            .notStarts("foo/bar");
        test("foo")
            .starts("foo")
            .notStarts("")
            .notStarts("f");
        test("foo/bar")
            .starts("foo")
            .starts("foo/bar")
            .notStarts("f")
            .notStarts("/foo")
            .notStarts("/foo/bar");
        test("")
             .starts("")
             .notStarts("/");
        test("/")
            .ends("/")
            .notEnds("")
            .notEnds("foo")
            .notEnds("/foo");
        test("/foo")
            .ends("foo")
            .ends("/foo")
            .notEnds("fool");
        test("/foo/bar")
            .ends("bar")
            .ends("foo/bar")
            .ends("/foo/bar")
            .notEnds("ar")
            .notEnds("barack")
            .notEnds("/bar")
            .notEnds("o/bar");
        test("foo")
            .ends("foo")
            .notEnds("")
            .notEnds("oo")
            .notEnds("oola");
        test("foo/bar")
            .ends("bar")
            .ends("foo/bar")
            .notEnds("r")
            .notEnds("barmaid")
            .notEnds("/bar");
        test("foo/bar/gus")
            .ends("gus")
            .ends("bar/gus")
            .ends("foo/bar/gus")
            .notEnds("g")
            .notEnds("/gus")
            .notEnds("r/gus")
            .notEnds("barack/gus")
            .notEnds("bar/gust");
        test("")
            .ends("")
            .notEnds("/");
        test("a/b/c")
            .element(0, "a")
            .element(1, "b")
            .element(2, "c");
        test("")
            .element(0, "");
        test("/foo")
            .subpath(0, 1, "foo");
        test("foo")
            .subpath(0, 1, "foo");
        test("/foo/bar")
            .subpath(0, 1, "foo")
            .subpath(1, 2, "bar")
            .subpath(0, 2, "foo/bar");
        test("foo/bar")
            .subpath(0, 1, "foo")
            .subpath(1, 2, "bar")
            .subpath(0, 2, "foo/bar");
        test("/foo/bar/gus")
            .subpath(0, 1, "foo")
            .subpath(1, 2, "bar")
            .subpath(2, 3, "gus")
            .subpath(0, 2, "foo/bar")
            .subpath(1, 3, "bar/gus")
            .subpath(0, 3, "foo/bar/gus");
        test("foo/bar/gus")
            .subpath(0, 1, "foo")
            .subpath(1, 2, "bar")
            .subpath(2, 3, "gus")
            .subpath(0, 2, "foo/bar")
            .subpath(1, 3, "bar/gus")
            .subpath(0, 3, "foo/bar/gus");
        test("")
            .subpath(0, 1, "");
        test("/")
            .absolute();
        test("/tmp")
            .absolute();
        test("tmp")
            .notAbsolute();
        test("")
            .notAbsolute();
        test("/tmp")
            .resolve("foo", "/tmp/foo")
            .resolve("/foo", "/foo")
            .resolve("", "/tmp");
        test("tmp")
            .resolve("foo", "tmp/foo")
            .resolve("/foo", "/foo")
            .resolve("", "tmp");
        test("")
            .resolve("", "")
            .resolve("foo", "foo")
            .resolve("/foo", "/foo");
        test("foo")
            .resolveSibling("bar", "bar")
            .resolveSibling("/bar", "/bar")
            .resolveSibling("", "");
        test("foo/bar")
            .resolveSibling("gus", "foo/gus")
            .resolveSibling("/gus", "/gus")
            .resolveSibling("", "foo");
        test("/foo")
            .resolveSibling("gus", "/gus")
            .resolveSibling("/gus", "/gus")
            .resolveSibling("", "/");
        test("/foo/bar")
            .resolveSibling("gus", "/foo/gus")
            .resolveSibling("/gus", "/gus")
            .resolveSibling("", "/foo");
        test("")
            .resolveSibling("foo", "foo")
            .resolveSibling("/foo", "/foo")
            .resolve("", "");
        test("/a/b/c")
            .relativize("/a/b/c", "")
            .relativize("/a/b/c/d/e", "d/e")
            .relativize("/a/x", "../../x")
            .relativize("/x", "../../../x");
        test("a/b/c")
            .relativize("a/b/c/d", "d")
            .relativize("a/x", "../../x")
            .relativize("x", "../../../x")
            .relativize("", "../../..");
        test("")
            .relativize("a", "a")
            .relativize("a/b/c", "a/b/c")
            .relativize("", "");
        test("/")
            .normalize("/");
        test("foo")
            .normalize("foo");
        test("/foo")
            .normalize("/foo");
        test(".")
            .normalize("");
        test("..")
            .normalize("..");
        test("/..")
            .normalize("/");
        test("/../..")
            .normalize("/");
        test("foo/.")
            .normalize("foo");
        test("./foo")
            .normalize("foo");
        test("foo/..")
            .normalize("");
        test("../foo")
            .normalize("../foo");
        test("../../foo")
            .normalize("../../foo");
        test("foo/bar/..")
            .normalize("foo");
        test("foo/bar/gus/../..")
            .normalize("foo");
        test("/foo/bar/gus/../..")
            .normalize("/foo");
        test("foo\u0000bar")
            .invalid();
        test("\u0000foo")
            .invalid();
        test("bar\u0000")
            .invalid();
        test("
            .invalid();
        test("
            .invalid();
        test("
            .invalid();
        test("
            .string("/foo/bar")
            .root("/")
            .parent("/foo")
            .name("bar");
    }
    static void npes() {
        header("NullPointerException");
        Path path = FileSystems.getDefault().getPath("foo");
        try {
            path.resolve((String)null);
            throw new RuntimeException("NullPointerException not thrown");
        } catch (NullPointerException npe) {
        }
        try {
            path.relativize(null);
            throw new RuntimeException("NullPointerException not thrown");
        } catch (NullPointerException npe) {
        }
        try {
            path.compareTo(null);
            throw new RuntimeException("NullPointerException not thrown");
        } catch (NullPointerException npe) {
        }
        try {
            path.startsWith((Path)null);
            throw new RuntimeException("NullPointerException not thrown");
        } catch (NullPointerException npe) {
        }
        try {
            path.endsWith((Path)null);
            throw new RuntimeException("NullPointerException not thrown");
        } catch (NullPointerException npe) {
        }
    }
    public static void main(String[] args) {
        npes();
        String osname = System.getProperty("os.name");
        if (osname.startsWith("Windows")) {
            doWindowsTests();
        }
        if (osname.equals("SunOS") || osname.equals("Linux")) {
            doUnixTests();
        }
    }
}
