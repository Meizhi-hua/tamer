public class X86XMMRegisters {
   public static final int NUM_XMM_REGISTERS = 8;
   public static final X86XMMRegister XMM0;
   public static final X86XMMRegister XMM1;
   public static final X86XMMRegister XMM2;
   public static final X86XMMRegister XMM3;
   public static final X86XMMRegister XMM4;
   public static final X86XMMRegister XMM5;
   public static final X86XMMRegister XMM6;
   public static final X86XMMRegister XMM7;
   private static X86XMMRegister xmmRegisters[];
   static {
      XMM0 = new X86XMMRegister(0, "%xmm0");
      XMM1 = new X86XMMRegister(1, "%xmm1");
      XMM2 = new X86XMMRegister(2, "%xmm2");
      XMM3 = new X86XMMRegister(3, "%xmm3");
      XMM4 = new X86XMMRegister(4, "%xmm4");
      XMM5 = new X86XMMRegister(5, "%xmm5");
      XMM6 = new X86XMMRegister(6, "%xmm6");
      XMM7 = new X86XMMRegister(7, "%xmm7");
      xmmRegisters = (new X86XMMRegister[] {
            XMM0, XMM1, XMM2, XMM3, XMM4, XMM5, XMM6, XMM7
      });
   }
   public static int getNumberOfRegisters() {
      return NUM_XMM_REGISTERS;
   }
   public static String getRegisterName(int regNum) {
      if (Assert.ASSERTS_ENABLED) {
         Assert.that(regNum > -1 && regNum < NUM_XMM_REGISTERS, "invalid XMM register number!");
      }
      return xmmRegisters[regNum].toString();
   }
   public static X86XMMRegister getRegister(int regNum) {
      if (Assert.ASSERTS_ENABLED) {
         Assert.that(regNum > -1 && regNum < NUM_XMM_REGISTERS, "invalid XMM register number!");
      }
     return xmmRegisters[regNum];
   }
}