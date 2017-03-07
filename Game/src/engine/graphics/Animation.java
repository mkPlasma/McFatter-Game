package engine.graphics;

public class Animation{
	
	// Class stores a sprite and animates it by changing certain properties
	
	/* Args reference
	================================================================
	ANIM_ROTATION
	[inc]
	
	Increments by [inc] every [time] frames
	
	
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
		ANIM_SCALE =			1,
		ANIM_SCALE_X =			2,
		ANIM_SCALE_Y =			3,
		ANIM_ALPHA =			4,
		ANIM_SET_SPRITE =		5;
	
	
	// Time increment to run the animation on
	private int inc;
	
	private final int type;
	private final float[] args;
	
	private Sprite spr;

	public Animation(int type, int inc, float[] args){
		this.type = type;
		this.inc = inc;
		this.args = args;
	}
	
	public Animation(int type, int inc, float aInc, float min, float max){
		this.type = type;
		this.inc = inc;
		args = new float[]{aInc, min, max};
	}
	
	public Animation(int inc, int xStart, int yStart, int xInc, int yInc, int xEnd, int yEnd, int xReturn, int yReturn){
		type = ANIM_SET_SPRITE;
		this.inc = inc;
		args = new float[]{xStart, yStart, xInc, yInc, xEnd, yEnd, xReturn, yReturn};
	}
	
	// Use time from entity
	// This will allow several different objects to use the same animation object but have different timings
	public void update(int time){
		
		// Multiplies the animation effect
		int m = (int)Math.floor(time/inc);
		
		// Rotation
		if(type == ANIM_ROTATION)
			spr.rotate(m*args[0]);
		
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
				spr.reload();
			}
		}
	}
	
	public void setSprite(Sprite spr){
		this.spr = spr;
	}
}
