package engine;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;

import javax.swing.JPanel;

public class GameThread extends JPanel implements Runnable{
	
	private Graphics2D g2d;
	private ScreenManager screenManager;
	
	// VolatileImage is used if hardware acceleration is on
	private VolatileImage screenImg;
	private BufferedImage screenImgB;
	
	// Resolution scale
	private float scale;
	
	private int fps;
	
	public GameThread(){
		super();
	}
	
	private void init(){
		
		setFocusable(true);
		requestFocus();
		
		final KeyboardListener keyListener = new KeyboardListener();
		addKeyListener(keyListener);
		
		// Image holds graphics for the screen
		
		scale = Settings.getWindowScale();
		
		if(Settings.useHardwareAcceleration()){
			screenImg = createVolatileImage((int)(800*scale), (int)(600*scale));
			g2d = (Graphics2D)screenImg.getGraphics();
		}
		else{
			screenImgB = new BufferedImage((int)(800*scale), (int)(600*scale), BufferedImage.TYPE_INT_ARGB);
			g2d = (Graphics2D)screenImgB.getGraphics();
		}
		
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		
		// Create test game screen
		MainScreen testScreen = new MainScreen();
		testScreen.init(g2d);
		
		// Set up screen manager
		screenManager = new ScreenManager();
		screenManager.setScreen(testScreen);
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
		int frameCount = 0;
		fps = 60;
		
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
			}
			
			// Timing
			try{
				long wait = (long)((lastLoopTime - System.nanoTime() + OPTIMAL_TIME)/1000000);
				wait = wait < 0 ? 1 : wait;
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
		screenManager.draw();
	}
	
	private void drawToScreen(){
		// Draw bufferedimage to the screen
		Graphics g = getGraphics();
		
		g2d.setColor(Color.WHITE);
		
		if(fps <= 57)
			g2d.setColor(Color.RED);
		
		g2d.drawString(Integer.toString(fps), 782*scale, 596*scale);
		
		
		if(Settings.useHardwareAcceleration())
			g.drawImage(screenImg, 0, 0, (int)(800*scale), (int)(600*scale), null);
		else
			g.drawImage(screenImgB, 0, 0, (int)(800*scale), (int)(600*scale), null);
		
		g.dispose();
	}
}
