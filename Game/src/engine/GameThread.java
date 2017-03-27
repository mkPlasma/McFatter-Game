package engine;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import engine.graphics.Renderer;
import engine.screens.ScreenManager;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;


public class GameThread implements Runnable{
	
	private long window;
	
	private GLFWKeyCallback keyCallback;
	
	// Resolution scale
	private float scale;
	
	private int fps;
	
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
		
		// Create window
		scale = Settings.getWindowScale();
		Renderer.updateScale();
		
		window = glfwCreateWindow((int)(800*scale), (int)(600*scale), "DSG", NULL, NULL);
		
		if(window == NULL)
			throw new RuntimeException("Failed to create GLFW window");
		
		glfwSetKeyCallback(window, keyCallback = new KeyboardListener());
		
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
		
		ScreenManager.init();
		ScreenManager.setScreen(ScreenManager.scrnMain);
		ScreenManager.initScreen();
	}
	
	public void run(){
		init();
		loop();
		
		// Cleanup
		glfwFreeCallbacks(window);
		glfwDestroyWindow(window);
		
		glfwTerminate();
		glfwSetErrorCallback(null).free();
	}
	
	private void loop(){
		
		GL.createCapabilities();
		
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		glOrtho(0, 800*scale, 600*scale, 0, 1, -1);
		
		glClearColor(0, 0, 0, 0);
		glMatrixMode(GL_MODELVIEW);
		
		// Timing
		double lastLoopTime = System.nanoTime();
		
		final int TARGET_FPS = 60;
		final long OPTIMAL_TIME = 1000000000/TARGET_FPS;
		
		// FPS count
		int lastSecondTime = (int)(lastLoopTime/1000000000);
		int frameCount = 0;
		fps = 60;
		
		while(!glfwWindowShouldClose(window)){
			// Timing
			long startTime = System.nanoTime();
			lastLoopTime = startTime;
			
			// Game logic and drawing
			update();
			render();
			
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
				Thread.sleep(wait);
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	private void update(){
		glfwPollEvents();
		
		ScreenManager.update();
	}
	
	private void render(){
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		ScreenManager.render();
        
		glfwSwapBuffers(window);
	}
}
