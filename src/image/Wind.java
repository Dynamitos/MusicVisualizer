package image;

import engine.math.Vector3f;

public class Wind {
    private Vector3f start;
    private Vector3f direction;
    private float strength;
    public Wind(Vector3f start, Vector3f direction)
    {
        this.start = start;
        this.strength = direction.length();
        this.direction = direction.normalize();
    }
    void influence(Particle p)
    {
        Vector3f m0m1 = start.subtract(p.position);
        float scalar = direction.dot(m0m1) / direction.dot(direction);
        Vector3f g = start.add(direction.scale(scalar));
        Vector3f partToG = g.subtract(p.position);
        Vector3f drag = partToG.scale(1 / partToG.length()).add(direction).scale(strength);
        p.speed.add(drag);
    }
}
