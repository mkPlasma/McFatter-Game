package engine.screens;

import engine.graphics.SpriteCache;

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
	private GameScreen screen;
	
	public MainScreen mainScreen;
	public MapScreen mapScreen;
	
	private SpriteCache spriteCache;
	
	public void init(){
		mainScreen = new MainScreen();
		mapScreen = new MapScreen();
		
		spriteCache = new SpriteCache();
	}
	
	public void initScreen(){
		screen.init(spriteCache);
	}
	
	public void update(){
		screen.update();
	}
	
	public void render(){
		screen.render();
	}
	
	public void cleanup(){
		screen.cleanup();
	}
	
	public void setScreen(GameScreen screen){
		this.screen = screen;
	}
}
