package engine.entities;

/*
 * 		Enemy.java
 * 		
 * 		Purpose:	Enemy entity.
 * 		Notes:		Does not fire its own shots. This will be handled
 * 					by scripting.
 * 					
 * 					Instead, it is simply an entity that can move around
 * 					and be shot by the player.
 * 		
 * 		Last modified by:	Daniel
 * 		Date:				
 * 		Changes:			
 */

public class Enemy extends MovableEntity{
	
	private final int hitboxSize;
	
	// Whether entity can collide
	private boolean collisions = true;
	
	private int health;
	private int hpmax;
	
	public Enemy(EnemyFrame frame){
		super();
		
		visible = true;
		
		this.frame = frame;
		
		hitboxSize = 8;
		hpmax = 50000;
		health = hpmax;
		
		
		onCreate();
	}
	
	
	public void update(){
		updateMovements();
		time++;
	}
	
	public void onCreate(){
		
	}
	
	public void onDestroy(){
		deleted = true;
	}
	
	public void damage(int damage){
		health -= damage;
		
		if(health <= 0)
			onDestroy();
	}
	
	
	public EnemyFrame getFrame(){
		return (EnemyFrame)frame;
	}
	
	public int getHealth(){
		return health;
	}
	
	public int getHitboxSize(){
		return hitboxSize;
	}
	
	public void setCollisions(boolean collisions){
		this.collisions = collisions;
	}
	
	public boolean collisionsEnabled(){
		return collisions;
	}
}
