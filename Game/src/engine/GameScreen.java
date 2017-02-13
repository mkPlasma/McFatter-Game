package engine;

import java.awt.Graphics2D;

import engine.graphics.Renderer;

public abstract class GameScreen{
	protected Renderer r;
	
	public abstract void init(Graphics2D g2d);
	public abstract void update();
	public abstract void draw();
}
