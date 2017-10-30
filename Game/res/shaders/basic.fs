#version 150

uniform sampler2D tex;

in vec2 fTexCoords;
in float fAlpha;

out vec4 fragColor;

void main(){
    fragColor = texture(tex, fTexCoords);
    fragColor.a = fAlpha*fragColor.a;
}
