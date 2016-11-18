package engine;

import java.awt.Color;
import java.awt.Graphics2D;

public class WorldScreen extends GameScreen{
	
	private KeyboardListener keyListener;
	private Player player;
	
	public void init(KeyboardListener keyListener){
		this.keyListener = keyListener;
		player = new Player(400, 400, keyListener);
	}

	public void update(){
		player.update();
	}

	public void draw(Graphics2D g2d){
		g2d.setColor(Color.BLACK);
		g2d.fillRect(0, 0, 800, 600);
		player.draw(g2d);
	}
}
