package engine.entities;

import java.util.ArrayList;

/*
 * 		EffectGenerator.java
 * 		
 * 		Purpose:	Generates a certain effect, like an explosion or particle trail.
 * 		Notes:		Currently unfinished. Will be able to create
 * 					one or more effect objects simultaneously.
 * 		
 * 		Last modified by:	Daniel
 * 		Date:				
 * 		Changes:			
 */

public class EffectGenerator{
	
	private static ArrayList<Effect> effects = new ArrayList<Effect>();
	
	public static void addEffect(Effect e){
		effects.add(e);
	}
	
	public static ArrayList<Effect> getEffects(){
		if(effects == null || effects.size() < 1)
			return null;
		
		ArrayList<Effect> temp = new ArrayList<Effect>(effects);
		effects.clear();
		
		return temp;
	}
	
	public static void clearEffects(){
		effects.clear();
	}
}
