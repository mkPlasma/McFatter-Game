package engine.newscript;

import engine.newscript.bytecodegen.Instruction;

public abstract class BIFunc{
	
	private final String name;
	private final int paramCount;
	
	public BIFunc(String name, int paramCount){
		this.name = name;
		this.paramCount = paramCount;
	}
	
	public Object call(Instruction inst, Object[] params) throws ScriptException{
		try{
			return run(inst, params);
		}
		catch(ClassCastException e){
			throw new ScriptException("Type mismatch on " + name + "(), expected " + getClassName(e.getMessage()), inst.getFileIndex(), inst.getLineNum());
		}
	}
	
	protected abstract Object run(Instruction inst, Object[] params) throws ScriptException;
	
	private String getClassName(String message){
		// Get full class name
		message = message.substring(57, message.indexOf(' '));
		
		return message.substring(message.lastIndexOf('.'));
	}
	
	public String getName(){
		return name;
	}
	
	public int getParamCount(){
		return paramCount;
	}
}
