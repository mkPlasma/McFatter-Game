package engine.graphics;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;

import engine.Settings;
import engine.entities.Bullet;
import engine.entities.Enemy;
import engine.entities.GameEntity;
import engine.entities.MovableEntity;

/*
 * 		Renderer.java
 * 		
 * 		Purpose:	Renders game objects.
 * 		Notes:		
 * 		
 * 		Last modified by:	Daniel
 * 		Date:				10/17
 * 		Changes:			
 */

public class Renderer{
	
	private ShaderProgram basicShader;
	private ShaderProgram illumiShader;
	
	public void init(){
		basicShader = new ShaderProgram("basic", "basic");
		basicShader.bindAttrib(0, "position");
		basicShader.bindAttrib(1, "rotation");
		basicShader.link();
		
		illumiShader = new ShaderProgram("basic", "illumi");
		illumiShader.bindAttrib(0, "position");
		illumiShader.bindAttrib(1, "rotation");
		illumiShader.link();
	}
	
	// Render functions

	public void renderBullets(ArrayList<Bullet> bli, int time){
		illumiShader.use();
		renderEntities(bli, time, true);
	}
	
	public void renderEnemies(ArrayList<Enemy> eli, int time){
		basicShader.use();
		renderEntities(eli, time, false);
	}
	
	// Renders a single entity
	public void renderEntity(GameEntity entity, int time){
		
		// Get animated sprite
		Sprite s = entity.getSprite().animate(entity.getTime(), time, entity);
		
		float[] c = {entity.getX(), entity.getY()};
		float[] v = getVertexCoords(c[0], c[1], s.getScaledWidth(), s.getScaledHeight());
		float[] t = s.getTextureCoords();
		
		float[] vertices = new float[16];
		
		// Fill vertices
		// x, y, textureX, textureY
		for(int i = 0; i < 4; i++){
			vertices[i*4]		= v[i*2];
			vertices[i*4 + 1]	= v[i*2 + 1];
			vertices[i*4 + 2]	= t[i*2];
			vertices[i*4 + 3]	= t[i*2 + 1];
		}
		
		int[] vao = genQuadVAO(1, vertices, null);
		
		
		// Render
		
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glBindTexture(GL_TEXTURE_2D, entity.getSprite().getTextureID());
		
		glBindVertexArray(vao[0]);
		
		glEnableVertexAttribArray(0);
		
		
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

		glDrawElements(GL_TRIANGLES, vertices.length/2, GL_UNSIGNED_INT, 0);
		
		glDisable(GL_BLEND);
		
		
		glDisableVertexAttribArray(0);
		
		glBindVertexArray(0);
		
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
	}
	
	// Renders a list of entities
	@SuppressWarnings("rawtypes")
	public void renderEntities(ArrayList entityList, int time, boolean useRotations){
		
		if(entityList == null || entityList.size() == 0)
			return;
		
		ArrayList<GameEntity> el = new ArrayList<GameEntity>();
		
		// Take only visible entities
		for(int i = 0; i < entityList.size(); i++)
			if(((GameEntity)entityList.get(i)).isVisible())
				el.add((GameEntity)entityList.get(i));
		
		// VAO arguments
		int n = el.size()*4;
		float[] vertices = new float[n*4];
		float[] rotations = null;
		
		if(useRotations)
			rotations = new float[n*3];
		
		for(int i = 0; i < el.size(); i++){
			
			GameEntity e = el.get(i);
			
			// Get animated sprite
			Sprite s = e.getSprite().animate(e.getTime(), time, e);
			
			float[] c = {e.getX(), e.getY()};
			float[] v = getVertexCoords(c[0], c[1], s.getScaledWidth(), s.getScaledHeight());
			float[] t = s.getTextureCoords();
			
			float r = s.getRotation();
			
			if(e instanceof MovableEntity)
				r += (e.getFrame().spriteAlign() ? ((MovableEntity)e).getDir() + 90 : 0);
			
			// Fill vertices
			// x, y, textureX, textureY
			for(int j = 0; j < 4; j++){
				vertices[i*16 + j*4]		= v[j*2];
				vertices[i*16 + j*4 + 1]	= v[j*2 + 1];
				vertices[i*16 + j*4 + 2]	= t[j*2];
				vertices[i*16 + j*4 + 3]	= t[j*2 + 1];
				
				if(useRotations){
					rotations[i*12 + j*3]		= c[0];
					rotations[i*12 + j*3 + 1]	= c[1];
					rotations[i*12 + j*3 + 2]	= (float)Math.toRadians(r);
				}
			}
		}
		
		int[] vao = genQuadVAO(el.size(), vertices, rotations);
		
		
		// Render
		
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glBindTexture(GL_TEXTURE_2D, el.get(0).getSprite().getTextureID());
		
		glBindVertexArray(vao[0]);
		
		glEnableVertexAttribArray(0);
		if(useRotations)
			glEnableVertexAttribArray(1);
		
		
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

		glDrawElements(GL_TRIANGLES, vertices.length/2, GL_UNSIGNED_INT, 0);
		
		glDisable(GL_BLEND);
		
		
		if(useRotations)
			glDisableVertexAttribArray(1);
		glDisableVertexAttribArray(0);
		
		glBindVertexArray(0);
		
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
	}
	
