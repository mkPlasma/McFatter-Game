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
import engine.entities.CollidableEntity;
import engine.entities.Enemy;
import engine.entities.GameEntity;
import engine.entities.Laser;
import engine.entities.MovableEntity;
import engine.entities.Player;

/**
 * 
 * Container for OpenGL vertex data for a set of entities.
 * 
 * @author Daniel
 *
 */

public class RenderBatch{
	
	public static final int
		UPDATE_VBO = 0b000001,
		UPDATE_SZE = 0b000010,
		UPDATE_TEX = 0b000100,
		UPDATE_RTS = 0b001000,
		UPDATE_ALP = 0b010000,
		UPDATE_SEG = 0b100000,
		
		UPDATE_NONE			= 0,
		UPDATE_ALL			= 0b011111,
		UPDATE_LASER		= 0b111101,
		UPDATE_HITBOX		= 0b001011,
		UPDATE_LASER_HITBOX	= 0b001011;
	
	public static final int
		SHADER_STANDARD	= 0,
		SHADER_LASER	= 1,
		SHADER_HITBOX	= 2,
		SHADER_L_HITBOX	= 3;
	
	private final int shader;
	
	// Number of quads to render
	private int size;
	
	private final int capacity;
	
	// Size in pixels of quad
	private final short sizePixelsX, sizePixelsY;
	
	private FloatBuffer vboBuffer, texBuffer, rtsBuffer, alpBuffer;
	private ShortBuffer szeBuffer, segBuffer;
	
	private final int vao, vbo, sze, tex, rts, alp, seg;
	
	private final int textureID;
	
	// Which buffers to update
	private int updates;
	private boolean uVBO, uSZE, uTEX, uRTS, uALP, uSEG;
	
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
		rts = glGenBuffers();
		alp = glGenBuffers();
		
		if(shader == SHADER_LASER)
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
		
		shader = SHADER_STANDARD;
		
		vao = glGenVertexArrays();
		vbo = glGenBuffers();
		sze = glGenBuffers();
		tex = glGenBuffers();
		rts = glGenBuffers();
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
		rts = glGenBuffers();
		alp = 0;
		seg = 0;
		
