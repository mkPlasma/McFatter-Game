package engine;

import java.awt.Graphics2D;

public abstract class GameScreen{
	public abstract void init(KeyboardListener keyListener);
	public abstract void update();
	public abstract void draw(Graphics2D g2d);
}
