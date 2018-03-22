package engine.script;

/**
 * 
 * DScript exception object thrown during compilation or running.
 * Contains error message and location of error.
 * 
 * @author Daniel
 * 
 */

public class ScriptException extends Exception{
	
	private static final long serialVersionUID = 1L;
	
	private final String file;
	private final int fileIndex;
	private final int line;
	
	public ScriptException(String message, int line){
		this(message, null, line);
	}
	
	public ScriptException(String message, String file, int line){
		super(message);
		this.file = file;
		fileIndex = 0;
		this.line = line;
	}
	
	public ScriptException(String message, int fileIndex, int line){
		super(message);
		file = null;
		this.fileIndex = fileIndex;
		this.line = line;
	}
	

	public String getFile(){
		return file;
	}
	
	public int getFileIndex(){
		return fileIndex;
	}
	
	public int getLine(){
		return line;
	}
}
