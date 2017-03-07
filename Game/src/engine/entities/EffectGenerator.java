package engine.entities;

import java.util.ArrayList;

public class EffectGenerator{
	
	// Creates effects which will be added to a screen
	// Includes presets for effects as well
	
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
