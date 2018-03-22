#version 150

layout(points) in;
layout(triangle_strip, max_vertices = 64) out;

in vec2 gSize[];
in float gRotation[];

const float PI = 3.1415927;

void main(){
    
    vec4 cPos = gl_in[0].gl_Position;
    
    float w = gSize[0].x;
    float h = gSize[0].y;
    float r = gRotation[0];
    
    int points = 31;
    
    for(int i = 0; i <= points; i++){
        float ang = i*(PI*2/points);
        vec2 pos = vec2(w*cos(ang), h*sin(ang));
        
        // Rotate
        pos *= mat2(cos(r), -sin(r), sin(r), cos(r));
        
        pos += cPos.xy;
        
        // Normalize
        pos.x = pos.x/320.0 - 1.0;
        pos.y = -(pos.y/240.0 - 1.0);
        
        gl_Position = vec4(cPos.x/320.0 - 1.0, -(cPos.y/240.0 - 1.0), 0.0, 1.0);
        EmitVertex();
        
        gl_Position = vec4(pos, 0.0, 1.0);
        EmitVertex();
    }
    
    EndPrimitive();
}