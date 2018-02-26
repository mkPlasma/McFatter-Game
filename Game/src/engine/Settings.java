package engine;

/**
 * 
 * Static variables for game settings.
 * 
 * @author Daniel
 *
 */

public class Settings{
	
	// Scale of window/resolution
	// Default 640x480
	private static float windowScale = 2f;
	
	public static void setWindowScale(float windowScale){
		Settings.windowScale = windowScale;
	}
	
	public static float getWindowScale(){
		return windowScale;
	}
}
