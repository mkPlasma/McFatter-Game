package engine.graphics;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;

import engine.entities.Bullet;
import engine.entities.Enemy;
import engine.entities.GameEntity;
import engine.entities.MovableEntity;
import engine.entities.Player;

/*
 * 		RenderBatch.java
 * 		
 * 		Purpose:	Stores VAOs/VBOs for a set of entities
 * 		Notes:		
 * 		
 */

public class RenderBatch{
	
	public static final int
		UPDATE_VBO = 0b00001,
		UPDATE_SZE = 0b00010,
		UPDATE_TEX = 0b00100,
		UPDATE_TFM = 0b01000,
		UPDATE_ALP = 0b10000,
		
		UPDATE_NONE			= 0,
		UPDATE_ALL_BUT_SIZE	= 0b11101,
		UPDATE_HITBOX		= 0b00011;
	
	
	private final int shader;
	
	// Number of quads to render
	private int size;
	
	private final int capacity;
	
	// Size in pixels of quad
	private final short sizePixelsX, sizePixelsY;
	
	private FloatBuffer vboBuffer, texBuffer, tfmBuffer, alpBuffer;
	private ShortBuffer szeBuffer;
	
	private final int vao, vbo, sze, tex, tfm, alp;
	
	private final int textureID;
	
	// Which buffers to update
	private int updates;
	private boolean uVBO, uSZE, uTEX, uTFM, uALP;
	
	// Additive rendering
	private boolean additive;
	
	public RenderBatch(int capacity, int sizePixels, int textureID, int updates, boolean additive){
		this.capacity = capacity;
		this.textureID = textureID;
		this.updates = updates;
		this.additive = additive;

		shader = 0;
		sizePixelsX = (short)sizePixels;
		sizePixelsY = (short)sizePixels;
		
		vao = glGenVertexArrays();
		vbo = glGenBuffers();
		sze = glGenBuffers();
		tex = glGenBuffers();
		tfm = glGenBuffers();
		alp = glGenBuffers();
		
		init();
	}
	
	public RenderBatch(int capacity, int sizePixelsX, int sizePixelsY, int textureID, int updates, boolean additive){
		this.capacity = capacity;
		this.sizePixelsX = (short)sizePixelsX;
		this.sizePixelsY = (short)sizePixelsY;
		this.textureID = textureID;
		this.updates = updates;
		this.additive = additive;

		shader = 0;
		
		vao = glGenVertexArrays();
		vbo = glGenBuffers();
		sze = glGenBuffers();
		tex = glGenBuffers();
		tfm = glGenBuffers();
		alp = glGenBuffers();
		
		init();
	}
	
	// For hitboxes
	public RenderBatch(int capacity, int updates){
		this.capacity = capacity;
		this.updates = updates;
		
		shader = 1;
		textureID = -1;
		sizePixelsX = 0;
		sizePixelsY = 0;
		
		vao = glGenVertexArrays();
		vbo = glGenBuffers();
		sze = glGenBuffers();
		tex = 0;
		tfm = 0;
		alp = 0;
		
		init();
	}
	
	private void init(){
		vboBuffer = BufferUtils.createFloatBuffer(capacity*2);
		
		if(shader == 0){
			szeBuffer = BufferUtils.createShortBuffer(capacity*2);
			texBuffer = BufferUtils.createFloatBuffer(capacity*4);
			tfmBuffer = BufferUtils.createFloatBuffer(capacity*3);
			alpBuffer = BufferUtils.createFloatBuffer(capacity);
			
			uVBO = true;
			uSZE = true;
			uTEX = true;
			uTFM = true;
			uALP = true;
			
			return;
		}
		if(shader == 1){
			szeBuffer = BufferUtils.createShortBuffer(capacity);
			
			uVBO = true;
			uSZE = true;
			
			return;
		}
	}
	
	public int getShader(){
		return shader;
	}
	
	public int getTextureID(){
		return textureID;
	}
	
	public void render(){
		enable();
		glDrawArrays(GL_POINTS, 0, size);
		disable();
	}
	
	public void bindTexture(){
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glBindTexture(GL_TEXTURE_2D, textureID);
	}
	
