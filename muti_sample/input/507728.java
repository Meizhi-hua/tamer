package proguard.classfile.util;
import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.annotation.*;
import proguard.classfile.attribute.preverification.*;
import proguard.classfile.constant.*;
import proguard.classfile.instruction.*;
public abstract class SimplifiedVisitor
{
    public void visitAnyClass(Clazz Clazz)
    {
        throw new UnsupportedOperationException("Method must be overridden in ["+this.getClass().getName()+"] if ever called");
    }
    public void visitProgramClass(ProgramClass programClass)
    {
        visitAnyClass(programClass);
    }
    public void visitLibraryClass(LibraryClass libraryClass)
    {
        visitAnyClass(libraryClass);
    }
    public void visitAnyMember(Clazz clazz, Member member)
    {
        throw new UnsupportedOperationException("Method must be overridden in ["+this.getClass().getName()+"] if ever called");
    }
    public void visitProgramMember(ProgramClass programClass, ProgramMember programMember)
    {
        visitAnyMember(programClass, programMember);
    }
    public void visitProgramField(ProgramClass programClass, ProgramField programField)
    {
        visitProgramMember(programClass, programField);
    }
    public void visitProgramMethod(ProgramClass programClass, ProgramMethod programMethod)
    {
        visitProgramMember(programClass, programMethod);
    }
    public void visitLibraryMember(LibraryClass libraryClass, LibraryMember libraryMember)
    {
        visitAnyMember(libraryClass, libraryMember);
    }
    public void visitLibraryField(LibraryClass libraryClass, LibraryField libraryField)
    {
        visitLibraryMember(libraryClass, libraryField);
    }
    public void visitLibraryMethod(LibraryClass libraryClass, LibraryMethod libraryMethod)
    {
        visitLibraryMember(libraryClass, libraryMethod);
    }
    public void visitAnyConstant(Clazz clazz, Constant constant)
    {
        throw new UnsupportedOperationException("Method must be overridden in ["+this.getClass().getName()+"] if ever called");
    }
    public void visitIntegerConstant(Clazz clazz, IntegerConstant integerConstant)
    {
        visitAnyConstant(clazz, integerConstant);
    }
    public void visitLongConstant(Clazz clazz, LongConstant longConstant)
    {
        visitAnyConstant(clazz, longConstant);
    }
    public void visitFloatConstant(Clazz clazz, FloatConstant floatConstant)
    {
        visitAnyConstant(clazz, floatConstant);
    }
    public void visitDoubleConstant(Clazz clazz, DoubleConstant doubleConstant)
    {
        visitAnyConstant(clazz, doubleConstant);
    }
    public void visitStringConstant(Clazz clazz, StringConstant stringConstant)
    {
        visitAnyConstant(clazz, stringConstant);
    }
    public void visitUtf8Constant(Clazz clazz, Utf8Constant utf8Constant)
    {
        visitAnyConstant(clazz, utf8Constant);
    }
    public void visitAnyRefConstant(Clazz clazz, RefConstant refConstant)
    {
        visitAnyConstant(clazz, refConstant);
    }
    public void visitFieldrefConstant(Clazz clazz, FieldrefConstant fieldrefConstant)
    {
        visitAnyRefConstant(clazz, fieldrefConstant);
    }
    public void visitAnyMethodrefConstant(Clazz clazz, RefConstant refConstant)
    {
        visitAnyRefConstant(clazz, refConstant);
    }
    public void visitInterfaceMethodrefConstant(Clazz clazz, InterfaceMethodrefConstant interfaceMethodrefConstant)
    {
        visitAnyMethodrefConstant(clazz, interfaceMethodrefConstant);
    }
    public void visitMethodrefConstant(Clazz clazz, MethodrefConstant methodrefConstant)
    {
        visitAnyMethodrefConstant(clazz, methodrefConstant);
    }
    public void visitClassConstant(Clazz clazz, ClassConstant classConstant)
    {
        visitAnyConstant(clazz, classConstant);
    }
    public void visitNameAndTypeConstant(Clazz clazz, NameAndTypeConstant nameAndTypeConstant)
    {
        visitAnyConstant(clazz, nameAndTypeConstant);
    }
    public void visitAnyAttribute(Clazz clazz, Attribute attribute)
    {
        throw new UnsupportedOperationException("Method must be overridden in ["+this.getClass().getName()+"] if ever called");
    }
    public void visitUnknownAttribute(Clazz clazz, UnknownAttribute unknownAttribute)
    {
        visitAnyAttribute(clazz, unknownAttribute);
    }
    public void visitSourceFileAttribute(Clazz clazz, SourceFileAttribute sourceFileAttribute)
    {
        visitAnyAttribute(clazz, sourceFileAttribute);
    }
    public void visitSourceDirAttribute(Clazz clazz, SourceDirAttribute sourceDirAttribute)
    {
        visitAnyAttribute(clazz, sourceDirAttribute);
    }
    public void visitInnerClassesAttribute(Clazz clazz, InnerClassesAttribute innerClassesAttribute)
    {
        visitAnyAttribute(clazz, innerClassesAttribute);
    }
    public void visitEnclosingMethodAttribute(Clazz clazz, EnclosingMethodAttribute enclosingMethodAttribute)
    {
        visitAnyAttribute(clazz, enclosingMethodAttribute);
    }
    public void visitDeprecatedAttribute(Clazz clazz, DeprecatedAttribute deprecatedAttribute)
    {
        visitAnyAttribute(clazz, deprecatedAttribute);
    }
    public void visitDeprecatedAttribute(Clazz clazz, Member member, DeprecatedAttribute deprecatedAttribute)
    {
        visitDeprecatedAttribute(clazz, deprecatedAttribute);
    }
    public void visitDeprecatedAttribute(Clazz clazz, Field field, DeprecatedAttribute deprecatedAttribute)
    {
        visitDeprecatedAttribute(clazz, (Member)field, deprecatedAttribute);
    }
    public void visitDeprecatedAttribute(Clazz clazz, Method method, DeprecatedAttribute deprecatedAttribute)
    {
        visitDeprecatedAttribute(clazz, (Member)method, deprecatedAttribute);
    }
    public void visitSyntheticAttribute(Clazz clazz, SyntheticAttribute syntheticAttribute)
    {
        visitAnyAttribute(clazz, syntheticAttribute);
    }
    public void visitSyntheticAttribute(Clazz clazz, Member member, SyntheticAttribute syntheticAttribute)
    {
        visitSyntheticAttribute(clazz, syntheticAttribute);
    }
    public void visitSyntheticAttribute(Clazz clazz, Field field, SyntheticAttribute syntheticAttribute)
    {
        visitSyntheticAttribute(clazz, (Member)field, syntheticAttribute);
    }
    public void visitSyntheticAttribute(Clazz clazz, Method method, SyntheticAttribute syntheticAttribute)
    {
        visitSyntheticAttribute(clazz, (Member)method, syntheticAttribute);
    }
    public void visitSignatureAttribute(Clazz clazz, SignatureAttribute signatureAttribute)
    {
        visitAnyAttribute(clazz, signatureAttribute);
    }
    public void visitSignatureAttribute(Clazz clazz, Member member, SignatureAttribute signatureAttribute)
    {
        visitSignatureAttribute(clazz, signatureAttribute);
    }
    public void visitSignatureAttribute(Clazz clazz, Field field, SignatureAttribute signatureAttribute)
    {
        visitSignatureAttribute(clazz, (Member)field, signatureAttribute);
    }
    public void visitSignatureAttribute(Clazz clazz, Method method, SignatureAttribute signatureAttribute)
    {
        visitSignatureAttribute(clazz, (Member)method, signatureAttribute);
    }
    public void visitConstantValueAttribute(Clazz clazz, Field field, ConstantValueAttribute constantValueAttribute)
    {
        visitAnyAttribute(clazz, constantValueAttribute);
    }
    public void visitExceptionsAttribute(Clazz clazz, Method method, ExceptionsAttribute exceptionsAttribute)
    {
        visitAnyAttribute(clazz, exceptionsAttribute);
    }
    public void visitCodeAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute)
    {
        visitAnyAttribute(clazz, codeAttribute);
    }
    public void visitStackMapAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, StackMapAttribute stackMapAttribute)
    {
        visitAnyAttribute(clazz, stackMapAttribute);
    }
    public void visitStackMapTableAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, StackMapTableAttribute stackMapTableAttribute)
    {
        visitAnyAttribute(clazz, stackMapTableAttribute);
    }
    public void visitLineNumberTableAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, LineNumberTableAttribute lineNumberTableAttribute)
    {
        visitAnyAttribute(clazz, lineNumberTableAttribute);
    }
    public void visitLocalVariableTableAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, LocalVariableTableAttribute localVariableTableAttribute)
    {
        visitAnyAttribute(clazz, localVariableTableAttribute);
    }
    public void visitLocalVariableTypeTableAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, LocalVariableTypeTableAttribute localVariableTypeTableAttribute)
    {
        visitAnyAttribute(clazz, localVariableTypeTableAttribute);
    }
    public void visitAnyAnnotationsAttribute(Clazz clazz, AnnotationsAttribute annotationsAttribute)
    {
        visitAnyAttribute(clazz, annotationsAttribute);
    }
    public void visitRuntimeVisibleAnnotationsAttribute(Clazz clazz, RuntimeVisibleAnnotationsAttribute runtimeVisibleAnnotationsAttribute)
    {
        visitAnyAnnotationsAttribute(clazz, runtimeVisibleAnnotationsAttribute);
    }
    public void visitRuntimeVisibleAnnotationsAttribute(Clazz clazz, Member member, RuntimeVisibleAnnotationsAttribute runtimeVisibleAnnotationsAttribute)
    {
        visitRuntimeVisibleAnnotationsAttribute(clazz, runtimeVisibleAnnotationsAttribute);
    }
    public void visitRuntimeVisibleAnnotationsAttribute(Clazz clazz, Field field, RuntimeVisibleAnnotationsAttribute runtimeVisibleAnnotationsAttribute)
    {
        visitRuntimeVisibleAnnotationsAttribute(clazz, (Member)field, runtimeVisibleAnnotationsAttribute);
    }
    public void visitRuntimeVisibleAnnotationsAttribute(Clazz clazz, Method method, RuntimeVisibleAnnotationsAttribute runtimeVisibleAnnotationsAttribute)
    {
        visitRuntimeVisibleAnnotationsAttribute(clazz, (Member)method, runtimeVisibleAnnotationsAttribute);
    }
    public void visitRuntimeInvisibleAnnotationsAttribute(Clazz clazz, RuntimeInvisibleAnnotationsAttribute runtimeInvisibleAnnotationsAttribute)
    {
        visitAnyAnnotationsAttribute(clazz, runtimeInvisibleAnnotationsAttribute);
    }
    public void visitRuntimeInvisibleAnnotationsAttribute(Clazz clazz, Member member, RuntimeInvisibleAnnotationsAttribute runtimeInvisibleAnnotationsAttribute)
    {
        visitRuntimeInvisibleAnnotationsAttribute(clazz, runtimeInvisibleAnnotationsAttribute);
    }
    public void visitRuntimeInvisibleAnnotationsAttribute(Clazz clazz, Field field, RuntimeInvisibleAnnotationsAttribute runtimeInvisibleAnnotationsAttribute)
    {
        visitRuntimeInvisibleAnnotationsAttribute(clazz, (Member)field, runtimeInvisibleAnnotationsAttribute);
    }
    public void visitRuntimeInvisibleAnnotationsAttribute(Clazz clazz, Method method, RuntimeInvisibleAnnotationsAttribute runtimeInvisibleAnnotationsAttribute)
    {
        visitRuntimeInvisibleAnnotationsAttribute(clazz, (Member)method, runtimeInvisibleAnnotationsAttribute);
    }
    public void visitAnyParameterAnnotationsAttribute(Clazz clazz, Method method, ParameterAnnotationsAttribute parameterAnnotationsAttribute)
    {
        visitAnyAttribute(clazz, parameterAnnotationsAttribute);
    }
    public void visitRuntimeVisibleParameterAnnotationsAttribute(Clazz clazz, Method method, RuntimeVisibleParameterAnnotationsAttribute runtimeVisibleParameterAnnotationsAttribute)
    {
        visitAnyParameterAnnotationsAttribute(clazz, method, runtimeVisibleParameterAnnotationsAttribute);
    }
    public void visitRuntimeInvisibleParameterAnnotationsAttribute(Clazz clazz, Method method, RuntimeInvisibleParameterAnnotationsAttribute runtimeInvisibleParameterAnnotationsAttribute)
    {
        visitAnyParameterAnnotationsAttribute(clazz, method, runtimeInvisibleParameterAnnotationsAttribute);
    }
    public void visitAnnotationDefaultAttribute(Clazz clazz, Method method, AnnotationDefaultAttribute annotationDefaultAttribute)
    {
        visitAnyAttribute(clazz, annotationDefaultAttribute);
    }
    public void visitAnyInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, Instruction instruction)
    {
        throw new UnsupportedOperationException("Method must be overridden in ["+this.getClass().getName()+"] if ever called");
    }
    public void visitSimpleInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, SimpleInstruction simpleInstruction)
    {
        visitAnyInstruction(clazz, method, codeAttribute, offset, simpleInstruction);
    }
    public void visitVariableInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, VariableInstruction variableInstruction)
    {
        visitAnyInstruction(clazz, method, codeAttribute, offset, variableInstruction);
    }
    public void visitConstantInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, ConstantInstruction constantInstruction)
    {
        visitAnyInstruction(clazz, method, codeAttribute, offset, constantInstruction);
    }
    public void visitBranchInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, BranchInstruction branchInstruction)
    {
        visitAnyInstruction(clazz, method, codeAttribute, offset, branchInstruction);
    }
    public void visitAnySwitchInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, SwitchInstruction switchInstruction)
    {
        visitAnyInstruction(clazz, method, codeAttribute, offset, switchInstruction);
    }
    public void visitTableSwitchInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, TableSwitchInstruction tableSwitchInstruction)
    {
        visitAnySwitchInstruction(clazz, method, codeAttribute, offset, tableSwitchInstruction);
    }
    public void visitLookUpSwitchInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, LookUpSwitchInstruction lookUpSwitchInstruction)
    {
        visitAnySwitchInstruction(clazz, method, codeAttribute, offset, lookUpSwitchInstruction);
    }
    public void visitAnyStackMapFrame(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, StackMapFrame stackMapFrame)
    {
        throw new UnsupportedOperationException("Method must be overridden in ["+this.getClass().getName()+"] if ever called");
    }
    public void visitSameZeroFrame(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, SameZeroFrame sameZeroFrame)
    {
        visitAnyStackMapFrame(clazz, method, codeAttribute, offset, sameZeroFrame);
    }
    public void visitSameOneFrame(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, SameOneFrame sameOneFrame)
    {
        visitAnyStackMapFrame(clazz, method, codeAttribute, offset, sameOneFrame);
    }
    public void visitLessZeroFrame(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, LessZeroFrame lessZeroFrame)
    {
        visitAnyStackMapFrame(clazz, method, codeAttribute, offset, lessZeroFrame);
    }
    public void visitMoreZeroFrame(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, MoreZeroFrame moreZeroFrame)
    {
        visitAnyStackMapFrame(clazz, method, codeAttribute, offset, moreZeroFrame);
    }
    public void visitFullFrame(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, FullFrame fullFrame)
    {
        visitAnyStackMapFrame(clazz, method, codeAttribute, offset, fullFrame);
    }
    public void visitAnyVerificationType(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, VerificationType verificationType)
    {
        throw new UnsupportedOperationException("Method must be overridden in ["+this.getClass().getName()+"] if ever called");
    }
    public void visitIntegerType(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, IntegerType integerType)
    {
        visitAnyVerificationType(clazz, method, codeAttribute, offset, integerType);
    }
    public void visitFloatType(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, FloatType floatType)
    {
        visitAnyVerificationType(clazz, method, codeAttribute, offset, floatType);
    }
    public void visitLongType(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, LongType longType)
    {
        visitAnyVerificationType(clazz, method, codeAttribute, offset, longType);
    }
    public void visitDoubleType(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, DoubleType doubleType)
    {
        visitAnyVerificationType(clazz, method, codeAttribute, offset, doubleType);
    }
    public void visitTopType(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, TopType topType)
    {
        visitAnyVerificationType(clazz, method, codeAttribute, offset, topType);
    }
    public void visitObjectType(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, ObjectType objectType)
    {
        visitAnyVerificationType(clazz, method, codeAttribute, offset, objectType);
    }
    public void visitNullType(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, NullType nullType)
    {
        visitAnyVerificationType(clazz, method, codeAttribute, offset, nullType);
    }
    public void visitUninitializedType(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, UninitializedType uninitializedType)
    {
        visitAnyVerificationType(clazz, method, codeAttribute, offset, uninitializedType);
    }
    public void visitUninitializedThisType(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, UninitializedThisType uninitializedThisType)
    {
        visitAnyVerificationType(clazz, method, codeAttribute, offset, uninitializedThisType);
    }
    public void visitStackIntegerType(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, int index, IntegerType integerType)
    {
        visitIntegerType(clazz, method, codeAttribute, offset, integerType);
    }
    public void visitStackFloatType(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, int index, FloatType floatType)
    {
        visitFloatType(clazz, method, codeAttribute, offset, floatType);
    }
    public void visitStackLongType(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, int index, LongType longType)
    {
        visitLongType(clazz, method, codeAttribute, offset, longType);
    }
    public void visitStackDoubleType(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, int index, DoubleType doubleType)
    {
        visitDoubleType(clazz, method, codeAttribute, offset, doubleType);
    }
    public void visitStackTopType(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, int index, TopType topType)
    {
        visitTopType(clazz, method, codeAttribute, offset, topType);
    }
    public void visitStackObjectType(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, int index, ObjectType objectType)
    {
        visitObjectType(clazz, method, codeAttribute, offset, objectType);
    }
    public void visitStackNullType(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, int index, NullType nullType)
    {
        visitNullType(clazz, method, codeAttribute, offset, nullType);
    }
    public void visitStackUninitializedType(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, int index, UninitializedType uninitializedType)
    {
        visitUninitializedType(clazz, method, codeAttribute, offset, uninitializedType);
    }
    public void visitStackUninitializedThisType(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, int index, UninitializedThisType uninitializedThisType)
    {
        visitUninitializedThisType(clazz, method, codeAttribute, offset, uninitializedThisType);
    }
    public void visitVariablesIntegerType(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, int index, IntegerType integerType)
    {
        visitIntegerType(clazz, method, codeAttribute, offset, integerType);
    }
    public void visitVariablesFloatType(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, int index, FloatType floatType)
    {
        visitFloatType(clazz, method, codeAttribute, offset, floatType);
    }
    public void visitVariablesLongType(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, int index, LongType longType)
    {
        visitLongType(clazz, method, codeAttribute, offset, longType);
    }
    public void visitVariablesDoubleType(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, int index, DoubleType doubleType)
    {
        visitDoubleType(clazz, method, codeAttribute, offset, doubleType);
    }
    public void visitVariablesTopType(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, int index, TopType topType)
    {
        visitTopType(clazz, method, codeAttribute, offset, topType);
    }
    public void visitVariablesObjectType(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, int index, ObjectType objectType)
    {
        visitObjectType(clazz, method, codeAttribute, offset, objectType);
    }
    public void visitVariablesNullType(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, int index, NullType nullType)
    {
        visitNullType(clazz, method, codeAttribute, offset, nullType);
    }
    public void visitVariablesUninitializedType(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, int index, UninitializedType uninitializedType)
    {
        visitUninitializedType(clazz, method, codeAttribute, offset, uninitializedType);
    }
    public void visitVariablesUninitializedThisType(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, int index, UninitializedThisType uninitializedThisType)
    {
        visitUninitializedThisType(clazz, method, codeAttribute, offset, uninitializedThisType);
    }
    public void visitAnnotation(Clazz clazz, Annotation annotation)
    {
        throw new UnsupportedOperationException("Method must be overridden in ["+this.getClass().getName()+"] if ever called");
    }
    public void visitAnnotation(Clazz clazz, Member member, Annotation annotation)
    {
        visitAnnotation(clazz, annotation);
    }
    public void visitAnnotation(Clazz clazz, Field field, Annotation annotation)
    {
        visitAnnotation(clazz, (Member)field, annotation);
    }
    public void visitAnnotation(Clazz clazz, Method method, Annotation annotation)
    {
        visitAnnotation(clazz, (Member)method, annotation);
    }
    public void visitAnnotation(Clazz clazz, Method method, int parameterIndex, Annotation annotation)
    {
        visitAnnotation(clazz, method, annotation);
    }
    public void visitAnyElementValue(Clazz clazz, Annotation annotation, ElementValue elementValue)
    {
        throw new UnsupportedOperationException("Method must be overridden in ["+this.getClass().getName()+"] if ever called");
    }
    public void visitConstantElementValue(Clazz clazz, Annotation annotation, ConstantElementValue constantElementValue)
    {
        visitAnyElementValue(clazz, annotation, constantElementValue);
    }
    public void visitEnumConstantElementValue(Clazz clazz, Annotation annotation, EnumConstantElementValue enumConstantElementValue)
    {
        visitAnyElementValue(clazz, annotation, enumConstantElementValue);
    }
    public void visitClassElementValue(Clazz clazz, Annotation annotation, ClassElementValue classElementValue)
    {
        visitAnyElementValue(clazz, annotation, classElementValue);
    }
    public void visitAnnotationElementValue(Clazz clazz, Annotation annotation, AnnotationElementValue annotationElementValue)
    {
        visitAnyElementValue(clazz, annotation, annotationElementValue);
    }
    public void visitArrayElementValue(Clazz clazz, Annotation annotation, ArrayElementValue arrayElementValue)
    {
        visitAnyElementValue(clazz, annotation, arrayElementValue);
    }
}
