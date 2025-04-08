package pcd.ass01.jpf;

public class P2d {
    private final double x;
    private final double y;

    public P2d(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public P2d sum(V2d v) {
        return new P2d(x + v.x(), y + v.y());
    }

    public P2d sub(P2d v) {
        return new P2d(x - v.x(), y - v.y());
    }

    public double distance(P2d p) {
        double dx = p.x() - x;
        double dy = p.y() - y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    @Override
    public String toString() {
        return "P2d{" + "x=" + x + ", y=" + y + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        P2d p2d = (P2d) o;
        return Double.compare(x, p2d.x) == 0 && Double.compare(y, p2d.y) == 0;
    }

    @Override
    public int hashCode() {
        return 31 * Double.hashCode(x) + Double.hashCode(y);
    }
}