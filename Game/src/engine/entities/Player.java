package engine.entities;

import static engine.KeyboardListener.isKeyDown;
import static engine.KeyboardListener.isKeyUp;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_DOWN;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_SHIFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_UP;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_X;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_Z;

import java.util.ArrayList;

import content.FrameList;
import engine.graphics.Sprite;

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
	
	
	private final int focusedSpeed = 2, unfocusedSpeed = 4;
	
	private int speed;
	private boolean focused, firing, bombing;
	
	private BulletFrame shot;
	
	// Timer variables counts up for timing shots/bombs
	private int shotCooldown = 0, bombCooldown = 0;
	
	private ArrayList<Bullet> bullets;
	
	// Temporary
	private Sprite sprite;
	private FrameList frameList;
	
	public Player(float x, float y, BulletFrame shot, FrameList frameList){
		super(null, x, y);
		
		this.shot = shot;
		
		bullets = new ArrayList<Bullet>();
		
		// temp
		sprite = new Sprite("player.png", 0, 0, 64, 64);
		this.frameList = frameList;
		
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
		bullets.add(new Bullet(shot, x - 10, y - 5, 270, 15, 500, 5, frameList));
		bullets.add(new Bullet(shot, x + 10, y - 5, 270, 15, 500, 5, frameList));
		shotCooldown = 4;
	}
	
	private void bomb(){
		bombing = true;
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
		alive = false;
	}
	
	public boolean isAlive(){
		return alive;
	}
	
	public int getHitboxSize(){
		return 3;
	}
	
	public ArrayList<Bullet> getBullets(){
		// Returns bullets then clears the array list
		// This is because the bullets on the frame should only be fired once
		// If not cleared, all previous bullets would keep firing forever
		
		if(bullets == null || bullets.size() < 1)
			return null;
		
		ArrayList<Bullet> temp = new ArrayList<Bullet>(bullets);
		bullets.clear();
		
		return temp;
	}
}
