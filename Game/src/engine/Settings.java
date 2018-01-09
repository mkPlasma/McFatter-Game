package engine;

/*
 * 		Settings.java
 * 		
 * 		Purpose:	Holds certain global variables.
 * 		Notes:		May be replaced by external definitions.
 * 					Will probably still keep variables after loading them.
 * 		
 */
public class Settings{
	
	// Scale of window/resolution
	// Default 640x480
	private static float windowScale = 1.5f;
	
	public static void setWindowScale(float windowScale){
		Settings.windowScale = windowScale;
	}
	
	public static float getWindowScale(){
		return windowScale;
	}
}
