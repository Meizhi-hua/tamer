import proguard.classfile.util.ClassUtil;
import java.io.*;
import java.util.ArrayList;
public class ProGuardTask extends ConfigurationTask
{
    public void setConfiguration(File configurationFile) throws BuildException
    {
        try
        {
            ConfigurationParser parser = new ConfigurationParser(configurationFile);
            try
            {
                parser.parse(configuration);
            }
            catch (ParseException ex)
            {
                throw new BuildException(ex.getMessage());
            }
            finally
            {
                parser.close();
            }
        }
        catch (IOException ex)
        {
            throw new BuildException(ex.getMessage());
        }
    }
    public void setOutjar(String parameters)
    {
        throw new BuildException("Use the <outjar> nested element instead of the 'outjar' attribute");
    }
    public void setSkipnonpubliclibraryclasses(boolean skipNonPublicLibraryClasses)
    {
        configuration.skipNonPublicLibraryClasses = skipNonPublicLibraryClasses;
    }
    public void setSkipnonpubliclibraryclassmembers(boolean skipNonPublicLibraryClassMembers)
    {
        configuration.skipNonPublicLibraryClassMembers = skipNonPublicLibraryClassMembers;
    }
    public void setTarget(String target)
    {
        configuration.targetClassVersion = ClassUtil.internalClassVersion(target);
        if (configuration.targetClassVersion == 0)
        {
            throw new BuildException("Unsupported target '"+target+"'");
        }
    }
    public void setForceprocessing(boolean forceProcessing)
    {
        configuration.lastModified = forceProcessing ? Long.MAX_VALUE : 0;
    }
    public void setPrintseeds(File printSeeds)
    {
        configuration.printSeeds = optionalFile(printSeeds);
    }
    public void setShrink(boolean shrink)
    {
        configuration.shrink = shrink;
    }
    public void setPrintusage(File printUsage)
    {
        configuration.printUsage = optionalFile(printUsage);
    }
    public void setOptimize(boolean optimize)
    {
        configuration.optimize = optimize;
    }
    public void setOptimizationpasses(int optimizationPasses)
    {
        configuration.optimizationPasses = optimizationPasses;
    }
    public void setAllowaccessmodification(boolean allowAccessModification)
    {
        configuration.allowAccessModification = allowAccessModification;
    }
    public void setMergeinterfacesaggressively(boolean mergeinterfacesaggressively)
    {
        configuration.mergeInterfacesAggressively = mergeinterfacesaggressively;
    }
    public void setObfuscate(boolean obfuscate)
    {
        configuration.obfuscate = obfuscate;
    }
    public void setPrintmapping(File printMapping)
    {
        configuration.printMapping = optionalFile(printMapping);
    }
    public void setApplymapping(File applyMapping)
    {
        configuration.applyMapping = resolvedFile(applyMapping);
    }
    public void setObfuscationdictionary(File obfuscationDictionary)
    {
        configuration.obfuscationDictionary = resolvedFile(obfuscationDictionary);
    }
    public void setClassobfuscationdictionary(File classObfuscationDictionary)
    {
        configuration.classObfuscationDictionary = resolvedFile(classObfuscationDictionary);
    }
    public void setPackageobfuscationdictionary(File packageObfuscationDictionary)
    {
        configuration.packageObfuscationDictionary = resolvedFile(packageObfuscationDictionary);
    }
    public void setOverloadaggressively(boolean overloadAggressively)
    {
        configuration.overloadAggressively = overloadAggressively;
    }
    public void setUseuniqueclassmembernames(boolean useUniqueClassMemberNames)
    {
        configuration.useUniqueClassMemberNames = useUniqueClassMemberNames;
    }
    public void setUsemixedcaseclassnames(boolean useMixedCaseClassNames)
    {
        configuration.useMixedCaseClassNames = useMixedCaseClassNames;
    }
    public void setFlattenpackagehierarchy(String flattenPackageHierarchy)
    {
        configuration.flattenPackageHierarchy = ClassUtil.internalClassName(flattenPackageHierarchy);
    }
    public void setRepackageclasses(String repackageClasses)
    {
        configuration.repackageClasses = ClassUtil.internalClassName(repackageClasses);
    }
    public void setDefaultpackage(String defaultPackage)
    {
        configuration.repackageClasses = ClassUtil.internalClassName(defaultPackage);
    }
    public void setRenamesourcefileattribute(String newSourceFileAttribute)
    {
        configuration.newSourceFileAttribute = newSourceFileAttribute;
    }
    public void setPreverify(boolean preverify)
    {
        configuration.preverify = preverify;
    }
    public void setMicroedition(boolean microEdition)
    {
        configuration.microEdition = microEdition;
    }
    public void setVerbose(boolean verbose)
    {
        configuration.verbose = verbose;
    }
    public void setNote(boolean note)
    {
        configuration.note = note ? null : new ArrayList();
    }
    public void setWarn(boolean warn)
    {
        configuration.warn = warn ? null : new ArrayList();
    }
    public void setIgnorewarnings(boolean ignoreWarnings)
    {
        configuration.ignoreWarnings = ignoreWarnings;
    }
    public void setPrintconfiguration(File printConfiguration)
    {
        configuration.printConfiguration = optionalFile(printConfiguration);
    }
    public void setDump(File dump)
    {
        configuration.dump = optionalFile(dump);
    }
    public void execute() throws BuildException
    {
        try
        {
            ProGuard proGuard = new ProGuard(configuration);
            proGuard.execute();
        }
        catch (IOException ex)
        {
            throw new BuildException(ex.getMessage());
        }
    }
    private File optionalFile(File file)
    {
        String fileName = file.getName();
        return
            fileName.equalsIgnoreCase("false") ||
            fileName.equalsIgnoreCase("no")    ||
            fileName.equalsIgnoreCase("off")    ? null :
            fileName.equalsIgnoreCase("true")  ||
            fileName.equalsIgnoreCase("yes")   ||
            fileName.equalsIgnoreCase("on")     ? new File("")   :
                                                  resolvedFile(file);
    }
    private File resolvedFile(File file)
    {
        return file.isAbsolute() ? file :
                                   new File(getProject().getBaseDir(),
                                            file.getName());
    }
}
