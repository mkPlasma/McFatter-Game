#version 150

in vec2 position;
in vec2 size;
in float rotation;

out vec2 gSize;
out float gRotation;

void main(){
    gSize = size;
    gRotation = rotation;
    
    gl_Position = vec4(position, 0.0, 1.0);
}