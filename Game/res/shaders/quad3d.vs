#version 150

in vec3 position;
in vec2 size;
in vec4 texCoords;
in vec3 rotation;
in float alpha;

out vec2 gSize;
out vec4 gTexCoords;
out vec3 gRotation;
out float gAlpha;

void main(){
    gSize = size;
    gTexCoords = texCoords;
    gRotation = rotation;
    gAlpha = alpha;
    
    gl_Position = vec4(position, 1.0);
}