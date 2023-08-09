public class VerifyProtectedConstructor extends ClassLoader {
  public static void main(String argv[]) throws Exception {
    VerifyProtectedConstructor t = new VerifyProtectedConstructor();
    t.loadSuperClass();
    try {
      t.checkClassVersion(49); 
    }
    catch(VerifyError e) {
       throw new Exception("FAIL: should be no VerifyError for CF version 49");
    }
    try {
      t.checkClassVersion(50); 
      throw new Exception("FAIL: should be a VerifyError for CF version 50");
    }
    catch(VerifyError e) {
       System.out.println("PASS");
    }
  }
  private void loadSuperClass() {
    long[] cls_data = {
      0xcafebabe00000032L, 0x000a0a0003000707L,
      0x0008070009010006L, 0x3c696e69743e0100L,
      0x0328295601000443L, 0x6f64650c00040005L,
      0x010003412f410100L, 0x106a6176612f6c61L,
      0x6e672f4f626a6563L, 0x7400210002000300L,
      0x0000000001000400L, 0x0400050001000600L,
      0x0000110001000100L, 0x0000052ab70001b1L,
      0x0000000000000000L 
    };
    final int EXTRA = 2;
    byte cf_bytes[] = toByteArray(cls_data);
    defineClass("A.A", cf_bytes, 0, cf_bytes.length - EXTRA);
  }
  private int num_calls;
  private static String classNames[] = { "B.B", "C.C" };
  private void checkClassVersion(int version) throws VerifyError {
    long[] cls_data = {
      0xcafebabe00000032L, 0x000b0a0002000807L,
      0x000907000a010006L, 0x3c696e69743e0100L,
      0x0328295601000443L, 0x6f6465010001660cL,
      0x0004000501000341L, 0x2f41010003422f42L,
      0x0021000300020000L, 0x0000000200010004L,
      0x0005000100060000L, 0x0011000100010000L,
      0x00052ab70001b100L, 0x0000000009000700L,
      0x0500010006000000L, 0x1500020000000000L,
      0x09bb000259b70001L, 0x57b1000000000000L 
    };
    final int EXTRA = 0;
    byte cf_bytes[] = toByteArray(cls_data);
    cf_bytes[7] = (byte)version;
    cf_bytes[61] += num_calls;
    cf_bytes[63] += num_calls;
    String name = classNames[num_calls];
    num_calls++;
    Class c = defineClass(name, cf_bytes, 0, cf_bytes.length - EXTRA);
    try { c.newInstance(); } 
    catch(InstantiationException e) {}
    catch(IllegalAccessException e) {}
  }
  static private byte[] toByteArray(long arr[]) {
    java.nio.ByteBuffer bbuf = java.nio.ByteBuffer.allocate(arr.length * 8);
    bbuf.asLongBuffer().put(java.nio.LongBuffer.wrap(arr));
    return bbuf.array();
  }
}
