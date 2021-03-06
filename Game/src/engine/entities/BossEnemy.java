package engine.entities;

import engine.screens.MainScreen;

/**
 * 
 * Boss enemy type.
 * 
 * @author Daniel
 *
 */

public class BossEnemy extends Enemy{
	
	private int maxHp;
	
	// temp
	private Text hpText;
	
	public BossEnemy(EnemyFrame frame, float x, float y, int hp, MainScreen screen){
		super(frame, x, y, hp, screen);
		maxHp = hp;
		
		hpText = new Text("100%", 422, 460, -1, 0.8f, -1, screen.getTextureCache());
		screen.addText(hpText);
	}
	
	public void update(){
		super.update();
		
		// temp
		hpText.setText(Integer.toString((int)(100*((float)hp/maxHp))) + "%");
	}
	
	public void delete(){
		super.delete();
		
		if(hpText != null)
			hpText.delete();
	}
	
	public void onDestroy(){
		super.onDestroy();
		hpText.delete();
	}
}