	private int[] genQuadVAO(int num, float[] vertices, float[] rotations){
		
		int vao = glGenVertexArrays();
		glBindVertexArray(vao);
		
		// Vertices
		FloatBuffer vBuffer = BufferUtils.createFloatBuffer(vertices.length);
		vBuffer.put(vertices);
		vBuffer.flip();
		int vbo = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glBufferData(GL_ARRAY_BUFFER, vBuffer, GL_DYNAMIC_DRAW);
		glVertexAttribPointer(0, 4, GL_FLOAT, false, 0, 0);
		
		int rts = 0;
		
		if(rotations != null){
			// Rotations
			FloatBuffer rBuffer = BufferUtils.createFloatBuffer(rotations.length);
			rBuffer.put(rotations);
			rBuffer.flip();
			rts = glGenBuffers();
			glBindBuffer(GL_ARRAY_BUFFER, rts);
			glBufferData(GL_ARRAY_BUFFER, rBuffer, GL_DYNAMIC_DRAW);
			glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
		}
		
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		
		// 6 elements per 4 vertices
		int[] elements = new int[num*6];
		
		for(int i = 0; i < elements.length/6; i++){
			for(int j = 0; j < 6; j++){
				int a = j >= 3 ? -1 : 0;
				a = j == 5 ? -5 : a;
				elements[i*6 + j] = i*4 + j + a;
			}
		}
		
		// Elements
		IntBuffer eBuffer = BufferUtils.createIntBuffer(elements.length);
		eBuffer.put(elements);
		eBuffer.flip();
		int ebo = glGenBuffers();
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, eBuffer, GL_DYNAMIC_DRAW);
		
		glBindVertexArray(0);
		
		if(rotations != null)
			return new int[]{vao, vbo, rts, ebo};
		return new int[]{vao, vbo, ebo};
	}
	
	public void drawRectangle(float x, float y, float w, float h){
		
		basicShader.use();
		
		float[] v = getVertexCoords(x, y, w, h);
		
		float tx = 16f/1024f, ty = 16f/512f;
		
		float[] vertices = {
			v[0], v[1], tx, ty,
			v[2], v[3], tx, ty,
			v[4], v[5], tx, ty,
			v[6], v[7], tx, ty,
		};
		
		int[] vao = genQuadVAO(1, vertices, null);
		
		// Render

		//glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		//glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		//glBindTexture(GL_TEXTURE_2D, 1);
		
		
		glBindVertexArray(vao[0]);
		glEnableVertexAttribArray(0);
		
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		
		glDrawElements(GL_TRIANGLES, vertices.length, GL_UNSIGNED_INT, 0);
		
		glDisable(GL_BLEND);
		
		glDisableVertexAttribArray(0);
		glBindVertexArray(0);
		
		
		// Delete VAO
		for(int i = 1; i < vao.length; i++)
			glDeleteBuffers(vao[i]);
		
		glDeleteVertexArrays(vao[0]);
	}
	
	// Delete vao/buffers
	public void cleanup(){
		glDeleteVertexArrays(0);
		
		for(int i = 0; i < 3; i++)
			glDeleteBuffers(i);
	}
	
	// Returns normalized vertex coordinates
	private float[] getVertexCoords(float x, float y, float w, float h){
		float left =	x - w/2;
		float right =	x + w/2;
		float top =		y - h/2;
		float bottom =	y + h/2;
		
		return new float[]{
			left,  top,
			right, top,
			right, bottom,
			left,  bottom
		};
	}
}
