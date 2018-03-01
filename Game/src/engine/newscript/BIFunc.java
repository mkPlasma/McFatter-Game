package engine.newscript;

import engine.newscript.bytecodegen.Instruction;

/**
 * 
 * DScript built-in function object.
 * 
 * @author Daniel
 * 
 */

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
			e.printStackTrace();
			throw new ScriptException("Type mismatch on parameter in " + name + "()", inst.getFileIndex(), inst.getLineNum());
		}
		catch(ArrayIndexOutOfBoundsException e){
			throw new ScriptException(name + "() requires an array with more elements", inst.getFileIndex(), inst.getLineNum());
		}
	}
	
	protected abstract Object run(Instruction inst, Object[] params) throws ScriptException;
	
	public String getName(){
		return name;
	}
	
	public int getParamCount(){
		return paramCount;
	}
}
