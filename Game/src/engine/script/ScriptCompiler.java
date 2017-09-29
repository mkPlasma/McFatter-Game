package engine.script;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class ScriptCompiler{
	
	private final char[] delimiters = {' ', ';', '('};
	
	// Stores variables by name while compiling
	private ArrayList<String> varList;
	
	public void compile(DScript s){
		
		// Initialize/reset variable list
		varList = new ArrayList<String>();
		
		ArrayList<Integer> bytecode = new ArrayList<Integer>();
		
		// Load file and read line by line
		try(BufferedReader br = new BufferedReader(new FileReader("Game/res/script/test.dscript"))){
		    for(String line; (line = br.readLine()) != null;){
		    	bytecode.add(processLine(line.trim()));
		    }
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
	
	// Takes a line and returns opcode
	private int processLine(String line){
		
		// Current opcode/operands
		int op = 0;
		
		// Earliest delimiter
		int delimiterIndex = line.length();
		
		for(char c:delimiters){
			int i = line.indexOf(c);
			
			if(i < delimiterIndex)
				i = delimiterIndex;
		}
		
		if(delimiterIndex == -1)
			System.err.println("No delimiters found!\n" + line );
		
		
		String part = line.substring(0, delimiterIndex);
		
		// Check keywords
		switch(part){
			case "set":
				
				
				break;
		}
		
    	return 0;
	}
	
	private void requireAfter(String s, int index, String r){
		
	}
}
