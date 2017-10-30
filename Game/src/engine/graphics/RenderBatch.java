package engine.graphics;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_ONE;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_POINTS;
import static org.lwjgl.opengl.GL11.GL_SHORT;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;

import engine.entities.GameEntity;
import engine.entities.MovableEntity;

/*
 * 		RenderBatch.java
 * 		
 * 		Purpose:	Stores VAOs/VBOs for a set of entities
 * 		Notes:		
 * 		
 */

public class RenderBatch{
	
	public static final int
		UPDATE_VBO = 0b0001,
		UPDATE_TEX = 0b0010,
		UPDATE_TFM = 0b0100,
		UPDATE_ALP = 0b1000,
		
		UPDATE_NONE = 0,
		UPDATE_ALL = 0b1111;
	
	
	// Number of quads to render
	private int size;
	
	private final int capacity;
	
	// Size in pixels of quad
	private final short sizePixelsX, sizePixelsY;
	
	private FloatBuffer vboBuffer, texBuffer, tfmBuffer, alpBuffer;
	
	private final int vao, vbo, sze, tex, tfm, alp;
	
	private final int textureID;
	
	// Which buffers to update
	private int updates;
	private boolean uVBO, uSZE, uTEX, uTFM, uALP;
	
	// Additive rendering
	private boolean additive;
	
	public RenderBatch(int capacity, int sizePixels, int textureID, int updates, boolean additive){
		this.capacity = capacity;
		sizePixelsX = (short)sizePixels;
		sizePixelsY = (short)sizePixels;
		this.textureID = textureID;
		this.updates = updates;
		this.additive = additive;
		
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
		
		vao = glGenVertexArrays();
		vbo = glGenBuffers();
		sze = glGenBuffers();
		tex = glGenBuffers();
		tfm = glGenBuffers();
		alp = glGenBuffers();
		
		init();
	}
	
	private void init(){
		vboBuffer = BufferUtils.createFloatBuffer(capacity*2);
		texBuffer = BufferUtils.createFloatBuffer(capacity*4);
		tfmBuffer = BufferUtils.createFloatBuffer(capacity*3);
		alpBuffer = BufferUtils.createFloatBuffer(capacity);
		
		uVBO = true;
		uSZE = true;
		uTEX = true;
		uTFM = true;
		uALP = true;
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
		glEnableVertexAttribArray(2);
		glEnableVertexAttribArray(3);
		glEnableVertexAttribArray(4);
		
		// Set blend mode
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, additive ? GL_ONE : GL_ONE_MINUS_SRC_ALPHA);
	}
	
	private void disable(){
		// Disable blending
		glDisable(GL_BLEND);
		
		// Disable arrays
		glDisableVertexAttribArray(4);
		glDisableVertexAttribArray(3);
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
	public void updateManual(float x, float y, float w, float h, float[] texCoords){
		
		/*
		size = 1;
		
		float[] vertices = new float[size*2];
		float[] tc = new float[size*2];
		
		// Vertex coords
		float[] v = getVertexCoords(x, y, w, h, 0);
		
		// Fill vertices/texCoords
		for(int i = 0; i < 6; i++){
			int a = i >= 3 ? i - 1 : i;
			a = i == 5 ? 0 : a;
			
			vertices[i*2]			= v[a*2];
			vertices[i*2 + 1]		= v[a*2 + 1];
			tc[i*2]		= texCoords[a*2];
			tc[i*2 + 1]	= texCoords[a*2 + 1];
		}
		
		updateVBOs(vertices, tc);
		*/
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
			
			// Vertex coords
			//float[] v = getVertexCoords(e.getX(), e.getY(), s.getScaledWidth(), s.getScaledHeight(), r);
			
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
				transforms[i*3 + 2]	= r;
			}
			
			if(uALP)
				alphas[i] = 0.25f;
		}
		
		updateVBOs(vertices, texCoords, transforms, alphas);
	}
	
	private void updateVBOs(float[] vertices, float[] texCoords, float[] transforms, float[] alphas){
		
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
		
		// Sizes (always update only once)
		if(uSZE){
			
			short[] sizes = new short[capacity*2];
			
			for(int i = 0; i < sizes.length/2; i++){
				sizes[i*2]		= sizePixelsX;
				sizes[i*2 + 1]	= sizePixelsY;
			}
			
			ShortBuffer szeBuffer = BufferUtils.createShortBuffer(capacity*2);
			szeBuffer.put(sizes);
			szeBuffer.flip();
			
			glBindBuffer(GL_ARRAY_BUFFER, sze);
			glBufferData(GL_ARRAY_BUFFER, szeBuffer, GL_STATIC_DRAW);
			glVertexAttribPointer(1, 2, GL_SHORT, false, 0, 0);
			
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
	
	// Returns vertex coordinates of object
	/*
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
	}*/
}
