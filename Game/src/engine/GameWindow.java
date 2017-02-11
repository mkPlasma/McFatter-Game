package engine;

import java.awt.Dimension;

import javax.swing.JFrame;

public class GameWindow extends JFrame{
	
	public GameWindow(String name){
		super(name);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		setResizable(false);
		
		float scale = Settings.getWindowScale();
		
		final GameThread gameThread = new GameThread();
		gameThread.setPreferredSize((new Dimension((int)(800*scale), (int)(600*scale))));
		add(gameThread);
		
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
		
		gameThread.run();
	}
	
}
