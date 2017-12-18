package image;

import org.lwjgl.opengl.GL15;

import java.util.List;

import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class ColumnRenderer {
    private ColumnShader shader;
    private int vaoID;
    private int displacementVBO;
    public ColumnRenderer(int numColumns, Line lines)
    {
        vaoID = glGenVertexArrays();
        int baseVBO = glGenBuffers();
        int transformVBO = glGenBuffers();
        float[] baseVertices = new float[8];
    }
}
