package engine;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

public class Player extends GameEntity{
	
	private final KeyboardListener keyListener;
	
	
	private boolean alive = true;
	
	
	private final int focusedSpeed = 2, unfocusedSpeed = 4;
	
	private int speed;
	private boolean focused = false;
	
	public Player(float x, float y, KeyboardListener keyListener){
		super(x, y);
		this.keyListener = keyListener;
	}
	
	public void update(){
		
		if(alive)
			updateMovement();
	}
	
	private void updateMovement(){
		
		speed = unfocusedSpeed;
		
		if(focused)
			speed = focusedSpeed;
		
		// Movement
		if(keyListener.isKeyPressed(KeyEvent.VK_RIGHT))
			x += speed;
		else if(keyListener.isKeyPressed(KeyEvent.VK_LEFT))
			x -= speed;
		if(keyListener.isKeyPressed(KeyEvent.VK_DOWN))
			y += speed;
		else if(keyListener.isKeyPressed(KeyEvent.VK_UP))
			y -= speed;
		
		
		if(keyListener.isKeyPressed(KeyEvent.VK_SHIFT))
			focused = true;
		else if(keyListener.isKeyReleased(KeyEvent.VK_SHIFT))
			focused = false;
	}
	
	public void draw(Graphics2D g2d){
		g2d.setColor(Color.RED);
		g2d.fillOval((int)x - 5, (int)y - 5, 10, 10);
	}
	
	
	public void death(){
		alive = false;
	}
	
	public boolean isAlive(){
		return alive;
	}
	
	public int getHitboxSize(){
		return 3;
	}
}
