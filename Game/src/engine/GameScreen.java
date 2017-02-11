package engine;

import java.awt.Graphics2D;

public abstract class GameScreen{
	protected Renderer renderer;
	
	public abstract void init(Graphics2D g2d);
	public abstract void update();
	public abstract void draw();
}
