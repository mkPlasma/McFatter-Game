#version 150

layout(points) in;
layout(triangle_strip, max_vertices = 4) out;

uniform int time;
uniform vec3 camPosition;
uniform vec3 camRotation;
uniform vec2 fogRange;

in vec2 gSize[];
in vec4 gTexCoords[];
in vec3 gRotation[];
in float gAlpha[];

out vec2 fTexCoords;
out float fAlpha;
out float fFogMix;

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

mat4 projectionMatrix(){
    
    const float fovy = 60.0;
    const float aspect = 4.0/3.0;
    const float near = 0.01;
    const float far = 100000.0;
    
    const float top = -near*tan(radians(fovy)/2);
    const float bottom = -top;
    const float right = -top*aspect;
    const float left = -right;
    
    return mat4(
        2.0*near/(right - left), 0.0, 0.0, -near*(right + left)/(right - left),
        0.0, 2.0*near/(top - bottom), 0.0, -near*(top + bottom)/(top - bottom),
        0.0, 0.0, -(far + near)/(far - near), 2.0*far*near/(near - far),
        0.0, 0.0, -1.0, 0.0
    );
}

void vertex(vec2 p, float tx, float ty){
    
    vec4 pos = gl_in[0].gl_Position;
    
    // Fix z positioning
    pos.z = -pos.z;
    
    // Rotate around and move to quad center
    pos.xyz += rotate(vec3(p, 0.0), gRotation[0]);
    
    // Move relative to camera
    pos.xyz += camPosition;
    
    // Rotate around camera
    pos.xyz = rotate(pos.xyz, camRotation);
    
    // Projection
    pos *= projectionMatrix();
    
    // Adjust projection to center
    pos.x -= pos.z*0.3;
    
    // Fog amount
    fFogMix = max(0.0, min((pos.z - fogRange.x)/(fogRange.y - fogRange.x), 1.0));
    
    fAlpha = gAlpha[0];
    fTexCoords = vec2(tx, ty);
    gl_Position = pos;
    EmitVertex();
}

void main(){
    
    vec4 tx = gTexCoords[0];
    
    float x = gSize[0].x/4;
    float y = gSize[0].y/4;
    
    vertex(vec2(-x, -y), tx.x, tx.z);
    vertex(vec2(x, -y), tx.y, tx.z);
    vertex(vec2(-x, y), tx.x, tx.w);
    vertex(vec2(x, y), tx.y, tx.w);
    
    EndPrimitive();
}