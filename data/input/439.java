public class LibprocClient extends Tool {
   public void run() {
      String version = VM.getVM().getVMRelease();
      Assert.that(version.startsWith("1.5"), "1.5 expected");
      Threads threads = VM.getVM().getThreads();
      boolean mainTested = false;
      for (JavaThread cur = threads.first(); cur != null; cur = cur.next()) {
         if (cur.isJavaThread()) {
             String name = cur.getThreadName();
             for (JavaVFrame vf = getLastJavaVFrame(cur); vf != null; vf = vf.javaSender()) {
                checkFrame(vf);
             }
             if (name.equals("main")) {
                checkMainThread(cur);
                mainTested = true;
             }
         }
      }
      Assert.that(mainTested, "main thread missing");
   }
   public static void main(String[] args) {
      try {
         LibprocClient lc = new LibprocClient();
         lc.start(args);
         lc.getAgent().detach();
         System.out.println("\nPASSED\n");
      } catch (Exception exp) {
         System.out.println("\nFAILED\n");
         exp.printStackTrace();
      }
   }
   private static JavaVFrame getLastJavaVFrame(JavaThread cur) {
      RegisterMap regMap = cur.newRegisterMap(true);
      Frame f = cur.getCurrentFrameGuess();
      if (f == null) {
         System.err.println(" (Unable to get a top most frame)");
         return null;
      }
      VFrame vf = VFrame.newVFrame(f, regMap, cur, true, true);
      if (vf == null) {
         System.err.println(" (Unable to create vframe for topmost frame guess)");
         return null;
      }
      if (vf.isJavaFrame()) {
         return (JavaVFrame) vf;
      }
      return (JavaVFrame) vf.javaSender();
   }
   private void checkMethodSignature(Symbol sig) {
      SignatureIterator itr = new SignatureIterator(sig) {
                                  public void doBool  () {}
                                  public void doChar  () {}
                                  public void doFloat () {}
                                  public void doDouble() {}
                                  public void doByte  () {}
                                  public void doShort () {}
                                  public void doInt   () {}
                                  public void doLong  () {}
                                  public void doVoid  () {}
                                  public void doObject(int begin, int end) {}
                                  public void doArray (int begin, int end) {}
                              };
      itr.iterate();
   }
   private void checkBCI(Method m, int bci) {
      if (! m.isNative()) {
         byte[] buf = m.getByteCode();
         Assert.that(bci >= 0 && bci < buf.length, "invalid bci, not in code range");
         if (m.hasLineNumberTable()) {
           int lineNum = m.getLineNumberFromBCI(bci);
           Assert.that(lineNum >= 0, "expecting non-negative line number");
         }
      }
   }
   private void checkMethodHolder(Method method) {
      Klass klass = method.getMethodHolder();
      Assert.that(klass != null, "expecting non-null instance klass");
   }
   private void checkFrame(JavaVFrame vf) {
      Method method = vf.getMethod();
      Assert.that(method != null, "expecting a non-null method here");
      Assert.that(method.getName() != null, "expecting non-null method name");
      checkMethodHolder(method);
      checkMethodSignature(method.getSignature());
      checkBCI(method, vf.getBCI());
   }
   private static String[] mainThreadMethods = new String[] {
                             "java.lang.Object.wait(long)",
                             "java.lang.Object.wait()",
                             "LibprocTest.main(java.lang.String[])"
                          };
   private void checkMainThread(JavaThread thread) {
      checkFrames(thread, mainThreadMethods);
   }
   private void checkFrames(JavaThread thread, String[] expectedMethodNames) {
      int i = 0;
      for (JavaVFrame vf = getLastJavaVFrame(thread); vf != null; vf = vf.javaSender(), i++) {
         Method m = vf.getMethod();
         Assert.that(m.externalNameAndSignature().equals(expectedMethodNames[i]),
                     "expected frame missing");
      }
   }
}
