package engine.entities;

import content.BulletList;
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
	
	// Properties
	protected int type, color;
	protected int hitboxSize;
	
	protected BulletFrame frame;
	
	// Customizable bullet sprite
	protected Sprite sprite;
	protected boolean spriteCloned;
	
	// Player shots only
	protected int damage, dmgReduce;
	
	// Whether entity can collide
	protected boolean collisions;
	
	// Despawn at screen borders
	protected boolean borderDespawn;
	
	// How far outside screen to despawn
	protected int despawnRange = 32;
	
	protected final FrameList frameList;
	
	// Despawn/spawn effect
	protected Effect effect;
	
	
	public Bullet(BulletFrame frame, float x, float y, float dir, float spd, FrameList frameList){
		super(frame, x, y, dir, spd);

		this.frame = frame;
		this.frameList = frameList;
		
		init();
		onCreate();
	}
	
	public Bullet(BulletFrame frame, float x, float y, float dir, float spd, int damage, int dmgReduce, FrameList frameList){
		super(frame, x, y, dir, spd);
		
		this.frame = frame;
		this.damage = damage;
		this.dmgReduce = dmgReduce;
		this.frameList = frameList;
		
		init();
		onCreate();
	}
	
	private void init(){
		collisions = true;
		borderDespawn = true;
		initFrameProperties();
	}
	
	public void initFrameProperties(){
		type			= frame.getType();
		color			= frame.getColor();
		hitboxSize		= frame.getHitboxSize();
		sprite			= frame.getSprite();
	}
	
	public void onCreate(){
		
	}
	
	public void onDestroy(){
		deleted = true;
		effect = new Effect(frameList.getEffect(EffectList.TYPE_CLOUD, color%16), x, y);
	}
	
	public void update(){
		
		super.update();
		
		// Delete at borders
		if(borderDespawn && x < 32 - despawnRange || x > 416 + despawnRange || y < 16 - despawnRange || y > 464 + despawnRange)
			deleted = true;
	}
	
	public void refreshSprite(){
		sprite.setTextureCoords(frameList.bulletList.getSprite(type, color).getTextureCoords());
	}
	
	
	public void setType(int type){
		this.type = type;
	}
	
	public int getType(){
		return type;
	}
	
	public void setColor(int color){
		this.color = color;
	}
	
	public int getColor(){
		return color;
	}
	
	public void setHitboxSize(int hitboxSize){
		this.hitboxSize = hitboxSize;
	}
	
	public int getHitboxSize(){
		return hitboxSize;
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
	
	public void setFrame(BulletFrame frame){
		this.frame = frame;
	}
	
	public BulletFrame getFrame(){
		return frame;
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
