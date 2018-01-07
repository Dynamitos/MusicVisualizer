package image;

import clojure.lang.IFn;
import com.sun.org.apache.bcel.internal.generic.ICONST;
import com.xuggle.ferry.IBuffer;
import com.xuggle.ferry.RefCounted;
import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.IMediaViewer;
import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.*;
import com.xuggle.xuggler.video.IConverter;
import ddf.minim.AudioBuffer;
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
import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
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
    private ByteBuffer buffer;
    private final double frameRate = 60f;
    private Dimension resolution;
    private IMediaWriter writer;
    private IContainer container;
    private int audioStreamId = -1;
    private IStreamCoder audioCoder = null;
    private ThreadPoolExecutor executor;
    private byte[] texBuffer;

    public OutputRenderer(Loader loader, String song)
    {
        output = new OutputShader();
        model = loader.loadToVAO(vertices, texCoords);

        targetBuffer = createFrameBuffer();
        targetTexture = createTextureAttachment(DisplayManager.WIDTH, DisplayManager.HEIGHT);

        buffer = BufferUtils.createByteBuffer(DisplayManager.WIDTH * DisplayManager.HEIGHT * 3/*RGB*/);

        resolution = new Dimension(DisplayManager.WIDTH, DisplayManager.HEIGHT);

        container = IContainer.make();
        container.open(song, IContainer.Type.READ, null);
        int numStreams = container.getNumStreams();

        // and iterate through the streams to find the first audio stream
        for(int i = 0; i < numStreams; i++)
        {
            // Find the stream object
            IStream stream = container.getStream(i);
            // Get the pre-configured decoder that can decode this stream;
            IStreamCoder coder = stream.getStreamCoder();

            if (audioStreamId == -1 && coder.getCodecType() == ICodec.Type.CODEC_TYPE_AUDIO)
            {
                audioStreamId = i;
                audioCoder = coder;
            }
        }
        audioCoder.open(container.getMetaData(), null);

        writer = ToolFactory.makeWriter("D:\\General\\Files\\test.mp4");
        writer.addVideoStream(0, 0, ICodec.ID.CODEC_ID_MPEG4, resolution.getWIDTH(), resolution.getHEIGHT());
        writer.addAudioStream(1, 0, ICodec.ID.CODEC_ID_MP3, audioCoder.getChannels(), audioCoder.getSampleRate());

        executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        texBuffer = new byte[DisplayManager.WIDTH * DisplayManager.HEIGHT * 4];

        /*frame = new JFrame();
        JPanel panel = new JPanel();
        GridLayout layout = new GridLayout();
        frame.add(panel);
        frame.setLayout(layout);
        frame.setSize(1280, 720);
        frame.setVisible(true);
        g = panel.getGraphics();*/
    }
    public void bindTarget() {
        bindFrameBuffer(targetBuffer, DisplayManager.WIDTH, DisplayManager.HEIGHT);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
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
        int width  = DisplayManager.WIDTH;
        int height = DisplayManager.HEIGHT;
        glReadPixels(0, 0, width, height, GL_RGB, GL_UNSIGNED_BYTE, buffer);

        //BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        //texBuffer = ((DataBufferInt)(img.getData().getDataBuffer()

        for(int x = 0; x < width; ++x)
        {
            for(int y = 0; y < height; ++y)
            {
                int index = (x+(width * y)) * 3;
                byte r = buffer.get(index);
                byte g = buffer.get(index + 1);
                byte b = buffer.get(index + 2);
                //texBuffer[x + (width * (height - y - 1))] = (r << 16) + (g << 8) + b;
                texBuffer[index] = -127;
                texBuffer[index+1] = r;
                texBuffer[index+2] = g;
                texBuffer[index+3] = b;
            }
        }
        IVideoPicture picture = IVideoPicture.make(IPixelFormat.Type.ARGB, width, height);
        picture.put(texBuffer, 0, 0, texBuffer.length);
        //picture.setKeyFrame(true);
        picture.setComplete(true, IPixelFormat.Type.ARGB, width, height, timeStamp);
        //img.setRGB(0, 0, img.getWidth(), img.getHeight(), texBuffer, 0, img.getWidth());
        writer.encodeVideo(0, picture);
    }

    public void encodeAudio() {
        executor.shutdown();
        IPacket packet = IPacket.make();
        while(container.readNextPacket(packet) >= 0)
        {
            if (packet.getStreamIndex() == audioStreamId)
            {
                /*
                 * We allocate a set of samples with the same number of channels as the
                 * coder tells us is in this buffer.
                 *
                 * We also pass in a buffer size (1024 in our example), although Xuggler
                 * will probably allocate more space than just the 1024 (it's not important why).
                 */
                IAudioSamples samples = IAudioSamples.make(4096, 1);

                /*
                 * A packet can actually contain multiple sets of samples (or frames of samples
                 * in audio-decoding speak).  So, we may need to call decode audio multiple
                 * times at different offsets in the packet's data.  We capture that here.
                 */
                int offset = 0;

                /*
                 * Keep going until we've processed all data
                 */
                while(offset < packet.getSize())
                {
                    int bytesDecoded = audioCoder.decodeAudio(samples, packet, offset);
                    if (bytesDecoded < 0)
                        throw new RuntimeException("got error decoding audio in");
                    offset += bytesDecoded;
                    /*
                     * Some decoder will consume data in a packet, but will not be able to construct
                     * a full set of samples yet.  Therefore you should always check if you
                     * got a complete set of samples from the decoder
                     */
                    if (samples.isComplete())
                    {
                        // note: this call will block if Java's sound buffers fill up, and we're
                        // okay with that.  That's why we have the video "sleeping" occur
                        // on another thread.
                        writer.encodeAudio(1, samples);
                    }
                }
            }
        }
        System.out.println("Finished encoding audio");
    }
    public void finish()
    {
        writer.close();
        System.out.println("Finish");
    }

}
