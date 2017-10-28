package engine.entities;

import content.EffectList;
import content.FrameList;
import engine.graphics.Sprite;

/*
 * 		Bullet.java
 * 		
 * 		Purpose:	Base bullet class.
 * 		Notes:		
 * 		
 * 		Children:	Laser.java
 * 		
 */

public class Bullet extends MovableEntity{
	
	// Player shots only
	protected int damage, dmgReduce;
	
	// Whether entity can collide
	protected boolean collisions = true;
	
	// How far outside screen to despawn
	protected int despawnRange = 32;
	
	private final FrameList frameList;
	
	// Despawn/spawn effect
	private Effect effect;
	
	// Customizable bullet sprite
	private Sprite sprite;
	private boolean spriteCloned;
	
	public Bullet(BulletFrame frame, float x, float y, float dir, float spd, FrameList frameList){
		super(frame, x, y, dir, spd);
		sprite = frame.getSprite();
		
		this.frameList = frameList;
		
		onCreate();
	}
	
	public Bullet(BulletFrame frame, float x, float y, float dir, float spd, int damage, int dmgReduce, FrameList frameList){
		super(frame, x, y, dir, spd);
		sprite = frame.getSprite();
		
		this.damage = damage;
		this.dmgReduce = dmgReduce;
		this.frameList = frameList;
		
		onCreate();
	}
	
	public void onCreate(){
		
	}
	
	public void onDestroy(){
		deleted = true;
		effect = new Effect(frameList.getEffect(EffectList.TYPE_CLOUD, ((BulletFrame)frame).getColor()%16), x, y);
	}
	
	public void update(){
		
		super.update();
		
		// Delete at borders
		if(x < 32 - despawnRange || x > 416 + despawnRange || y < 16 - despawnRange || y > 464 + despawnRange)
			deleted = true;
	}
	
	public void setFrame(BulletFrame frame){
		this.frame = frame;
	}
	
	public BulletFrame getFrame(){
		return (BulletFrame)frame;
	}
	
	public Sprite getSprite(){
		return sprite;
	}
	
	// Must clone sprite if modifying it
	public void cloneSprite(){
		
		if(spriteCloned)
			return;
		
		sprite = new Sprite(sprite);
		spriteCloned = true;
	}
	
	public int getHitboxSize(){
		return ((BulletFrame)frame).getHitboxSize();
	}
	
	public void setDamage(int damage){
		this.damage = damage;
	}
	
	public int getDamage(){
		return damage;
	}
	
	public void setDamageReduce(int dmgReduce){
		this.dmgReduce = dmgReduce;
	}
	
	public int getDamageReduce(){
		return dmgReduce;
	}

	public void setCollisions(boolean collisions){
		this.collisions = collisions;
	}
	
	public boolean collisionsEnabled(){
		return collisions;
	}
	
	public Effect getEffect(){
		return effect;
	}
}
