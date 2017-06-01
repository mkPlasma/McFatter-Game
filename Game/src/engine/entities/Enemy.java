package engine.entities;

public class Enemy extends MovableEntity{
	
	private final int hitboxSize;
	
	// Whether entity can collide
	private boolean collisions = true;
	
	private int health;
	private int hpmax;
	
	public Enemy(EnemyFrame frame, InstructionSet inst){
		super();
		
		visible = true;
		
		this.frame = frame;
		
		inst.setEntity(this);
		this.inst = inst;
		inst.init();
		
		
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
		remove = true;
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
