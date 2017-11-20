package engine.entities;

import content.FrameList;
import engine.screens.MainScreen;

public class BossEnemy extends Enemy{
	
	private int maxHp;
	
	public BossEnemy(EnemyFrame frame, float x, float y, int hp, FrameList frameList, MainScreen screen){
		super(frame, x, y, hp, frameList, screen);
		maxHp = hp;
	}
	
	public void update(){
		super.update();
		
		// temp
		screen.addText(Integer.toString((int)(100*((float)hp/maxHp))) + "%", 422, 460, -1, 0.8f, 2);
	}
}
