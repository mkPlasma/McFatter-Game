package engine.script;

public class Opcodes{
	public static final String[] opcodes = {
		"load",		// 0	id		Load a value.
		"store"		// 1	id		Store
	};
	
	// Return index of bytecode
	public static int get(String opcode){
		for(int i = 0; i < opcodes.length; i++)
			if(opcodes[i].equals(opcode))
				return i;
		return -1;
	}
}
