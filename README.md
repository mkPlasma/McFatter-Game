# McFatter-Game

This is a 2D bullet-hell game engine made in Java using LWJGL for window management and user input, and OpenGL for rendering.
It includes a player with movement and firing, stages, and enemies and bullets with complex, programmable behaviors.

Bullets, enemies, and stages are controlled by a custom scripting language called DScript. DScript is a Java-like scripting language with features like variables, loops, and functions.
Scripts can be loaded individually or combined, and can be reloaded without exiting the game.

Variables in DScript are implemented using the Java generic "Object", which allows them to store engine objects like bullets and enemies.
DScript is mostly standalone, and is connected to the rest of the engine using built-in functions. These functions allow scripts to generate and modify game objects, control stage events, and do math.

There are also features specifically made for game scripting. 'Tasks' are functions that are similar to coroutines, which branch the script into a second control flow when called.
Branches do not execute simultaneously, but all branches in a script will execute within a single frame, making their execution appear simultaneous to the engine.
DScript also includes a 'wait' keyword which allows branches to stop executing for a fixed or variable number of frames.
These features make it easy to create and properly time game stages, and create complex bullet behaviors by changing their properties at specified times.

DScript is available in Game/src/script/.
Example scripts are in Game/res/script/.
