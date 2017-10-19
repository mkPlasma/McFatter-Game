package engine.entities;

/*
 * 		Effect.java
 * 		
 * 		Purpose:	Effect objects, drawn to screen
 * 		Notes:		Not completely implemented, effects do
 * 					not render and cannot be generated easily.
 * 		
 * 		Last modified by:	Daniel
 * 		Date:				
 * 		Changes:			
 */

public class Effect extends MovableEntity{
	
	public Effect(EntityFrame frame){
		this.frame = frame;
		
		visible = true;
	}
	
	public void onCreate(){
		
	}
	
	public void onDestroy(){
		remove = true;
	}
	
	public void update(){
		updateMovements();
		time++;
	}
	
	public void render(){
		if(!visible)
			return;
		
		if(frame.spriteAlign())
			frame.getSprite().rotate(dir + 90);
		
		//Renderer.render(frame.getSprite(), time, x, y);
	}
	
}
