package engine;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

public class Renderer{
	
	private final Graphics2D g2d;
	
	// Resolution scale
	private float scale;
	
	public Renderer(Graphics2D g2d){
		this.g2d = g2d;
		
		updateScale();
	}
	
	public void render(Sprite spr, float x, float y, float rot, float sc){
		
		AffineTransform t = new AffineTransform();
		
		// Set sprite to location
		t.setToTranslation(x*scale, y*scale);
		
		// Set rotation
		t.rotate(Math.toRadians(rot));
		
		// Set scale
		t.scale(sc*scale, sc*scale);
		
		// Set transform to center
		t.translate(-spr.getWidth()/2, -spr.getHeight()/2);
		
		// Draw sprite
		//g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
		g2d.drawImage(spr.getImg(), t, null);
	}
	
	public void drawCircle(int x, int y, int w, int h, Color c){
		g2d.setColor(c);
		g2d.fillOval((int)(x*scale), (int)(y*scale), (int)(w*scale), (int)(h*scale));
	}
	
	public void drawRectangle(int x, int y, int w, int h, Color c){
		g2d.setColor(c);
		g2d.fillRect((int)(x*scale), (int)(y*scale), (int)(w*scale), (int)(h*scale));
	}
	
	public void updateScale(){
		scale = Settings.getWindowScale();
	}
}
