package engine.entities;

import java.util.ArrayList;
import java.util.Collections;

import engine.graphics.TextureCache;

/**
 * 
 * Stage background container.
 * 
 * @author Daniel
 *
 */

public abstract class Background{
	
	protected final TextureCache tc;
	
	protected BGEntity camera;
	protected ArrayList<BGEntity> elements;
	
	// Fog start/max range
	protected int fogStart, fogMax;
	
	// Fog color
	protected float fogR, fogG, fogB, fogA;
	
	protected int time;
	
	private final BGComparator comp;
	
	public Background(TextureCache tc){
		this.tc = tc;
		
		camera = new BGEntity(null, 0, 0, 0);
		elements = new ArrayList<BGEntity>();
		
		comp = new BGComparator();
	}
	
	
	public abstract void init();
	public abstract void update();
	
	protected void updateElements(){
		
		camera.update();
		
		for(BGEntity e:elements)
			e.update();
	}
	
	protected void sortElements(){
		Collections.sort(elements, comp);
	}
	
	public BGEntity getCamera(){
		return camera;
	}
	
	public ArrayList<BGEntity> getElements(){
		return elements;
	}
	
	public void setFogRange(int start, int max){
		fogStart = start;
		fogMax = max;
	}
	
	public int getFogStart(){
		return fogStart;
	}
	
	public int getFogMax(){
		return fogMax;
	}
	
	public void setFogColor(float r, float g, float b, float a){
		fogR = r;
		fogG = g;
		fogB = b;
		fogA = a;
	}
	
	public float getFogR(){
		return fogR;
	}
	
	public float getFogG(){
		return fogG;
	}
	
	public float getFogB(){
		return fogB;
	}
	
	public float getFogA(){
		return fogA;
	}
}
