public abstract class Ellipse2D extends RectangularShape {
    public static class Float extends Ellipse2D {
        public float x;
        public float y;
        public float width;
        public float height;
        public Float() {
        }
        public Float(float x, float y, float width, float height) {
            setFrame(x, y, width, height);
        }
        @Override
        public double getX() {
            return x;
        }
        @Override
        public double getY() {
            return y;
        }
        @Override
        public double getWidth() {
            return width;
        }
        @Override
        public double getHeight() {
            return height;
        }
        @Override
        public boolean isEmpty() {
            return width <= 0.0 || height <= 0.0;
        }
        public void setFrame(float x, float y, float width, float height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
        @Override
        public void setFrame(double x, double y, double width, double height) {
            this.x = (float)x;
            this.y = (float)y;
            this.width = (float)width;
            this.height = (float)height;
        }
        public Rectangle2D getBounds2D() {
            return new Rectangle2D.Float(x, y, width, height);
        }
    }
    public static class Double extends Ellipse2D {
        public double x;
        public double y;
        public double width;
        public double height;
        public Double() {
        }
        public Double(double x, double y, double width, double height) {
            setFrame(x, y, width, height);
        }
        @Override
        public double getX() {
            return x;
        }
        @Override
        public double getY() {
            return y;
        }
        @Override
        public double getWidth() {
            return width;
        }
        @Override
        public double getHeight() {
            return height;
        }
        @Override
        public boolean isEmpty() {
            return width <= 0.0 || height <= 0.0;
        }
        @Override
        public void setFrame(double x, double y, double width, double height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
        public Rectangle2D getBounds2D() {
            return new Rectangle2D.Double(x, y, width, height);
        }
    }
    class Iterator implements PathIterator {
        final double u = 2.0 / 3.0 * (Math.sqrt(2.0) - 1.0);
        final double points[][] = {
                {
                        1.0, 0.5 + u, 0.5 + u, 1.0, 0.5, 1.0
                }, {
                        0.5 - u, 1.0, 0.0, 0.5 + u, 0.0, 0.5
                }, {
                        0.0, 0.5 - u, 0.5 - u, 0.0, 0.5, 0.0
                }, {
                        0.5 + u, 0.0, 1.0, 0.5 - u, 1.0, 0.5
                }
        };
        double x;
        double y;
        double width;
        double height;
        AffineTransform t;
        int index;
        Iterator(Ellipse2D e, AffineTransform t) {
            this.x = e.getX();
            this.y = e.getY();
            this.width = e.getWidth();
            this.height = e.getHeight();
            this.t = t;
            if (width < 0.0 || height < 0.0) {
                index = 6;
            }
        }
        public int getWindingRule() {
            return WIND_NON_ZERO;
        }
        public boolean isDone() {
            return index > 5;
        }
        public void next() {
            index++;
        }
        public int currentSegment(double[] coords) {
            if (isDone()) {
                throw new NoSuchElementException(Messages.getString("awt.4B")); 
            }
            if (index == 5) {
                return SEG_CLOSE;
            }
            int type;
            int count;
            if (index == 0) {
                type = SEG_MOVETO;
                count = 1;
                double p[] = points[3];
                coords[0] = x + p[4] * width;
                coords[1] = y + p[5] * height;
            } else {
                type = SEG_CUBICTO;
                count = 3;
                double p[] = points[index - 1];
                int j = 0;
                for (int i = 0; i < 3; i++) {
                    coords[j] = x + p[j++] * width;
                    coords[j] = y + p[j++] * height;
                }
            }
            if (t != null) {
                t.transform(coords, 0, coords, 0, count);
            }
            return type;
        }
        public int currentSegment(float[] coords) {
            if (isDone()) {
                throw new NoSuchElementException(Messages.getString("awt.4B")); 
            }
            if (index == 5) {
                return SEG_CLOSE;
            }
            int type;
            int count;
            if (index == 0) {
                type = SEG_MOVETO;
                count = 1;
                double p[] = points[3];
                coords[0] = (float)(x + p[4] * width);
                coords[1] = (float)(y + p[5] * height);
            } else {
                type = SEG_CUBICTO;
                count = 3;
                int j = 0;
                double p[] = points[index - 1];
                for (int i = 0; i < 3; i++) {
                    coords[j] = (float)(x + p[j++] * width);
                    coords[j] = (float)(y + p[j++] * height);
                }
            }
            if (t != null) {
                t.transform(coords, 0, coords, 0, count);
            }
            return type;
        }
    }
    protected Ellipse2D() {
    }
    public boolean contains(double px, double py) {
        if (isEmpty()) {
            return false;
        }
        double a = (px - getX()) / getWidth() - 0.5;
        double b = (py - getY()) / getHeight() - 0.5;
        return a * a + b * b < 0.25;
    }
    public boolean intersects(double rx, double ry, double rw, double rh) {
        if (isEmpty() || rw <= 0.0 || rh <= 0.0) {
            return false;
        }
        double cx = getX() + getWidth() / 2.0;
        double cy = getY() + getHeight() / 2.0;
        double rx1 = rx;
        double ry1 = ry;
        double rx2 = rx + rw;
        double ry2 = ry + rh;
        double nx = cx < rx1 ? rx1 : (cx > rx2 ? rx2 : cx);
        double ny = cy < ry1 ? ry1 : (cy > ry2 ? ry2 : cy);
        return contains(nx, ny);
    }
    public boolean contains(double rx, double ry, double rw, double rh) {
        if (isEmpty() || rw <= 0.0 || rh <= 0.0) {
            return false;
        }
        double rx1 = rx;
        double ry1 = ry;
        double rx2 = rx + rw;
        double ry2 = ry + rh;
        return contains(rx1, ry1) && contains(rx2, ry1) && contains(rx2, ry2) && contains(rx1, ry2);
    }
    public PathIterator getPathIterator(AffineTransform at) {
        return new Iterator(this, at);
    }
}
