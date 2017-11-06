#version 150

in vec2 position;
in vec2 size;
in vec4 texCoords;
in vec3 transforms;
in float alpha;
in int segments;

out vec2 gSize;
out vec4 gTexCoords;
out vec3 gTransforms;
out float gAlpha;
out int gSegments;

void main(){
    gSize = size;
    gTexCoords = texCoords;
    gTransforms = transforms;
    gAlpha = alpha;
    gSegments = segments;
    
    gl_Position = vec4(position, 0.0, 1.0);
}