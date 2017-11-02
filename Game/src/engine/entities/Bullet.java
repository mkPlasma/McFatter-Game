package engine.entities;

import content.EffectList;
import content.FrameList;
import engine.graphics.Animation;
import engine.graphics.Sprite;
import engine.screens.MainScreen;

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
	
	// Player shots only
	protected int damage, dmgReduce;
	
	// Delay effect before spawning
	protected int delay;
	protected boolean delayAnim;
	
	// Whether entity can collide
	protected boolean collisions;
	
	// Despawn at screen borders
	protected boolean borderDespawn;
	
	// How far outside screen to despawn
	protected int despawnRange = 32;
	
	protected final FrameList frameList;
	
	// Screen to add effects to
	protected MainScreen screen;
	
	
	public Bullet(BulletFrame frame, float x, float y, float dir, float spd, int delay, FrameList frameList, MainScreen screen){
		super(frame, x, y, dir, spd);

		this.frame = frame;
		this.delay = delay;
		this.frameList = frameList;
		this.screen = screen;
		
		init();
		onCreate();
	}
	
	public Bullet(BulletFrame frame, float x, float y, float dir, float spd, int delay, int damage, int dmgReduce, FrameList frameList, MainScreen screen){
		super(frame, x, y, dir, spd);
		
		this.frame = frame;
		this.delay = delay;
		this.damage = damage;
		this.dmgReduce = dmgReduce;
		this.frameList = frameList;
		this.screen = screen;
		
		init();
		onCreate();
	}
	
	private void init(){
		
		initFrameProperties();
		
		borderDespawn = true;
		
		if(delay < 15)
			collisions = true;
		
		if(delay > 0){
			visible = false;
			
			Effect e = new Effect(frameList.getEffect(EffectList.TYPE_CLOUD, color%16), x, y);

			e.setLifetime(delay);
			
			e.getSprite().setY(160);
			e.getSprite().genTextureCoords();
			e.getSprite().getAnimations().clear();
			
			e.getSprite().setScale(3);
			
			e.getSprite().addAnimation(new Animation(Animation.ANIM_SCALE, 1, Math.max(delay - 15, 0), false, -1f/Math.min(delay, 15), 2, 5));
			e.getSprite().addAnimation(new Animation(Animation.ANIM_ALPHA, 1, Math.max(delay - 15, 0), false, -1f/Math.min(delay, 15), 0, 1));
			screen.addEffect(e);
		}
	}
	
	public void initFrameProperties(){
		type			= frame.getType();
		color			= frame.getColor();
		hitboxSize		= frame.getHitboxSize();
		sprite			= new Sprite(frame.getSprite());
	}
	
	public void onCreate(){
		
	}
	
	public void onDestroy(){
		deleted = true;
		
		// check temporary
		if(screen != null)
			screen.addEffect(new Effect(frameList.getEffect(EffectList.TYPE_CLOUD, color%16), x, y));
	}
	
	public void update(){
		
		// Delay effect
		if(delay > 0){
			
			// Show bullet and add animation
			if(!delayAnim && delay < 15){
				visible = true;
				
				sprite.setScale(3);
				sprite.setAlpha(0);
				sprite.addAnimation(new Animation(Animation.ANIM_SCALE, 1, false, -2f/delay, 1, 5));
				sprite.addAnimation(new Animation(Animation.ANIM_ALPHA, 1, false, 1f/delay, 0, 1));
				
				delayAnim = true;
				collisions = true;
			}
			
			delay--;
			
			if(delay >= 15)
				return;
		}
		
		// Movement
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
}
