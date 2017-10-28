package engine.entities;

/*
 * 		Effect.java
 * 		
 * 		Purpose:	Effect objects, drawn to screen
 * 		Notes:		
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
		
		if(time >= lifetime)
			deleted = true;
	}
}
