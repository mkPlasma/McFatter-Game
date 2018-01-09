package engine.newscript;

public class ScriptException extends Exception{
	
	private static final long serialVersionUID = 1L;
	
	private final String file;
	private final int line;
	
	public ScriptException(String message, int line){
		this(message, null, line);
	}
	
	public ScriptException(String message, String file, int line){
		super(message);
		this.file = file;
		this.line = line;
	}
	
	
	public String getFile(){
		return file;
	}
	
	public int getLine(){
		return line;
	}
}
