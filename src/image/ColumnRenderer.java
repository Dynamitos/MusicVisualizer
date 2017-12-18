package image;

import engine.math.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL33;

import java.nio.FloatBuffer;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.opengl.GL31.glDrawArraysInstanced;
import static org.lwjgl.opengl.GL33.glVertexAttribDivisor;

public class ColumnRenderer {
    private ColumnShader shader;
    private int vaoID;
    private int displacementBuffer;
    private int numColumns;
    private FloatBuffer valueBuffer;
    public ColumnRenderer(int numColumns, Line line)
    {
        this.numColumns = numColumns;
        shader = new ColumnShader();
        vaoID = glGenVertexArrays();
        int vertexBuffer = glGenBuffers();
        int positionBuffer = glGenBuffers();
        displacementBuffer = glGenBuffers();

        float width = Math.abs(line.end.x - line.start.x) / numColumns;
        float[] baseVertices = new float[]
        {
                -width/2.f, 1.f,
                width/2.f, 1.f,
                -width/2.f, -1.f,
                width/2.f, -1.f
        };
        float[] positions = new float[numColumns*2];
        float dx = (line.end.x-line.start.x)/numColumns;
        float dy = (line.end.y-line.start.y)/numColumns;
        for(int i = 0; i < positions.length; i+=2)
        {
            positions[i] = line.start.x+(i/2)*dx;
            positions[i+1] = line.start.y+(i/2)*dy;
        }
        FloatBuffer vertices = BufferUtils.createFloatBuffer(baseVertices.length);
        vertices.put(baseVertices);
        vertices.flip();


        FloatBuffer posBuffer = BufferUtils.createFloatBuffer(positions.length);
        posBuffer.put(positions);
        posBuffer.flip();

        valueBuffer = BufferUtils.createFloatBuffer(numColumns);

        glBindVertexArray(vaoID);
        glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0);

        glBindBuffer(GL_ARRAY_BUFFER, positionBuffer);
        glBufferData(GL_ARRAY_BUFFER, posBuffer, GL_STATIC_DRAW);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);

        glBindBuffer(GL_ARRAY_BUFFER, displacementBuffer);
        glBufferData(GL_ARRAY_BUFFER, valueBuffer, GL_STREAM_DRAW);
        glVertexAttribPointer(2, 1, GL_FLOAT, false, 0, 0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        glVertexAttribDivisor(0, 0);
        glVertexAttribDivisor(1, 1);
        glVertexAttribDivisor(2, 1);
        glBindVertexArray(0);
        shader.start();
        shader.loadColor(new Vector4f(1, 1, 1, 0.5f));
        shader.stop();
    }
    public void render(float[] data)
    {
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        shader.start();
        glBindVertexArray(vaoID);
        valueBuffer.put(data);
        valueBuffer.flip();
        glBindBuffer(GL_ARRAY_BUFFER, displacementBuffer);
        glBufferData(GL_ARRAY_BUFFER, BufferUtils.createFloatBuffer(numColumns), GL_STREAM_DRAW);
        glBufferSubData(GL_ARRAY_BUFFER, 0, valueBuffer);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);
        glDrawArraysInstanced(GL_TRIANGLE_STRIP, 0, 4, numColumns);
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glDisableVertexAttribArray(2);
        glBindVertexArray(0);
        shader.stop();
        glDisable(GL_BLEND);
    }
}
