package engine.entities;

import content.BulletList;
import content.EffectList;
import content.FrameList;
import engine.graphics.Animation;
import engine.graphics.Sprite;
import engine.screens.MainScreen;

/**
 * 
 * Standard bullet projectile.
 * 
 * @author Daniel
 *
 */

public class Bullet extends CollidableEntity{
	
	// Properties
	protected int type, color;
	
	// Customizable bullet sprite
	protected Sprite sprite;
	
	// Player shots only
	protected float damage, dmgReduce;
	
	// Won't despawn from bomb or player hit
	protected boolean resistant;
	
	// Delay effect before spawning
	protected int delay;
	
	// false - fade		true - flare
	protected boolean delayFlare;
	
	// Screen to add effects to
	protected final MainScreen screen;
	
	
	public Bullet(BulletFrame frame, float x, float y, float dir, float spd, int delay, MainScreen screen){
		super(frame, x, y, dir, spd);
		
		this.frame = frame;
		delayFlare = delay > 0;
		this.delay = Math.abs(delay);
		this.screen = screen;
		
		onCreate();
	}
	
	public Bullet(BulletFrame frame, float x, float y, float dir, float spd, float minSpd, float maxSpd, float accel, int delay, MainScreen screen){
		super(frame, x, y, dir, spd, minSpd, maxSpd, accel);
		
		this.frame = frame;
		delayFlare = delay > 0;
		this.delay = Math.abs(delay);
		this.screen = screen;
		
		onCreate();
	}
	
	public Bullet(BulletFrame frame, float x, float y, float dir, float spd, int delay, float damage, float dmgReduce, MainScreen screen){
		super(frame, x, y, dir, spd);
		
		this.frame = frame;
		delayFlare = delay > 0;
		this.delay = Math.abs(delay);
		this.damage = damage;
		this.dmgReduce = dmgReduce;
		this.screen = screen;
		
		onCreate();
	}
	
	// For lasers
	public Bullet(BulletFrame frame, float x, float y, float dir, int delay, MainScreen screen){
		super(frame, x, y, dir, 0);
		
		this.frame = frame;
		delayFlare = delay > 0;
		this.delay = Math.abs(delay);
		this.screen = screen;
	}
	
	private void onCreate(){
		
		initFrameProperties();
		
		borderDespawn = true;
		
		// Delay effects
		if(delay != 0){
			collisions = false;
			
			if(delayFlare){
				visible = false;
				
				int effectCol = color % 16;
				Effect e = new Effect(screen.getFrameList().getEffect(EffectList.TYPE_FLARE, effectCol), x, y);
				
				e.setLifetime(delay);
				
				e.getSprite().setScale(2 + delay*0.2f);
				e.getSprite().setAlpha(0);
				
				e.getSprite().addAnimation(new Animation(Animation.ANIM_SCALE, 1, false, -0.2f, 2, 2 + delay*0.2f));
				e.getSprite().addAnimation(new Animation(Animation.ANIM_ALPHA, 1, false, 1f/delay, 0, 1));

				e.getSprite().rotate((float)Math.random()*360);
				
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
	
	protected void initFrameProperties(){
		super.initFrameProperties();
		type			= frame.getType();
		color			= ((BulletFrame)frame).getColor();
		sprite			= new Sprite(frame.getSprite());
	}
	
	public void onDestroy(boolean force){
		
		// If resistant
		if(!force && resistant)
			return;
		
		deleted = true;
		
		
		// Explosion effect
		
		int effectCol = color % 16;
		boolean explosion = false;
		
		// Black bullets use gray despawn effect
		if(color == FrameList.COLOR_BLACK || color == BulletList.COLOR_BLACK_D)
			effectCol--;
		
		// "Explosion" despawn effect
		if(type == BulletList.TYPE_MISSILE || type == BulletList.TYPE_MINE){
			effectCol = FrameList.COLOR_BLACK;
			explosion = true;
		}
		
		Effect e = new Effect(screen.getFrameList().getEffect(EffectList.TYPE_CLOUD, effectCol, explosion ? 2 : 1), x, y);
		e.getSprite().rotate((float)Math.random()*360);
		e.getSprite().setScale(explosion ? 3 : 2);
		screen.addEffect(e);
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
		
		// Damage reduction
		damage -= dmgReduce;
		
		// Delete at borders
		if(shouldBorderDespawn())
			delete();
	}
	
	public void refreshSprite(){
		sprite.setTextureCoords(screen.getFrameList().bulletList.getSprite(type, color).getTextureCoords());
		sprite.genTextureCoords();
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
	
	public Sprite getSprite(){
		return sprite;
	}
	
	public void setFrame(BulletFrame frame){
		this.frame = frame;
		refreshSprite();
	}
	
	public BulletFrame getFrame(){
		return (BulletFrame)frame;
	}
	
	public void setResistant(boolean resistant){
		this.resistant = resistant;
	}
	
	public boolean isResistant(){
		return resistant;
	}
	
	public void setDamage(float damage){
		this.damage = damage;
	}
	
	public float getDamage(){
		return damage;
	}
	
	public void setDamageReduce(float dmgReduce){
		this.dmgReduce = dmgReduce;
	}
	
	public float getDamageReduce(){
		return dmgReduce;
	}
}
