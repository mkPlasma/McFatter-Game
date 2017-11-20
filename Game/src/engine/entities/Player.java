package engine.entities;

import static engine.KeyboardListener.*;
import static org.lwjgl.glfw.GLFW.*;

import content.BulletList;
import content.EffectList;
import content.FrameList;
import engine.graphics.Sprite;
import engine.screens.MainScreen;

/*
 * 		Player.java
 * 		
 * 		Purpose:	Player class.
 * 		Notes:		Will need a PlayerFrame or similar class
 * 					to differentiate player character/shot types.
 * 					
 * 					Also needs upgrade functionality.
 * 		
 */

public class Player extends GameEntity{
	
	
	private boolean alive = true;
	
	
	private final float focusedSpeed = 1.5f, unfocusedSpeed = 3;
	
	private float speed;
	private boolean focused, firing;
	
	private BulletFrame shot;
	
	// Timer variables counts up for timing shots/bombs
	private int shotCooldown = 0, bombCooldown = 0;

	private final FrameList frameList;
	private final MainScreen screen;
	
	// Temporary
	private Sprite sprite;
	
	public Player(float x, float y, FrameList frameList, MainScreen screen){
		super(null, x, y);
		
		this.frameList = frameList;
		this.screen = screen;
		
		// temp
		shot = frameList.getBullet(BulletList.TYPE_CRYSTAL, FrameList.COLOR_LIGHT_BLUE);
		sprite = new Sprite("player.png", 0, 0, 64, 64);
		
		onCreate();
	}
	
	public void setSprite(Sprite sprite){
		this.sprite = sprite;
	}
	
	public Sprite getSprite(){
		return sprite;
	}
	
	public void onCreate(){
		
	}
	
	public void onDestroy(){
		
	}
	
	public void update(){
		
		// Keypresses
		
		// Movement
		if(isKeyDown(GLFW_KEY_RIGHT))
			x += speed;
		else if(isKeyDown(GLFW_KEY_LEFT))
			x -= speed;
		if(isKeyDown(GLFW_KEY_DOWN))
			y += speed;
		else if(isKeyDown(GLFW_KEY_UP))
			y -= speed;
		
		
		// Focusing
		if(isKeyDown(GLFW_KEY_RIGHT_SHIFT) || isKeyDown(GLFW_KEY_LEFT_SHIFT))
			focused = true;
		else if(isKeyUp(GLFW_KEY_RIGHT_SHIFT) || isKeyUp(GLFW_KEY_LEFT_SHIFT))
			focused = false;
		
		// Firing
		if(isKeyDown(GLFW_KEY_Z))
			firing = true;
		else if(isKeyUp(GLFW_KEY_Z))
			firing = false;
		
		
		if(isKeyDown(GLFW_KEY_X) && bombCooldown == 0)
			bomb();
		
		// Movement borders
		if(x < 48)	x = 48;
		if(x > 400)	x = 400;
		if(y < 32)	y = 32;
		if(y > 448)	y = 448;
		
		// Set speed
		speed = unfocusedSpeed;
		
		if(focused)
			speed = focusedSpeed;
		
		// Shots
		if(shotCooldown > 0)
			shotCooldown--;
		
		if(bombCooldown > 0)
			bombCooldown--;
		
		if(firing && shotCooldown == 0)
			fire();
	}
	
	private void fire(){
		screen.addPlayerBullet(new Bullet(shot, x - 10, y + 10, 270, 15, 0, 15, 0.1f, frameList, screen));
		screen.addPlayerBullet(new Bullet(shot, x + 10, y + 10, 270, 15, 0, 15, 0.1f, frameList, screen));
		shotCooldown = 4;
	}
	
	private void bomb(){
		/*
		Random r = new Random();
		
		for(int i = 0; i < 500; i++){
			InstructionSet inst = new InstructionSet(InstructionSet.INST_BULLET);
			inst.add(new MovementInstruction(null, 0, MovementInstruction.ENT_BULLET, MovementInstruction.SET_POS, new double[]{x, y}));
			inst.add(new MovementInstruction(null, 0, MovementInstruction.ENT_BULLET, MovementInstruction.SET_VISIBLE, new double[]{0}));
			inst.add(new MovementInstruction(null, i/10, MovementInstruction.ENT_BULLET, MovementInstruction.SET_VISIBLE, new double[]{1}));
			inst.add(new MovementInstruction(null, 0, MovementInstruction.ENT_BULLET, MovementInstruction.CONST_DIR_SPD, new double[]{r.nextInt(360), 8}));
			inst.add(new MovementInstruction(null, 10, MovementInstruction.ENT_BULLET, MovementInstruction.SET_ANGVEL, new double[]{r.nextBoolean() ? -2 : 2}));
			inst.add(new MovementInstruction(null, 20, MovementInstruction.ENT_BULLET, MovementInstruction.SET_ANGVEL, new double[]{0}));
			
			Bullet b = new Bullet(inst, Bullet.TYPE_MISSILE, Bullet.COLOR_ORANGE);
			b.setDamage(1000);
			b.setDamageReduce(5);
			
			bullets.add(b);
		}
		*/
		bombCooldown = 120;
	}
	
	public void death(){
		//x = 224;		y = 450;
		//alive = false;
		
		Effect e = new Effect(frameList.getEffect(EffectList.TYPE_CLOUD, FrameList.COLOR_BLACK, 2), x, y);
		e.getSprite().rotate((float)Math.random()*360);
		e.getSprite().setScale(4);
		screen.addEffect(e);
	}
	
	public boolean isAlive(){
		return alive;
	}
	
	public int getHitboxSize(){
		return 1;
	}
}
