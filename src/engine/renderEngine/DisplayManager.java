package engine.renderEngine;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_1;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_SAMPLES;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwGetCursorPos;
import static org.lwjgl.glfw.GLFW.glfwGetMouseButton;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPos;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetScrollCallback;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL13.GL_MULTISAMPLE;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.nio.DoubleBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

import engine.math.Vector3f;
import engine.toolbox.Input;
import engine.toolbox.MouseInput;

public class DisplayManager {

	public static long window;

	public static int WIDTH = 9001;
	public static int HEIGHT = 9001;

	private static long lastFrameTime;
	private static float delta;

	private static double newX = 400;
	private static double newY = 300;

	private static boolean mouseLocked = false;

	private static float mouseSpeed = 0.1f;
	private static int nbFrames;
	private static float lastPrintTime;

	public static Input input;
	public static MouseInput mouseWheel;
	public static Vector3f lightPosition;

	public static float deltaX, deltaY;
	public static boolean buffering = false;

	public static void setDimension(Dimension dimension) {
		WIDTH = dimension.getWIDTH();
		HEIGHT = dimension.getHEIGHT();
	}

	public static void createDisplay() {
		// System.setProperty("java.library.path", "C:\\Program
		// Files\\Java\\lwjgl\\native");
		if (!glfwInit())
			throw new IllegalStateException();
		glfwWindowHint(GLFW_SAMPLES, 4);
		// glfwWindowHint(GLFW_STEREO, GL_TRUE);

		GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
		boolean full = false;
		if (vidMode.width() == WIDTH && vidMode.height() == HEIGHT)
			full = true;
		window = glfwCreateWindow(WIDTH, HEIGHT, "Music Visualizer", (full) ? glfwGetPrimaryMonitor() : NULL, NULL);
		mouseWheel = new MouseInput();
		input = new Input();
		glfwSetKeyCallback(window, input);
		glfwSetScrollCallback(window, mouseWheel);
		// glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
		glfwShowWindow(window);
		glfwMakeContextCurrent(window);
		GL.createCapabilities();
		glfwSwapInterval(0);
		glEnable(GL_MULTISAMPLE);
		lastFrameTime = System.currentTimeMillis();
		lightPosition = new Vector3f(0, 0, -1);
		lastPrintTime = (float) glfwGetTime();

	}

	public static void updateDisplay() {
		glfwPollEvents();
		double currentTime = glfwGetTime();
		nbFrames++;
		if (currentTime - lastPrintTime >= 1.0f) {
			System.out.println(1000.0f / (double) nbFrames + " ms per frame");
			nbFrames = 0;
			lastPrintTime += 1.0f;
		}
		glfwSwapBuffers(window);
		// updateMouse();
		long currentFrameTime = System.currentTimeMillis();
		delta = (currentFrameTime - lastFrameTime) / 1000f;
		lastFrameTime = currentFrameTime;
	}

	@SuppressWarnings("unused")
	private static void updateMouse() {
		if (glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_1) == GLFW_PRESS) {
			glfwSetCursorPos(window, WIDTH / 2, HEIGHT / 2);

			mouseLocked = true;
		}

		DoubleBuffer x = BufferUtils.createDoubleBuffer(1);
		DoubleBuffer y = BufferUtils.createDoubleBuffer(1);

		glfwGetCursorPos(window, x, y);

		x.rewind();
		y.rewind();

		newX = x.get();
		newY = y.get();

		deltaX = (float) (newX - (WIDTH / 2)) * mouseSpeed;
		deltaY = (float) (newY - (HEIGHT / 2)) * mouseSpeed;

		glfwSetCursorPos(window, WIDTH / 2, HEIGHT / 2);

	}

	public static float getFrameTimeSeconds() {
		return delta;
	}

	public static void closeDisplay() {
		glfwDestroyWindow(window);
		glfwTerminate();
	}

	public static boolean shouldClose() {
		return glfwWindowShouldClose(window);
	}

	public static double getNewX() {
		return newX;
	}

	public static void setNewX(double newX) {
		DisplayManager.newX = newX;
	}

	public static double getNewY() {
		return newY;
	}

	public static void setNewY(double newY) {
		DisplayManager.newY = newY;
	}

	public static boolean isMouseLocked() {
		return mouseLocked;
	}

	public static void setMouseLocked(boolean mouseLocked) {
		DisplayManager.mouseLocked = mouseLocked;
	}

	public static float getMouseSpeed() {
		return mouseSpeed;
	}

	public static void setMouseSpeed(float mouseSpeed) {
		DisplayManager.mouseSpeed = mouseSpeed;
	}

}
