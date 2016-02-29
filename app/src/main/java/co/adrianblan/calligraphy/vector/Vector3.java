package co.adrianblan.calligraphy.vector;

/** Defines a vector in 3D space */
public class Vector3 {

    private float x;
    private float y;
    private float z;

    public Vector3(){}

    public Vector3(float x, float y, float z) {
        set(x, y, z);
    }

    public Vector3(Vector3 v) {
        set(v.getX(), v.getY(), v.getZ());
    }

    public void set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void set(Vector2 v, float z) {
        this.x = v.getX();
        this.y = v.getY();
        this.z = z;
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

    public float getZ() {
        return z;
    }

    public void setZ(float z) {
        this.z = z;
    }

    /** Returns a new object with the same member variables as the current object */
    public Vector3 clone() {
        return new Vector3(this);
    }

    /** Returns the euclidian length of the vector */
    public float length() {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    /** Returns the euclidian distance of the vectors */
    public float distance(Vector3 vec) {
        float xDistance = this.x - vec.getX();
        float yDistance = this.y - vec.getY();
        float zDistance = this.z - vec.getZ();

        return (float) Math.sqrt(xDistance * xDistance + yDistance * yDistance + zDistance * zDistance);
    }

    /** Returns a vector specifying the distance between both vectors */
    public Vector3 distanceVector(Vector3 vec) {
        return this.subtract(vec);
    }

    /** Returns a new vector that has the position of both vectors added together */
    public Vector3 add(Vector3 vec) {
        return new Vector3(x + vec.getX(), y + vec.getY(), z + vec.getZ());
    }

    /** Subtracts the given vector from the current vector */
    public Vector3 subtract (Vector3 vec) {
        return new Vector3(x - vec.getX(), y - vec.getY(), z - vec.getZ());
    }

    /** Returns a new vector that is the current vector scaled to a factor */
    public Vector3 scale(float scale) {
        return new Vector3(x * scale, y * scale, z * scale);
    }

    public String toString() {
        return "(" + x + ", " + y + ", " + z + ")";
    }

    public float[] toFloatArray () {
        float [] ret = {x, y, z};
        return ret;
    }
}
