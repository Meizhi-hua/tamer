public class Immediate extends ImmediateOrRegister {
   private final Number value;
   public Immediate(Number value) {
      this.value = value;
   }
   public Number getNumber() {
      return value;
   }
   public boolean isImmediate() {
      return true;
   }
   public String toString() {
      return value.toString();
   }
   public int hashCode() {
      return value.hashCode();
   }
   public boolean equals(Object obj) {
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      Immediate other = (Immediate) obj;
      return value.equals(other.value);
   }
}
