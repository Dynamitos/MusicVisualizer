package image;

import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IVideoPicture;
import engine.model.RawModel;
import engine.renderEngine.Dimension;
import engine.renderEngine.DisplayManager;
import engine.renderEngine.Loader;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;
import org.lwjgl.system.MemoryUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.*;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

public class OutputRenderer {
    private int targetBuffer;
    private int targetTexture;
    private static final float[] vertices = { -1f, -1f, 0f, 1f, -1f, 0f, -1f, 1f, 0.0f, 1f, 1f, 0f, };
    private static final float[] texCoords = { 0, 1, 1, 1, 0, 0, 1, 0 };
    private RawModel model;
    private OutputShader output;
    private IntBuffer buffer;
    private final double frameRate = 60f;
    private Dimension resolution;
    private IMediaWriter writer;
    private ExecutorService executor;
    private Future future;
    private int[] texBuffer;

    public OutputRenderer(Loader loader)
    {
        output = new OutputShader();
        model = loader.loadToVAO(vertices, texCoords);

        targetBuffer = createFrameBuffer();
        targetTexture = createTextureAttachment(DisplayManager.WIDTH, DisplayManager.HEIGHT);

        buffer = BufferUtils.createIntBuffer(DisplayManager.WIDTH * DisplayManager.HEIGHT);
        resolution = new Dimension(DisplayManager.WIDTH, DisplayManager.HEIGHT);
        writer = ToolFactory.makeWriter("D:\\General\\Files\\test.mp4");
        writer.addVideoStream(0, 0, ICodec.ID.CODEC_ID_MPEG4, resolution.getWIDTH()/2, resolution.getHEIGHT()/2);
        executor = Executors.newSingleThreadExecutor();
        texBuffer = new int[DisplayManager.WIDTH * DisplayManager.HEIGHT];
    }
    public void bindTarget() {
        bindFrameBuffer(targetBuffer, DisplayManager.WIDTH, DisplayManager.HEIGHT);
    }
    private void bindFrameBuffer(int frameBuffer, int width, int height) {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);// To make sure the texture
        // isn't bound
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, frameBuffer);
        GL11.glViewport(0, 0, width, height);
    }

    private int createFrameBuffer() {
        int frameBuffer = GL30.glGenFramebuffers();
        // generate name for frame buffer
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, frameBuffer);
        // create the framebuffer
        GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
        // indicate that we will always render to color attachment 0
        return frameBuffer;
    }

    private int createTextureAttachment(int width, int height) {
        int texture = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, width, height, 0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE,
                (ByteBuffer) null);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, texture, 0);
        return texture;
    }

    public void render(long timeStamp) {
        glReadPixels(0, 0, DisplayManager.WIDTH, DisplayManager.HEIGHT, GL_RGB, GL_UNSIGNED_INT, buffer);
        BufferedImage img = new BufferedImage(DisplayManager.WIDTH, DisplayManager.HEIGHT, BufferedImage.TYPE_3BYTE_BGR);
        long t1 = System.currentTimeMillis();
        buffer.get(texBuffer);
        buffer.flip();
        //for(int x = 0; x < img.getWidth(); ++x)
        //{
        //    for(int y = 0; y < img.getHeight(); ++y)
        //    {
        //        int index = x+(img.getWidth() * y);
        //        texBuffer[index] = buffer.get(index);
        //    }
        //}
        img.setRGB(0, 0, img.getWidth(), img.getHeight(), texBuffer, 0, img.getWidth());
        long t2 = System.currentTimeMillis();

        try{
            ImageIO.write(img, "PNG", new File("D:\\General\\Files\\test.png"));
        } catch (IOException e)
        {

        }
        //future = executor.submit(() -> writer.encodeVideo(0, img, timeStamp, TimeUnit.MILLISECONDS));

        //System.out.println("Rendertime: "+(t2-t1));
    }

    public void finish()
    {
        while (!future.isDone());
        writer.close();
    }
}
