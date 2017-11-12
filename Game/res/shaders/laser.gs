#version 150

layout(points) in;
layout(triangle_strip, max_vertices = 80) out;

uniform int time;
const int period = 10;

in vec2 gSize[];
in vec4 gTexCoords[];
in vec3 gTransforms[];
in float gAlpha[];
in int gSegments[];

out vec2 fTexCoords;
out float fAlpha;

void vertex(vec2 pos2, float tx, float ty){
    
    vec4 pos = gl_in[0].gl_Position;
    vec3 trans = gTransforms[0];
    
    // Scale
    pos2.x *= trans.x;
    pos2.y *= trans.y;
    
    // Rotate
    float r = trans.z;
    pos2 *= mat2(cos(r), -sin(r), sin(r), cos(r));
    
    pos += vec4(pos2, 0.0, 0.0);
    
    // Normalize
    pos.x = pos.x/320.0 - 1.0;
    pos.y = -(pos.y/240.0 - 1.0);
    
    fAlpha = gAlpha[0];
    fTexCoords = vec2(tx, ty);
    gl_Position = pos;
    EmitVertex();
}

void main(){
    
    vec4 tx = gTexCoords[0];
    
    float x = gSize[0].x/4;
    float y = gSize[0].y/4;
    
    int seg = gSegments[0];
    
    float sl = (y*2)/(seg - 1);
    float tl = tx.w - tx.z;
    float tm = (time % period)/float(period);
    
    for(int i = 0; i < seg; i++){
        
        // Start
        if(i == 0){
            vertex(vec2(x, y), tx.x, tx.z + tm*tl);
            vertex(vec2(-x, y), tx.y, tx.z + tm*tl);
        }
        // Segment start
        else{
            vertex(vec2(x, y - (i - 1+ tm)*sl), tx.x, tx.w);
            vertex(vec2(-x, y - (i - 1 + tm)*sl), tx.y, tx.w);
        }
        
        // End
        if(i == seg - 1){
            vertex(vec2(x, -y), tx.x, tx.z + tm*tl);
            vertex(vec2(-x, -y), tx.y, tx.z + tm*tl);
        }
        // Segment end
        else{
            vertex(vec2(x, y - (i + tm)*sl), tx.x, tx.z);
            vertex(vec2(-x, y - (i + tm)*sl), tx.y, tx.z);
        }
        
        EndPrimitive();
    }
}