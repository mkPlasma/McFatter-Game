package engine.script;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class ScriptCompiler{
	
	private final char[] delimiters = {' ', '('};
	
	// Stores variables by name while compiling
	private ArrayList<String> varList;
	
	public void compile(DScript s){
		
		// Initialize/reset variable list
		varList = new ArrayList<String>();
		
		ArrayList<Long> bytecode = new ArrayList<Long>();
		
		// Load file and read line by line
		try(BufferedReader br = new BufferedReader(new FileReader("Game/res/script/test.dscript"))){
			for(String line; (line = br.readLine()) != null;){
				if(!line.trim().isEmpty())
		    			bytecode.add(processLine(line.trim()));
			}
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
	
	// Takes a line and returns opcode
	private long processLine(String line){
		
		System.out.println(line);
		
		// Current opcode/operands
		int op = 0;
		
		// Earliest delimiter
		int delimiterIndex = line.length();
		
		for(char c:delimiters){
			int i = line.indexOf(c);
			
			if(i != -1 && i < delimiterIndex)
				delimiterIndex = i;
		}
		
		String partFirst = line.substring(0, delimiterIndex);
		String partSecond = line.substring(delimiterIndex);
		
		String regex;
		
		// Check keywords
		switch(partFirst){
			case "set":
				regex = "\\s+.+(\\s*?=\\s*?.+)?";
				
				System.out.print(partSecond.matches(regex) + "\n");
				break;
		}
		
		System.out.println("");
		
    		return 0;
	}
}
