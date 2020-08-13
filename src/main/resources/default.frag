#version 330
in vec4 coord;
in vec2 TexCoord;

uniform sampler2D ourTexture;
uniform vec4 blend;
uniform float xOffset;
uniform float yOffset;
uniform float xMax;
uniform float yMax;

out vec4 frag_colour;

void main(void) {
    gl_FragDepth = coord.z;
    vec4 blendActual = blend;
    if (TexCoord.x < xOffset || TexCoord.x > xMax || TexCoord.y < yOffset || TexCoord.y > yMax) {
        blendActual = vec4(0, 0, 0, 0);
    }
    frag_colour = texture(ourTexture, TexCoord) * blendActual;
}