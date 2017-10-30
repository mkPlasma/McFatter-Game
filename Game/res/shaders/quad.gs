#version 150

layout(points) in;
layout(triangle_strip, max_vertices = 5) out;

in vec2 gSize[];
in vec4 gTexCoords[];
in vec3 gTransforms[];
in float gAlpha[];

out vec2 fTexCoords;
out float fAlpha;

void vertex(vec4 pos, float x, float y, float tx, float ty){
    
    vec3 trans = gTransforms[0];
    
    x *= trans.x;
    y *= trans.y;
    
    pos += vec4(x, y, 0.0, 0.0);
    
    pos.x = pos.x/320.0 - 1.0;
    pos.y = -(pos.y/240.0 - 1.0);
    
    fAlpha = gAlpha[0];
    fTexCoords = vec2(tx, ty);
    gl_Position = pos;
    EmitVertex();
}

void main(){
    
    vec4 tx = gTexCoords[0];
    
    vec4 pos = gl_in[0].gl_Position;
    
    float x = gSize[0].x/4;
    float y = gSize[0].y/4;
    
    vertex(pos, x, y, tx.z, tx.w);
    vertex(pos, x, -y, tx.z, tx.y);
    vertex(pos, -x, -y, tx.x, tx.y);
    vertex(pos, -x, y, tx.x, tx.w);
    vertex(pos, x, y, tx.z, tx.w);
    
    EndPrimitive();
}