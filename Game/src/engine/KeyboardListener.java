package engine;

import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import org.lwjgl.glfw.GLFWKeyCallback;

/*
 * 		KeyboardListener.java
 * 		
 * 		Purpose:	Key listener used for user input.
 * 		Notes:		May be changed to store specific inputs (firing, moving) which
 * 					can be modified by the user.
 * 		
 * 		Last modified by:	Daniel
 * 		Date:				
 * 		Changes:			
 */

public class KeyboardListener extends GLFWKeyCallback{
	
	// Holds booleans for key presses
	private static boolean keys[] = new boolean[65536];
	
	public void invoke(long window, int key, int scancode, int action, int mods){
		keys[key] = action != GLFW_RELEASE;
	}
	
	public static boolean isKeyDown(int key){
		return keys[key];
	}

	public static boolean isKeyUp(int key){
		return !keys[key];
	}
}
