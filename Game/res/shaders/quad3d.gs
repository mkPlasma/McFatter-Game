#version 150

layout(points) in;
layout(triangle_strip, max_vertices = 4) out;

uniform int time;
uniform vec3 camPosition;
uniform vec3 camRotation;

in vec2 gSize[];
in vec4 gTexCoords[];
in vec3 gRotation[];
in float gAlpha[];

out vec2 fTexCoords;
out float fAlpha;

vec3 rotate(vec3 pos, vec3 r){
    pos *= mat3(
        1.0, 0.0, 0.0,
        0.0, cos(r.x), -sin(r.x),
        0.0, sin(r.x), cos(r.x)
    )
    *mat3(
        cos(r.y), 0.0, sin(r.y),
        0.0, 1.0, 0.0,
        -sin(r.y), 0.0, cos(r.y)
    )
    *mat3(
        cos(r.z), -sin(r.z), 0.0,
        sin(r.z), cos(r.z), 0.0,
        0.0, 0.0, 1.0
    );
    
    return pos;
}

void vertex(vec3 pos2, float tx, float ty){
    
    vec4 pos = gl_in[0].gl_Position;
    
    // Z position is multiplied to fit in short
    pos.z /= 100;
    
    // Rotate around quad center
    pos2 = rotate(pos2, gRotation[0]);
    // Move to quad center
    pos.xyz += pos2;
    
    // Move relative to camera
    pos.xyz -= camPosition;
    // Rotate around camera
    pos.xyz = rotate(pos.xyz, camRotation);
    
    // Projection
    pos = vec4((pos.x)/pos.z, (pos.y)/pos.z, 0.0, 1.0);
    
    
    // Normalize
    pos.x = pos.x/320.0 - 1.0;
    pos.y = -(pos.y/240.0 - 1.0);
    pos.z = 0.0;
    
    fAlpha = gAlpha[0];
    fTexCoords = vec2(tx, ty);
    gl_Position = pos;
    EmitVertex();
}

void main(){
    
    vec4 tx = gTexCoords[0];
    
    float x = gSize[0].x/4;
    float y = gSize[0].y/4;
    
    vertex(vec3(-x, -y, 0), tx.x, tx.z);
    vertex(vec3(x, -y, 0), tx.y, tx.z);
    vertex(vec3(-x, y, 0), tx.x, tx.w);
    vertex(vec3(x, y, 0), tx.y, tx.w);
    
    EndPrimitive();
}