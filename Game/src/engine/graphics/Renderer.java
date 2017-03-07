package engine.graphics;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import engine.Settings;

public class Renderer{
	
	private final Graphics2D g2d;
	
	// Resolution scale
	private float scale;
	
	public Renderer(Graphics2D g2d){
		this.g2d = g2d;
		
		updateScale();
	}
	
	public void render(Sprite sprs, int time, float x, float y, float alpha, float rot, float scx, float scy){
		
		AffineTransform t = new AffineTransform();
		
		Sprite spr = sprs.animate(time);
		
		// Set sprite to location
		t.setToTranslation(x*scale, y*scale);
		
		// Set rotation
		t.rotate(Math.toRadians(spr.getRotation() + rot));
		
		// Set scale
		t.scale(spr.getScaleX()*scale*scx, spr.getScaleY()*scale*scy);
		
		// Set transform to center
		t.translate(-spr.getWidth()/2, -spr.getHeight()/2);
		
		// Draw sprite
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, spr.getAlpha()*alpha));
		g2d.drawImage(spr.getImg(), t, null);
		
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
	}
	
	public void render(Sprite spr, int time, float x, float y){
		render(spr, time, x, y, 1, 0, 1, 1);
	}
	
	public void drawCircle(int x, int y, int w, int h, Color c){
		g2d.setColor(c);
		g2d.fillOval((int)(x*scale), (int)(y*scale), (int)(w*scale), (int)(h*scale));
	}
	
	public void drawRectangle(int x, int y, int w, int h, Color c){
		g2d.setColor(c);
		g2d.fillRect((int)(x*scale), (int)(y*scale), (int)(w*scale), (int)(h*scale));
	}

	public void drawLine(int x1, int y1, int x2, int y2, Color c){
		g2d.setColor(c);
		g2d.drawLine((int)(x1*scale), (int)(y1*scale), (int)(x2*scale), (int)(y2*scale));
	}
	
	// Gets window scale from settings
	// Use on launch or when resolution is changed
	public void updateScale(){
		scale = Settings.getWindowScale();
	}
}