		init();
	}
	
	private void init(){
		vboBuffer = BufferUtils.createFloatBuffer(capacity*2);
		
		if(shader == SHADER_STANDARD || shader == SHADER_LASER || shader == SHADER_L_HITBOX){
			szeBuffer = BufferUtils.createShortBuffer(capacity*2);
			texBuffer = BufferUtils.createFloatBuffer(capacity*4);
			rtsBuffer = BufferUtils.createFloatBuffer(capacity);
			alpBuffer = BufferUtils.createFloatBuffer(capacity);
			
			if(shader == SHADER_LASER)
				segBuffer = BufferUtils.createShortBuffer(capacity);
			
			uVBO = true;
			uSZE = true;
			uTEX = true;
			uRTS = true;
			uALP = true;
			uSEG = shader == SHADER_LASER;
			
			return;
		}
		
		if(shader == SHADER_HITBOX){
			szeBuffer = BufferUtils.createShortBuffer(capacity);
			rtsBuffer = BufferUtils.createFloatBuffer(capacity);
			
			uVBO = true;
			uSZE = true;
			uRTS = true;
			
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
		
		if(shader == SHADER_STANDARD || shader == SHADER_LASER)
			glEnableVertexAttribArray(2);
		if(shader == SHADER_STANDARD || shader == SHADER_LASER || shader == SHADER_HITBOX || shader == SHADER_L_HITBOX)
			glEnableVertexAttribArray(3);
		if(shader == SHADER_STANDARD || shader == SHADER_LASER)
			glEnableVertexAttribArray(4);
		if(shader == SHADER_LASER)
			glEnableVertexAttribArray(5);
		
		// Set blend mode
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, additive ? GL_ONE : GL_ONE_MINUS_SRC_ALPHA);
	}
	
	private void disable(){
		// Disable blending
		glDisable(GL_BLEND);
		
		// Disable arrays
		if(shader == SHADER_LASER)
			glDisableVertexAttribArray(5);
		if(shader == SHADER_STANDARD || shader == SHADER_LASER)
			glDisableVertexAttribArray(4);
		if(shader == SHADER_STANDARD || shader == SHADER_LASER || shader == SHADER_HITBOX || shader == SHADER_L_HITBOX)
			glDisableVertexAttribArray(3);
		if(shader == SHADER_STANDARD || shader == SHADER_LASER)
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
		glDeleteBuffers(rts);
		glDeleteBuffers(alp);
	}
	
	// Update batch
	public void updateManual(float x, float y, float[] texCoords){
		size = 1;
		updateVBOs(new float[]{x, y}, null, texCoords, new float[]{0}, new float[]{1}, null);
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
				
				if(shader == SHADER_L_HITBOX && !((Laser)e).collisionsEnabled())
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
			rotations	= null,
			alphas		= null;
		
		short[]
			sizes = null,
			segments = null;
		
		if(uVBO) vertices	= new float[size*2];
		if(uSZE) sizes		= new short[size*2];
		if(uTEX) texCoords	= new float[size*4];
		if(uRTS) rotations	= new float[size];
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
			if((e instanceof MovableEntity && e.getFrame().spriteAlign()) || e instanceof Laser)
				r += ((MovableEntity)e).getDir() + 90;
			
			// Fill arrays
			if(uVBO){
				vertices[i*2]		= e.getX();
				vertices[i*2 + 1]	= e.getY();
			}
			
			if(uSZE){
				sizes[i*2]		= (short)s.getScaledWidth();
				sizes[i*2 + 1]	= (short)s.getScaledHeight();
			}
			
			if(uTEX){
				for(int j = 0; j < 4; j++)
					texCoords[i*4 + j] = t[j];
			}
			
			if(uRTS)
				rotations[i] = (float)Math.toRadians(r);
			
			if(uALP)
				alphas[i] = s.getAlpha();
			
			// Laser corrections
			if(e instanceof Laser){
				
				Laser l = (Laser)e;
				
				float len = l.getLength()/4f;
				
				// Correct origin
				float ang = (float)Math.toRadians(l.getDir());
				vertices[i*2]		+= len*Math.cos(ang);
				vertices[i*2 + 1]	+= len*Math.sin(ang);

				// Set scale
				if(shader == SHADER_STANDARD || shader == SHADER_LASER){
					sizes[i*2]		*= l.getScaleX();
					sizes[i*2 + 1]	*= l.getScaleY();
					
					if(uSEG)
						segments[i] = (short)Math.max((int)(l.getLength()/(l.getActualWidth()/2f)), 1);
				}
				
				// For hitboxes
				if(shader == SHADER_L_HITBOX){
					//sizes[i*2]		= (short)(8*l.getHitboxSize());
					//sizes[i*2 + 1]	= (short)(2*(l.getLength() - crop*3));
				}
			}
		}
		
		updateVBOs(vertices, sizes, texCoords, rotations, alphas, segments);
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
		float[]
			vertices	 = null,
			rotations = null;
		
		short[] sizes = null;
		
		if(uVBO) vertices	= new float[size*2];
		if(uSZE) sizes		= new short[size*2];
		if(uRTS) rotations	= new float[size];
		
		for(int i = 0; i < el.size(); i++){
			
			GameEntity e = el.get(i);
			
			// Fill arrays
			if(uVBO){
				vertices[i*2]		= e.getX();
				vertices[i*2 + 1]	= e.getY();
				
				if(e instanceof CollidableEntity){
					int offset = ((CollidableEntity)e).getHitboxOffset();
					
					if(offset != 0){
						float dir = (float)Math.toRadians(((CollidableEntity)e).getDir());
						vertices[i*2]		+= (float)(offset*Math.cos(dir));
						vertices[i*2 + 1]	+= (float)(offset*Math.sin(dir));
					}
				}
			}
			
			if(uSZE){
				if(e instanceof CollidableEntity){
					sizes[i*2]		= (short)((CollidableEntity)e).getHitboxWidth();
					sizes[i*2 + 1]	= (short)((CollidableEntity)e).getHitboxLength();
				}
				
				else if(e instanceof Player)
					sizes[i*2] = sizes[i*2 + 1] = (short)((Player)e).getHitboxSize();
			}
			
			if(uRTS){
				if(e instanceof Player)
					rotations[i] = 0;
				else
					rotations[i] = (float)Math.toRadians(((CollidableEntity)e).getDir() + 90);
			}
		}
		
		updateVBOs(vertices, sizes, null, rotations, null, null);
	}
	
	private void updateVBOs(float[] vertices, short[] sizes, float[] texCoords, float[] rotations, float[] alphas, short[] segments){
		
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
			glVertexAttribPointer(1, 2, GL_SHORT, false, 0, 0);
			
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
		
		if(uRTS){
			rtsBuffer.clear();
			rtsBuffer.put(rotations);
			rtsBuffer.flip();
			
			glBindBuffer(GL_ARRAY_BUFFER, rts);
			glBufferData(GL_ARRAY_BUFFER, rtsBuffer, GL_DYNAMIC_DRAW);
			glVertexAttribPointer(3, 1, GL_FLOAT, false, 0, 0);
			
			if((updates & UPDATE_RTS) == 0)
				uRTS = false;
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
