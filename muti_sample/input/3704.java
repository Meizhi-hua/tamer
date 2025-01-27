public class Timing {
    private long lastValue;
    private long sum;
    private String name;
    public Timing(String name) {
        this.name = name;
    }
    @Override
    public String toString() {
        long val = sum;
        if (lastValue != 0) {
            long newValue = System.nanoTime();
            val += (newValue - lastValue);
        }
        return "Timing for " + name + " is: " + val / 1000000 + " ms";
    }
    public void print() {
        System.out.println(toString());
    }
    public void start() {
        lastValue = System.nanoTime();
    }
    public void stop() {
        if (lastValue == 0) {
            throw new IllegalStateException("You must call start before stop");
        }
        long newValue = System.nanoTime();
        sum += newValue - lastValue;
        lastValue = 0;
    }
}
