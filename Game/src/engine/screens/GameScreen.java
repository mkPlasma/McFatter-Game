package engine.screens;

import engine.graphics.Renderer;
import engine.graphics.SpriteCache;

/*
 * 		GameScreen.java
 * 		
 * 		Purpose:	Game screen.
 * 		Notes:		Simple container for screen-specific
 * 					update and drawing methods.
 * 		
 * 		Children:	MainScreen.java, MapScreen.java
 * 		
 * 		Last modified by:	Daniel
 * 		Date:				
 * 		Changes:			
 */

public abstract class GameScreen{
	
	protected SpriteCache sc;
	protected Renderer r;
	
	public GameScreen(Renderer r, SpriteCache sc){
		this.r = r;
		this.sc = sc;
	}
	
	public abstract void init();
	public abstract void update();
	public abstract void render();
	public abstract void cleanup();
}
