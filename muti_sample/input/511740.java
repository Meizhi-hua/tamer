package proguard.classfile.constant;
import proguard.classfile.*;
import proguard.classfile.constant.visitor.ConstantVisitor;
public class MethodrefConstant extends RefConstant
{
    public MethodrefConstant()
    {
    }
    public MethodrefConstant(int    u2classIndex,
                             int    u2nameAndTypeIndex,
                             Clazz  referencedClass,
                             Member referencedMember)
    {
        this.u2classIndex       = u2classIndex;
        this.u2nameAndTypeIndex = u2nameAndTypeIndex;
        this.referencedClass    = referencedClass;
        this.referencedMember   = referencedMember;
    }
    public int getTag()
    {
        return ClassConstants.CONSTANT_Methodref;
    }
    public void accept(Clazz clazz, ConstantVisitor constantVisitor)
    {
        constantVisitor.visitMethodrefConstant(clazz, this);
    }
}