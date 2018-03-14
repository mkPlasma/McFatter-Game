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
	
	// Hitbox radius
	protected int hitboxSize;
	
	// Offset hitbox on y-axis
	protected int hitboxOffset;
	
	// Whether entity can collide
	protected boolean collisions;
	
	// Despawn at screen borders
	protected boolean borderDespawn;
	
	// How far outside screen to despawn
	protected int despawnRange = 32;
	
	
	public CollidableEntity(EntityFrame frame, float x, float y){
		super(frame, x, y);
		collisions = true;
	}
	
	public CollidableEntity(EntityFrame frame, float x, float y, float dir, float spd){
		super(frame, x, y, dir, spd);
		collisions = true;
	}

	public CollidableEntity(EntityFrame frame, float x, float y, float dir, float spd, float minSpd, float maxSpd, float accel){
		super(frame, x, y, dir, spd, minSpd, maxSpd, accel);
		collisions = true;
	}
	
	
	public void setHitboxSize(int hitboxSize){
		this.hitboxSize = hitboxSize;
	}
	
	public int getHitboxSize(){
		return hitboxSize;
	}
	
	public void setHitboxOffset(int hitboxOffset){
		this.hitboxOffset = hitboxOffset;
	}
	
	public int getHitboxOffset(){
		return hitboxOffset;
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
