package engine.entities;

import engine.graphics.Sprite;

public class EffectFrame extends EntityFrame{
	
	// Time until effect despawns
	private final int lifetime;
	
	public EffectFrame(int type, Sprite sprite, boolean spriteAlign, float spriteRotation, int lifetime){
		super(type, sprite, spriteAlign, spriteRotation);
		this.lifetime = lifetime;
	}
	
	public int getLifetime(){
		return lifetime;
	}
}
