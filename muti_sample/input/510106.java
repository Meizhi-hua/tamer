public class Point {
    public int x;
    public int y;
    public Point() {}
    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }
    public Point(Point src) {
        this.x = src.x;
        this.y = src.y;
    }
    public void set(int x, int y) {
        this.x = x;
        this.y = y;
    }
    public final void negate() {
        x = -x;
        y = -y;
    }
    public final void offset(int dx, int dy) {
        x += dx;
        y += dy;
    }
    public final boolean equals(int x, int y) {
        return this.x == x && this.y == y;
    }
    @Override public boolean equals(Object o) {
        if (o instanceof Point) {
            Point p = (Point) o;
            return this.x == p.x && this.y == p.y;
        }
        return false;
    }
    @Override public int hashCode() {
        return x * 32713 + y;
    }
    @Override public String toString() {
        return "Point(" + x + ", " + y+ ")";
    }
}
