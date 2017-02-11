package engine;

public class MovementInstruction extends Instruction{
	
	// Class controls movements and arguments of enemies and bullets
	
	// Type of bullet instruction will determine movement pattern.
	
	// args reference
	// CONST_DIR_SPD		[dir, spd]
	// CONST_ACCEL			[dir, spd, accel, spdMin, spdMax]
	// CONST_ANGVEL			[dir, angVel, spd]
	// CONST_ANGVEL_ACCEL	[dir, angVel, spd, accel, spdMin, spdMax]
	
	// SET_BOUNCES			[num, borders]
	
	public static final short
		// Base values
		SET_X =					0b000000000000001,
		SET_Y =					0b000000000000010,
		SET_POS =				SET_X | SET_Y,
		SET_DIR =				0b000000000000100,
		SET_SPD =				0b000000000001000,
		SET_ACCEL =				0b000000000010000,
		SET_SPD_MIN =			0b000000000100000,
		SET_SPD_MAX =			0b000000001000000,
		SET_ANGVEL =			0b000000010000000,
		SET_SPD_MIN_MAX =		SET_SPD_MIN | SET_SPD_MAX,
		
		// Change values
		CHANGE_VALUE =			0b100000000000000,
		CHANGE_X =				CHANGE_VALUE | SET_X,
		CHANGE_Y =				CHANGE_VALUE | SET_Y,
		CHANGE_POS =			CHANGE_VALUE | SET_POS,
		CHANGE_DIR =			CHANGE_VALUE | SET_DIR,
		CHANGE_SPD =			CHANGE_VALUE | SET_SPD,
		CHANGE_ACCEL =			CHANGE_VALUE | SET_ACCEL,
		CHANGE_SPD_MIN =		CHANGE_VALUE | SET_SPD_MIN,
		CHANGE_SPD_MAX =		CHANGE_VALUE | SET_SPD_MAX,
		CHANGE_SPD_MIN_MAX =	CHANGE_VALUE | SET_SPD_MIN_MAX,
		CHANGE_ANGVEL =			CHANGE_VALUE | SET_ANGVEL,
		
		// Simple movement patterns
		// SET_POS should be used first
		CONST_DIR_SPD =			SET_DIR | SET_SPD,
		CONST_ACCEL =			CONST_DIR_SPD | SET_ACCEL | SET_SPD_MIN_MAX,
		CONST_ANGVEL =			CONST_DIR_SPD | SET_ANGVEL,
		CONST_ANGVEL_ACCEL =	CONST_ACCEL | CONST_ANGVEL,
		
		// Same as above, except values are changed rather than set
		OFFSET_SPD_DIR =		CHANGE_VALUE | CONST_DIR_SPD,
		OFFSET_ACCEL =			CHANGE_VALUE | CONST_ACCEL,
		OFFSET_ANGVEL =			CHANGE_VALUE | CONST_ANGVEL,
		OFFSET_ANGVEL_ACCEL =	CHANGE_VALUE | CONST_ANGVEL_ACCEL,
		
		// Attributes
		USE_SPD_MIN =			0b000000100000000,
		USE_SPD_MAX =			0b000001000000000,
		SET_BOUNCES =			0b000010000000000,
		
		// Misc
		SET_VISIBLE =			0b000100000000000;
	
	// Entity controlled
	private MovableEntity e;
	
	private final int entType;
	
	public static final byte
		ENT_BULLET = 0,
		ENT_ENEMY = 1;
	
	
	// Bullet values
	private float x, y, dir, angVel, spd, accel, spdMin, spdMax;
	private int[] attr = new int[4];
	private boolean visible;
	
	public MovementInstruction(MovableEntity entity, int time, int entType, int type, float[] args){
		super(time, type, args);
		e = entity;
		this.entType = entType;
		init(false);
	}
	
	// For cloning
	// Remember to set entity manually
	public MovementInstruction(MovementInstruction inst){
		super(inst.getTime(), inst.getType(), inst.getArgs());
		entType = inst.getEntType();
		init(false);
	}
	
