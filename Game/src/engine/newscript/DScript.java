package engine.newscript;

import java.util.ArrayList;

import engine.newscript.bytecodegen.Instruction;
import engine.newscript.lexer.Token;

public class DScript{
	
	private final String path;
	
	private String[][] rFiles;
	private String[] filePaths;
	
	private String[] file;
	private Token[] tokens;
	private ArrayList<Object> parseTree;
	
	private Instruction[] bytecode;
	private int numVariables;
	
	
	public DScript(String path){
		this.path = "Game/res/script/" + path;
	}
	
	public String getPath(){
		return path;
	}
	
	public String getFolder(){
		return path.substring(0, path.lastIndexOf('/'));
	}
	
	public String getFileName(){
		return path.substring(path.lastIndexOf('/') + 1);
	}
	
	
	public void setRawFiles(String[][] rFiles){
		this.rFiles = rFiles;
	}

	public String getLine(String file, int line){
		return rFiles[getFileIndex(file)][line - 1];
	}
	
	public String getLine(int fileIndex, int line){
		return rFiles[fileIndex][line - 1];
	}
	
	public void setFilePaths(String[] filePaths){
		this.filePaths = filePaths;
	}
	
	public int getFileIndex(String file){
		
		if(file == null)
			return 0;
		
		for(int i = 0; i < filePaths.length; i++)
			if(file.equals(filePaths[i]))
				return i;
		
		return -1;
	}
	
	public String getFileName(int fileIndex){
		return filePaths[fileIndex].substring(16);
	}
	
	
	public void setFile(String[] file){
		this.file = file;
	}
	
	public void clearFile(){
		file = null;
	}
	
	public String[] getFile(){
		return file;
	}
	
	public void setTokens(Token[] tokens){
		this.tokens = tokens;
	}
	
	public void clearTokens(){
		tokens = null;
	}
	
	public Token[] getTokens(){
		return tokens;
	}
	
	public void setParseTree(ArrayList<Object> parseTree){
		this.parseTree = parseTree;
	}
	
	public void clearParseTree(){
		parseTree = null;
	}
	
	public ArrayList<Object> getParseTree(){
		return parseTree;
	}
	
	public void setBytecode(Instruction[] bytecode){
		this.bytecode = bytecode;
	}
	
	public Instruction[] getBytecode(){
		return bytecode;
	}
	
	public void setNumVariables(int numVariables){
		this.numVariables = numVariables;
	}
	
	public int getNumVariables(){
		return numVariables;
	}
}
