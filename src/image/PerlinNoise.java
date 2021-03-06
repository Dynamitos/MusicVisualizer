package image;

import engine.math.Vector3f;
import engine.toolbox.Maths;

public class PerlinNoise {
    private float[][][][] grid;
    private float[] x0y0, x0y1, x1y0, x1y1, x0, x1, result;
    private Vector3f resultVec;
    private Vector3f defaultVec;
    private int dimension;

    private float[] lerp(float[] a, float[] b, float w, float[] r) {
        r[0] = a[0] * (1 - w) + b[0] * w;
        r[1] = a[1] * (1 - w) + b[1] * w;
        r[2] = a[2] * (1 - w) + b[2] * w;
        return r;
    }

    public Vector3f perlin(float x, float y, float z) {
        x+=dimension/2;
        y+=dimension/2;
        z+=dimension/2;
        if(x < 0 || y < 0 || z < 0 || x >= dimension-1 || y >= dimension-1 || z >= dimension-1)
            return defaultVec;

        int ix0 = (int) x;
        int ix1 = ix0 + 1;
        int iy0 = (int) y;
        int iy1 = iy0 + 1;
        int iz0 = (int) z;
        int iz1 = iz0 + 1;

        float sx = x - (float) ix0;
        float sy = y - (float) iy0;
        float sz = z - (float) iz0;

        float[] x0y0z0 = grid[ix0][iy0][iz0];
        float[] x0y0z1 = grid[ix0][iy0][iz1];
        float[] x0y1z0 = grid[ix0][iy1][iz0];
        float[] x0y1z1 = grid[ix0][iy1][iz1];
        float[] x1y0z0 = grid[ix1][iy0][iz0];
        float[] x1y0z1 = grid[ix1][iy0][iz1];
        float[] x1y1z0 = grid[ix1][iy1][iz0];
        float[] x1y1z1 = grid[ix1][iy1][iz1];

        lerp(x0y0z0, x0y0z1, sz, x0y0);
        lerp(x0y1z0, x0y1z1, sz, x0y1);
        lerp(x1y0z0, x1y0z1, sz, x1y0);
        lerp(x1y1z0, x1y1z1, sz, x1y1);

        lerp(x0y0, x0y1, sy, x0);
        lerp(x1y0, x1y1, sy, x1);

        lerp(x0, x1, sx, result);

        resultVec.x = result[0];
        resultVec.y = result[1];
        resultVec.z = result[2];
        return resultVec;
    }

    public PerlinNoise(int dimensions) {
        resultVec = new Vector3f();
        defaultVec = new Vector3f();
        x0y0 = new float[3];
        x0y1 = new float[3];
        x1y0 = new float[3];
        x1y1 = new float[3];
        x0 = new float[3];
        x1 = new float[3];
        result = new float[3];
        this.dimension = dimensions;
        grid = new float[dimensions][dimensions][dimensions][3];
        for (int x = 0; x < dimensions; ++x) {
            for (int y = 0; y < dimensions; ++y) {
                for(int z = 0; z < dimensions; ++z) {
                    grid[x][y][z][0] = Maths.random(-1, 1);
                    grid[x][y][z][1] = Maths.random(-1, 1);
                    grid[x][y][z][1] = Maths.random(-1, 1);
                }
            }
        }
    }
}
