package engine.screens;

import engine.graphics.Renderer;
import engine.graphics.TextureCache;

/**
 * 
 * Abstract game screen with update and drawing methods.
 * 
 * @author Daniel
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
