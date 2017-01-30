package image;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_LINE_STRIP;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.GL_STREAM_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glBufferSubData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;

import engine.math.Vector4f;
import engine.sound.MasterSound;

public class LineRenderer {
	private int vaoID;
	private int displacementVBO;
	private LineShader shader;
	private Vector4f lineColor;
	FloatBuffer dataBuffer;
	public LineRenderer(int numPoints, Line line)
	{
		this.lineColor = line.color;
		line.start.x = line.start.x*2-1;
		line.start.y = -1*(line.start.y*2-1);
		line.end.x = line.end.x*2-1;
		line.end.y = -1*(line.end.y*2-1);
		shader = new LineShader();
		vaoID = glGenVertexArrays();
		glBindVertexArray(vaoID);
		int baseVBO = glGenBuffers();
		float[] baseVertices = new float[numPoints*3];
		float dx = (line.end.x-line.start.x)/numPoints;
		float dy = (line.end.y-line.start.y)/numPoints;
		for(int i = 0; i < baseVertices.length; i+=3)
		{
			baseVertices[i] = line.start.x+(i/3)*dx;
			baseVertices[i+1] = line.start.y+(i/3)*dy;
			baseVertices[i+2] = -1;
		}
		FloatBuffer buffer = BufferUtils.createFloatBuffer(baseVertices.length);
		buffer.put(baseVertices);
		buffer.flip();
		glBindBuffer(GL_ARRAY_BUFFER, baseVBO);
		glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		displacementVBO = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, displacementVBO);
		glBufferData(GL_ARRAY_BUFFER, BufferUtils.createFloatBuffer(numPoints), GL_STREAM_DRAW);
		glVertexAttribPointer(1, 1, GL_FLOAT, false, 0, 0);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindVertexArray(0);
		dataBuffer = BufferUtils.createFloatBuffer(MasterRenderer.NUM_SAMPLES * MasterSound.TESS_LEVEL);
	}
	public void render(float[] data)
	{
		shader.start();
		shader.loadColor(lineColor);
		glBindVertexArray(vaoID);
		glBindBuffer(GL_ARRAY_BUFFER, displacementVBO);
		dataBuffer.put(data);
		dataBuffer.flip();
		glBufferData(GL_ARRAY_BUFFER, BufferUtils.createFloatBuffer(data.length), GL_STREAM_DRAW);
		glBufferSubData(GL_ARRAY_BUFFER, 0, dataBuffer);
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		glDrawArrays(GL_LINE_STRIP, 0, data.length);
		glDisableVertexAttribArray(0);
		glDisableVertexAttribArray(1);
		shader.stop();
	}
}
