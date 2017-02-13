package content;

import java.util.ArrayList;

import engine.Mission;
import engine.entities.Bullet;
import engine.entities.Enemy;
import engine.entities.InstructionSet;
import engine.entities.MovementInstruction;
import engine.entities.Player;
import engine.graphics.Renderer;

public class TestMission extends Mission{
	
	public TestMission(Renderer r){
		super(r);
	}
	
	public void init(){
		player = new Player(400, 500);
		
		bullets = new ArrayList<Bullet>();
		enemies = new ArrayList<Enemy>();

		InstructionSet inst = new InstructionSet(InstructionSet.INST_MOVABLE);
		inst.add(new MovementInstruction(null, 0, MovementInstruction.ENT_ENEMY, MovementInstruction.SET_POS, new float[]{400, 200}));
		
		enemies.add(new Enemy(inst));
	}
	
	int counter;
	
	public void update(){
		int c = 64;
		
		int t = 120;
		
		if(time % t == 0){
			for(int i = 0; i < c; i++){
				float dir = i*(360f/c) + (180/c) + 90;
				
				InstructionSet inst = new InstructionSet(InstructionSet.INST_MOVABLE);
				inst.add(new MovementInstruction(null, 0, MovementInstruction.ENT_BULLET, MovementInstruction.SET_POS, new float[]{400, 200}));
				inst.add(new MovementInstruction(null, 0, MovementInstruction.ENT_BULLET, MovementInstruction.CONST_ACCEL, new float[]{dir, 6, -0.1f, 0, 10}));
				inst.add(new MovementInstruction(null, t, MovementInstruction.ENT_BULLET, MovementInstruction.CONST_ACCEL, new float[]{dir, 0, 0.05f, 0, 10}));
				
				Bullet b = new Bullet(inst, BulletSheet.get((byte)counter, BulletSheet.COLOR_PURPLE));
				bullets.add(b);
			}
			
			counter++;
			
			if(counter == 8)
				counter = 0;
		}
		
		
		time++;
	}
	
	public void draw(){
		player.draw(r);
	}
}
