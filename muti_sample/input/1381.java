public class LinearAllocBlock extends VMObject {
   static {
      VM.registerVMInitializedObserver(new Observer() {
         public void update(Observable o, Object data) {
            initialize(VM.getVM().getTypeDataBase());
         }
      });
   }
   private static synchronized void initialize(TypeDataBase db) {
      Type type = db.lookupType("LinearAllocBlock");
      word_sizeField= type.getCIntegerField("_word_size");
   }
   private static CIntegerField word_sizeField;
   public long word_size() {
      return word_sizeField.getValue(addr);
   }
   public LinearAllocBlock(Address addr) {
      super(addr);
   }
}
