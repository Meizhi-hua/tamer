public class ProjectCreator {
    public static void usage() {
        System.out.println("ProjectCreator options:");
        System.err.println("WinGammaPlatform platform-specific options:");
        System.err.println("  -sourceBase <path to directory (workspace) " +
                           "containing source files; no trailing slash>");
        System.err.println("  -dspFileName <full pathname to which .dsp file " +
                           "will be written; all parent directories must " +
                           "already exist>");
        System.err.println("  -envVar <environment variable to be inserted " +
                           "into .dsp file, substituting for path given in " +
                           "-sourceBase. Example: HotSpotWorkSpace>");
        System.err.println("  -dllLoc <path to directory in which to put " +
                           "jvm.dll and jvm_g.dll; no trailing slash>");
        System.err.println("  If any of the above are specified, "+
                           "they must all be.");
        System.err.println("  Additional, optional arguments, which can be " +
                           "specified multiple times:");
        System.err.println("    -absoluteInclude <string containing absolute " +
                           "path to include directory>");
        System.err.println("    -relativeInclude <string containing include " +
                           "directory relative to -envVar>");
        System.err.println("    -define <preprocessor flag to be #defined " +
                           "(note: doesn't yet support " +
                           "#define (flag) (value))>");
        System.err.println("    -perFileLine <file> <line>");
        System.err.println("    -conditionalPerFileLine <file> <line for " +
                           "release build> <line for debug build>");
        System.err.println("  (NOTE: To work around a bug in nmake, where " +
                           "you can't have a '#' character in a quoted " +
                           "string, all of the lines outputted have \"#\"" +
                           "prepended)");
        System.err.println("    -startAt <subdir of sourceBase>");
        System.err.println("    -ignoreFile <file which won't be able to be " +
                           "found in the sourceBase because it's generated " +
                           "later>");
        System.err.println("    -additionalFile <file not in database but " +
                           "which should show up in .dsp file>");
        System.err.println("    -additionalGeneratedFile <environment variable of " +
                           "generated file's location> <relative path to " +
                           "directory containing file; no trailing slash> " +
                           "<name of file generated later in the build process>");
        System.err.println("    -prelink <build> <desc> <cmds>:");
        System.err.println(" Generate a set of prelink commands for the given BUILD");
        System.err.println(" (\"Debug\" or \"Release\"). The prelink description and commands");
        System.err.println(" are both quoted strings.");
        System.err.println("    Default includes: \".\"");
        System.err.println("    Default defines: WIN32, _WINDOWS, \"HOTSPOT_BUILD_USER=$(USERNAME)\"");
    }
    public static void main(String[] args) {
        try {
            if (args.length < 3) {
                usage();
                System.exit(1);
            }
            String platformName = args[0];
            Class platformClass = Class.forName(platformName);
            WinGammaPlatform platform = (WinGammaPlatform) platformClass.newInstance();
            String[] platformArgs = new String[args.length - 1];
            System.arraycopy(args, 1, platformArgs, 0, platformArgs.length);
            platform.createVcproj(platformArgs);
        }
        catch (Exception e) {
            e.printStackTrace();
              System.exit(1);
        }
    }
}
