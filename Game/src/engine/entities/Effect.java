package engine.entities;

/**
 * 
 * Effect object drawn to screen.
 * 
 * @author Daniel
 *
 */

public class Effect extends MovableEntity{
	
	private int lifetime;
	
	public Effect(EffectFrame frame, float x, float y){
		super(frame, x, y);
		lifetime = frame.getLifetime();
	}
	
	public void update(){
		super.update();
		
		if(lifetime > 0 && time >= lifetime)
			deleted = true;
	}
	
	public void setLifetime(int lifetime){
		this.lifetime = lifetime;
	}
}
