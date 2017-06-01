package engine.entities;

import static engine.KeyboardListener.*;
import static org.lwjgl.glfw.GLFW.*;

import java.util.ArrayList;

import content.BulletList;
import engine.graphics.Renderer;

public class Player extends GameEntity{
	
	
	private boolean alive = true;
	
	
	private final int focusedSpeed = 2, unfocusedSpeed = 4;
	
	private int speed;
	private boolean focused, firing, bombing;
	
	private BulletFrame shot;
	
	// Timer variables counts up for timing shots/bombs
	private int shotCooldown = 0, bombCooldown = 0;
	
	private ArrayList<Bullet> bullets;
	
	
	public Player(float x, float y){
		super(x, y);
		
		bullets = new ArrayList<Bullet>();
		shot = BulletList.get(BulletList.TYPE_MISSILE, BulletList.COLOR_RED);
		
		onCreate();
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
		if(x < 0)	x = 0;
		if(x > 800)	x = 800;
		if(y < 0)	y = 0;
		if(y > 600)	y = 600;
		
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
		bullets.add(new Bullet(shot, x - 5, y, 270, 15, 500, 5));
		bullets.add(new Bullet(shot, x + 5, y, 270, 15, 500, 5));
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
	
	public void onCreate(){
		
	}
	
	public void onDestroy(){
		
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