	private void enable(){
		
		// Bind VAO
		glBindVertexArray(vao);
		
		// Enable arrays
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		
		if(shader == 0){
			glEnableVertexAttribArray(2);
			glEnableVertexAttribArray(3);
			glEnableVertexAttribArray(4);
		}
		
		// Set blend mode
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, additive ? GL_ONE : GL_ONE_MINUS_SRC_ALPHA);
	}
	
	private void disable(){
		// Disable blending
		glDisable(GL_BLEND);
		
		// Disable arrays
		if(shader == 0){
			glDisableVertexAttribArray(4);
			glDisableVertexAttribArray(3);
			glDisableVertexAttribArray(2);
		}
		
		glDisableVertexAttribArray(1);
		glDisableVertexAttribArray(0);
		
		// Unbind VAO
		glBindVertexArray(0);
	}
	
	public void cleanup(){
		glDeleteVertexArrays(vao);
		glDeleteBuffers(vbo);
		glDeleteBuffers(sze);
		glDeleteBuffers(tex);
		glDeleteBuffers(tfm);
		glDeleteBuffers(alp);
	}
	
	// Update batch
	public void updateManual(float x, float y, float[] texCoords){
		size = 1;
		updateVBOs(new float[]{x, y}, null, texCoords, new float[]{1, 1, 0}, new float[]{1});
	}
	
	public void updateWithEntity(GameEntity e, int time){
		ArrayList<GameEntity> l = new ArrayList<GameEntity>(1);
		l.add(e);
		updateWithEntities(l, time);
	}
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	public void updateWithEntities(ArrayList entityList, int time){
		
		ArrayList<GameEntity> el = new ArrayList<GameEntity>();
		
		// Take only visible entities
		for(GameEntity e:(ArrayList<GameEntity>)entityList){
			if(e.isVisible() && !e.isDeleted())
				el.add(e);
		}
		
		size = el.size();
		
		if(el.isEmpty())
			return;
		
		// VAO arguments
		float[] vertices	= null;
		float[] texCoords	= null;
		float[] transforms	= null;
		float[] alphas		= null;

		if(uVBO) vertices	= new float[size*2];
		if(uTEX) texCoords	= new float[size*4];
		if(uTFM) transforms	= new float[size*3];
		if(uALP) alphas		= new float[size];
		
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
			
			// Fill arrays
			if(uVBO){
				vertices[i*2]		= e.getX();
				vertices[i*2 + 1]	= e.getY();
			}
			
			if(uTEX){
				for(int j = 0; j < 4; j++)
					texCoords[i*4 + j] = t[j];
			}
			
			if(uTFM){
				transforms[i*3]		= s.getScaleX();
				transforms[i*3 + 1]	= s.getScaleY();
				transforms[i*3 + 2]	= (float)Math.toRadians(r);
			}
			
			if(uALP)
				alphas[i] = s.getAlpha();
		}
		
		updateVBOs(vertices, null, texCoords, transforms, alphas);
	}
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	public void updateHitboxes(ArrayList entityList){
		
		ArrayList<GameEntity> el = new ArrayList<GameEntity>();
		
		// Take only visible entities
		for(GameEntity e:(ArrayList<GameEntity>)entityList){
			if(e.isVisible() && !e.isDeleted() && (e instanceof Bullet || e instanceof Player || e instanceof Enemy))
				el.add(e);
		}
		
		size = el.size();
		
		if(el.isEmpty())
			return;
		
		// VAO arguments
		float[] vertices	 = null;
		short[] sizes = null;
		
		if(uVBO) vertices	= new float[size*2];
		if(uSZE) sizes		= new short[size];
		
		for(int i = 0; i < el.size(); i++){
			
			GameEntity e = el.get(i);
			
			// Fill arrays
			if(uVBO){
				vertices[i*2]		= e.getX();
				vertices[i*2 + 1]	= e.getY();
			}
			
			if(uSZE){
				if(e instanceof Bullet)	sizes[i] = (short)((Bullet)e).getHitboxSize();
				if(e instanceof Player)	sizes[i] = (short)((Player)e).getHitboxSize();
				if(e instanceof Enemy)	sizes[i] = (short)((Enemy)e).getHitboxSize();
			}
		}
		
		updateVBOs(vertices, sizes, null, null, null);
	}
	
	private void updateVBOs(float[] vertices, short[] sizes, float[] texCoords, float[] transforms, float[] alphas){
		
		glBindVertexArray(vao);
		
		// Vertices
		if(uVBO){
			vboBuffer.clear();
			vboBuffer.put(vertices);
			vboBuffer.flip();
			
			glBindBuffer(GL_ARRAY_BUFFER, vbo);
			glBufferData(GL_ARRAY_BUFFER, vboBuffer, GL_DYNAMIC_DRAW);
			glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0);
			
			if((updates & UPDATE_VBO) == 0)
				uVBO = false;
		}
		
		// Sizes
		if(uSZE){
			
			if(sizes == null){
				sizes = new short[capacity*2];
				
				for(int i = 0; i < sizes.length/2; i++){
					sizes[i*2]		= sizePixelsX;
					sizes[i*2 + 1]	= sizePixelsY;
				}
			}
			
			szeBuffer.clear();
			szeBuffer.put(sizes);
			szeBuffer.flip();
			
			glBindBuffer(GL_ARRAY_BUFFER, sze);
			glBufferData(GL_ARRAY_BUFFER, szeBuffer, GL_STATIC_DRAW);
			
			if(shader == 0)
				glVertexAttribPointer(1, 2, GL_SHORT, false, 0, 0);
			else
				glVertexAttribIPointer(1, 1, GL_SHORT, 0, 0);
			
			if((updates & UPDATE_SZE) == 0)
				uSZE = false;
		}
		
		// Texture coords
		if(uTEX){
			texBuffer.clear();
			texBuffer.put(texCoords);
			texBuffer.flip();
			
			glBindBuffer(GL_ARRAY_BUFFER, tex);
			glBufferData(GL_ARRAY_BUFFER, texBuffer, GL_DYNAMIC_DRAW);
			glVertexAttribPointer(2, 4, GL_FLOAT, false, 0, 0);
			
			if((updates & UPDATE_TEX) == 0)
				uTEX = false;
		}
		
		if(uTFM){
			tfmBuffer.clear();
			tfmBuffer.put(transforms);
			tfmBuffer.flip();
			
			glBindBuffer(GL_ARRAY_BUFFER, tfm);
			glBufferData(GL_ARRAY_BUFFER, tfmBuffer, GL_DYNAMIC_DRAW);
			glVertexAttribPointer(3, 3, GL_FLOAT, false, 0, 0);
			
			if((updates & UPDATE_TFM) == 0)
				uTFM = false;
		}
		
		if(uALP){
			alpBuffer.clear();
			alpBuffer.put(alphas);
			alpBuffer.flip();
			
			glBindBuffer(GL_ARRAY_BUFFER, alp);
			glBufferData(GL_ARRAY_BUFFER, alpBuffer, GL_DYNAMIC_DRAW);
			glVertexAttribPointer(4, 1, GL_FLOAT, false, 0, 0);
			
			if((updates & UPDATE_ALP) == 0)
				uALP = false;
		}
		
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		
		glBindVertexArray(0);
	}
}
