package engine;

import java.awt.Graphics2D;

public class ScreenManager{
	
	private GameScreen screen;
	
	public void update(){
		screen.update();
	}
	
	public void draw(Graphics2D g2d){
		screen.draw(g2d);
	}
	
	public void setScreen(GameScreen screen){
		this.screen = screen;
	}
	
	public GameScreen getGameScreen(){
		return screen;
	}
}
