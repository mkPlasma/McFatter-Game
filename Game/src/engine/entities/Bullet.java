package engine.entities;

import java.awt.Color;

import engine.graphics.Renderer;

public class Bullet extends MovableEntity{
	
	public static final byte
		BOUNCE_LEFT =			0b0000001,
		BOUNCE_RIGHT =			0b0000010,
		BOUNCE_TOP =			0b0000100,
		BOUNCE_BOTTOM =			0b0001000,
		BOUNCE_SIDES =			BOUNCE_LEFT | BOUNCE_RIGHT,
		BOUNCE_SIDES_TOP =		BOUNCE_SIDES | BOUNCE_TOP,
		BOUNCE_ALL =			BOUNCE_SIDES_TOP | BOUNCE_BOTTOM,
		BOUNCE_SPRITE_SIZE =	0b1000000;
		
	
	// Bullet attributes
	// 0 - Use min spd
	// 1 - Use max spd
	// 2 - Num bounces
	// 3 - Bounce borders
	protected byte attr[] = new byte[4];
	
	
	// Holds sprite data and hitbox size
	private BulletFrame frame;
	
	// Player shots only
	private int damage, dmgReduce;
	
	// Whether entity can collide
	protected boolean collisions = true;
	
	// If true, bullet will not be updated
	private boolean paused;
	
	public Bullet(MovementInstruction inst, BulletFrame frame){
		super();
		
		visible = true;

		this.frame = frame;
		
		inst.setEntity(this);
		this.inst = new InstructionSet(inst);
		this.inst.init();
		
		onCreate();
	}
	
	public Bullet(InstructionSet inst, BulletFrame frame){
		super();
		
		visible = true;
		
		this.frame = frame;
		
		inst.setEntity(this);
		this.inst = inst;
		inst.init();
		
		onCreate();
	}
	
	public Bullet(float x, float y, float dir, float spd, BulletFrame frame){
		super(x, y);
		
		visible = true;

		this.frame = frame;
		
		inst = new InstructionSet(InstructionSet.INST_MOVABLE);
		inst.add(new MovementInstruction(this, 0, MovementInstruction.ENT_BULLET, MovementInstruction.SET_POS, new float[]{x, y}));
		inst.add(new MovementInstruction(this, 0, MovementInstruction.ENT_BULLET, MovementInstruction.CONST_DIR_SPD, new float[]{dir, spd}));
		inst.init(2);
		
		onCreate();
	}
	
	public Bullet(float x, float y, float dir, float spd, BulletFrame frame, int damage, int dmgReduce){
		super(x, y);
		
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
	
	public void update(){
		if(paused)
			return;
		
		// Set up bullet movements
		inst.run();
		
		// Acceleration
		spd += accel;
		
		// Keep speed within range
		
		if(attr[1] == 1 && spd > spdMax)
			spd = spdMax;
		else if(attr[0] == 1 && spd < spdMin)
			spd = spdMin;
		
		// Angular velocity
		dir += angVel;
		
		// Bullet movement
		x += spd*Math.cos(Math.toRadians(dir));
		y += spd*Math.sin(Math.toRadians(dir));
		
		// Delete at borders
		if(x < -64 || x > 864 || y < -64 || y > 664)
			remove = true;
		
		// Bouncing
		if(attr[2] > 0 || attr[2] == -1){
			if(((attr[3] & BOUNCE_LEFT) == BOUNCE_LEFT && x <= 0) || ((attr[3] & BOUNCE_LEFT) == BOUNCE_LEFT && x >= 800)){
				dir = -dir + 180;
				if(attr[2] > 0) attr[2]--;
			}
			if(((attr[3] & BOUNCE_TOP) == BOUNCE_TOP && y <= 0) || ((attr[3] & BOUNCE_BOTTOM) == BOUNCE_BOTTOM && y >= 600)){
				dir = -dir;
				if(attr[2] > 0) attr[2]--;
			}
		}
		
		damage -= dmgReduce;
		
		time++;
	}
	
	public void draw(Renderer r){
		
		if(!visible)
			return;
		
		float rotation = 0;
		
		if(frame.spriteAlign())
			rotation = dir + 90;
		
		rotation += time*frame.getSpriteRotation()*(frame.spriteRotationBySpd() ? spd : 1);
		
		if(visible)
			r.render(frame.getSprite(), x, y, rotation, 1f);
		
		// Draw hitbox
		int hitboxSize = frame.getHitboxSize();
		//r.drawCircle((int)(x - (hitboxSize)), (int)(y - (hitboxSize)), (int)hitboxSize*2, (int)hitboxSize*2, Color.RED);
	}
	
	public void onCreate(){
		
	}
	
	public void onDestroy(){
		remove = true;
	}
	
	
	public void setBulletFrame(BulletFrame frame){
		this.frame = frame;
	}
	
	public BulletFrame getBulletFrame(){
		return frame;
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
