#version 150

in vec2 position;
in vec2 size;
in vec4 texCoords;
in float rotation;
in float alpha;

out vec2 gSize;
out vec4 gTexCoords;
out float gRotation;
out float gAlpha;

void main(){
    gSize = size;
    gTexCoords = texCoords;
    gRotation = rotation;
    gAlpha = alpha;
    
    gl_Position = vec4(position, 0.0, 1.0);
}