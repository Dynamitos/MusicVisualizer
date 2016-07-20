package engine.toolbox;

import org.lwjgl.glfw.GLFWScrollCallback;

public class MouseInput extends GLFWScrollCallback{

	public static double rotation = 0;
	
	@Override
	public void invoke(long window, double xoffset, double yoffset) {
		rotation = yoffset;
	}

}
