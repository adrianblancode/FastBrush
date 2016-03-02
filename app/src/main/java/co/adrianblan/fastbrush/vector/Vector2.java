package co.adrianblan.fastbrush.vector;

/** Defines a vector in 2D space */
public class Vector2 {

    private float x;
    private float y;

    public Vector2(){}

    public Vector2(float x, float y) {
        set(x, y);
    }

    public Vector2(Vector2 v) {
        set(v.getX(), v.getY());
    }

    public void set(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    /** Returns a new object with the same member variables as the current object */
    public Vector2 clone() {
        return new Vector2(this);
    }

    /** Returns the euclidian length of the vector */
    public float length() {
        return (float) Math.sqrt(x * x + y * y);
    }

    /** Returns the euclidian distance of the vectors */
    public float distance(Vector2 vec) {
        float xDistance = this.x - vec.getX();
        float yDistance = this.y - vec.getY();

        return (float) Math.sqrt(xDistance * xDistance + yDistance * yDistance);
    }

    /** Returns a vector specifying the distance between both vectors */
    public Vector2 distanceVector(Vector2 vec) {
        return this.subtract(vec);
    }

    /** Returns a new vector that has the position of both vectors added together */
    public Vector2 add(Vector2 vec) {
        return new Vector2(x + vec.getX(), y + vec.getY());
    }

    /** Subtracts the given vector from the current vector */
    public Vector2 subtract (Vector2 vec) {
        return new Vector2(x - vec.getX(), y - vec.getY());
    }

    /** Returns a new vector that is the current vector scaled to a factor */
    public Vector2 scale(float scale) {
        return new Vector2(x * scale, y * scale);
    }

    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
