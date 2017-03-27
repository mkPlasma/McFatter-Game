package engine.screens;

import engine.graphics.Renderer;

public abstract class GameScreen{
	protected Renderer r;
	
	public abstract void init();
	public abstract void update();
	public abstract void render();
}
