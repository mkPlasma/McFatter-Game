package engine.graphics;

import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.stb.STBImage.stbi_load_from_memory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImage;

import engine.IOFunctions;

/*
 * 		Texture.java
 * 		
 * 		Purpose:	Stores texture and ID.
 * 		Notes:		
 * 		
 */

public class Texture{
	
	private final String path;
	private int width, height;
	
	private boolean loaded;
	
	private ByteBuffer image;
	private int id;
	
	public Texture(String path){
		this.path = path;
	}
	
	public void load(){
		if(loaded)
			return;
		
		IntBuffer w = BufferUtils.createIntBuffer(1);
		IntBuffer h = BufferUtils.createIntBuffer(1);
		IntBuffer comp = BufferUtils.createIntBuffer(1);
		
		ByteBuffer t = null;
		
		try{
			t = stbi_load_from_memory(IOFunctions.readToByteBuffer("Game/res/img/" + path), w, h, comp, 0);
			loaded = true;
		}catch(IOException e){
			e.printStackTrace();
		}
		
		if(t == null)
			throw new RuntimeException("Failed to load image: " + STBImage.stbi_failure_reason());
		
		width = w.get(0);
		height = h.get(0);
		
		image = t;
		
		id = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, id);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, image);
		glBindTexture(GL_TEXTURE_2D, 0);
	}
	
	public String getPath(){
		return path;
	}
	
	public int getWidth(){
		return width;
	}
	
	public int getHeight(){
		return height;
	}
	
	public ByteBuffer getImage(){
		return image;
	}
	
	public int getID(){
		return id;
	}
}
