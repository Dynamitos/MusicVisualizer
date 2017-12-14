package image;

import engine.math.Vector3f;

public class Wind {
    private Vector3f start;
    private Vector3f direction;
    private float strength;
    public Wind(Vector3f start, Vector3f direction)
    {
        this.start = start;
        this.direction = direction;
        this.strength = direction.length();
    }
    void influence(Particle p)
    {
        Vector3f m0m1 = start.subtract(p.position);
        float d = m0m1.cross(direction).length() / direction.length();

    }
}
