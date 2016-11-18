package engine;

import javax.swing.JFrame;

public class GameWindow extends JFrame{
	
	private final GameThread gameThread;
	
	private final KeyboardListener keyListener;
	
	public GameWindow(String name){
		super(name);
		// Init window
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		setSize(800, 600);
		setResizable(false);
		setLocationRelativeTo(null);
		
		// Init other stuff
		
		keyListener = new KeyboardListener();
		addKeyListener(keyListener);
		
		gameThread = new GameThread(keyListener);
		
		add(gameThread);
		
		setVisible(true);
		
		gameThread.run();
	}
	
}
