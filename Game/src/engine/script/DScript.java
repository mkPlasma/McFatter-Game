package engine.script;

import java.util.ArrayList;

import engine.IOFunctions;

/*
 * 		DScript.java
 * 		
 * 		Purpose:	Holds DScript bytecode.
 * 		Notes:		
 * 		
 */

public class DScript{
	
	private String[] tokens;
	private long[] bytecode;
	private final String path;
	private ArrayList<String> paths;
	
	public DScript(String path){
		this.path = path;
		paths = new ArrayList<String>();
	}

	public void setTokens(String[] tokens){
		this.tokens = tokens;
	}
	
	// Clear tokens after parsing
	public void clearTokens(){
		tokens = null;
	}
	
	public void setBytecode(long[] bytecode){
		this.bytecode = bytecode;
	}
	
	public String[] getTokens(){
		return tokens;
	}
	
	public long[] getBytecode(){
		return bytecode;
	}
	
	public String getPath(){
		return path;
	}
	
	public String getFileName(){
		return path.substring(path.lastIndexOf('/') + 1);
	}
	
	public void addFile(String path){
		paths.add(path);
	}
	
	public String getLine(int lineNum){
		
		int n = IOFunctions.getLineCount(path);
		
		if(lineNum <= n)
			return IOFunctions.getLine(path, lineNum);
		
		lineNum -= n;
		
		for(String p:paths){
			n = IOFunctions.getLineCount(p);
			
			if(lineNum <= n)
				return IOFunctions.getLine(p, lineNum);
			
			lineNum -= n;
		}
		
		return "";
	}
}