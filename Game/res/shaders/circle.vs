#version 150

in vec2 position;
in int radius;

out int gRadius;

void main(){
    gRadius = radius;
    gl_Position = vec4(position, 0.0, 1.0);
}