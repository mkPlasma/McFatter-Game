package engine.entities;

import engine.graphics.Animation;
import engine.graphics.Renderer;
import engine.graphics.Sprite;
import engine.graphics.SpriteCache;

public class Bullet extends MovableEntity{
	
	public static final byte
		BOUNCE_LEFT =			0b0000001,
		BOUNCE_RIGHT =			0b0000010,
		BOUNCE_TOP =				0b0000100,
		BOUNCE_BOTTOM =			0b0001000,
		BOUNCE_SIDES =			BOUNCE_LEFT | BOUNCE_RIGHT,
		BOUNCE_SIDES_TOP =		BOUNCE_SIDES | BOUNCE_TOP,
		BOUNCE_ALL =				BOUNCE_SIDES_TOP | BOUNCE_BOTTOM,
		BOUNCE_SPRITE_SIZE =		0b1000000;
		
	
	// Bullet attributes
	// 1 - Num bounces
	// 2 - Bounce borders
	protected byte attr[] = new byte[2];
	
	
	// Player shots only
	protected int damage, dmgReduce;
	
	// Whether entity can collide
	protected boolean collisions = true;
	
	// If true, bullet will not be updated
	protected boolean paused;
	
	public Bullet(BulletFrame frame, MovementInstruction inst){
		//super();
		
		visible = true;

		this.frame = frame;
		
		inst.setEntity(this);
		this.inst = new InstructionSet(inst);
		this.inst.init();
		
		onCreate();
	}
	
	public Bullet(BulletFrame frame, InstructionSet inst){
		//super();
		
		visible = true;
		
		this.frame = frame;
		
		inst.setEntity(this);
		this.inst = inst;
		inst.init();
		
		onCreate();
	}
	
	public Bullet(BulletFrame frame, float x, float y, float dir, float spd){
		//super(x, y);
		
		visible = true;

		this.frame = frame;
		
		inst = new InstructionSet(InstructionSet.INST_MOVABLE);
		inst.add(new MovementInstruction(this, 0, MovementInstruction.ENT_BULLET, MovementInstruction.SET_POS, new float[]{x, y}));
		inst.add(new MovementInstruction(this, 0, MovementInstruction.ENT_BULLET, MovementInstruction.CONST_DIR_SPD, new float[]{dir, spd}));
		inst.init(2);
		
		onCreate();
	}
	
	public Bullet(BulletFrame frame, float x, float y, float dir, float spd, int damage, int dmgReduce){
		//super(x, y);
		
		visible = true;
		
		this.frame = frame;
		
		this.damage = damage;
		this.dmgReduce = dmgReduce;
		
		inst = new InstructionSet(InstructionSet.INST_MOVABLE);
		inst.add(new MovementInstruction(this, 0, MovementInstruction.ENT_BULLET, MovementInstruction.SET_POS, new float[]{x, y}));
		inst.add(new MovementInstruction(this, 0, MovementInstruction.ENT_BULLET, MovementInstruction.CONST_DIR_SPD, new float[]{dir, spd}));
		inst.init(2);
		
		onCreate();
	}
	
	public void onCreate(){
		getSprite().addUser();
	}
	
	public void onDestroy(){
		remove = true;
		getSprite().removeUser();
		
		
		// Temporary
		InstructionSet inst = new InstructionSet(InstructionSet.INST_MOVABLE);
		inst.add(new MovementInstruction(null, 0, MovementInstruction.ENT_EFFECT, MovementInstruction.SET_POS, new float[]{x, y}));
		inst.add(new MovementInstruction(null, 0, MovementInstruction.ENT_EFFECT, MovementInstruction.CONST_DIR_SPD, new float[]{-dir, 5}));
		inst.add(new MovementInstruction(null, 50, MovementInstruction.ENT_EFFECT, MovementInstruction.DESTROY, null));
		
		Animation[] a = new Animation[]{new Animation(Animation.ANIM_ALPHA, 1, false, -.02f, 0, 1), new Animation(Animation.ANIM_SCALE, 1, false, -.02f, 0, 1)};
		
		Sprite s = new Sprite("Game/res/img/bullets/01.png", 0, 0, 32, 32, a);
		s = SpriteCache.cache(s);
		
		EntityFrame f = new EntityFrame(0, s, false, 0, false);
		
		Effect e = new Effect(inst, f);
		
		EffectGenerator.addEffect(e);
	}
	
	public void update(){
		if(paused)
			return;
		
		updateMovements();
		
		// Delete at borders
		if(x < -64 || x > 864 || y < -64 || y > 664)
			remove = true;
		
		// Bouncing
		if(attr[0] > 0 || attr[0] == -1){
			if(((attr[1] & BOUNCE_LEFT) == BOUNCE_LEFT && x <= 0) || ((attr[1] & BOUNCE_LEFT) == BOUNCE_LEFT && x >= 800)){
				dir = -dir + 180;
				if(attr[0] > 0) attr[2]--;
			}
			if(((attr[1] & BOUNCE_TOP) == BOUNCE_TOP && y <= 0) || ((attr[1] & BOUNCE_BOTTOM) == BOUNCE_BOTTOM && y >= 600)){
				dir = -dir;
				if(attr[0] > 0) attr[2]--;
			}
		}
		
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
	
	
	public void setAttributes(byte[] attr){
		this.attr = attr;
	}
	
	public byte[] getAttributes(){
		return attr;
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
