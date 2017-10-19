#version 150

in vec4 position;
in vec3 rotation;

out vec2 texCoords;

vec2 rotate(){
    vec2 pos = position.xy;
    pos -= rotation.xy;

    float a = rotation.z;

    pos *= mat2(cos(a), -sin(a), sin(a), cos(a));

    pos += rotation.xy;
    return pos;
}

vec2 normalize(vec2 inp){
    vec2 norm = inp;
    norm.x = (norm.x/400.0) - 1.0;
    norm.y = -((norm.y/300.0) - 1.0);
    return norm;
}

void main(){
    texCoords = position.zw;

    vec2 pos = normalize(rotate());
    gl_Position = vec4(pos, 0.0, 1.0);
}