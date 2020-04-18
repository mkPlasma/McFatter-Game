package engine.script;

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
import engine.entities.CollidableEntity;
import engine.entities.Enemy;
import engine.entities.GameEntity;
import engine.entities.Laser;
import engine.entities.MovableEntity;
import engine.entities.Player;
import engine.graphics.Sprite;
import engine.screens.MainScreen;
import engine.script.bytecodegen.Instruction;
import engine.script.runner.Branch;

public class BuiltInFunctionList{
	
	private final BIFunc[] funcList;
	
	private final Random random;
	
	
	public BuiltInFunctionList(final MainScreen screen){
		
		random = new Random();
		final Player player		= screen != null ? screen.getPlayer() : null;
		final FrameList frameList	= screen != null ? screen.getFrameList() : null;
		
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
			
			// Script branches
			new BIFunc("stop", 1){
				protected Object run(Instruction inst, Object[] params){
					((Branch)params[0]).finish();
					return null;
				}
			},
			new BIFunc("pause", 1){
				protected Object run(Instruction inst, Object[] params){
					((Branch)params[0]).setPaused(true);
					return null;
				}
			},
			new BIFunc("resume", 1){
				protected Object run(Instruction inst, Object[] params){
					((Branch)params[0]).setPaused(false);
					return null;
				}
			},
			new BIFunc("setPaused", 2){
				protected Object run(Instruction inst, Object[] params){
					((Branch)params[0]).setPaused((boolean)params[1]);
					return null;
				}
			},
			new BIFunc("isPaused", 1){
				protected Object run(Instruction inst, Object[] params){
					return ((Branch)params[0]).isPaused();
				}
			},
			
			
			// Array
			new BIFunc("length", 1){
				protected Object run(Instruction inst, Object[] params){
					return ((ArrayList<Object>)params[0]).size();
				}
			},
			new BIFunc("remove", 2){
				protected Object run(Instruction inst, Object[] params){
					return ((ArrayList<Object>)params[0]).remove(castInt(params[1]));
				}
			},
			
			
			
			
			// Math
			new BIFunc("abs", 1){
				protected Object run(Instruction inst, Object[] params){
					return castNumber(Math.abs(castFloat(params[0])));
				}
			},
			new BIFunc("round", 1){
				protected Object run(Instruction inst, Object[] params){
					return (int)Math.round(castFloat(params[0]));
				}
			},
			new BIFunc("trunc", 1){
				protected Object run(Instruction inst, Object[] params){
					return (int)castFloat(params[0]);
				}
			},
			new BIFunc("floor", 1){
				protected Object run(Instruction inst, Object[] params){
					return (int)Math.floor(castFloat(params[0]));
				}
			},
			new BIFunc("ceil", 1){
				protected Object run(Instruction inst, Object[] params){
					return (int)Math.ceil(castFloat(params[0]));
				}
			},
			
			new BIFunc("sqrt", 1){
				protected Object run(Instruction inst, Object[] params){
					return castNumber(Math.sqrt(castFloat(params[0])));
				}
			},
			new BIFunc("cbrt", 1){
				protected Object run(Instruction inst, Object[] params){
					return castNumber(Math.cbrt(castFloat(params[0])));
				}
			},
			new BIFunc("log", 1){
				protected Object run(Instruction inst, Object[] params){
					return castNumber(Math.log(castFloat(params[0])));
				}
			},
			new BIFunc("log10", 1){
				protected Object run(Instruction inst, Object[] params){
					return castNumber(Math.log10(castFloat(params[0])));
				}
			},
			new BIFunc("degrees", 1){
				protected Object run(Instruction inst, Object[] params){
					return castNumber(Math.toDegrees(castFloat(params[0])));
				}
			},
			new BIFunc("radians", 1){
				protected Object run(Instruction inst, Object[] params){
					return castNumber(Math.toRadians(castFloat(params[0])));
				}
			},

			new BIFunc("cos", 1){
				protected Object run(Instruction inst, Object[] params){
					return castNumber(Math.cos(Math.toRadians(castFloat(params[0]))));
				}
			},
			new BIFunc("sin", 1){
				protected Object run(Instruction inst, Object[] params){
					return castNumber(Math.sin(Math.toRadians(castFloat(params[0]))));
				}
			},
			new BIFunc("tan", 1){
				protected Object run(Instruction inst, Object[] params){
					return castNumber(Math.tan(Math.toRadians(castFloat(params[0]))));
				}
			},
			new BIFunc("acos", 1){
				protected Object run(Instruction inst, Object[] params){
					return castNumber(Math.acos(castFloat(params[0])));
				}
			},
			new BIFunc("asin", 1){
				protected Object run(Instruction inst, Object[] params){
					return castNumber(Math.asin(castFloat(params[0])));
				}
			},
			new BIFunc("atan", 1){
				protected Object run(Instruction inst, Object[] params){
					return castNumber(Math.atan(castFloat(params[0])));
				}
			},
			new BIFunc("atan2", 2){
				protected Object run(Instruction inst, Object[] params){
					return castNumber(Math.atan2(castFloat(params[0]), castFloat(params[1])));
				}
			},
			new BIFunc("min", 2){
				protected Object run(Instruction inst, Object[] params){
					return castNumber(Math.min(castFloat(params[0]), castFloat(params[1])));
				}
			},
			new BIFunc("max", 2){
				protected Object run(Instruction inst, Object[] params){
					return castNumber(Math.max(castFloat(params[0]), castFloat(params[1])));
				}
			},
			
			// Returns {x, y} array, radius r angle t
			// t, r
			new BIFunc("radius", 2){
				protected Object run(Instruction inst, Object[] params){
					float t = (float)Math.toRadians(castFloat(params[0]));
					float r = castFloat(params[1]);
					
					ArrayList<Object> array = new ArrayList<Object>(2);
					array.add((float)(r*Math.cos(t)));
					array.add((float)(r*Math.sin(t)));
					
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
					
					return (float)Math.toDegrees(Math.atan2(player.getY() - y, player.getX() - x));
				}
			},
			// x, y
			new BIFunc("angleToPlayer", 2){
				protected Object run(Instruction inst, Object[] params){
					float x = castFloat(params[0]);
					float y = castFloat(params[1]);
					
					return (float)Math.toDegrees(Math.atan2(player.getY() - y, player.getX() - x));
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
					
					return (float)Math.toDegrees(Math.atan2(y1 - y2, x1 - x2));
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
					
					return (float)Math.toDegrees(Math.atan2(y2 - y1, x2 - x1));
				}
			},
			new BIFunc("angleToLocation", 4){
				protected Object run(Instruction inst, Object[] params){
					float x1 = castFloat(params[0]);
					float y1 = castFloat(params[1]);
					float x2 = castFloat(params[2]);
					float y2 = castFloat(params[3]);
					
					return (float)Math.toDegrees(Math.atan2(y2 - y1, x2 - x1));
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
					
					return random.nextInt(a*2 + 1) - a;
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
					ArrayList<Object> array = (ArrayList<Object>)params[0];
					
					return array.get(random.nextInt(array.size()));
				}
			},
			
			
			
			
			
			// Force delete all enemy bullets
			new BIFunc("clearBullets", 0){
				protected Object run(Instruction inst, Object[] params){
					screen.clearEnemyBullets();
					return null;
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
						castInt(params[6]),
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
					((GameEntity)params[0]).setVisible(true);
					return null;
				}
			},
			new BIFunc("setInvisible", 1){
				protected Object run(Instruction inst, Object[] params){
					((GameEntity)params[0]).setVisible(false);
					return null;
				}
			},
			// Set to parameter
			new BIFunc("setVisible", 2){
				protected Object run(Instruction inst, Object[] params){
					((GameEntity)params[0]).setVisible((boolean)params[1]);
					return null;
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
			
			// Sprite properties
			new BIFunc("setScale", 2){
				protected Object run(Instruction inst, Object[] params){
					((GameEntity)params[0]).getSprite().setScale(castFloat(params[1]));
					return null;
				}
			},
			new BIFunc("getScale", 1){
				protected Object run(Instruction inst, Object[] params){
					ArrayList<Object> array = new ArrayList<Object>(2);
					Sprite s = ((GameEntity)params[0]).getSprite();
					
					array.add(s.getScaleX());
					array.add(s.getScaleY());
					
					return array;
				}
			},
			new BIFunc("setScaleX", 2){
				protected Object run(Instruction inst, Object[] params){
					((GameEntity)params[0]).getSprite().setScaleX(castFloat(params[1]));
					return null;
				}
			},
			new BIFunc("getScaleX", 1){
				protected Object run(Instruction inst, Object[] params){
					return ((GameEntity)params[0]).getSprite().getScaleX();
				}
			},
			new BIFunc("setScaleY", 2){
				protected Object run(Instruction inst, Object[] params){
					((GameEntity)params[0]).getSprite().setScaleY(castFloat(params[1]));
					return null;
				}
			},
			new BIFunc("getScaleY", 1){
				protected Object run(Instruction inst, Object[] params){
					return ((GameEntity)params[0]).getSprite().getScaleY();
				}
			},
			new BIFunc("setAdditive", 1){
				protected Object run(Instruction inst, Object[] params){
					((GameEntity)params[0]).getSprite().setAdditive(true);
					return null;
				}
			},
			new BIFunc("setAdditive", 2){
				protected Object run(Instruction inst, Object[] params){
					((GameEntity)params[0]).getSprite().setAdditive((boolean)params[1]);
					return null;
				}
			},
			new BIFunc("isAdditive", 1){
				protected Object run(Instruction inst, Object[] params){
					return ((GameEntity)params[0]).getSprite().isAdditive();
				}
			},
			new BIFunc("setAlpha", 2){
				protected Object run(Instruction inst, Object[] params){
					((GameEntity)params[0]).getSprite().setAlpha(castFloat(params[1]));
					return null;
				}
			},
			new BIFunc("getAlpha", 1){
				protected Object run(Instruction inst, Object[] params){
					return ((GameEntity)params[0]).getSprite().getAlpha();
				}
			},
			
			
			// Bullet properties
			new BIFunc("setType", 2){
				protected Object run(Instruction inst, Object[] params){
					((Bullet)params[0]).setFrame(frameList.getBullet(castInt(params[1]), ((Bullet)params[0]).getColor()));
					return null;
				}
			},
			new BIFunc("getType", 1){
				protected Object run(Instruction inst, Object[] params){
					return ((Bullet)params[0]).getType();
				}
			},
			new BIFunc("setColor", 2){
				protected Object run(Instruction inst, Object[] params){
					((Bullet)params[0]).setFrame(frameList.getBullet(((Bullet)params[0]).getType(), castInt(params[1])));
					return null;
				}
			},
			new BIFunc("getColor", 1){
				protected Object run(Instruction inst, Object[] params){
					return ((Bullet)params[0]).getColor();
				}
			},

			new BIFunc("setDamage", 2){
				protected Object run(Instruction inst, Object[] params){
					((Bullet)params[0]).setDamage(castFloat(params[1]));
					return null;
				}
			},
			new BIFunc("getDamage", 1){
				protected Object run(Instruction inst, Object[] params){
					return ((Bullet)params[0]).getDamage();
				}
			},
			new BIFunc("setDamageReduce", 2){
				protected Object run(Instruction inst, Object[] params){
					((Bullet)params[0]).setDamageReduce(castFloat(params[1]));
					return null;
				}
			},
			new BIFunc("getDamageReduce", 1){
				protected Object run(Instruction inst, Object[] params){
					return ((Bullet)params[0]).getDamageReduce();
				}
			},
			
			new BIFunc("setResistant", 1){
				protected Object run(Instruction inst, Object[] params){
					((Bullet)params[0]).setResistant(true);
					return null;
				}
			},
			new BIFunc("setResistant", 2){
				protected Object run(Instruction inst, Object[] params){
					((Bullet)params[0]).setResistant((boolean)params[1]);
					return null;
				}
			},
			new BIFunc("isResistant", 1){
				protected Object run(Instruction inst, Object[] params){
					return ((Bullet)params[0]).isResistant();
				}
			},
			
			// Shared by Bullet/Enemy
			new BIFunc("disableCollisions", 1){
				protected Object run(Instruction inst, Object[] params){
					((CollidableEntity)params[0]).setCollisions(false);
					return null;
				}
			},
			new BIFunc("enableCollisions", 1){
				protected Object run(Instruction inst, Object[] params){
					((CollidableEntity)params[0]).setCollisions(true);
					return null;
				}
			},
			new BIFunc("setCollisions", 2){
				protected Object run(Instruction inst, Object[] params){
					((CollidableEntity)params[0]).setCollisions((boolean)params[1]);
					return null;
				}
			},
			new BIFunc("getCollisions", 1){
				protected Object run(Instruction inst, Object[] params){
					return ((CollidableEntity)params[0]).collisionsEnabled();
				}
			},
			
			new BIFunc("setHitboxScale", 2){
				protected Object run(Instruction inst, Object[] params){
					((CollidableEntity)params[0]).setHitboxScale(castFloat(params[1]));
					return null;
				}
			},
			new BIFunc("getHitboxScaleX", 1){
				protected Object run(Instruction inst, Object[] params){
					return ((CollidableEntity)params[0]).getHitboxScaleX();
				}
			},
			new BIFunc("getHitboxScaleY", 1){
				protected Object run(Instruction inst, Object[] params){
					return ((CollidableEntity)params[0]).getHitboxScaleY();
				}
			},
			
			new BIFunc("setBorderDespawn", 2){
				protected Object run(Instruction inst, Object[] params){
					((CollidableEntity)params[0]).setBorderDespawn(false);
					return null;
				}
			},
			
			new BIFunc("getBorderDespawn", 1){
				protected Object run(Instruction inst, Object[] params){
					return ((CollidableEntity)params[0]).getBorderDespawn();
				}
			},
			
			new BIFunc("setDespawnRange", 2){
				protected Object run(Instruction inst, Object[] params){
					((CollidableEntity)params[0]).setDespawnRange(castInt(params[1]));
					return null;
				}
			},
			
			new BIFunc("setDespawnRange", 1){
				protected Object run(Instruction inst, Object[] params){
					return ((CollidableEntity)params[0]).getDespawnRange();
				}
			},
			
			
			
			
			// Laser properties

			new BIFunc("setLength", 2){
				protected Object run(Instruction inst, Object[] params){
					((Laser)params[0]).setLength(castInt(params[1]));
					return null;
				}
			},
			new BIFunc("getLength", 1){
				protected Object run(Instruction inst, Object[] params){
					return ((Laser)params[0]).getLength();
				}
			},
			new BIFunc("setWidth", 2){
				protected Object run(Instruction inst, Object[] params){
					((Laser)params[0]).setWidth(castInt(params[1]));
					return null;
				}
			},
			new BIFunc("getWidth", 1){
				protected Object run(Instruction inst, Object[] params){
					return ((Laser)params[0]).getWidth();
				}
			},
			
			
			
			
			// Enemy properties
			new BIFunc("setHealth", 2){
				protected Object run(Instruction inst, Object[] params){
					((Enemy)params[0]).setHealth(castInt(params[1]));
					return null;
				}
			},
			
			new BIFunc("getHealth", 1){
				protected Object run(Instruction inst, Object[] params){
					return ((Enemy)params[0]).getHealth();
				}
			},
			
			new BIFunc("setInvulnerable", 1){
				protected Object run(Instruction inst, Object[] params){
					((Enemy)params[0]).setInvulnerable(true);
					return null;
				}
			},
			
			new BIFunc("setVulnerable", 1){
				protected Object run(Instruction inst, Object[] params){
					((Enemy)params[0]).setInvulnerable(false);
					return null;
				}
			},
			
			new BIFunc("setInvulnerable", 2){
				protected Object run(Instruction inst, Object[] params){
					((Enemy)params[0]).setInvulnerable((boolean)params[1]);
					return null;
				}
			},
			
			new BIFunc("isInvulnerable", 1){
				protected Object run(Instruction inst, Object[] params){
					return ((Enemy)params[0]).isInvulnerable();
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
	
	// Cast to int or float from float
	private float castNumber(double d){
		return (int)d == d ? (int)d : (float)d;
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
