#version 150

uniform sampler2D tex;

in vec2 texCoords;

out vec4 fragColor;

void main(){
    fragColor = texture(tex, texCoords);
    fragColor = vec4(1.0, 0.0, 0.0, 1.0);
}