package jpf;

public class V2d {
    private final double x;
    private final double y;

    public V2d(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public V2d sum(V2d v) {
        return new V2d(x + v.x(), y + v.y());
    }

    public double abs() {
        return Math.sqrt(x * x + y * y);
    }

    public V2d getNormalized() {
        double module = Math.sqrt(x * x + y * y);
        return new V2d(x / module, y / module);
    }

    public V2d mul(double fact) {
        return new V2d(x * fact, y * fact);
    }

    @Override
    public String toString() {
        return "V2d{" + "x=" + x + ", y=" + y + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        V2d v2d = (V2d) o;
        return Double.compare(x, v2d.x) == 0 && Double.compare(y, v2d.y) == 0;
    }

    @Override
    public int hashCode() {
        return 31 * Double.hashCode(x) + Double.hashCode(y);
    }
}