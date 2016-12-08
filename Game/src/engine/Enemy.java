package engine;

import java.awt.Color;
import java.awt.Graphics2D;

public class Enemy extends GameEntity{
	
	public Enemy(){
		
	}
	
	public void update(){
		
	}
	
	public void draw(Graphics2D g2d){
		g2d.setColor(Color.GREEN);
		g2d.fillOval((int)x - 5, (int)y - 5, 10, 10);
	}
}
