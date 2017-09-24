package content;

import java.util.ArrayList;

import engine.entities.Bullet;
import engine.entities.Enemy;
import engine.entities.EnemyFrame;
import engine.entities.InstructionSet;
import engine.entities.MovementInstruction;
import engine.entities.Player;
import engine.screens.MainScreen;
import engine.screens.Mission;

/*
 * 		TestMission.java
 * 		
 * 		Purpose:	Temporary mission for testing bullets, collisions, etc.
 * 		Notes:		Will be replaced by external definitions.
 * 		
 * 		Last modified by:	Daniel
 * 		Date:				
 * 		Changes:			
 */

public class TestMission extends Mission{
	
	public TestMission(MainScreen screen){
		super(screen);
	}
	
	public void init(){
		player = new Player(400, 500);
		
		bullets = new ArrayList<Bullet>();
		enemies = new ArrayList<Enemy>();
		
		
		
		InstructionSet inst = new InstructionSet(InstructionSet.INST_MOVABLE);
		inst.add(new MovementInstruction(null, 0, MovementInstruction.ENT_ENEMY, MovementInstruction.SET_POS, new float[]{400, 200}));
		
		enemies.add(new Enemy(new EnemyFrame(), inst));
		
	}
	
	
	int counter, counter2;
	int cn;
	
	
	public void update(){
		
		testFire1();
		
		/*
		if(cn == 0){
			for(int i = 0; i < c; i++){
				float dir = i*(360f/c) + (180f/c) + 90;
				
				int r = 100;
				
				InstructionSet inst = new InstructionSet(InstructionSet.INST_MOVABLE);
				inst.add(new MovementInstruction(null, 0, MovementInstruction.ENT_LASER, MovementInstruction.SET_POS, new float[]{400 + (float)(r*Math.cos(Math.toRadians(dir))), 200 + (float)(r*Math.sin(Math.toRadians(dir)))}));
				inst.add(new MovementInstruction(null, 0, MovementInstruction.ENT_LASER, MovementInstruction.SET_SIZE, new float[]{256, 32}));
				inst.add(new MovementInstruction(null, 0, MovementInstruction.ENT_LASER, MovementInstruction.SET_DIR, new float[]{dir}));
				inst.add(new MovementInstruction(null, 0, MovementInstruction.ENT_LASER, MovementInstruction.SET_ANGVEL, new float[]{0.5f}));
				Laser l = new Laser(inst, BulletSheet.get((byte)16, (byte)(i%16)));
				bullets.add(l);
			}
		}
		*/
		
		cn++;
		time++;
	}
	
	public void testFire1(){
		
		if(screen.getEnemies() == null)
			return;
		if(screen.getEnemies().size() == 0)
			return;
		
		Enemy e = screen.getEnemies().get(0);
		
		if(e == null)
			return;
		if(e.remove())
			return;
		
		int c = 1;
		int t = 0;
		int s = 20;
		
		for(int i = 0; i < c; i++){
			float dir = i*(360f/c) + (180f/c) - 90;
			
			InstructionSet inst = new InstructionSet(InstructionSet.INST_MOVABLE);
			inst.add(new MovementInstruction(null, 0, MovementInstruction.ENT_BULLET, MovementInstruction.SET_POS, new float[]{400, 200}));
			//inst.add(new MovementInstruction(null, 0, MovementInstruction.ENT_BULLET, MovementInstruction.CONST_ACCEL, new float[]{dir, s, -((float)s/(float)t), 0, s}));
			inst.add(new MovementInstruction(null, t, MovementInstruction.ENT_BULLET, MovementInstruction.CONST_ACCEL, new float[]{dir + time*5, 0, 0.02f, 0, 10}));
			
			Bullet b = new Bullet(BulletList.get((byte)counter, (byte)counter2), inst);
			bullets.add(b);
		}
		
		counter++;
		
		if(counter == 16){
			counter = 0;
			counter2++;
		}

		if(counter2 == 16)
			counter2 = 0;
	}
	
	public void render(){
		
	}
}