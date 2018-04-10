package engine.entities;

import java.util.ArrayList;

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
	
	public Background(TextureCache tc){
		this.tc = tc;
		
		camera = new BGEntity(null, 0, 0, 0);
		elements = new ArrayList<BGEntity>();
	}
	
	
	public abstract void init();
	public abstract void update();
	
	protected void updateElements(){
		for(BGEntity e:elements)
			e.update();
	}
	
	
	public BGEntity getCamera(){
		return camera;
	}
	
	public ArrayList<BGEntity> getElements(){
		return elements;
	}
}
