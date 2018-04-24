#version 150

uniform sampler2D tex;
uniform vec4 fogColor;

in vec2 fTexCoords;
in float fAlpha;
in float fFogMix;

out vec4 fragColor;

void main(){
    fragColor = texture(tex, fTexCoords);
    fragColor.a *= fAlpha;
    fragColor = fragColor*(1 - fFogMix) + fogColor*fFogMix;
}
