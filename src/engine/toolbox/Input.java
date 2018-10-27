package engine.toolbox;

import org.lwjgl.glfw.GLFWKeyCallback;

import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;


public class Input extends GLFWKeyCallback{

	public static boolean[] keys = new boolean[65536];
	
	@Override
	public void invoke(long window, int key, int scancode, int action, int mods) {
		if(key==-1)
			return;
		keys[key] = action != GLFW_RELEASE;
	}
}
