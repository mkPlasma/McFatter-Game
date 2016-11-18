package engine;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class GameThread extends JPanel implements Runnable{
	
	private Graphics2D g2d;
	private KeyboardListener keyListener;
	private ScreenManager screenManager;
	
	private BufferedImage screenImg;
	
	public GameThread(KeyboardListener keyListener){
		super();
		setFocusable(true);
		requestFocus();
		
		addKeyListener(keyListener);
		this.keyListener = keyListener;
	}
	
	private void init(){
		WorldScreen testScreen = new WorldScreen();
		testScreen.init(keyListener);
		
		screenManager = new ScreenManager();
		screenManager.setScreen(testScreen);
		
		screenImg = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);
		g2d = (Graphics2D) screenImg.getGraphics();
	}
	
	public void run(){

		init();
		
		boolean running = true;
		
		final int TARGET_FPS = 60;
		final long OPTIMAL_TIME = 1000000000/TARGET_FPS;
		long lastLoopTime = System.nanoTime();
		
		while(running){
			long currentTime = System.nanoTime();
			long updateLength = currentTime - lastLoopTime;
			lastLoopTime = currentTime;
			
			double delta = updateLength/((double)OPTIMAL_TIME);
			
			update();
			draw();
			drawToScreen();
			
			try{
				Thread.sleep((lastLoopTime - System.nanoTime() + OPTIMAL_TIME)/1000000);
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	private void update(){
		screenManager.update();
	}
	
	private void draw(){
		screenManager.draw(g2d);
	}
	
	private void drawToScreen(){
		Graphics g = getGraphics();
		g.drawImage(screenImg, 0, 0, 800, 600, null);
		g.dispose();
	}
}
