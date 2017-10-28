package engine.screens;

import engine.graphics.Renderer;
import engine.graphics.TextureCache;

/*
 * 		ScreenManager.java
 * 		
 * 		Purpose:	Holds instances of various game screens.
 * 		Notes:		
 * 		
 */

public class ScreenManager{
	
	// Active screen
	private GameScreen screen;
	
	private Renderer r;
	
	public MainScreen mainScreen;
	public MapScreen mapScreen;
	
	private TextureCache spriteCache;
	
	public void init(){
		r = new Renderer();
		r.init();
		
		spriteCache = new TextureCache();
		
		mainScreen = new MainScreen(r, spriteCache);
		mapScreen = new MapScreen(r, spriteCache);
	}
	
	public void initScreen(){
		screen.init();
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
