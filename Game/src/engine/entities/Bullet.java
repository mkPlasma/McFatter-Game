package engine.entities;

import content.BulletList;
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
	
	// Won't despawn from bomb or player hit
	protected boolean bombResist;
	
	// Delay effect before spawning
	protected int delay;
	
	// false - fade		true - flare
	protected boolean delayFlare;
	
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
		delayFlare = delay > 0;
		this.delay = Math.abs(delay);
		this.frameList = frameList;
		this.screen = screen;
		
		onCreate();
	}
	
	public Bullet(BulletFrame frame, float x, float y, float dir, float spd, int delay, int damage, int dmgReduce, FrameList frameList, MainScreen screen){
		super(frame, x, y, dir, spd);
		
		this.frame = frame;
		delayFlare = delay > 0;
		this.delay = Math.abs(delay);
		this.damage = damage;
		this.dmgReduce = dmgReduce;
		this.frameList = frameList;
		this.screen = screen;
		
		onCreate();
	}
	
	// For lasers
	public Bullet(BulletFrame frame, float x, float y, float dir, int delay, FrameList frameList, MainScreen screen){
		super(frame, x, y, dir, 0);
		
		this.frame = frame;
		delayFlare = delay > 0;
		this.delay = Math.abs(delay);
		this.frameList = frameList;
		this.screen = screen;
	}
	
	private void onCreate(){
		
		initFrameProperties();
		
		borderDespawn = true;
		
		// Delay effects
		if(delay != 0){
			if(delayFlare){
				visible = false;
				
				int effectCol = color % 16;
				Effect e = new Effect(frameList.getEffect(EffectList.TYPE_FLARE, effectCol), x, y);
				
				e.setLifetime(delay);
				
				e.getSprite().setScale(2 + delay*0.2f);
				e.getSprite().setAlpha(0);
				
				e.getSprite().addAnimation(new Animation(Animation.ANIM_SCALE, 1, false, -0.2f, 2, 2 + delay*0.2f));
				e.getSprite().addAnimation(new Animation(Animation.ANIM_ALPHA, 1, false, 1f/delay, 0, 1));
				
				screen.addEffect(e);
				
				delay--;
			}
			else{
				sprite.setScale(3);
				sprite.setAlpha(0);
				sprite.addAnimation(new Animation(Animation.ANIM_SCALE, 1, false, -2f/delay, 1, 5));
				sprite.addAnimation(new Animation(Animation.ANIM_ALPHA, 1, false, 1f/delay, 0, 1));
			}
		}
	}
	
	public void initFrameProperties(){
		type			= frame.getType();
		color			= frame.getColor();
		hitboxSize		= frame.getHitboxSize();
		sprite			= new Sprite(frame.getSprite());
	}
	
	public void onDestroy(boolean force){
		
		if(!force && bombResist)
			return;
		
		deleted = true;
		
		int effectCol = color % 16;
		
		// Black bullets use gray despawn effect
		if(color == FrameList.COLOR_BLACK || color == BulletList.COLOR_BLACK_D)
			effectCol--;
		
		// "Explosion" despawn effect
		if(type == BulletList.TYPE_MISSILE || type == BulletList.TYPE_MINE)
			effectCol = FrameList.COLOR_BLACK;
		
		// check temporary
		if(screen != null){
			Effect e = new Effect(frameList.getEffect(EffectList.TYPE_CLOUD, effectCol), x, y);
			e.getSprite().rotate((float)Math.random()*360);
			e.getSprite().setScale(effectCol == FrameList.COLOR_BLACK ? 3 : 2);
			screen.addEffect(e);
		}
	}
	
	public void update(){
		
		if(delay > -1){
			if(delay == 0){
				if(delayFlare)
					visible = true;
				
				collisions = true;
			}
			
			delay--;
			
			if(delayFlare)
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
	
	public void setBombResist(boolean bombResist){
		this.bombResist = bombResist;
	}
	
	public boolean bombResist(){
		return bombResist;
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
