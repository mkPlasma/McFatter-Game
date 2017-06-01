package engine.graphics;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ShaderProgram{
	
	int program, vShader, fShader;
	
	public ShaderProgram(String vertShader, String fragShader){
		
		vShader = compileShader(vertShader + ".vs", GL_VERTEX_SHADER);
		fShader = compileShader(fragShader + ".fs", GL_FRAGMENT_SHADER);
		
		program = glCreateProgram();

		glAttachShader(program, vShader);
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
		glDetachShader(program, fShader);
		glDeleteShader(vShader);
		glDeleteShader(fShader);
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
	
	
	private int compileShader(String path, int type){
		
		int shader = glCreateShader(type);
		
		String code = "";
		
		try{
			code = new String(Files.readAllBytes(Paths.get("Game/res/shaders/" + path)));
		}
		catch (IOException e){
			e.printStackTrace();
		}
		
		glShaderSource(shader, code);
		glCompileShader(shader);
		
		String infoLog = glGetShaderInfoLog(shader, glGetShaderi(shader, GL_INFO_LOG_LENGTH));
		
		if(glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE){
			String name = type == GL_VERTEX_SHADER ? "vertex" : "fragment";
			System.err.println("Failed to compile " + name + " shader:\n" + infoLog);
			System.exit(1);
		}
		
		return shader;
	}
}