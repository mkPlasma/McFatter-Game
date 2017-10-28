package engine.screens;

import engine.graphics.Renderer;
import engine.graphics.TextureCache;

/*
 * 		GameScreen.java
 * 		
 * 		Purpose:	Game screen.
 * 		Notes:		Simple container for screen-specific
 * 					update and drawing methods.
 * 		
 * 		Children:	MainScreen.java, MapScreen.java
 * 		
 */

public abstract class GameScreen{
	
	protected TextureCache tc;
	protected Renderer r;
	
	public GameScreen(Renderer r, TextureCache sc){
		this.r = r;
		this.tc = sc;
	}
	
	public abstract void init();
	public abstract void update();
	public abstract void render();
	public abstract void cleanup();
}
