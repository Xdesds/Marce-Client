#version 330 core
in vec2 uv;
uniform sampler2D source;
uniform vec2 direction;
uniform float weights[7];
out vec4 fragColor;
void main() {
    vec3 c=texture(source,uv).rgb*weights[0];
    for(int i=1;i<=6;i++) c+=(texture(source,uv+direction*float(i)).rgb+texture(source,uv-direction*float(i)).rgb)*weights[i];
    fragColor=vec4(c,1);
}
