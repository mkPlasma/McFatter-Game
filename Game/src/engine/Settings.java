package engine;

/*
 * 		Settings.java
 * 		
 * 		Purpose:	Holds certain global variables.
 * 		Notes:		May be replaced by external definitions.
 * 					Will probably still keep variables after loading them.
 * 		
 * 		Last modified by:	Daniel
 * 		Date:				9/24
 * 		Changes:			Removed hardwareAcceleration, unnecessary
 */
public class Settings{
	
	// Scale of window/resolution
	// Default 640x480
	private static float windowScale = 2;
	
	public static void setWindowScale(float windowScale){
		Settings.windowScale = windowScale;
	}
	
	public static float getWindowScale(){
		return windowScale;
	}
}
