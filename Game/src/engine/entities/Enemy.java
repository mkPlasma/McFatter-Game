package engine.entities;

import content.EffectList;
import content.FrameList;
import engine.graphics.Sprite;
import engine.screens.MainScreen;

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
 */

public class Enemy extends MovableEntity{
	
	protected EnemyFrame frame;
	
	// Customizable sprite
	protected Sprite sprite;
	
	// Whether entity can collide
	protected boolean collisions = true;
	
	protected int hitboxSize;
	
	protected boolean invulnerable;
	protected int hp;
	
	protected final FrameList frameList;
	protected final MainScreen screen;
	
	public Enemy(EnemyFrame frame, float x, float y, int hp, FrameList frameList, MainScreen screen){
		super(frame, x, y);
		
		this.frame = frame;
		this.hp = hp;
		this.frameList = frameList;
		this.screen = screen;
		
		onCreate();
	}
	
	public void onCreate(){
		initFrameProperties();
	}
	
	private void initFrameProperties(){
		sprite = new Sprite(frame.getSprite());
		hitboxSize = frame.getHitboxSize();
	}
	
	public void onDestroy(){
		deleted = true;
		
		
		// Explosion effect
		
		Effect e = new Effect(frameList.getEffect(EffectList.TYPE_CLOUD, FrameList.COLOR_BLACK, 2), x, y);
		e.getSprite().rotate((float)Math.random()*360);
		e.getSprite().setScale(4);
		screen.addEffect(e);
	}
	
	public void damage(int damage){
		
		if(!invulnerable)
			hp -= damage;
		
		if(hp <= 0)
			onDestroy();
	}
	
	
	public EnemyFrame getFrame(){
		return (EnemyFrame)frame;
	}
	
	public Sprite getSprite(){
		return sprite;
	}
	
	public void setHealth(int hp){
		this.hp = hp;
	}
	
	public int getHealth(){
		return hp;
	}
	
	public void setInvulnerable(boolean invulnerable){
		this.invulnerable = invulnerable;
	}
	
	public boolean isInvulnerable(){
		return invulnerable;
	}
	
	public void setHitboxSize(int hitboxSize){
		this.hitboxSize = hitboxSize;
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
