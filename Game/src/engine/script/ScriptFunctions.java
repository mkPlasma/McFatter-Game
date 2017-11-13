package engine.script;

import java.util.ArrayList;

import engine.entities.Bullet;
import engine.entities.Enemy;
import engine.entities.GameEntity;
import engine.entities.Laser;
import engine.entities.MovableEntity;

/*
 * 		ScriptFunctions.java
 * 		
 * 		Purpose:	DScript built-in function list.
 * 		Notes:		
 * 		
 */

public class ScriptFunctions{

	public static final String[] builtInFunctions = {
		
		// General
		"print:1",
		
		"scriptTime:0",
		
		"int:1",
		
		"centerPos:0",
		
		"playerX:0",
		"playerY:0",
		"playerPos:0",
		
		"angleToPlayer:1",
		"angleToPlayer:2",
		"angleToLocation:2",
		"angleToLocation:4",

		"rand:2",
		"randFloat:2",
		"randBool:0",
		
		// Math
		"pi:0",
		"abs:1",
		"round:1",
		"trunc:1",
		"floor:1",
		"ceil:1",
		"sqrt:1",
		"log:1",
		"log10:1",
		"degrees:1",
		"radians:1",
		
		"sin:1",
		"cos:1",
		"tan:1",
		"ain:1",
		"acos:1",
		"atan:1",
		
		"atan2:2",
		"min:2",
		"max:2",
		
		// Array
		"length:d0",
		"add:d1",
		"remove:d0",
		"remove:d1",
		
		// Entites
		"bullet:6",
		"bullet:7",
		"bullet:9",
		"bullet:10",
		
		"laser:7",
		"laser:8",
		
		"enemy:2",
		"enemy:3",
		
		
		"setX:d1",
		"setY:d1",
		"delete:d0",
		"deleteImmediate:d0",
		"setPos:d1",
		"setSpd:d1",
		"setDir:d1",
		
		"setAngVel:d1",
		"setAccel:d1",
		"setMinSpd:d1",
		"setMaxSpd:d1",
		
		"setWidth:d1",
		"setLength:d1",
		"setSegmented:d0",
		"setSegmented:d1",
		
		"setVisible:d1",
		"setCollisions:d1",
		"setHitboxSize:d1",
		"setBombResist:d0",
		"setBombResist:d1",
		"setBorderDespawn:d1",
		"setDespawnRange:d1",
		
		"setHealth:d1",
		"setInvulnerable:d0",
		"setInvulnerable:d1",
		
		"setType:d1",
		"setColor:d1",
		"setFrame:d1",
		"setFrame:d2",
		
		"setAdditive:d0",
		"setAdditive:d1",
		"setScale:d1",
		"setScale:d2",
		"setScaleX:d1",
		"setScaleY:d1",
		"setAlpha:d1",
		
		
		"getX:d0",
		"getY:d0",
		"getPos:d0",
		"getTime:d0",
		"isDeleted:d0",
		"getSpd:d0",
		"getDir:d0",
		
		"getAngVel:d0",
		"getAccel:d0",
		"getMinSpd:d0",
		"getMaxSpd:d0",
		
		"getLength:d0",
		"getWidth:d0",
		"isSegmented:d0",
		
		"isVisible:d0",
		"getCollisions:d0",
		"getHitboxSize:d0",
		"getBombResist:d0",
		"getBorderDespawn:d0",
		"getDespawnRange:d0",
		
		"getHealth:d0",
		"isInvulnerable:d0",
		
		"getType:d0",
		"getColor:d0",
		"getFrame:d0",
		"isAdditive:d0",
		"getScale:d0",
		"getScaleX:d0",
		"getScaleY:d0",
		"getAlpha:d0",
	};
	
	
	// Built in functions
	
	// Get index of name
	public static int getBuiltInFunctionIndex(String func){
		for(int i = 0; i < builtInFunctions.length; i++)
			if(builtInFunctions[i].replace(":d", ":").equals(func))
				return i;
		return -1;
	}
	
	// Get name
	public static String getBuiltInFunctionName(int index){
		String name = builtInFunctions[index];
		return name.substring(0, name.indexOf(':'));
	}
	
	public static int getBuiltInFunctionParameterCount(int index){
		String f = builtInFunctions[index].replaceAll(":d", ":");
		return Integer.parseInt(f.substring(f.indexOf(':') + 1));
	}
	
