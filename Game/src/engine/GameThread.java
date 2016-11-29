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
		
		// Create test game screen
		MainScreen testScreen = new MainScreen();
		testScreen.init(keyListener);
		
		// Set up screen manager
		screenManager = new ScreenManager();
		screenManager.setScreen(testScreen);
		
		// Image holds graphics for the screen
		screenImg = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);
		g2d = (Graphics2D) screenImg.getGraphics();
	}
	
	public void run(){
		
		init();
		
		boolean running = true;
		
		// Timing
		double lastLoopTime = System.nanoTime();
		
		final int TARGET_FPS = 60;
		final long OPTIMAL_TIME = 1000000000/TARGET_FPS;
		
		// FPS count
		int lastSecondTime = (int)(lastLoopTime/1000000000);
		int fps = 60;
		int frameCount = 0;
		
		while(running){
			// Timing
			long startTime = System.nanoTime();
			lastLoopTime = startTime;
			
			// Game logic and drawing
			update();
			draw();
			drawToScreen();
			frameCount++;
			
			// FPS
			int currentSecond = (int)(lastLoopTime/1000000000);
			
			if(currentSecond > lastSecondTime){
				fps = frameCount;
				frameCount = 0;
				lastSecondTime = currentSecond;
				
				System.out.println("FPS: " + fps);
			}
			
			// Timing
			try{
				long wait = (long)((lastLoopTime - System.nanoTime() + OPTIMAL_TIME)/1000000);
				wait = wait < 0 ? 1 : wait;
				//Thread.yield();
				Thread.sleep(wait);
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
		// Draw bufferedimage to the screen
		Graphics g = getGraphics();
		g.drawImage(screenImg, 0, 0, 800, 600, null);
		g.dispose();
	}
}
