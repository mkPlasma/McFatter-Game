package engine.entities;

/**
 * 
 * Abstract collidable game entity.
 * Contains certain properties shared between Bullet and Enemy.
 * 
 * @author Daniel
 *
 */

public abstract class CollidableEntity extends MovableEntity{
	
	public static final int
		HITBOX_CIRCLE	= 0,
		HITBOX_ELLIPSE		= 1,
		HITBOX_RECTANGLE	= 2;
	
	// Shape of hitbox
	protected int hitboxType;
	
	// Hitbox dimensions
	protected int hitboxWidth, hitboxLength;
	
	// Offset hitbox on y-axis
	protected int hitboxOffset;
	
	// Scale hitbox size, y-scale affects offset
	protected float hitboxScaleX, hitboxScaleY;
	
	// Whether entity can collide
	protected boolean collisions;
	
	// Despawn at screen borders
	protected boolean borderDespawn;
	
	// How far outside screen to despawn
	protected int despawnRange = 32;
	
	
	public CollidableEntity(EntityFrame frame, float x, float y){
		super(frame, x, y);
		collisions = true;
		setHitboxScale(1);
	}
	
	public CollidableEntity(EntityFrame frame, float x, float y, float dir, float spd){
		super(frame, x, y, dir, spd);
		collisions = true;
		setHitboxScale(1);
	}

	public CollidableEntity(EntityFrame frame, float x, float y, float dir, float spd, float minSpd, float maxSpd, float accel){
		super(frame, x, y, dir, spd, minSpd, maxSpd, accel);
		collisions = true;
		setHitboxScale(1);
	}
	
	protected void initFrameProperties(){
		CollidableFrame f = ((CollidableFrame)frame);
		
		hitboxType		= f.getHitboxType();
		hitboxWidth		= f.getHitboxWidth();
		hitboxLength		= f.getHitboxLength();
		hitboxOffset		= f.getHitboxOffset();
	}
	
	public void setHitboxType(int hitboxType){
		this.hitboxType = hitboxType;
	}
	
	public int getHitboxType(){
		return hitboxType;
	}
	
	public void setHitboxWidth(int hitboxSize){
		this.hitboxWidth = hitboxSize;
	}
	
	public int getHitboxWidth(){
		return (int)(hitboxWidth*hitboxScaleX);
	}
	
	public void setHitboxLength(int hitboxLength){
		this.hitboxLength = hitboxLength;
	}
	
	public int getHitboxLength(){
		return (int)(hitboxLength*hitboxScaleY);
	}
	
	public void setHitboxOffset(int hitboxOffset){
		this.hitboxOffset = hitboxOffset;
	}
	
	public float getHitboxOffset(){
		return hitboxOffset*hitboxScaleY;
	}
	
	public void setHitboxScale(float hitboxScale){
		hitboxScaleX = hitboxScale;
		hitboxScaleY = hitboxScale;
	}
	
	public void setHitboxScaleX(float hitboxScaleX){
		this.hitboxScaleX = hitboxScaleX;
	}
	
	public float getHitboxScaleX(){
		return hitboxScaleX;
	}
	
	public void setHitboxScaleY(float hitboxScaleY){
		this.hitboxScaleY = hitboxScaleY;
	}
	
	public float getHitboxScaleY(){
		return hitboxScaleY;
	}
	
	public void setCollisions(boolean collisions){
		this.collisions = collisions;
	}
	
	public boolean collisionsEnabled(){
		return collisions;
	}
	
	public void setBorderDespawn(boolean borderDespawn){
		this.borderDespawn = borderDespawn;
	}
	
	public boolean getBorderDespawn(){
		return borderDespawn;
	}
	
	public void setDespawnRange(int despawnRange){
		this.despawnRange = despawnRange;
	}
	
	public int getDespawnRange(){
		return despawnRange;
	}
}
