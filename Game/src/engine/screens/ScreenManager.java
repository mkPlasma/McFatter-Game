package engine.screens;

/*
 * 		ScreenManager.java
 * 		
 * 		Purpose:	Holds instances of various game screens.
 * 		Notes:		
 * 		
 * 		Last modified by:	Daniel
 * 		Date:				
 * 		Changes:			
 */

public class ScreenManager{
	
	// Active screen
	private static GameScreen screen;
	
	public static MainScreen scrnMain;
	public static MapScreen scrnMap;
	
	public static void init(){
		scrnMain = new MainScreen();
		scrnMap = new MapScreen();
	}
	
	public static void initScreen(){
		screen.init();
	}
	
	public static void update(){
		screen.update();
	}
	
	public static void render(){
		screen.render();
	}
	
	public static void setScreen(GameScreen screen){
		ScreenManager.screen = screen;
	}
	
	public static GameScreen getGameScreen(){
		return screen;
	}
}
