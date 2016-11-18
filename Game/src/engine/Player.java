package engine;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

public class Player extends GameEntity{
	
	private final KeyboardListener keyListener;
	
	public Player(int x, int y, KeyboardListener keyListener){
		super(x, y);
		this.keyListener = keyListener;
	}
	
	private int speed = 5;
	
	public void update(){
		
		if(keyListener.isKeyPressed(KeyEvent.VK_D)){
			x += speed;
		}
		if(keyListener.isKeyPressed(KeyEvent.VK_A)){
			x -= speed;
		}
		if(keyListener.isKeyPressed(KeyEvent.VK_S)){
			y += speed;
		}
		if(keyListener.isKeyPressed(KeyEvent.VK_W)){
			y -= speed;
		}
		
	}
	
	public void draw(Graphics2D g2d){
		g2d.setColor(Color.RED);
		g2d.fillRect(x - 5, y - 5, 10, 10);
	}
}
