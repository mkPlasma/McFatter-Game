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
	
	protected int despawnRange = 32;
	
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
		
		updateMovements();
		
		// Delete at borders
		if(x < -despawnRange || x > 640 + despawnRange || y < -despawnRange || y > 480 + despawnRange)
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

	public void setCollisions(boolean collisions){
		this.collisions = collisions;
	}
	
	public boolean collisionsEnabled(){
		return collisions;
	}
}
