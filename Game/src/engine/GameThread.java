package engine;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import engine.screens.ScreenManager;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

import java.nio.IntBuffer;

/*
 * 		GameThread.java
 * 		
 * 		Purpose:	Game core. Initializes game window and runs logic and rendering code.
 * 		Notes:		Should not need to be modified. Ask Daniel before modifying this file.
 * 		
 */

public class GameThread implements Runnable{
	
	private long window;
	
	// Resolution scale
	private float scale;
	
	final int TARGET_FPS = 60;
	final long OPTIMAL_TIME = 1000000000/TARGET_FPS;
	
	private ScreenManager screenManager;
	
	public GameThread(){
		super();
	}
	
	private void init(){
		
		// Set GLFW to print errors
		GLFWErrorCallback.createPrint(System.err).set();
		
		if(!glfwInit())
			throw new IllegalStateException("Unabled to initialize GLFW");
		
		// Make not resizable
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
		
		// Set OpenGL context
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
		glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
		glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
		
		// Create window
		scale = Settings.getWindowScale();
		
		window = glfwCreateWindow((int)(640*scale), (int)(480*scale), "DSG", NULL, NULL);
		
		if(window == NULL)
			throw new RuntimeException("Failed to create GLFW window");
		
		glfwSetKeyCallback(window, new KeyboardListener());
		
		try(MemoryStack stack = stackPush()){
			IntBuffer pWidth = stack.mallocInt(1);
			IntBuffer pHeight = stack.mallocInt(1);
			
			glfwGetWindowSize(window, pWidth, pHeight);
			
			// Get monitor
			GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
			
			// Center window
			glfwSetWindowPos(window, (vidmode.width() - pWidth.get(0))/2, (vidmode.height() - pHeight.get(0))/2);
		}
		
		glfwMakeContextCurrent(window);
		
		// Vsync
		glfwSwapInterval(1);
		
		glfwShowWindow(window);
		
		
		GL.createCapabilities();
		glClearColor(0, 0, 0, 0);
		
		screenManager = new ScreenManager();
		screenManager.init();
		screenManager.setScreen(screenManager.mainScreen);
		screenManager.initScreen();
		
		screenManager.mainScreen.setFPS(0);
	}
	
	public void run(){
		init();
		loop();
	}
	
	private void loop(){
		
		// Timing
		long lastLoopTime = System.nanoTime();
		
		// FPS count
		int lastSecond = (int)(lastLoopTime/1000000000);
		int frameCount = 0;
		
		while(!glfwWindowShouldClose(window)){
			// Timing
			lastLoopTime = System.nanoTime();
			
			// Game logic and drawing
			update();
			render();
			
			int currentSecond = (int)(lastLoopTime/1000000000);
			frameCount++;
			
			// FPS
			if(currentSecond > lastSecond){
				screenManager.mainScreen.setFPS(frameCount);
				frameCount = 0;
				lastSecond = currentSecond;
			}
			
			
			// Timing
			try{
				long wait = (long)((lastLoopTime - System.nanoTime() + OPTIMAL_TIME)/1000000);
				wait = wait < 0 ? 0 : wait;
				Thread.sleep(wait);
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		
		cleanup();
	}
	
	private void update(){
		glfwPollEvents();
		
		screenManager.update();
	}
	
	private void render(){
		glClear(GL_COLOR_BUFFER_BIT);
		
		screenManager.render();
        
		glfwSwapBuffers(window);
	}
	
	private void cleanup(){
		screenManager.cleanup();
		
		glfwFreeCallbacks(window);
		glfwDestroyWindow(window);

		glfwSetErrorCallback(null).free();
		glfwTerminate();
	}
}
