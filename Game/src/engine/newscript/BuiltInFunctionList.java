package engine.newscript;

import java.util.ArrayList;
import java.util.Random;

import engine.newscript.bytecodegen.Instruction;

public class BuiltInFunctionList{
	
	private final BIFunc[] funcList;
	
	// Type casters
	private final Caster cInt;
	private final Caster cFloat;
	private final Caster cArray;
	
	private final Random random;
	
	
	public BuiltInFunctionList(){
		
		cInt		= new Caster<Integer>();
		cFloat	= new Caster<Float>();
		cArray	= new Caster<ArrayList<Object>>();
		
		random = new Random();
		
		// Create functions
		funcList = new BIFunc[]{
			
			// Generate random int between two numbers
			new BIFunc("rand", 2){
				protected Object run(Instruction inst, Object[] params){
					
					int a = castInt(params[0]);
					int b = castInt(params[1]);
					
					int min = Math.min(a, b);
					int max = Math.max(a, b);
					
					if(max - min == 0)
						return 0;
					
					return random.nextInt(max - min) + min;
				}
			},
			
			// Generate random positive or negative int (inclusive)
			new BIFunc("rand", 1){
				protected Object run(Instruction inst, Object[] params){
					
					int a = castInt(params[0]);
					
					if(a == 0)
						return 0;
					
					return random.nextInt(a*2 + 2) - a;
				}
			},
			// Generate random float between two numbers
			new BIFunc("randf", 2){
				protected Object run(Instruction inst, Object[] params){
					
					float a = castFloat(params[0]);
					float b = castFloat(params[1]);
					
					float min = Math.min(a, b);
					float max = Math.max(a, b);
					
					if(max - min == 0)
						return 0;
					
					return random.nextFloat()*(max - min) + min;
				}
			},
			
			// Generate random positive or negative float (inclusive)
			new BIFunc("randf", 1){
				protected Object run(Instruction inst, Object[] params){
					
					float a = castFloat(params[0]);
					
					if(a == 0)
						return 0;
					
					return random.nextFloat()*(a*2) - a;
				}
			},
		};
	}
	
	// Cast to int from int or float
	private int castInt(Object o){
		return o instanceof Float ? (int)(float)o : (int)o;
	}
	
	// Cast to float from int or float
	private float castFloat(Object o){
		return o instanceof Integer ? (float)(int)o : (float)o;
	}
	
	
	public BIFunc get(int index){
		return funcList[index];
	}
	
	public boolean isBuiltInFunction(String func){
		
		for(BIFunc f:funcList)
			if(func.equals(f.getName() + ',' + f.getParamCount()))
				return true;
		
		return false;
	}
	
	public int getBuiltInFunctionIndex(String func){
		
		for(int i = 0; i < funcList.length; i++){
			BIFunc f = funcList[i];
			
			if(func.equals(f.getName() + ',' + f.getParamCount()))
				return i;
		}
		
		return -1;
	}
	
	private static class Caster<T>{
		@SuppressWarnings("unchecked")
		public T cast(Object o){
			return (T)o;
		}
	}
}
