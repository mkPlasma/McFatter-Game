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
	
	private TextureCache tc;
	
	public void init(){
		tc = new TextureCache();
		
		r = new Renderer(tc);
		r.init();
		
		mainScreen = new MainScreen(r, tc);
		mapScreen = new MapScreen(r, tc);
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
