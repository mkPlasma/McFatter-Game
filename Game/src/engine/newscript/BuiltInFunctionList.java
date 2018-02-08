package engine.newscript;

import java.util.Random;

import engine.newscript.bytecodegen.Instruction;

public class BuiltInFunctionList{
	
	private final BIFunc[] funcList;
	
	private final Random random;
	
	public BuiltInFunctionList(){
		
		random = new Random();
		
		funcList = new BIFunc[]{
			
				new BIFunc("rand", 2){
					protected Object run(Instruction inst, Object[] params){
						
						int a = params[0];
						
						return null;
					}
				},
				
		};
	}
}
