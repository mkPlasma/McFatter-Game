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
import engine.entities.Laser;
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
		UPDATE_VBO = 0b000001,
		UPDATE_SZE = 0b000010,
		UPDATE_TEX = 0b000100,
		UPDATE_TFM = 0b001000,
		UPDATE_ALP = 0b010000,
		UPDATE_SEG = 0b100000,
		
		UPDATE_NONE			= 0,
		UPDATE_ALL_BUT_SIZE	= 0b011101,
		UPDATE_LASER		= 0b111101,
		UPDATE_HITBOX		= 0b000011,
		UPDATE_LASER_HITBOX	= 0b001001,
		UPDATE_TEXT			= 0b001101;
	
	
	private final int shader;
	
	// Number of quads to render
	private int size;
	
	private final int capacity;
	
	// Size in pixels of quad
	private final short sizePixelsX, sizePixelsY;
	
	private FloatBuffer vboBuffer, texBuffer, tfmBuffer, alpBuffer;
	private ShortBuffer szeBuffer, segBuffer;
	
	private final int vao, vbo, sze, tex, tfm, alp, seg;
	
	private final int textureID;
	
	// Which buffers to update
	private int updates;
	private boolean uVBO, uSZE, uTEX, uTFM, uALP, uSEG;
	
	// Additive rendering
	private boolean additive;
	
	public RenderBatch(int shader, int capacity, int sizePixels, int textureID, int updates, boolean additive){
		this.shader = shader;
		this.capacity = capacity;
		this.textureID = textureID;
		this.updates = updates;
		this.additive = additive;
		
		sizePixelsX = (short)sizePixels;
		sizePixelsY = (short)sizePixels;
		
		vao = glGenVertexArrays();
		vbo = glGenBuffers();
		sze = glGenBuffers();
		tex = glGenBuffers();
		tfm = glGenBuffers();
		alp = glGenBuffers();
		
		if(shader == 1)
			seg = glGenBuffers();
		else
			seg = 0;
		
		init();
	}
	
	public RenderBatch(int capacity, int sizePixelsX, int sizePixelsY, int textureID, int updates){
		this.capacity = capacity;
		this.sizePixelsX = (short)sizePixelsX;
		this.sizePixelsY = (short)sizePixelsY;
		this.textureID = textureID;
		this.updates = updates;
		
		shader = 0;
		
		vao = glGenVertexArrays();
		vbo = glGenBuffers();
		sze = glGenBuffers();
		tex = glGenBuffers();
		tfm = glGenBuffers();
		alp = glGenBuffers();
		seg = 0;
		
		init();
	}
	
	// For hitboxes
	public RenderBatch(int shader, int capacity, int updates){
		this.shader = shader;
		this.capacity = capacity;
		this.updates = updates;
		
		textureID = -1;
		sizePixelsX = 32;
		sizePixelsY = 32;
		
		vao = glGenVertexArrays();
		vbo = glGenBuffers();
		sze = glGenBuffers();
		tex = 0;
		alp = 0;
		seg = 0;
		
		if(shader == 2)
			tfm = 0;
		else
			tfm = glGenBuffers();
		
		init();
	}
	
	private void init(){
		vboBuffer = BufferUtils.createFloatBuffer(capacity*2);
		
		if(shader == 0 || shader == 1 || shader == 3){
			szeBuffer = BufferUtils.createShortBuffer(capacity*2);
			texBuffer = BufferUtils.createFloatBuffer(capacity*4);
			tfmBuffer = BufferUtils.createFloatBuffer(capacity*3);
			alpBuffer = BufferUtils.createFloatBuffer(capacity);
			
			if(shader == 1)
				segBuffer = BufferUtils.createShortBuffer(capacity);
			
			uVBO = true;
			uSZE = true;
			uTEX = true;
			uTFM = true;
			uALP = true;
			uSEG = shader == 1;
			
			return;
		}
		
		if(shader == 2){
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
		
		if(shader == 0 || shader == 1)
			glEnableVertexAttribArray(2);
		if(shader == 0 || shader == 1 || shader == 3)
			glEnableVertexAttribArray(3);
		if(shader == 0 || shader == 1)
			glEnableVertexAttribArray(4);
		if(shader == 1)
			glEnableVertexAttribArray(5);
		
		// Set blend mode
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, additive ? GL_ONE : GL_ONE_MINUS_SRC_ALPHA);
	}
	
	private void disable(){
		// Disable blending
		glDisable(GL_BLEND);
		
		// Disable arrays
		if(shader == 1)
			glDisableVertexAttribArray(5);
		if(shader == 0 || shader == 1)
			glDisableVertexAttribArray(4);
		if(shader == 0 || shader == 1 || shader == 3)
			glDisableVertexAttribArray(3);
		if(shader == 0 || shader == 1)
			glDisableVertexAttribArray(2);
		
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
		updateVBOs(new float[]{x, y}, null, texCoords, new float[]{1, 1, 0}, new float[]{1}, null);
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
			if(e.isVisible() && !e.isDeleted()){
				
				if(shader == 3 && !((Laser)e).collisionsEnabled())
					continue;
				
				el.add(e);
			}
		}
		
		size = el.size();
		
		if(el.isEmpty())
			return;
		
		// VAO arguments
		float[]
			vertices	= null,
			texCoords	= null,
			transforms	= null,
			alphas		= null;
		
		short[] segments = null;
		
		if(uVBO) vertices	= new float[size*2];
		if(uTEX) texCoords	= new float[size*4];
		if(uTFM) transforms	= new float[size*3];
		if(uALP) alphas		= new float[size];
		if(uSEG) segments	= new short[size];
		
		for(int i = 0; i < el.size(); i++){
			
			GameEntity e = el.get(i);
			
			// Get animated sprite
			Sprite s = e.getSprite().animate(e.getTime(), time, e);
			
			// Tex coords
			float[] t = s.getTextureCoords();
			
			// Rotation
			float r = s.getRotation();
			
			// Align to direction
			if(e instanceof MovableEntity && e.getFrame().spriteAlign())
				r += ((MovableEntity)e).getDir() + 90;
			
			if(e instanceof Laser)
				r = ((MovableEntity)e).getDir() + 90;
			
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
			
			// Laser corrections
			if(e instanceof Laser){
				
				Laser l = (Laser)e;
				
				float len = l.getLength()/2f;
				
				// Correct origin
				vertices[i*2]		+= len*Math.cos(Math.toRadians(l.getDir()));
				vertices[i*2 + 1]	+= len*Math.sin(Math.toRadians(l.getDir()));

				// Set scale
				if(shader == 0 || shader == 1){
					transforms[i*3]		*= l.getScaleX();
					transforms[i*3 + 1]	*= l.getScaleY();
					
					if(uSEG)
						segments[i] = (short)Math.max((int)(l.getLength()/(l.getActualWidth()/2f)), 1);
				}
				
				// For hitboxes
				if(shader == 3){
					int crop = l.getHBLengthCrop();
					transforms[i*3]		= l.getHitboxSize()/8f;
					transforms[i*3 + 1]	= (l.getLength() - crop*2)/16f;
				}
			}
		}
		
		updateVBOs(vertices, null, texCoords, transforms, alphas, segments);
	}
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	public void updateHitboxes(ArrayList entityList){
		
		ArrayList<GameEntity> el = new ArrayList<GameEntity>();
		
		// Take only visible entities
		for(GameEntity e:(ArrayList<GameEntity>)entityList){
			if(e.isVisible() && !e.isDeleted()){
				
				if(e instanceof Bullet && !((Bullet)e).collisionsEnabled())
					continue;
				
				if(e instanceof Enemy && !((Enemy)e).collisionsEnabled())
					continue;
				
				el.add(e);
			}
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
		
		updateVBOs(vertices, sizes, null, null, null, null);
	}
	
	private void updateVBOs(float[] vertices, short[] sizes, float[] texCoords, float[] transforms, float[] alphas, short[] segments){
		
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
			
			if(shader == 0 || shader == 1 || shader == 3)
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
		
		if(uSEG){
			segBuffer.clear();
			segBuffer.put(segments);
			segBuffer.flip();
			
			glBindBuffer(GL_ARRAY_BUFFER, seg);
			glBufferData(GL_ARRAY_BUFFER, segBuffer, GL_DYNAMIC_DRAW);
			glVertexAttribIPointer(5, 1, GL_SHORT, 0, 0);
			
			if((updates & UPDATE_SZE) == 0)
				uSZE = false;
		}
		
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		
		glBindVertexArray(0);
	}
}
