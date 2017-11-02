#version 150

layout(points) in;
layout(triangle_strip, max_vertices = 32) out;

in int gRadius[];

const float PI = 3.1415927;

void main(){
    
    vec4 cPos = gl_in[0].gl_Position;
    
    float r = gRadius[0];
    
    int points = 15;
    
    for(int i = 0; i <= points; i++){
        float ang = i*(PI*2/points);
        vec4 pos = cPos + vec4(r*cos(ang), r*sin(ang), 0.0, 0.0);
        
        pos.x = pos.x/320.0 - 1.0;
        pos.y = -(pos.y/240.0 - 1.0);
        
        gl_Position = vec4(cPos.x/320.0 - 1.0, -(cPos.y/240.0 - 1.0), 0.0, 1.0);
        EmitVertex();
        
        gl_Position = pos;
        EmitVertex();
    }
    
    EndPrimitive();
}