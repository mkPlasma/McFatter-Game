package engine.script;

import java.io.IOException;

import engine.IOFunctions;

public class DScript{
	
	private final String path;
	private String script;
	
	public DScript(String path){
		this.path = path;
	}
	
	public boolean loadScript(){
		
		try{
			script = IOFunctions.readToString(path);
		}
		catch(IOException e){
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public String getScript(){
		return script;
	}
	
	public String getPath(){
		return path;
	}
}