	// Check for type mismatch
	public static boolean builtInFunctionTypeMatch(int index, Object[] params){
		
		Object o1 = params.length > 0 ? params[0] : null;
		Object o2 = params.length > 1 ? params[1] : null;
		Object o3 = params.length > 2 ? params[2] : null;
		Object o4 = params.length > 3 ? params[3] : null;
		
		int p = getBuiltInFunctionParameterCount(index);
		
		switch(getBuiltInFunctionName(index)){
			case "int":
				return o1 instanceof Integer || o1 instanceof Float;
			
			case "angleToPlayer":
				if(p == 1)
					return o1 instanceof ArrayList;
				return (o1 instanceof Integer || o1 instanceof Float) && (o2 instanceof Integer || o2 instanceof Float);
			
			case "angleToLocation":
				
				if(p == 2)
					return o1 instanceof ArrayList && o2 instanceof ArrayList;
				
				return (o1 instanceof Integer || o1 instanceof Float) && (o2 instanceof Integer || o2 instanceof Float) &&
					   (o3 instanceof Integer || o3 instanceof Float) && (o4 instanceof Integer || o4 instanceof Float);
			
			case "abs": case "round": case "trunc": case "floor": case "ceil": case "sqrt": case "log": case "log10": case "degrees": case "radians":
			case "sin": case "cos": case "tan": case "asin": case "acos": case "atan":
				return o1 instanceof Integer || o1 instanceof Float;
			
			case "rand": case "randFloat": case "atan2": case "min": case "max":
				return (o1 instanceof Integer || o1 instanceof Float) && (o2 instanceof Integer || o2 instanceof Float);
			
			case "length":
				return o1 instanceof ArrayList || o1 instanceof String;
			
			case "add":
				return o1 instanceof ArrayList && !(o2 instanceof ArrayList);
			
			case "remove":
				return o1 instanceof ArrayList && (p == 0 || o2 instanceof Integer);
			
			case "bullet":{
				Object o5 = params[4];
				Object o6 = params[5];
				
				if(p == 6)
					return o1 instanceof Integer && o2 instanceof Integer && o3 instanceof ArrayList &&
						  (o4 instanceof Integer || o4 instanceof Float) &&
						  (o5 instanceof Integer || o5 instanceof Float) &&
						  (o6 instanceof Integer || o6 instanceof Float);
				
				Object o7 = params[6];
				
				if(p == 7)
					return o1 instanceof Integer && o2 instanceof Integer &&
						  (o3 instanceof Integer || o3 instanceof Float) &&
						  (o4 instanceof Integer || o4 instanceof Float) &&
						  (o5 instanceof Integer || o5 instanceof Float) &&
						  (o6 instanceof Integer || o6 instanceof Float) &&
						  (o7 instanceof Integer || o7 instanceof Float);

				Object o8 = params[7];
				Object o9 = params[8];
				
				if(p == 9)
					return o1 instanceof Integer && o2 instanceof Integer && o3 instanceof ArrayList &&
						  (o4 instanceof Integer || o4 instanceof Float) &&
						  (o5 instanceof Integer || o5 instanceof Float) &&
						  (o6 instanceof Integer || o6 instanceof Float) &&
						  (o7 instanceof Integer || o7 instanceof Float) &&
						  (o8 instanceof Integer || o8 instanceof Float) &&
						  (o9 instanceof Integer || o9 instanceof Float);
				
				Object o10 = params[9];
				
				return o1 instanceof Integer && o2 instanceof Integer &&
					  (o3 instanceof Integer || o3 instanceof Float) &&
					  (o4 instanceof Integer || o4 instanceof Float) &&
					  (o5 instanceof Integer || o5 instanceof Float) &&
					  (o6 instanceof Integer || o6 instanceof Float) &&
					  (o7 instanceof Integer || o7 instanceof Float) &&
					  (o8 instanceof Integer || o8 instanceof Float) &&
					  (o9 instanceof Integer || o9 instanceof Float) &&
					  (o10 instanceof Integer || o10 instanceof Float);
			}
			
			case "laser":{
				Object o5 = params[4];
				Object o6 = params[5];
				Object o7 = params[6];
				
				if(p == 7)
					return o1 instanceof Integer && o2 instanceof Integer && o3 instanceof ArrayList &&
						  (o4 instanceof Integer || o4 instanceof Float) &&
						  (o5 instanceof Integer || o5 instanceof Float) &&
						  (o6 instanceof Integer || o6 instanceof Float) &&
						  (o7 instanceof Integer || o7 instanceof Float);
				
				Object o8 = params[7];
				
				return o1 instanceof Integer && o2 instanceof Integer &&
					  (o3 instanceof Integer || o3 instanceof Float) &&
					  (o4 instanceof Integer || o4 instanceof Float) &&
					  (o5 instanceof Integer || o5 instanceof Float) &&
					  (o6 instanceof Integer || o6 instanceof Float) &&
					  (o7 instanceof Integer || o7 instanceof Float) &&
					  (o8 instanceof Integer || o8 instanceof Float);
			}
			
			case "enemy":{
				
				if(p == 2)
					return o1 instanceof ArrayList &&
						  (o2 instanceof Integer || o2 instanceof Float);
				
				return (o1 instanceof Integer || o1 instanceof Float) &&
					  (o2 instanceof Integer || o2 instanceof Float) &&
					  (o3 instanceof Integer || o3 instanceof Float);
			}
			
			case "setX": case "setY":
				return o1 instanceof GameEntity && (o2 instanceof Integer || o2 instanceof Float);
			
			case "setSpd": case "setDir": case "setAngVel": case "setAccel": case "setMinSpd": case "setMaxSpd":
				return o1 instanceof MovableEntity && (o2 instanceof Integer || o2 instanceof Float);
			
			case "setScale": case "setScaleX": case "setScaleY": case "setAlpha":
				return (o1 instanceof Bullet || o1 instanceof Enemy) && (o2 instanceof Integer || o2 instanceof Float);
			
			case "setPos":
				return o1 instanceof GameEntity && o2 instanceof ArrayList;

			case "setType":
				return (o1 instanceof Bullet || o1 instanceof Enemy) && o2 instanceof Integer;
			
			case "setColor":
				return o1 instanceof Bullet && o2 instanceof Integer;
			
			case "setFrame":
				return o1 instanceof Bullet && ((p == 2 && o2 instanceof Integer && o3 instanceof Integer) || (p == 1 && o2 instanceof ArrayList));
			
			case "setAdditive":
				return o1 instanceof GameEntity && (p == 0 || (p == 1 && o2 instanceof Boolean));
			
			case "delete": case "deleteImmediate": case "getX": case "getY": case "getPos": case "getTime": case "isDeleted": case "isVisible":
				return o1 instanceof GameEntity;
			
			case "getSpd": case "getDir": case "getAngVel": case "getAccel": case "getMinSpd": case "getMaxSpd":
				return o1 instanceof MovableEntity;
			
			case "getScale": case "getScaleX": case "getScaleY": case "getAlpha":
				return (o1 instanceof Bullet || o1 instanceof Enemy);
			
			case "setVisible":
				return o1 instanceof GameEntity;
			
			case "setCollisions":
				return (o1 instanceof Bullet || o1 instanceof Enemy) && o2 instanceof Boolean;

			case "setHitboxSize":
				return (o1 instanceof Bullet || o1 instanceof Enemy) && o2 instanceof Integer;
			
			case "setDespawnRange":
				return o1 instanceof Bullet && (o2 instanceof Integer || o2 instanceof Float);
			
			case "getCollisions": case "getHitboxSize":
				return (o1 instanceof Bullet || o1 instanceof Enemy);
			
			case "setBombResist":
				return o1 instanceof Bullet && (p == 0 || (p == 1 && o2 instanceof Boolean));
			
			case "setBorderDespawn":
				return o1 instanceof Bullet && o2 instanceof Boolean;
			
			case "getType": case "isAdditive":
				return o1 instanceof Bullet || o1 instanceof Enemy;
			
			case "getColor": case "getFrame": case "getBorderDespawn": case "getDespawnRange":
				return o1 instanceof Bullet;
				
			case "setWidth": case "setLength":
				return o1 instanceof Laser &&
					(o2 instanceof Integer || o2 instanceof Float);
			
			case "setSegmented":
				return o1 instanceof Laser && (p == 0 || (p == 1 && o2 instanceof Boolean));
			
			case "getWidth": case "getLength": case "isSegmented":
				return o1 instanceof Laser;

			case "setHealth":
				return o1 instanceof Enemy &&
					(o2 instanceof Integer || o2 instanceof Float);
			
			case "setInvulnerable":
				return o1 instanceof Enemy && (p == 0 || (p == 1 && o2 instanceof Boolean));
			
			case "getHealth": case "isInvulnerable":
				return o1 instanceof Enemy;
		}
		
		return true;
	}
	
	// Function requires dot separator
	public static boolean builtInFunctionDot(int index){
		return builtInFunctions[index].contains(":d");
	}
}