	private void init(boolean attrOnly){
		if(!attrOnly){
			// Initialize variables to arguments
			x = type == SET_X || type == SET_POS || type == CHANGE_X || type == CHANGE_POS ? args[0] : 0;
			y = type == SET_Y || type == CHANGE_Y ? args[0] : type == SET_POS || type == CHANGE_POS ? args[1] : 0;
			dir = type == SET_DIR || type == CHANGE_DIR ? args[0] : type == CONST_DIR_SPD || type == CONST_ACCEL || type == CONST_ANGVEL || type == CONST_ANGVEL_ACCEL || type == OFFSET_SPD_DIR || type == OFFSET_ACCEL || type == OFFSET_ANGVEL || type == OFFSET_ANGVEL_ACCEL  ? args[0] : 0;
			angVel = type == SET_ANGVEL || type == CHANGE_ANGVEL ? args[0] : type == CONST_ANGVEL || type == CONST_ANGVEL_ACCEL || type == OFFSET_ANGVEL || type == OFFSET_ANGVEL_ACCEL ? args[1] : 0;
			spd = type == SET_SPD || type == CHANGE_SPD ? args[0] : type == CONST_DIR_SPD || type == CONST_ACCEL || type == OFFSET_SPD_DIR || type == OFFSET_ACCEL ? args[1] : type == CONST_ANGVEL || type == CONST_ANGVEL_ACCEL || type == OFFSET_ANGVEL || type == OFFSET_ANGVEL_ACCEL ? args[2] : 0;
			accel = type == SET_ACCEL || type == CHANGE_ACCEL ? args[0] : type == CONST_ACCEL || type == OFFSET_ACCEL ? args[2] : type == CONST_ANGVEL_ACCEL || type == OFFSET_ANGVEL_ACCEL ? args[5] : 0;
			spdMin = type == SET_SPD_MIN || type == SET_SPD_MIN_MAX || type == CHANGE_SPD_MIN || type == CHANGE_SPD_MIN_MAX ? args[0] : type == CONST_ACCEL || type == OFFSET_ACCEL ? args[3] : type == CONST_ANGVEL_ACCEL || type == OFFSET_ANGVEL_ACCEL ? args[4] : 0;
			spdMax = type == SET_SPD_MAX || type == CHANGE_SPD_MAX ? args[0] : type == SET_SPD_MIN_MAX || type == CHANGE_SPD_MIN_MAX ? args[1] : type == CONST_ACCEL || type == OFFSET_ANGVEL ? args[4] : type == CONST_ANGVEL_ACCEL || type == OFFSET_ANGVEL_ACCEL ? args[5] : 0;
		}

		if(entType == ENT_BULLET){
			// These values should stay the same if not modified
			
			// Auto set use min/max spd
			attr[0] = type == SET_SPD_MIN || type == SET_SPD_MIN_MAX || type == CHANGE_SPD_MIN || type == CHANGE_SPD_MIN_MAX || type == CONST_ACCEL || type == OFFSET_ACCEL ? 1 : attr[0];
			attr[1] = type == SET_SPD_MAX || type == SET_SPD_MIN_MAX || type == CHANGE_SPD_MAX || type == CHANGE_SPD_MIN_MAX || type == CONST_ACCEL || type == OFFSET_ACCEL ? 1 : attr[1];
			
			// Override that if user sets it manually
			attr[0] = type == USE_SPD_MIN ? (int)args[0] : attr[0];
			attr[1] = type == USE_SPD_MAX ? (int)args[0] : attr[1];
			
			attr[2] = type == SET_BOUNCES ? (int)args[0] : attr[2];
			attr[3] = type == SET_BOUNCES ? (int)args[1] : attr[3];
			
			visible = type == SET_VISIBLE ? args[0] == 1 ? true : false : visible;
		}
	}
	
	
	public boolean run(int time){
		// Return false if not time for instruction to be run yet
		if(time < this.time)
			return false;

		// Offset variables if they should be changed rather than set
		if((type & CHANGE_VALUE) == CHANGE_VALUE){
			x += e.getX();
			y += e.getY();
			dir += e.getDir();
			spd += e.getSpd();
			accel += e.getAccel();
			spdMin += e.getSpdMin();
			spdMax += e.getSpdMax();
			angVel += e.getAngVel();
		}
		
		//  Set values if necessary
		if((type & SET_X) == SET_X)
			e.setX(x);
		if((type & SET_Y) == SET_Y)
			e.setY(y);
		if((type & SET_DIR) == SET_DIR)
			e.setDir(dir);
		if((type & SET_SPD) == SET_SPD)
			e.setSpd(spd);
		if((type & SET_ACCEL) == SET_ACCEL)
			e.setAccel(accel);
		if((type & SET_SPD_MIN) == SET_SPD_MIN)
			e.setSpdMin(spdMin);
		if((type & SET_SPD_MAX) == SET_SPD_MAX)
			e.setSpdMax(spdMax);
		if((type & SET_ANGVEL) == SET_ANGVEL)
			e.setAngVel(angVel);
		
		if(entType == ENT_BULLET){
			Bullet b = (Bullet)e;
			
			// Set attributes so they do not get overwritten
			attr = b.getAttributes();
			visible = b.isVisible();
			init(true);
			
			b.setAttributes(attr);
			b.setVisible(visible);
		}
		
		return true;
	}
	
	public MovableEntity getEntity(){
		return e;
	}
	
	public void setEntity(MovableEntity entity){
		e = entity;
	}
	
	public int getEntType(){
		return entType;
	}
}
