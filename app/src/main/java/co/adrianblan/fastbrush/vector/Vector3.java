package co.adrianblan.fastbrush.vector;

import android.support.v8.renderscript.Float3;

/** Defines a vector in 3D space */
public class Vector3 {

    private Float3 f3 = new Float3();
    public float[] vector = {0f, 0f, 0f, 1f};

    public Vector3(){}

    public Vector3(float x, float y, float z) {
        set(x, y, z);
    }

    public Vector3(Vector3 v) {
        set(v.getX(), v.getY(), v.getZ());
    }

    public void set(float x, float y, float z) {
        setX(x);
        setY(y);
        setZ(z);
    }

    public void set(Vector2 v, float z) {
        setX(v.getX());
        setY(v.getY());
        setZ(z);
    }

    public float getX() {
        return vector[0];
    }

    public void setX(float x) {
        vector[0] = x;
        f3.x = x;
    }

    public void addX(float x) {
        vector[0] += x;
        f3.x += x;
    }

    public float getY() {
        return vector[1];
    }

    public void setY(float y) {
        vector[1] = y;
        f3.y = y;
    }

    public void addY(float y) {
        vector[1] += y;
        f3.y += y;
    }

    public float getZ() {
        return vector[2];
    }

    public void setZ(float z) {
        vector[2] = z;
        f3.z = z;
    }

    public void addZ(float z) {
        vector[2] += z;
        f3.z += z;
    }

    public float getW(){
        return vector[3];
    }

    /** Returns a new object with the same member variables as the current object */
    public Vector3 clone() {
        return new Vector3(this);
    }

    /** Returns the euclidian length of the vector */
    public float length() {
        return (float) Math.sqrt(getX() * getX() + getY() * getY() + getZ() * getZ());
    }

    /** Returns the euclidian distance of the vectors */
    public float distance(Vector3 vec) {
        float xDistance = this.getX() - vec.getX();
        float yDistance = this.getY() - vec.getY();
        float zDistance = this.getZ() - vec.getZ();

        return (float) Math.sqrt(xDistance * xDistance + yDistance * yDistance + zDistance * zDistance);
    }

    /** Returns a vector specifying the distance between both vectors */
    public Vector3 distanceVector(Vector3 vec) {
        return this.subtract(vec);
    }

    /** Returns a new vector that has the position of both vectors added together */
    public Vector3 add(Vector3 vec) {
        return new Vector3(getX() + vec.getX(), getY() + vec.getY(), getZ() + vec.getZ());
    }

    /** Adds the vector in the current vector */
    public void addFast(Vector3 vec) {
        vector[0] += vec.vector[0];
        vector[1] += vec.vector[1];
        vector[2] += vec.vector[2];
    }

    /** Sums the two vectors in the current vector */
    public void addFast(Vector3 vec1, Vector3 vec2) {
        vector[0] = vec1.vector[0] + vec2.vector[0];
        vector[1] = vec1.vector[1] + vec2.vector[1];
        vector[2] = vec1.vector[2] + vec2.vector[2];
    }

    /** Subtracts the given vector from the current vector */
    public Vector3 subtract (Vector3 vec) {
        return new Vector3(getX() - vec.getX(), getY() - vec.getY(), getZ() - vec.getZ());
    }

    /** Returns a new vector that is the current vector scaled to a factor */
    public Vector3 scale(float scale) {
        return new Vector3(getX() * scale, getY() * scale, getZ() * scale);
    }

    public Float3 getFloat3() {
        return f3;
    }

    public String toString() {
        return "(" + getX() + ", " + getY() + ", " + getZ() + ", " + getW() + ")";
    }
}
