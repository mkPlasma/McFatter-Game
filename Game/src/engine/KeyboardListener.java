package engine;

import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import org.lwjgl.glfw.GLFWKeyCallback;

/**
 * 
 * GLFW key listener class for keyboard input.
 * 
 * @author Daniel
 *
 */

public class KeyboardListener extends GLFWKeyCallback{
	
	// Holds booleans for key presses
	private static boolean keys[] = new boolean[65536];
	private static boolean pressed[] = new boolean[65536];
	
	public void invoke(long window, int key, int scancode, int action, int mods){
		keys[key] = action != GLFW_RELEASE;
		pressed[key] = keys[key];
	}
	
	public static boolean isKeyDown(int key){
		return keys[key];
	}

	public static boolean isKeyUp(int key){
		return !keys[key];
	}
	
	public static boolean isKeyPressed(int key){
		if(pressed[key]){
			pressed[key] = false;
			return true;
		}
		return false;
	}
}
