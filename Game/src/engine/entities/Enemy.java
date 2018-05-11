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

public class Enemy extends CollidableEntity{
	
	protected EnemyFrame frame;
	
	// Customizable sprite
	protected Sprite sprite;
	
	protected int hp;
	protected boolean invulnerable;
	
	// Don't despawn immediately if spawned outside border
	protected boolean borderDespawnImmune;
	
	protected final MainScreen screen;
	
	public Enemy(EnemyFrame frame, float x, float y, int hp, MainScreen screen){
		super(frame, x, y);
		
		this.frame = frame;
		this.hp = hp;
		this.screen = screen;
		
		onCreate();
	}
	
	public void onCreate(){
		initFrameProperties();
		
		borderDespawn = true;
		borderDespawnImmune = shouldBorderDespawn();
	}
	
	protected void initFrameProperties(){
		super.initFrameProperties();
		sprite = new Sprite(frame.getSprite());
	}
	
	public void onDestroy(){
		deleted = true;
		
		
		// Explosion effect
		Effect e = new Effect(screen.getFrameList().getEffect(EffectList.TYPE_CLOUD, FrameList.COLOR_BLACK, 2), x, y);
		e.getSprite().rotate((float)Math.random()*360);
		e.getSprite().setScale(4);
		screen.addEffect(e);
	}
	
	public void update(){
		super.update();
		
		// Allow despawning if enemy enters border
		if(!shouldBorderDespawn())
			borderDespawnImmune = false;
		
		// Delete at borders
		if(!borderDespawnImmune && shouldBorderDespawn())
			delete();
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
}
