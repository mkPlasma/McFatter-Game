package engine.entities;

import engine.graphics.Renderer;

public class Effect extends MovableEntity{
	
	private EntityFrame frame;
	
	public Effect(MovementInstruction inst, EntityFrame frame){
		this.frame = frame;
		
		visible = true;
		
		inst.setEntity(this);
		this.inst = new InstructionSet(inst);
		this.inst.init();
	}
	
	public Effect(InstructionSet inst, EntityFrame frame){
		this.frame = frame;
		
		visible = true;
		
		inst.setEntity(this);
		this.inst = inst;
		inst.init();
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
