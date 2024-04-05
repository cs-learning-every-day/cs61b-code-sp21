package byow.Core;

public class Rect {
    public final Point leftBottom;
    public final Point rightTop;


    public Rect(int x1, int y1, int w, int h) {
        leftBottom = new Point(x1, y1);
        rightTop = new Point(x1 + w, y1 + h);
    }


    public boolean intersect(Rect other) {
        return this.leftBottom.x <= other.rightTop.x && this.rightTop.x >= other.leftBottom.x && this.leftBottom.y <= other.rightTop.y && this.rightTop.y >= other.leftBottom.y;
    }

    public Point center() {
        return new Point((this.leftBottom.x + this.rightTop.x) / 2, (this.leftBottom.y + this.rightTop.y) / 2);
    }

}
