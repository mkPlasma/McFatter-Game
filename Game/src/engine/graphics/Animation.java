package engine.graphics;

import engine.entities.GameEntity;
import engine.entities.MovableEntity;

/**
 * 
 * Holds animation data used by a sprite.
 * 
 * @author Daniel
 *
 */

public class Animation{
	
	/* Args reference
	================================================================
	
	ANIM_ROTATION, ANIM_ROTATION_BY_SPD
	[inc]
	
	Increments by [inc] every [timeInc] frames
	Multiplies by speed if BY_SPD
	
	
	
	ANIM_SCALE, ANIM_ALPHA
	[inc, min, max]
	
	Increments by [inc] every [timeInc] frames
	Stops at [min] or [max] values
	
	
	
	ANIM_SET_SPRITE
	[xStart, yStart, xInc, yInc, num, xReturn, yReturn]
	
	Moves sprite region from source image, used to animate from within a spritesheet
	Starts at (xStart, yStart) moves (xInc, yInc) every [timeInc] frames, stops after [num] times and loops back to (xReturn, yReturn)
	Note that this can only move the sprite region downwards and/or rightwards
	
	
	ANIM_SET_SPRITE_FLIP
	[xStart, yStart, xInc, yInc, num]
	
	Moves sprite region from (xStart, yStart) by (xInc, yInc) [num] times, then back and forth
	
	
	ANIM_FLIP_X
	Flips X scale from positive to negative every [timeInc] frames
	Used for atom bullet type animation
	
	================================================================
	*/
	
	public static final int
		ANIM_ROTATION			= 0,
		ANIM_ROTATION_BY_SPD	= 1,
		ANIM_SCALE				= 2,
		ANIM_SCALE_X			= 3,
		ANIM_SCALE_Y			= 4,
		ANIM_ALPHA				= 5,
		ANIM_SET_SPRITE			= 6,
		ANIM_SET_SPRITE_FLIP	= 7,
		ANIM_FLIP_X				= 8;
	
	
	// Time increment to run the animation on
	private int tInc;
	
	// Delay before starting animation
	private int delay;
	
	// Sync animations with each other
	// If true, it uses a global time rather than the entity time
	private final boolean sync;
	
	private final int type;
	private final float[] args;
	
	private boolean finished;
	
	private Sprite spr;
	
	private GameEntity e;
	
	public Animation(int type, int timeInc, boolean sync, float[] args){
		this.type = type;
		this.tInc = timeInc;
		this.sync = sync;
		this.args = args;
	}
	
	public Animation(int type, int timeInc, boolean sync, float inc, float min, float max){
		this.type = type;
		this.tInc = timeInc;
		this.sync = sync;
		args = new float[]{inc, min, max};
	}
	
	public Animation(int type, int timeInc, int delay, boolean sync, float inc, float min, float max){
		this.type = type;
		this.tInc = timeInc;
		this.delay = delay;
		this.sync = sync;
		args = new float[]{inc, min, max};
	}
	
	// Use time from entity
	// This will allow several different objects to use the same animation object but have different timings
	
	// For sync = true, use global time
	
	public void update(int time){
		
		if(time < delay)
			return;
		
		// Multiplies the animation effect
		int m = (int)Math.floor((time - delay)/tInc);
		
		
		// Rotation
		if(type == ANIM_ROTATION || type == ANIM_ROTATION_BY_SPD){
			if(type == ANIM_ROTATION_BY_SPD)
				spr.rotate(m*args[0]*((MovableEntity)e).getSpd());
			else
				spr.rotate(m*args[0]);
			return;
		}
		
		// Scale X
		if(type == ANIM_SCALE || type == ANIM_SCALE_X){
			float scx = spr.getScaleX();
			
			scx += m*args[0];
			
			// Min/Max
			if(scx < args[1]){
				scx = args[1];
				finished = true;
			}
			else if(scx > args[2]){
				scx = args[2];
				finished = true;
			}
			
			spr.setScaleX(scx);
		}
		
		// Scale Y
		if(type == ANIM_SCALE || type == ANIM_SCALE_Y){
			float scy = spr.getScaleY();
			
			scy += m*args[0];
			
			if(scy < args[1]){
				scy = args[1];
				finished = true;
			}
			else if(scy > args[2]){
				scy = args[2];
				finished = true;
			}
			
			spr.setScaleY(scy);
			return;
		}
		
		if(type == ANIM_ALPHA){
			float a = spr.getAlpha();

			a += m*args[0];
			
			if(a < args[1]){
				a = args[1];
				finished = true;
			}
			else if(a > args[2]){
				a = args[2];
				finished = true;
			}
			
			spr.setAlpha(a);
			return;
		}
		
		if(type == ANIM_SET_SPRITE){
			int[] pos = {(int)args[0], (int)args[1]};
			
			if(m > (int)args[4]){
				pos[0] = (int)args[5];
				pos[1] = (int)args[6];
			}
			
			m = m % (int)args[4];
			
			pos[0] += m*args[2];
			pos[1] += m*args[3];
			
			spr.setPos(pos);
			return;
		}
		
		if(type == ANIM_SET_SPRITE_FLIP){
			int[] pos = {(int)args[0], (int)args[1]};
			
			m = m % (((int)args[4]*2) - 2);
			
			if(m >= (int)args[4])
				m = (int)args[4] - (m - (int)args[4]) - 2;
			
			pos[0] += m*args[2];
			pos[1] += m*args[3];
			
			spr.setPos(pos);
			return;
		}
		
		if(type == ANIM_FLIP_X){
			float scx = spr.getScaleX();
			scx *= m % 2 == 0 ? 1 : -1;
			spr.setScaleX(scx);
			return;
		}
	}
	
	public boolean equals(Animation anim){
		
		if(anim == null)
			return false;
		
		return	anim.getType() == type &&
				anim.getArgs() == args &&
				anim.getSprite().equals(spr);
	}
	
	public void setEntity(GameEntity e){
		this.e = e;
	}
	
	public int getType(){
		return type;
	}
	
	public boolean sync(){
		return sync;
	}
	
	public float[] getArgs(){
		return args;
	}
	
	public boolean isFinished(){
		return finished;
	}
	
	public void setSprite(Sprite spr){
		this.spr = spr;
	}
	
	public Sprite getSprite(){
		return spr;
	}
}
