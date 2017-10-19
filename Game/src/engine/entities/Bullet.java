package engine.entities;

/*
 * 		Bullet.java
 * 		
 * 		Purpose:	Base bullet class.
 * 		Notes:		
 * 		
 * 		Children:	Laser.java
 * 		
 * 		Last modified by:	Daniel
 * 		Date:				
 * 		Changes:			
 */

public class Bullet extends MovableEntity{
	
	// Player shots only
	protected int damage, dmgReduce;
	
	// Whether entity can collide
	protected boolean collisions = true;
	
	// If true, bullet will not be updated
	protected boolean paused;
	
	public Bullet(BulletFrame frame, float x, float y, float dir, float spd){
		super(frame, x, y, dir, spd);
		
		visible = true;
		
		onCreate();
	}
	
	public Bullet(BulletFrame frame, float x, float y, float dir, float spd, int damage, int dmgReduce){
		super(frame, x, y, dir, spd);
		
		this.damage = damage;
		this.dmgReduce = dmgReduce;

		visible = true;
		
		onCreate();
	}
	
	public void onCreate(){
		getSprite().addUser();
	}
	
	public void onDestroy(){
		remove = true;
		getSprite().removeUser();
	}
	
	public void update(){
		if(paused)
			return;
		
		updateMovements();
		
		// Delete at borders
		if(x < -64 || x > 864 || y < -64 || y > 664)
			remove = true;
		
		damage -= dmgReduce;
		
		time++;
	}
	
	public void setFrame(BulletFrame frame){
		this.frame = frame;
	}
	
	public BulletFrame getFrame(){
		return (BulletFrame)frame;
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
	
	public void setPaused(boolean paused){
		this.paused = paused;
	}
	
	public boolean isPaused(){
		return paused;
	}

	public void setCollisions(boolean collisions){
		this.collisions = collisions;
	}
	
	public boolean collisionsEnabled(){
		return collisions;
	}
}
