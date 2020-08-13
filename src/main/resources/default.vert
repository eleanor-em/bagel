#version 330
layout (location = 0) in vec3 vp;
layout (location = 1) in vec2 inTexCoord;

uniform float width;
uniform float height;
uniform vec3 translation;
uniform vec3 scale;
uniform float rotation;

out vec2 TexCoord;
out vec4 coord;

void main(void) {
    mat4 ortho = mat4(2.0 / width,  0.0,          0.0, -1.0,
                      0.0,         -2.0 / height, 0.0,  1.0,
                      0.0,          0.0,          0.0,  0.0,
                      0.0,          0.0,          0.0,  1.0);

    mat4 translate = mat4(1.0, 0.0, 0.0, translation.x,
                          0.0, 1.0, 0.0, translation.y,
                          0.0, 0.0, 1.0, translation.z,
                          0.0, 0.0, 0.0, 1.0);

    mat4 scale = mat4(scale.x, 0.0,     0.0,     0.0,
                      0.0,     scale.y, 0.0,     0.0,
                      0.0,     0.0,     scale.z, 0.0,
                      0.0,     0.0,     0.0,     1.0);
    mat4 rot = mat4(cos(rotation), -sin(rotation), 0.0, 0.0,
                    sin(rotation),  cos(rotation), 0.0, 0.0,
                    0.0,            0.0,           1.0, 0.0,
                    0.0,            0.0,           0.0, 1.0);

     // GL matrices are column major for some reason, so transpose
    gl_Position = transpose(ortho)  * transpose(translate) * transpose(rot) * scale * vec4(vp, 1.0);
    TexCoord = inTexCoord;
    coord = gl_Position;
}