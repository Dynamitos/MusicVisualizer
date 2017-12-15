package image;

import engine.math.Vector3f;

public class PerlinNoise {
    private Vector3f[][] grid;

    private float lerp(float a, float b, float w) {
        return (1.0f - w) * a + w * b;
    }

    private float dotGridGradient(int ix, int iy, float x, float y) {
        float dx = x - (float) ix;
        float dy = y - (float) iy;

        return (dx * grid[ix][iy].x + dy * grid[ix][iy].y);
    }

    private float perlin(float x, float y, float z) {
        int x0 = (int) x;
        int x1 = x0 + 1;
        int y0 = (int) y;
        int y1 = y0 + 1;

        float sx = x - (float) x0;
        float sy = y - (float) y0;

        float n0, n1, ix0, ix1, value;
        n0 = dotGridGradient(x0, y0, x, y);
        n1 = dotGridGradient(x1, y0, x, y);
        ix0 = lerp(n0, n1, sx);
        n0 = dotGridGradient(x0, y1, x, y);
        n1 = dotGridGradient(x1, y1, x, y);
        ix1 = lerp(n0, n1, sx);
        value = lerp(ix0, ix1, sy);

        return value;
    }

    public PerlinNoise(int dimensions) {
        grid = new Vector3f[dimensions][dimensions];
        for (int x = 0; x < dimensions; ++x) {
            for (int y = 0; y < dimensions; ++y) {

                grid[x][y] = new Vector3f(
                        ParticleRenderer.random(-1, 1),
                        ParticleRenderer.random(-1, 1),
                        ParticleRenderer.random(-1, 1)).normalize();

            }
        }
    }
}
