import proguard.classfile.ClassConstants;
import proguard.classfile.util.ClassUtil;
import proguard.util.ListUtil;
import java.io.*;
import java.util.List;
public class ConfigurationWriter
{
    private static final String[] KEEP_OPTIONS = new String[]
    {
        ConfigurationConstants.KEEP_OPTION,
        ConfigurationConstants.KEEP_CLASS_MEMBERS_OPTION,
        ConfigurationConstants.KEEP_CLASSES_WITH_MEMBERS_OPTION
    };
    private final PrintWriter writer;
    private File        baseDir;
    public ConfigurationWriter(File configurationFile) throws IOException
    {
        this(new PrintWriter(new FileWriter(configurationFile)));
        baseDir = configurationFile.getParentFile();
    }
    public ConfigurationWriter(OutputStream outputStream) throws IOException
    {
        this(new PrintWriter(outputStream));
    }
    public ConfigurationWriter(PrintWriter writer) throws IOException
    {
        this.writer = writer;
    }
    public void close() throws IOException
    {
        writer.close();
    }
    public void write(Configuration configuration) throws IOException
    {
        writeJarOptions(ConfigurationConstants.INJARS_OPTION,
                        ConfigurationConstants.OUTJARS_OPTION,
                        configuration.programJars);
        writer.println();
        writeJarOptions(ConfigurationConstants.LIBRARYJARS_OPTION,
                        ConfigurationConstants.LIBRARYJARS_OPTION,
                        configuration.libraryJars);
        writer.println();
        writeOption(ConfigurationConstants.DONT_SKIP_NON_PUBLIC_LIBRARY_CLASSES_OPTION,       !configuration.skipNonPublicLibraryClasses);
        writeOption(ConfigurationConstants.DONT_SKIP_NON_PUBLIC_LIBRARY_CLASS_MEMBERS_OPTION, !configuration.skipNonPublicLibraryClassMembers);
        writeOption(ConfigurationConstants.KEEP_DIRECTORIES_OPTION,                           configuration.keepDirectories);
        writeOption(ConfigurationConstants.TARGET_OPTION,                                     ClassUtil.externalClassVersion(configuration.targetClassVersion));
        writeOption(ConfigurationConstants.FORCE_PROCESSING_OPTION,                           configuration.lastModified == Long.MAX_VALUE);
        writeOption(ConfigurationConstants.DONT_SHRINK_OPTION, !configuration.shrink);
        writeOption(ConfigurationConstants.PRINT_USAGE_OPTION, configuration.printUsage);
        writeOption(ConfigurationConstants.DONT_OPTIMIZE_OPTION,                 !configuration.optimize);
        writeOption(ConfigurationConstants.OPTIMIZATIONS,                        configuration.optimize ? ListUtil.commaSeparatedString(configuration.optimizations) : null);
        writeOption(ConfigurationConstants.OPTIMIZATION_PASSES,                  configuration.optimizationPasses);
        writeOption(ConfigurationConstants.ALLOW_ACCESS_MODIFICATION_OPTION,     configuration.allowAccessModification);
        writeOption(ConfigurationConstants.MERGE_INTERFACES_AGGRESSIVELY_OPTION, configuration.mergeInterfacesAggressively);
        writeOption(ConfigurationConstants.DONT_OBFUSCATE_OPTION,                  !configuration.obfuscate);
        writeOption(ConfigurationConstants.PRINT_MAPPING_OPTION,                   configuration.printMapping);
        writeOption(ConfigurationConstants.APPLY_MAPPING_OPTION,                   configuration.applyMapping);
        writeOption(ConfigurationConstants.OBFUSCATION_DICTIONARY_OPTION,          configuration.obfuscationDictionary);
        writeOption(ConfigurationConstants.CLASS_OBFUSCATION_DICTIONARY_OPTION,    configuration.classObfuscationDictionary);
        writeOption(ConfigurationConstants.PACKAGE_OBFUSCATION_DICTIONARY_OPTION,  configuration.packageObfuscationDictionary);
        writeOption(ConfigurationConstants.OVERLOAD_AGGRESSIVELY_OPTION,           configuration.overloadAggressively);
        writeOption(ConfigurationConstants.USE_UNIQUE_CLASS_MEMBER_NAMES_OPTION,   configuration.useUniqueClassMemberNames);
        writeOption(ConfigurationConstants.DONT_USE_MIXED_CASE_CLASS_NAMES_OPTION, !configuration.useMixedCaseClassNames);
        writeOption(ConfigurationConstants.KEEP_PACKAGE_NAMES_OPTION,              configuration.keepPackageNames, true);
        writeOption(ConfigurationConstants.FLATTEN_PACKAGE_HIERARCHY_OPTION,       configuration.flattenPackageHierarchy, true);
        writeOption(ConfigurationConstants.REPACKAGE_CLASSES_OPTION,               configuration.repackageClasses, true);
        writeOption(ConfigurationConstants.KEEP_ATTRIBUTES_OPTION,                 configuration.keepAttributes);
        writeOption(ConfigurationConstants.RENAME_SOURCE_FILE_ATTRIBUTE_OPTION,    configuration.newSourceFileAttribute);
        writeOption(ConfigurationConstants.ADAPT_CLASS_STRINGS_OPTION,             configuration.adaptClassStrings, true);
        writeOption(ConfigurationConstants.ADAPT_RESOURCE_FILE_NAMES_OPTION,       configuration.adaptResourceFileNames);
        writeOption(ConfigurationConstants.ADAPT_RESOURCE_FILE_CONTENTS_OPTION,    configuration.adaptResourceFileContents);
        writeOption(ConfigurationConstants.DONT_PREVERIFY_OPTION, !configuration.preverify);
        writeOption(ConfigurationConstants.MICRO_EDITION_OPTION,  configuration.microEdition);
        writeOption(ConfigurationConstants.VERBOSE_OPTION,             configuration.verbose);
        writeOption(ConfigurationConstants.DONT_NOTE_OPTION,           configuration.note, true);
        writeOption(ConfigurationConstants.DONT_WARN_OPTION,           configuration.warn, true);
        writeOption(ConfigurationConstants.IGNORE_WARNINGS_OPTION,     configuration.ignoreWarnings);
        writeOption(ConfigurationConstants.PRINT_CONFIGURATION_OPTION, configuration.printConfiguration);
        writeOption(ConfigurationConstants.DUMP_OPTION,                configuration.dump);
        writeOption(ConfigurationConstants.PRINT_SEEDS_OPTION,     configuration.printSeeds);
        writer.println();
        writeOptions(ConfigurationConstants.WHY_ARE_YOU_KEEPING_OPTION, configuration.whyAreYouKeeping);
        writeOptions(KEEP_OPTIONS, configuration.keep);
        writeOptions(ConfigurationConstants.ASSUME_NO_SIDE_EFFECTS_OPTION, configuration.assumeNoSideEffects);
        if (writer.checkError())
        {
            throw new IOException("Can't write configuration");
        }
    }
    private void writeJarOptions(String    inputEntryOptionName,
                                 String    outputEntryOptionName,
                                 ClassPath classPath)
    {
        if (classPath != null)
        {
            for (int index = 0; index < classPath.size(); index++)
            {
                ClassPathEntry entry = classPath.get(index);
                String optionName = entry.isOutput() ?
                     outputEntryOptionName :
                     inputEntryOptionName;
                writer.print(optionName);
                writer.print(' ');
                writer.print(relativeFileName(entry.getFile()));
                boolean filtered = false;
                filtered = writeFilter(filtered, entry.getZipFilter());
                filtered = writeFilter(filtered, entry.getEarFilter());
                filtered = writeFilter(filtered, entry.getWarFilter());
                filtered = writeFilter(filtered, entry.getJarFilter());
                filtered = writeFilter(filtered, entry.getFilter());
                if (filtered)
                {
                    writer.print(ConfigurationConstants.CLOSE_ARGUMENTS_KEYWORD);
                }
                writer.println();
            }
        }
    }
    private boolean writeFilter(boolean filtered, List filter)
    {
        if (filtered)
        {
            writer.print(ConfigurationConstants.SEPARATOR_KEYWORD);
        }
        if (filter != null)
        {
            if (!filtered)
            {
                writer.print(ConfigurationConstants.OPEN_ARGUMENTS_KEYWORD);
            }
            for (int index = 0; index < filter.size(); index++)
            {
                if (index > 0)
                {
                    writer.print(ConfigurationConstants.ARGUMENT_SEPARATOR_KEYWORD);
                }
                writer.print(quotedString((String)filter.get(index)));
            }
            filtered = true;
        }
        return filtered;
    }
    private void writeOption(String optionName, boolean flag)
    {
        if (flag)
        {
            writer.println(optionName);
        }
    }
    private void writeOption(String optionName, int argument)
    {
        if (argument != 1)
        {
            writer.print(optionName);
            writer.print(' ');
            writer.println(argument);
        }
    }
    private void writeOption(String optionName, List arguments)
    {
        writeOption(optionName, arguments, false);
    }
    private void writeOption(String  optionName,
                             List    arguments,
                             boolean replaceInternalClassNames)
    {
        if (arguments != null)
        {
            if (arguments.isEmpty())
            {
                writer.println(optionName);
            }
            else
            {
                String argumentString = ListUtil.commaSeparatedString(arguments);
                if (replaceInternalClassNames)
                {
                    argumentString = ClassUtil.externalClassName(argumentString);
                }
                writer.print(optionName);
                writer.print(' ');
                writer.println(quotedString(argumentString));
            }
        }
    }
    private void writeOption(String optionName, String arguments)
    {
        writeOption(optionName, arguments, false);
    }
    private void writeOption(String  optionName,
                             String  arguments,
                             boolean replaceInternalClassNames)
    {
        if (arguments != null)
        {
            if (replaceInternalClassNames)
            {
                arguments = ClassUtil.externalClassName(arguments);
            }
            writer.print(optionName);
            writer.print(' ');
            writer.println(quotedString(arguments));
        }
    }
    private void writeOption(String optionName, File file)
    {
        if (file != null)
        {
            if (file.getPath().length() > 0)
            {
                writer.print(optionName);
                writer.print(' ');
                writer.println(relativeFileName(file));
            }
            else
            {
                writer.println(optionName);
            }
        }
    }
    private void writeOptions(String[] optionNames,
                              List     keepClassSpecifications)
    {
        if (keepClassSpecifications != null)
        {
            for (int index = 0; index < keepClassSpecifications.size(); index++)
            {
                writeOption(optionNames, (KeepClassSpecification)keepClassSpecifications.get(index));
            }
        }
    }
    private void writeOption(String[]               optionNames,
                             KeepClassSpecification keepClassSpecification)
    {
        String optionName = optionNames[keepClassSpecification.markConditionally ? 2 :
                                        keepClassSpecification.markClasses       ? 0 :
                                                                              1];
        if (keepClassSpecification.allowShrinking)
        {
            optionName += ConfigurationConstants.ARGUMENT_SEPARATOR_KEYWORD +
                          ConfigurationConstants.ALLOW_SHRINKING_SUBOPTION;
        }
        if (keepClassSpecification.allowOptimization)
        {
            optionName += ConfigurationConstants.ARGUMENT_SEPARATOR_KEYWORD +
                          ConfigurationConstants.ALLOW_OPTIMIZATION_SUBOPTION;
        }
        if (keepClassSpecification.allowObfuscation)
        {
            optionName += ConfigurationConstants.ARGUMENT_SEPARATOR_KEYWORD +
                          ConfigurationConstants.ALLOW_OBFUSCATION_SUBOPTION;
        }
        writeOption(optionName, keepClassSpecification);
    }
    private void writeOptions(String optionName,
                              List   classSpecifications)
    {
        if (classSpecifications != null)
        {
            for (int index = 0; index < classSpecifications.size(); index++)
            {
                writeOption(optionName, (ClassSpecification)classSpecifications.get(index));
            }
        }
    }
    private void writeOption(String             optionName,
                             ClassSpecification classSpecification)
    {
        writer.println();
        writeComments(classSpecification.comments);
        writer.print(optionName);
        writer.print(' ');
        if (classSpecification.annotationType != null)
        {
            writer.print(ConfigurationConstants.ANNOTATION_KEYWORD);
            writer.print(ClassUtil.externalType(classSpecification.annotationType));
            writer.print(' ');
        }
        writer.print(ClassUtil.externalClassAccessFlags(classSpecification.requiredUnsetAccessFlags,
                                                        ConfigurationConstants.NEGATOR_KEYWORD));
        writer.print(ClassUtil.externalClassAccessFlags(classSpecification.requiredSetAccessFlags));
        if (((classSpecification.requiredSetAccessFlags |
              classSpecification.requiredUnsetAccessFlags) &
             (ClassConstants.INTERNAL_ACC_INTERFACE |
              ClassConstants.INTERNAL_ACC_ENUM)) == 0)
        {
            writer.print(ConfigurationConstants.CLASS_KEYWORD);
        }
        writer.print(' ');
        writer.print(classSpecification.className != null ?
            ClassUtil.externalClassName(classSpecification.className) :
            ConfigurationConstants.ANY_CLASS_KEYWORD);
        if (classSpecification.extendsAnnotationType != null ||
            classSpecification.extendsClassName      != null)
        {
            writer.print(' ');
            writer.print(ConfigurationConstants.EXTENDS_KEYWORD);
            writer.print(' ');
            if (classSpecification.extendsAnnotationType != null)
            {
                writer.print(ConfigurationConstants.ANNOTATION_KEYWORD);
                writer.print(ClassUtil.externalType(classSpecification.extendsAnnotationType));
                writer.print(' ');
            }
            writer.print(classSpecification.extendsClassName != null ?
                ClassUtil.externalClassName(classSpecification.extendsClassName) :
                ConfigurationConstants.ANY_CLASS_KEYWORD);
        }
        if (classSpecification.fieldSpecifications  != null ||
            classSpecification.methodSpecifications != null)
        {
            writer.print(' ');
            writer.println(ConfigurationConstants.OPEN_KEYWORD);
            writeFieldSpecification( classSpecification.fieldSpecifications);
            writeMethodSpecification(classSpecification.methodSpecifications);
            writer.println(ConfigurationConstants.CLOSE_KEYWORD);
        }
        else
        {
            writer.println();
        }
    }
    private void writeComments(String comments)
    {
        if (comments != null)
        {
            int index = 0;
            while (index < comments.length())
            {
                int breakIndex = comments.indexOf('\n', index);
                if (breakIndex < 0)
                {
                    breakIndex = comments.length();
                }
                writer.print('#');
                if (comments.charAt(index) != ' ')
                {
                    writer.print(' ');
                }
                writer.println(comments.substring(index, breakIndex));
                index = breakIndex + 1;
            }
        }
    }
    private void writeFieldSpecification(List memberSpecifications)
    {
        if (memberSpecifications != null)
        {
            for (int index = 0; index < memberSpecifications.size(); index++)
            {
                MemberSpecification memberSpecification =
                    (MemberSpecification)memberSpecifications.get(index);
                writer.print("    ");
                if (memberSpecification.annotationType != null)
                {
                    writer.print(ConfigurationConstants.ANNOTATION_KEYWORD);
                    writer.println(ClassUtil.externalType(memberSpecification.annotationType));
                    writer.print("    ");
                }
                writer.print(ClassUtil.externalFieldAccessFlags(memberSpecification.requiredUnsetAccessFlags,
                                                                ConfigurationConstants.NEGATOR_KEYWORD));
                writer.print(ClassUtil.externalFieldAccessFlags(memberSpecification.requiredSetAccessFlags));
                String name       = memberSpecification.name;
                String descriptor = memberSpecification.descriptor;
                writer.print(descriptor == null ? name == null ?
                    ConfigurationConstants.ANY_FIELD_KEYWORD             :
                    ConfigurationConstants.ANY_TYPE_KEYWORD + ' ' + name :
                    ClassUtil.externalFullFieldDescription(0,
                                                           name == null ? ConfigurationConstants.ANY_CLASS_MEMBER_KEYWORD : name,
                                                           descriptor));
                writer.println(ConfigurationConstants.SEPARATOR_KEYWORD);
            }
        }
    }
    private void writeMethodSpecification(List memberSpecifications)
    {
        if (memberSpecifications != null)
        {
            for (int index = 0; index < memberSpecifications.size(); index++)
            {
                MemberSpecification memberSpecification =
                    (MemberSpecification)memberSpecifications.get(index);
                writer.print("    ");
                if (memberSpecification.annotationType != null)
                {
                    writer.print(ConfigurationConstants.ANNOTATION_KEYWORD);
                    writer.println(ClassUtil.externalType(memberSpecification.annotationType));
                    writer.print("    ");
                }
                writer.print(ClassUtil.externalMethodAccessFlags(memberSpecification.requiredUnsetAccessFlags,
                                                                 ConfigurationConstants.NEGATOR_KEYWORD));
                writer.print(ClassUtil.externalMethodAccessFlags(memberSpecification.requiredSetAccessFlags));
                String name       = memberSpecification.name;
                String descriptor = memberSpecification.descriptor;
                writer.print(descriptor == null ? name == null ?
                    ConfigurationConstants.ANY_METHOD_KEYWORD :
                    ConfigurationConstants.ANY_TYPE_KEYWORD + ' ' + name + ConfigurationConstants.OPEN_ARGUMENTS_KEYWORD + ConfigurationConstants.ANY_ARGUMENTS_KEYWORD + ConfigurationConstants.CLOSE_ARGUMENTS_KEYWORD :
                    ClassUtil.externalFullMethodDescription(ClassConstants.INTERNAL_METHOD_NAME_INIT,
                                                            0,
                                                            name == null ? ConfigurationConstants.ANY_CLASS_MEMBER_KEYWORD : name,
                                                            descriptor));
                writer.println(ConfigurationConstants.SEPARATOR_KEYWORD);
            }
        }
    }
    private String relativeFileName(File file)
    {
        String fileName = file.getAbsolutePath();
        if (baseDir != null)
        {
            String baseDirName = baseDir.getAbsolutePath() + File.separator;
            if (fileName.startsWith(baseDirName))
            {
                fileName = fileName.substring(baseDirName.length());
            }
        }
        return quotedString(fileName);
    }
    private String quotedString(String string)
    {
        return string.length()     == 0 ||
               string.indexOf(' ') >= 0 ||
               string.indexOf('@') >= 0 ||
               string.indexOf('{') >= 0 ||
               string.indexOf('}') >= 0 ||
               string.indexOf('(') >= 0 ||
               string.indexOf(')') >= 0 ||
               string.indexOf(':') >= 0 ||
               string.indexOf(';') >= 0 ||
               string.indexOf(',') >= 0  ? ("'" + string + "'") :
                                           (      string      );
    }
    public static void main(String[] args) {
        try
        {
            ConfigurationWriter writer = new ConfigurationWriter(new File(args[0]));
            writer.write(new Configuration());
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
