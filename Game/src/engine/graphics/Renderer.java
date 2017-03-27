package engine.graphics;

import static org.lwjgl.opengl.GL11.*;

import engine.Settings;

public class Renderer{
	
	// Resolution scale
	private static float scale;
	
	public static void render(Sprite sprs, int time, float x, float y, float alpha, float rot, float scx, float scy){
		
		// Render
		Sprite spr = sprs.animate(time);
		
		float[][] tc = spr.getTextureCoords();
		float[][] vc = getVertexCoords(x, y, spr.getScaleX()*spr.getWidth(), spr.getScaleY()*spr.getHeight());
		
		int id = spr.getTextureID();
		
		glBindTexture(GL_TEXTURE_2D, 0);
		glBindTexture(GL_TEXTURE_2D, id);
		
		if(spr.getComp() != 3){
			glEnable(GL_BLEND);
			//glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
			glBlendFunc(GL_SRC_ALPHA, GL_ONE);
			glColor4f(1, 1, 1, spr.getAlpha());
		}
		
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		
		glPushMatrix();
		
		// Set scale
		glScalef(scale, scale, 1);
		
		// Set rotation
		glTranslatef(x, y, 0);
		glRotatef(spr.getRotation() + rot, 0, 0, 1);
		glTranslatef(-x, -y, 0);
		
		glEnable(GL_TEXTURE_2D);
		
		glBegin(GL_TRIANGLES);
		
		// Tri 1
		
		// Top left
		glTexCoord2f(tc[0][0], tc[0][1]);
		glVertex2f(vc[0][0], vc[0][1]);
		
		// Top right
		glTexCoord2f(tc[1][0], tc[1][1]);
		glVertex2f(vc[1][0], vc[1][1]);
		
		// Bottom left
		glTexCoord2f(tc[3][0], tc[3][1]);
		glVertex2f(vc[3][0], vc[3][1]);
		
		// Tri 2
		// Bottom left
		glTexCoord2f(tc[3][0], tc[3][1]);
		glVertex2f(vc[3][0], vc[3][1]);
		
		// Bottom right
		glTexCoord2f(tc[2][0], tc[2][1]);
		glVertex2f(vc[2][0], vc[2][1]);
		
		// Top right
		glTexCoord2f(tc[1][0], tc[1][1]);
		glVertex2f(vc[1][0], vc[1][1]);
		
		glEnd();
		
		glDisable(GL_TEXTURE_2D);
		glDisable(GL_BLEND);
		
		glPopMatrix();
	}
	
	public static void render(Sprite spr, int time, float x, float y){
		render(spr, time, x, y, 1, 0, 1, 1);
	}
	
	public static void drawRectangle(float x, float y, float w, float h){
		
		float[][] vc = getVertexCoords(x, y, w, h);
		
		glColor3f(1, 1, 1);

		glPushMatrix();
		
		// Set scale
		glScalef(scale, scale, 1);
		
		glBegin(GL_QUADS);
		glVertex2f(vc[0][0], vc[0][1]);
		glVertex2f(vc[1][0], vc[1][1]);
		glVertex2f(vc[2][0], vc[2][1]);
		glVertex2f(vc[3][0], vc[3][1]);
		glEnd();
		
		glPopMatrix();
	}
	
	// Returns normalized vertex coordinates
	// 0 - Top left
	// 1 - Top right
	// 2 - Bottom right
	// 3 - Bottom left
	private static float[][] getVertexCoords(float x, float y, float w, float h){
		float left =	(x - (w/2));
		float right =	(x + (w/2));
		float top =		(y - (h/2));
		float bottom =	(y + (h/2));
		
		return new float[][]{
			{left,  top},
			{right, top},
			{right, bottom},
			{left,  bottom}
		};
	}
	
	// Gets window scale from settings
	// Use on launch or when resolution is changed
	public static void updateScale(){
		scale = Settings.getWindowScale();
	}
}
