package engine;

public class Settings{
	
	// If true, use VolatileImage rather than BufferedImage
	// VolatileImage is faster in some cases
	private static boolean hardwareAcceleration = true;
	
	// Scale of window/resolution
	// Default 800x600
	private static float windowScale = 1.5f;
	
	public static void setHardwareAcceleration(boolean hwa){
		hardwareAcceleration = hwa;
	}
	
	public static boolean useHardwareAcceleration(){
		return hardwareAcceleration;
	}
	
	public static void setWindowScale(float windowScale){
		Settings.windowScale = windowScale;
	}
	
	public static float getWindowScale(){
		return windowScale;
	}
}
