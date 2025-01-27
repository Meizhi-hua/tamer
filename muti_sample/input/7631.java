public class AMD64FloatRegisters {
   public static int getNumRegisters() {
      return NUM_REGIXMMERS;
   }
   public static AMD64FloatRegister getRegister(int regNum) {
      if (Assert.ASSERTS_ENABLED) {
         Assert.that(regNum > -1 && regNum < NUM_REGIXMMERS, "invalid float register number!");
      }
      return registers[regNum];
   }
   public static String getRegisterName(int i) {
      return "XMM(" + i + ")";
   }
   public static final AMD64FloatRegister XMM0;
   public static final AMD64FloatRegister XMM1;
   public static final AMD64FloatRegister XMM2;
   public static final AMD64FloatRegister XMM3;
   public static final AMD64FloatRegister XMM4;
   public static final AMD64FloatRegister XMM5;
   public static final AMD64FloatRegister XMM6;
   public static final AMD64FloatRegister XMM7;
   public static final AMD64FloatRegister XMM8;
   public static final AMD64FloatRegister XMM9;
   public static final AMD64FloatRegister XMM10;
   public static final AMD64FloatRegister XMM11;
   public static final AMD64FloatRegister XMM12;
   public static final AMD64FloatRegister XMM13;
   public static final AMD64FloatRegister XMM14;
   public static final AMD64FloatRegister XMM15;
   public static final int NUM_REGIXMMERS = 16;
   private static final AMD64FloatRegister[] registers;
   static {
      XMM0 = new AMD64FloatRegister(0);
      XMM1 = new AMD64FloatRegister(1);
      XMM2 = new AMD64FloatRegister(2);
      XMM3 = new AMD64FloatRegister(3);
      XMM4 = new AMD64FloatRegister(4);
      XMM5 = new AMD64FloatRegister(5);
      XMM6 = new AMD64FloatRegister(6);
      XMM7 = new AMD64FloatRegister(7);
      XMM8 = new AMD64FloatRegister(8);
      XMM9 = new AMD64FloatRegister(9);
      XMM10 = new AMD64FloatRegister(10);
      XMM11 = new AMD64FloatRegister(11);
      XMM12 = new AMD64FloatRegister(12);
      XMM13 = new AMD64FloatRegister(13);
      XMM14 = new AMD64FloatRegister(14);
      XMM15 = new AMD64FloatRegister(15);
      registers = new AMD64FloatRegister[] {
                     XMM0, XMM1, XMM2, XMM3, XMM4, XMM5, XMM6, XMM7,
                     XMM8, XMM9, XMM10, XMM11, XMM12, XMM13, XMM14, XMM15
                  };
   }
}
