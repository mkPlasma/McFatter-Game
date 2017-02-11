package engine;

public class ScreenManager{
	
	private GameScreen screen;
	
	public void update(){
		screen.update();
	}
	
	public void draw(){
		screen.draw();
	}
	
	public void setScreen(GameScreen screen){
		this.screen = screen;
	}
	
	public GameScreen getGameScreen(){
		return screen;
	}
}
