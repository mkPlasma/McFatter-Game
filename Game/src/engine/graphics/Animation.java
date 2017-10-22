package engine.graphics;

import engine.entities.GameEntity;
import engine.entities.MovableEntity;

/*
 * 		Animation.java
 * 		
 * 		Purpose:	Holds a single animation.
 * 		Notes:		Includes rotations, scaling, and spritesheet animation
 * 		
 * 		Last modified by:	Daniel
 * 		Date:				
 * 		Changes:			
 */

public class Animation{
	
	/* Args reference
	================================================================
	ANIM_ROTATION
	[inc]
	ANIM_ROTATION_BY_SPD
	[inc, fac]
	
	Increments by [inc] every [time] frames
	Multiplies by speed*fac if BY_SPD
	
	
	
	ANIM_SCALE, ANIM_ALPHA
	[inc, min, max]
	
	Increments by [inc] every [time] frames
	Stops at [min] or [max] values
	
	
	
	ANIM_SET_SPRITE
	[xStart, yStart, xInc, yInc, xEnd, yEnd, xReturn, yReturn]
	
	Moves sprite region from source image, used to animate from within a spritesheet
	Starts at (xStart, yStart) moves (xInc, yInc) every [time] frames, stops at (xEnd, yEnd) and loops back to (xReturn, yReturn)
	Note that this can only move sprites rightwards or downwards along the sheet, but can still loop back
	================================================================
	*/
	
	public static final int
		ANIM_ROTATION =			0,
		ANIM_ROTATION_BY_SPD =	1,
		ANIM_SCALE =				2,
		ANIM_SCALE_X =			3,
		ANIM_SCALE_Y =			4,
		ANIM_ALPHA =				5,
		ANIM_SET_SPRITE =		6;
	
	
	// Time increment to run the animation on
	private int tInc;
	
	// Sync animations with each other
	// If true, it uses a global time rather than the entity time
	private final boolean sync;
	
	private final int type;
	private final float[] args;
	
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
	
	public Animation(int timeInc, boolean sync, int xStart, int yStart, int xInc, int yInc, int xEnd, int yEnd, int xReturn, int yReturn){
		type = ANIM_SET_SPRITE;
		this.tInc = timeInc;
		this.sync = sync;
		args = new float[]{xStart, yStart, xInc, yInc, xEnd, yEnd, xReturn, yReturn};
	}
	
	// Use time from entity
	// This will allow several different objects to use the same animation object but have different timings
	
	// For sync = true, use global time
	
	public void update(int time){
		
		// Multiplies the animation effect
		int m = (int)Math.floor(time/tInc);
		
		// Rotation
		if(type == ANIM_ROTATION || type == ANIM_ROTATION_BY_SPD){
			if(type == ANIM_ROTATION_BY_SPD)
				spr.rotate(m*args[0]*((MovableEntity)e).getSpd());
			else
				spr.rotate(m*args[0]);
		}
		
		// Scale X
		if(type == ANIM_SCALE || type == ANIM_SCALE_X){
			float scx = spr.getScaleX();
			
			scx += m*args[0];
			
			// Min/Max
			if(scx < args[1])
				scx = args[1];
			else if(scx > args[2])
				scx = args[2];
			
			spr.setScaleX(scx);
		}
		
		// Scale Y
		if(type == ANIM_SCALE || type == ANIM_SCALE_Y){
			float scy = spr.getScaleY();
			
			scy += m*args[0];
			
			if(scy < args[1])
				scy = args[1];
			else if(scy > args[2])
				scy = args[2];
			
			spr.setScaleY(scy);
		}
		
		if(type == ANIM_ALPHA){
			float a = spr.getAlpha();

			a += m*args[0];
			
			if(a < args[1])
				a = args[1];
			else if(a > args[2])
				a = args[2];
			
			spr.setAlpha(a);
		}
		
		if(type == ANIM_SET_SPRITE){
			if(time == 0)// Set start position
				spr.setPos((int)args[0], (int)args[1]);
			else{
				int[] pos = spr.getPos();
				
				pos[0] += m*args[2];
				pos[1] += m*args[3];
				
				if((pos[0] >= args[4] && args[4] >= 0) || (pos[1] >= args[5] && args[5] >= 0)){// Loop back
					pos[0] = (int)args[6];
					pos[1] = (int)args[7];
				}
				
				spr.setPos(pos);
			}
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
	
	public void setSprite(Sprite spr){
		this.spr = spr;
	}
	
	public Sprite getSprite(){
		return spr;
	}
}
