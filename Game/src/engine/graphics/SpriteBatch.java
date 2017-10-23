package engine.graphics;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;

import engine.entities.GameEntity;
import engine.entities.MovableEntity;

/*
 * 		Renderer.java
 * 		
 * 		Purpose:	Stores VAOs/VBOs for a set of entities
 * 		Notes:		
 * 		
 */

public class SpriteBatch{
	
	private int size;
	
	private FloatBuffer vBuffer;
	private FloatBuffer tcBuffer;
	
	private final int vao, vbo, tex;
	
	private int textureID;
	
	// Additive rendering
	private boolean additive;
	
	public SpriteBatch(int capacity, boolean additive){
		vBuffer		= BufferUtils.createFloatBuffer(capacity);
		tcBuffer		= BufferUtils.createFloatBuffer(capacity);
		
		vao = glGenVertexArrays();
		vbo = glGenBuffers();
		tex = glGenBuffers();
		
		this.additive = additive;
	}
	
	public void setTextureID(int textureID){
		this.textureID = textureID;
	}
	
	public void draw(){
		enable();
		glDrawArrays(GL_TRIANGLES, 0, size);
		disable();
	}
	
	private void enable(){
		// Bind texture
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glBindTexture(GL_TEXTURE_2D, textureID);
		
		// Bind VAO
		glBindVertexArray(vao);
		
		// Enable arrays
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		
		// Set blend mode
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, additive ? GL_ONE : GL_ONE_MINUS_SRC_ALPHA);
	}
	
	private void disable(){
		// Disable blending
		glDisable(GL_BLEND);
		
		// Disable arrays
		glDisableVertexAttribArray(1);
		glDisableVertexAttribArray(0);
		
		// Unbind VAO
		glBindVertexArray(0);
	}
	
	public void cleanup(){
		glDeleteVertexArrays(vao);
		glDeleteBuffers(vbo);
		glDeleteBuffers(tex);
	}
	
	// Update batch
	@SuppressWarnings({"unchecked", "rawtypes"})
	public void update(ArrayList entityList, int time){
		
		ArrayList<GameEntity> el = new ArrayList<GameEntity>();
		
		// Take only visible entities
		for(GameEntity e:(ArrayList<GameEntity>)entityList){
			if(e.isVisible() && !e.remove())
				el.add(e);
		}
		
		// Set vertices count
		size = el.size()*12;
		
		if(el.isEmpty())
			return;
		
		// VAO arguments
		float[] vertices = new float[size];
		float[] texCoords = new float[size];
		
		for(int i = 0; i < el.size(); i++){
			
			GameEntity e = el.get(i);
			
			// Get animated sprite
			Sprite s = e.getSprite().animate(e.getTime(), time, e);
			
			// Tex coords
			float[] t = s.getTextureCoords();
			
			// Rotation
			float r = s.getRotation();
			
			if(e instanceof MovableEntity)
				r += (e.getFrame().spriteAlign() ? ((MovableEntity)e).getDir() + 90 : 0);
			
			// Vertex coords
			float[] v = getVertexCoords(e.getX(), e.getY(), s.getScaledWidth(), s.getScaledHeight(), r);
			
			// Fill vertices/texCoords
			for(int j = 0; j < 6; j++){
				int a = j >= 3 ? j - 1 : j;
				a = j == 5 ? 0 : a;
				
				vertices[i*12 + j*2]			= v[a*2];
				vertices[i*12 + j*2 + 1]		= v[a*2 + 1];
				texCoords[i*12 + j*2]		= t[a*2];
				texCoords[i*12 + j*2 + 1]	= t[a*2 + 1];
			}
		}
		
		// Generate
		glBindVertexArray(vao);
		
		// Vertices
		vBuffer.clear();
		vBuffer.put(vertices);
		vBuffer.flip();
		
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glBufferData(GL_ARRAY_BUFFER, vBuffer, GL_DYNAMIC_DRAW);
		glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0);
		
		// Texture coords
		tcBuffer.clear();
		tcBuffer.put(texCoords);
		tcBuffer.flip();
		
		glBindBuffer(GL_ARRAY_BUFFER, tex);
		glBufferData(GL_ARRAY_BUFFER, tcBuffer, GL_DYNAMIC_DRAW);
		glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		
		glBindVertexArray(0);
	}
	
	// Returns vertex coordinates of object
	private float[] getVertexCoords(float cx, float cy, float w, float h, float r){
		
		r = (float)Math.toRadians(r);
		
		// Sin and cos for rotations
		float sin = (float)Math.sin(r);
		float cos = (float)Math.cos(r);

		// Divide by 4 since textures are 2x scale at 640x480
		w /= 4;
		h /= 4;
		
		// Top left, top right, bottom right, bottom left
		float x[] = {-w, w, w, -w};
		float y[] = {-h, -h, h, h};
		
		for(int i = 0; i < 4; i++){
			
			// Rotate
			float x2 = x[i]*cos - y[i]*sin;
			float y2 = x[i]*sin + y[i]*cos;
			
			// Move to center
			x2 += cx;
			y2 += cy;
			
			// Normalize
			x2 = x2/320 - 1;
			y2 = -(y2/240 - 1);
			
			x[i] = x2;
			y[i] = y2;
		}
		
		return new float[]{
			x[0], y[0],
			x[1], y[1],
			x[2], y[2],
			x[3], y[3],
		};
	}
}
