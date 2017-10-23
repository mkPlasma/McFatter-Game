#version 150

in vec2 position;
in vec2 texCoordsIn;

out vec2 texCoords;

void main(){
    texCoords = texCoordsIn;
    gl_Position = vec4(position, 0.0, 1.0);
}