package engine.newscript;

/**
 * 
 * List of all built-in functions for DScript.
 * Any such functions are also run here.
 * 
 * @author Daniel
 * 
 */

import java.util.ArrayList;
import java.util.Random;

import content.FrameList;
import engine.entities.BossEnemy;
import engine.entities.Bullet;
import engine.entities.Enemy;
import engine.entities.GameEntity;
import engine.entities.Laser;
import engine.entities.MovableEntity;
import engine.entities.Player;
import engine.newscript.bytecodegen.Instruction;
import engine.screens.MainScreen;

public class BuiltInFunctionList{
	
	private final BIFunc[] funcList;
	
	private final MainScreen screen;
	
	private final Random random;
	
	
	public BuiltInFunctionList(MainScreen screen){
		this.screen = screen;
		
		random = new Random();
		Player player		= screen != null ? screen.getPlayer() : null;
		FrameList frameList	= screen != null ? screen.getFrameList() : null;
		
		// Create functions
		funcList = new BIFunc[]{
			
			// Print given value
			// obj
			new BIFunc("print", 1){
				protected Object run(Instruction inst, Object[] params){
					System.out.println(params[0]);
					return null;
				}
			},
			
			
			
			// Cast to integer
			// num
			new BIFunc("int", 1){
				protected Object run(Instruction inst, Object[] params){
					return (int)castFloat(params[0]);
				}
			},
			
			
			
			// Player position
			new BIFunc("playerX", 0){
				protected Object run(Instruction inst, Object[] params){
					return player.getX();
				}
			},
			new BIFunc("playerY", 0){
				protected Object run(Instruction inst, Object[] params){
					return player.getY();
				}
			},
			new BIFunc("playerPos", 0){
				protected Object run(Instruction inst, Object[] params){
					ArrayList<Object> array = new ArrayList<Object>(2);
					array.add(player.getX());
					array.add(player.getY());
					
					return array;
				}
			},
			

			// Angle from position to player
			// pos
			new BIFunc("angleToPlayer", 1){
				protected Object run(Instruction inst, Object[] params){
					ArrayList<Object> array = (ArrayList<Object>)params[0];
					
					float x = castFloat(array.get(0));
					float y = castFloat(array.get(1));
					
					return (float)Math.atan2(player.getY() - y, player.getX() - x);
				}
			},
			// x, y
			new BIFunc("angleToPlayer", 2){
				protected Object run(Instruction inst, Object[] params){
					float x = castFloat(params[0]);
					float y = castFloat(params[1]);
					
					return (float)Math.atan2(player.getY() - y, player.getX() - x);
				}
			},
			
			
			// Returns x, y array, radius r angle t
			// r, t
			new BIFunc("radius", 2){
				protected Object run(Instruction inst, Object[] params){
					float r = castFloat(params[0]);
					float t = (float)Math.toRadians(castFloat(params[1]));
					
					ArrayList<Object> array = new ArrayList<Object>(2);
					array.add((float)(r*Math.cos(t)));
					array.add((float)(r*Math.sin(t)));
					
					return array;
				}
			},
			
			
			
			// Angle from one position to another
			new BIFunc("angleToLocation", 2){
				protected Object run(Instruction inst, Object[] params){
					ArrayList<Object> array1 = (ArrayList<Object>)params[0];
					ArrayList<Object> array2 = (ArrayList<Object>)params[1];
					
					float x1 = castFloat(array1.get(0));
					float y1 = castFloat(array1.get(1));
					float x2 = castFloat(array2.get(0));
					float y2 = castFloat(array2.get(1));
					
					return (float)Math.atan2(y1 - y2, x1 - x2);
				}
			},
			new BIFunc("angleToLocation", 3){
				protected Object run(Instruction inst, Object[] params){
					
					boolean firstArray = params[0] instanceof ArrayList;
					
					ArrayList<Object> array = (ArrayList<Object>)params[firstArray ? 0 : 2];
					
					float x1, y1, x2, y2;
					
					if(firstArray){
						x1 = castFloat(array.get(0));
						y1 = castFloat(array.get(1));
						x2 = castFloat(params[1]);
						y2 = castFloat(params[2]);
					}
					else{
						x1 = castFloat(params[0]);
						y1 = castFloat(params[1]);
						x2 = castFloat(array.get(0));
						y2 = castFloat(array.get(1));
					}
					
					return (float)Math.atan2(y1 - y2, x1 - x2);
				}
			},
			new BIFunc("angleToLocation", 4){
				protected Object run(Instruction inst, Object[] params){
					float x1 = castFloat(params[0]);
					float y1 = castFloat(params[1]);
					float x2 = castFloat(params[2]);
					float y2 = castFloat(params[3]);

					return (float)Math.atan2(y1 - y2, x1 - x2);
				}
			},
			
			
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
			
			
			
			// Generate random boolean
			new BIFunc("randBool", 0){
				protected Object run(Instruction inst, Object[] params){
					return random.nextBoolean();
				}
			},
			
			
			
			// Pick random item from an array
			new BIFunc("randChoice", 1){
				protected Object run(Instruction inst, Object[] params){
					return random.nextBoolean();
				}
			},
			
			
			
			

			
			
			
			// Bullet
			// type, color, pos, dir, spd, delay
			new BIFunc("bullet", 6){
				protected Object run(Instruction inst, Object[] params){
					
					ArrayList<Object> pos = (ArrayList<Object>)params[2];
					
					Bullet b = new Bullet(
						frameList.getBullet(castInt(params[0]), castInt(params[1])),
						castFloat(pos.get(0)), castFloat(pos.get(1)),
						castFloat(params[3]),
						castFloat(params[4]),
						castInt(params[5]),
						screen
					);
					
					screen.addEnemyBullet(b);
					return b;
				}
			},
			// type, color, x, y, dir, spd, delay
			new BIFunc("bullet", 7){
				protected Object run(Instruction inst, Object[] params){
					
					Bullet b = new Bullet(
						frameList.getBullet(castInt(params[0]), castInt(params[1])),
						castFloat(params[2]), castFloat(params[3]),
						castFloat(params[4]),
						castFloat(params[5]),
						castInt(params[6]),
						screen
					);
					
					screen.addEnemyBullet(b);
					return b;
				}
			},
			// type, color, pos, dir, spd, minSpd, maxSpd, accel, delay
			new BIFunc("bullet", 9){
				protected Object run(Instruction inst, Object[] params){
					
					ArrayList<Object> pos = (ArrayList<Object>)params[2];
					
					Bullet b = new Bullet(
						frameList.getBullet(castInt(params[0]), castInt(params[1])),
						castFloat(pos.get(0)), castFloat(pos.get(1)),
						castFloat(params[3]),
						castFloat(params[4]),
						castFloat(params[5]),
						castFloat(params[6]),
						castFloat(params[7]),
						castInt(params[8]),
						screen
					);
					
					screen.addEnemyBullet(b);
					return b;
				}
			},
			// type, color, x, y, dir, spd, minSpd, maxSpd, accel, delay
			new BIFunc("bullet", 10){
				protected Object run(Instruction inst, Object[] params){
					
					Bullet b = new Bullet(
						frameList.getBullet(castInt(params[0]), castInt(params[1])),
						castFloat(params[2]), castFloat(params[3]),
						castFloat(params[4]),
						castFloat(params[5]),
						castFloat(params[6]),
						castFloat(params[7]),
						castFloat(params[8]),
						castInt(params[9]),
						screen
					);
					
					screen.addEnemyBullet(b);
					return b;
				}
			},
			
			// Laser
			// type, color, pos, dir, length, width, delay
			new BIFunc("laser", 7){
				protected Object run(Instruction inst, Object[] params){
					
					ArrayList<Object> pos = (ArrayList<Object>)params[2];
					
					Laser l = new Laser(
						frameList.getBullet(castInt(params[0]), castInt(params[1])),
						castFloat(pos.get(0)), castFloat(pos.get(1)),
						castFloat(params[3]),
						castInt(params[4]),
						castInt(params[5]),
						castInt(params[7]),
						screen
					);
					
					screen.addEnemyBullet(l);
					return l;
				}
			},
			// type, color, x, y, dir, length, width, delay
			new BIFunc("laser", 8){
				protected Object run(Instruction inst, Object[] params){
					
					Laser l = new Laser(
						frameList.getBullet(castInt(params[0]), castInt(params[1])),
						castFloat(params[2]), castFloat(params[3]),
						castFloat(params[4]),
						castInt(params[5]),
						castInt(params[6]),
						castInt(params[7]),
						screen
					);
					
					screen.addEnemyBullet(l);
					return l;
				}
			},
			
			// Enemy
			// type, pos, health
			new BIFunc("enemy", 3){
				protected Object run(Instruction inst, Object[] params){
					
					ArrayList<Object> pos = (ArrayList<Object>)params[1];
					
					Enemy e = new Enemy(
						frameList.getEnemy(castInt(params[0])),
						castFloat(pos.get(0)), castFloat(pos.get(1)),
						castInt(params[2]),
						screen
					);
					
					screen.addEnemy(e);
					return e;
				}
			},
			// type, x, y, health
			new BIFunc("enemy", 4){
				protected Object run(Instruction inst, Object[] params){
					
					Enemy e = new Enemy(
						frameList.getEnemy(castInt(params[0])),
						castFloat(params[1]), castFloat(params[2]),
						castInt(params[3]),
						screen
					);
					
					screen.addEnemy(e);
					return e;
				}
			},
			
			// Boss
			// type, pos, health
			new BIFunc("boss", 3){
				protected Object run(Instruction inst, Object[] params){
					
					ArrayList<Object> pos = (ArrayList<Object>)params[1];
					
					BossEnemy b = new BossEnemy(
						frameList.getEnemy(castInt(params[0])),
						castFloat(pos.get(0)), castFloat(pos.get(1)),
						castInt(params[2]),
						screen
					);
					
					screen.addEnemy(b);
					return b;
				}
			},
			// type, x, y, health
			new BIFunc("boss", 4){
				protected Object run(Instruction inst, Object[] params){
					
					BossEnemy b = new BossEnemy(
						frameList.getEnemy(castInt(params[0])),
						castFloat(params[1]), castFloat(params[2]),
						castInt(params[3]),
						screen
					);
					
					screen.addEnemy(b);
					return b;
				}
			},
			
			
			// Entity functions
			// Delete entity
			new BIFunc("delete", 1){
				protected Object run(Instruction inst, Object[] params){
					GameEntity e = (GameEntity)params[0];
					
					if(e instanceof Bullet)
						((Bullet)e).onDestroy(true);
					else
						e.delete();
					
					return null;
				}
			},
			
			// Delete without despawn effect
			new BIFunc("deleteImmediate", 1){
				protected Object run(Instruction inst, Object[] params){
					((GameEntity)params[0]).delete();
					return null;
				}
			},
			new BIFunc("isDeleted", 1){
				protected Object run(Instruction inst, Object[] params){
					return ((GameEntity)params[0]).isDeleted();
				}
			},
			
			// Set position
			new BIFunc("setX", 2){
				protected Object run(Instruction inst, Object[] params){
					((GameEntity)params[0]).setX(castFloat(params[1]));
					return null;
				}
			},
			new BIFunc("getX", 1){
				protected Object run(Instruction inst, Object[] params){
					return ((GameEntity)params[0]).getX();
				}
			},
			new BIFunc("setY", 2){
				protected Object run(Instruction inst, Object[] params){
					((GameEntity)params[0]).setY(castFloat(params[1]));
					return null;
				}
			},
			new BIFunc("getY", 1){
				protected Object run(Instruction inst, Object[] params){
					return ((GameEntity)params[0]).getY();
				}
			},
			// pos array
			new BIFunc("setPos", 2){
				protected Object run(Instruction inst, Object[] params){
					ArrayList<Object> pos = (ArrayList<Object>)params[1];
					((GameEntity)params[0]).setPos(castFloat(pos.get(0)), castFloat(pos.get(1)));
					return null;
				}
			},
			// x, y
			new BIFunc("setPos", 3){
				protected Object run(Instruction inst, Object[] params){
					((GameEntity)params[0]).setPos(castFloat(params[1]), castFloat(params[2]));
					return null;
				}
			},
			new BIFunc("getPos", 1){
				protected Object run(Instruction inst, Object[] params){
					
					GameEntity g = (GameEntity)params[0];
					ArrayList<Object> array = new ArrayList<Object>(2);

					array.add(castFloat(g.getX()));
					array.add(castFloat(g.getY()));
					
					return array;
				}
			},
			
			new BIFunc("getTime", 1){
				protected Object run(Instruction inst, Object[] params){
					return ((GameEntity)params[0]).getTime();
				}
			},
			// Set to true
			new BIFunc("setVisible", 1){
				protected Object run(Instruction inst, Object[] params){
					return ((GameEntity)params[0]).getTime();
				}
			},
			// Set to parameter
			new BIFunc("setVisible", 2){
				protected Object run(Instruction inst, Object[] params){
					return ((GameEntity)params[0]).getTime();
				}
			},
			new BIFunc("isVisible", 1){
				protected Object run(Instruction inst, Object[] params){
					return ((GameEntity)params[0]).isVisible();
				}
			},
			
			
			// Movement
			new BIFunc("setDir", 2){
				protected Object run(Instruction inst, Object[] params){
					((MovableEntity)params[0]).setDir(castFloat(params[1]));
					return null;
				}
			},
			new BIFunc("getDir", 1){
				protected Object run(Instruction inst, Object[] params){
					return ((MovableEntity)params[0]).getDir();
				}
			},
			new BIFunc("setAngVel", 2){
				protected Object run(Instruction inst, Object[] params){
					((MovableEntity)params[0]).setAngVel(castFloat(params[1]));
					return null;
				}
			},
			new BIFunc("getAngVel", 1){
				protected Object run(Instruction inst, Object[] params){
					return ((MovableEntity)params[0]).getAngVel();
				}
			},
			new BIFunc("setSpd", 2){
				protected Object run(Instruction inst, Object[] params){
					((MovableEntity)params[0]).setSpd(castFloat(params[1]));
					return null;
				}
			},
			new BIFunc("getSpd", 1){
				protected Object run(Instruction inst, Object[] params){
					return ((MovableEntity)params[0]).getSpd();
				}
			},
			new BIFunc("setAccel", 2){
				protected Object run(Instruction inst, Object[] params){
					((MovableEntity)params[0]).setAccel(castFloat(params[1]));
					return null;
				}
			},
			new BIFunc("getAccel", 1){
				protected Object run(Instruction inst, Object[] params){
					return ((MovableEntity)params[0]).getAccel();
				}
			},
			new BIFunc("setMinSpd", 2){
				protected Object run(Instruction inst, Object[] params){
					((MovableEntity)params[0]).setMinSpd(castFloat(params[1]));
					return null;
				}
			},
			new BIFunc("getMinSpd", 1){
				protected Object run(Instruction inst, Object[] params){
					return ((MovableEntity)params[0]).getMinSpd();
				}
			},
			new BIFunc("setMaxSpd", 2){
				protected Object run(Instruction inst, Object[] params){
					((MovableEntity)params[0]).setMaxSpd(castFloat(params[1]));
					return null;
				}
			},
			new BIFunc("getMaxSpd", 1){
				protected Object run(Instruction inst, Object[] params){
					return ((MovableEntity)params[0]).getMaxSpd();
				}
			},
			
			
			// Bullet properties
			
			
			
			new BIFunc("getHealth", 1){
				protected Object run(Instruction inst, Object[] params){
					return ((Enemy)params[0]).getHealth();
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
}
