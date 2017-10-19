package engine.screens;

import engine.graphics.Renderer;

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
	protected Renderer r;
	
	public abstract void init();
	public abstract void update();
	public abstract void render();
	public abstract void cleanup();
}
