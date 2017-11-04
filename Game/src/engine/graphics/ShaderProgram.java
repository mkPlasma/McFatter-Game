package engine.graphics;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL32.*;

import java.io.IOException;

import engine.IOFunctions;

/*
 * 		ShaderProgram.java
 * 		
 * 		Purpose:	GLSL shader program.
 * 		Notes:		Should not need to be changed.
 * 		
 * 		Last modified by:	Daniel
 * 		Date:				
 * 		Changes:			
 */

public class ShaderProgram{
	
	private final int program, vShader, fShader, gShader;
	
	public ShaderProgram(String vertShader, String geomShader, String fragShader){
		
		vShader = compileShader(vertShader + ".vs", GL_VERTEX_SHADER);
		gShader = compileShader(geomShader + ".gs", GL_GEOMETRY_SHADER);
		fShader = compileShader(fragShader + ".fs", GL_FRAGMENT_SHADER);
		
		program = glCreateProgram();
		
		glAttachShader(program, vShader);
		glAttachShader(program, gShader);
		glAttachShader(program, fShader);
		
		glBindFragDataLocation(program, 0, "fragColor");
	}
	
	public void bindAttrib(int index, String name){
		glBindAttribLocation(program, index, name);
	}
	
	public void link(){
		
		glLinkProgram(program);
		
		String infoLog = glGetProgramInfoLog(program, glGetProgrami(program, GL_INFO_LOG_LENGTH));
		
		if(glGetProgrami(program, GL_LINK_STATUS) == GL_FALSE){
			System.err.println("Failed to link shader program:\n" + infoLog);
			System.exit(1);
		}

		glDetachShader(program, vShader);
		glDetachShader(program, gShader);
		glDetachShader(program, fShader);
		
		glDeleteShader(vShader);
		glDeleteShader(gShader);
		glDeleteShader(fShader);
	}
	
	private int compileShader(String path, int type){
		
		int shader = glCreateShader(type);
		
		String code = "";
		
		try{
			code = IOFunctions.readToString("Game/res/shaders/" + path);
		}
		catch (IOException e){
			e.printStackTrace();
		}
		
		glShaderSource(shader, code);
		glCompileShader(shader);
		
		String infoLog = glGetShaderInfoLog(shader, glGetShaderi(shader, GL_INFO_LOG_LENGTH));
		
		if(glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE){
			System.err.println(infoLog);
			System.exit(1);
		}
		
		return shader;
	}
	
	public int getUniformLocation(String uni){
		return glGetUniformLocation(program, uni);
	}
	
	public void use(){
		glUseProgram(program);
	}
	
	public void destroy(){
		glDeleteProgram(program);
	}
	
	public int getProgram(){
		return program;
	}
}