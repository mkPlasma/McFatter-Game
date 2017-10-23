package engine.graphics;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;

import engine.entities.Bullet;
import engine.entities.Enemy;
import engine.entities.GameEntity;
import engine.entities.MovableEntity;
import engine.entities.Player;
import engine.screens.MainScreen;

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

	// 0 - Player
	// 1 - Enemy bullets
	// 2 - Player bullets
	
	// Vertices, tex coords, elements
	private Buffer[][] buffers = {
		{null, null, null},
		{null, null, null},
		{null, null, null},
	};
	
	// VAO first
	private int[][] bufferIndices = {
		{-1, -1, -1, -1},
		{-1, -1, -1, -1},
		{-1, -1, -1, -1},
	};
	
	public void init(){
		// Init shaders
		basicShader = new ShaderProgram("basic", "basic");
		basicShader.bindAttrib(0, "position");
		basicShader.bindAttrib(1, "texCoords");
		basicShader.link();
		
		illumiShader = new ShaderProgram("basic", "illumi");
		illumiShader.bindAttrib(0, "position");
		illumiShader.bindAttrib(1, "texCoords");
		illumiShader.link();
		
		initBuffers();
	}
	
	private void initBuffers(){
		// Player
		buffers[0] = new Buffer[]{
			BufferUtils.createFloatBuffer(8),
			BufferUtils.createFloatBuffer(8),
			BufferUtils.createIntBuffer(6),
		};
		
		// Enemy bullets
		int max = MainScreen.MAX_ENEMY_BULLETS;
		
		buffers[1] = new Buffer[]{
			BufferUtils.createFloatBuffer(max*8),
			BufferUtils.createFloatBuffer(max*8),
			BufferUtils.createIntBuffer(max*6),
		};
		
		// Player bullets
		max = MainScreen.MAX_PLAYER_BULLETS;
		
		buffers[2] = new Buffer[]{
			BufferUtils.createFloatBuffer(max*8),
			BufferUtils.createFloatBuffer(max*8),
			BufferUtils.createIntBuffer(max*6),
		};
		
		// Flip buffers
		for(Buffer[] ba:buffers)
			for(Buffer b:ba)
				b.flip();
		
		// Indices
		for(int i = 0; i < bufferIndices.length; i++){
			bufferIndices[i][0] = glGenVertexArrays();
			
			for(int j = 0; j < 3; j++){
				int b = glGenBuffers();
				bufferIndices[i][j + 1] = b;
				
				if(j < 2){
					glBindBuffer(GL_ARRAY_BUFFER, b);
					glBufferData(GL_ARRAY_BUFFER, (FloatBuffer)buffers[i][j], GL_DYNAMIC_DRAW);
				}
				else{
					glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, b);
					glBufferData(GL_ELEMENT_ARRAY_BUFFER, (IntBuffer)buffers[i][j], GL_DYNAMIC_DRAW);
				}
			}
		}

		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
	}
	
	// Render functions
	public void renderPlayer(Player p, int time){
		basicShader.use();
		
		ArrayList<GameEntity> pl = new ArrayList<GameEntity>();
		pl.add(p);
		
		renderEntities(0, pl, time);
	}
	
	public void renderEnemyBullets(ArrayList<Bullet> bl, int time){
		illumiShader.use();
		renderEntities(1, bl, time);
	}
	
	public void renderPlayerBullets(ArrayList<Bullet> bl, int time){
		illumiShader.use();
		renderEntities(2, bl, time);
	}
	
	public void renderEnemies(ArrayList<Enemy> el, int time){
		//basicShader.use();
		//renderEntities(el, time, false);
	}
	
	// Renders a list of entities
	@SuppressWarnings("rawtypes")
	public void renderEntities(int bufferIndex, ArrayList entityList, int time){
		
		if(entityList == null || entityList.size() == 0)
			return;
		
		ArrayList<GameEntity> el = new ArrayList<GameEntity>();
		
		// Take only visible entities
		for(int i = 0; i < entityList.size(); i++)
			if(((GameEntity)entityList.get(i)).isVisible())
				el.add((GameEntity)entityList.get(i));
		
		if(el.size() == 0)
			return;
		
		// VAO arguments
		int size = el.size()*8;
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
			float[] v = getVertexCoords(e.getX(), e.getY(), s.getScaledWidth(), s.getScaledHeight(), (float)Math.toRadians(r));
			
			// Fill vertices/texCoords
			for(int j = 0; j < 8; j++){
				vertices[i*8 + j] = v[j];
				texCoords[i*8 + j] = t[j];
			}
		}
		
		genQuadVAO(bufferIndex, el.size(), vertices, texCoords);
		
		// Render
		
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glBindTexture(GL_TEXTURE_2D, el.get(0).getSprite().getTextureID());
		
		glBindVertexArray(bufferIndices[bufferIndex][0]);
		
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		
		
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

		
		glDrawElements(GL_TRIANGLES, vertices.length, GL_UNSIGNED_INT, 0);
		
		glDisable(GL_BLEND);
		

		glDisableVertexAttribArray(1);
		glDisableVertexAttribArray(0);
		
		glBindVertexArray(0);
		
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
	}
	
	private void genQuadVAO(int bufferIndex, int num, float[] vertices, float[] texCoords){
		
		Buffer[] buffers = this.buffers[bufferIndex];
		int[] bIndices = bufferIndices[bufferIndex];
		
		glBindVertexArray(bIndices[0]);
		
		// Vertices
		FloatBuffer fb = (FloatBuffer)buffers[0];
		fb.clear();
		fb.put(vertices);
		fb.flip();
		
		glBindBuffer(GL_ARRAY_BUFFER, bIndices[1]);
		glBufferData(GL_ARRAY_BUFFER, fb, GL_DYNAMIC_DRAW);
		glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0);
		
		// Texture coords
		fb = (FloatBuffer)buffers[1];
		fb.clear();
		fb.put(texCoords);
		fb.flip();
		
		glBindBuffer(GL_ARRAY_BUFFER, bIndices[2]);
		glBufferData(GL_ARRAY_BUFFER, fb, GL_DYNAMIC_DRAW);
		glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		
		// Elements
		// 6 elements per 4 vertices
		int[] elements = new int[num*6];
		
		for(int i = 0; i < elements.length/6; i++){
			for(int j = 0; j < 6; j++){
				int a = j >= 3 ? -1 : 0;
				a = j == 5 ? -5 : a;
				elements[i*6 + j] = i*4 + j + a;
			}
		}
		
		IntBuffer eb = (IntBuffer)buffers[2];
		eb.clear();
		eb.put(elements);
		eb.flip();
		
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bIndices[3]);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, eb, GL_DYNAMIC_DRAW);
		
		glBindVertexArray(0);
	}
	
	// Delete vao/buffers
	public void cleanup(){
		for(int[] ia:bufferIndices){
			for(int i = 0; i < ia.length; i++){
				
				if(ia[i] == -1) continue;
				
				if(i == 0) glDeleteVertexArrays(ia[i]);
				else glDeleteBuffers(ia[i]);
			}
		}
	}
	
	// Returns vertex coordinates of object
	private float[] getVertexCoords(float cx, float cy, float w, float h, float r){
		
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

			float x2 = x[i];
			float y2 = y[i];
			
			// Rotate
			//x2 = x2*cos - y2*sin;
			//y2 = x2*sin + y2*cos;
			
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
