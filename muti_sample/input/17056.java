public class FlagDumper extends Tool {
   public void run() {
      VM.Flag[] flags = VM.getVM().getCommandLineFlags();
      PrintStream out = System.out;
      if (flags == null) {
         out.println("Command Flags info not available! (use 1.4.1_03 or later)");
      } else {
         for (int f = 0; f < flags.length; f++) {
            out.print(flags[f].getName());
            out.print(" = ");
            out.println(flags[f].getValue());
         }
      }
   }
   public static void main(String[] args) {
      FlagDumper fd = new FlagDumper();
      fd.start(args);
      fd.stop();
   }
}
