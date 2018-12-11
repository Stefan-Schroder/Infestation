#ifdef GL_ES
    precision mediump float;
    precision mediump int;
#endif

#define PI 3.1415926

uniform sampler2D u_texture;
varying vec2 v_texCoords;

float amount = 0.6;
float power = 0.5;


void main(){
    float pos0 = ((v_texCoords.y + 1.0) * 170.0 * amount);
    float pos1 = cos((fract(pos0) - 0.5) * PI * power) * 1.5;

    vec4 rgba = texture2D(u_texture, v_texCoords);
    vec3 rgb = rgba.rgb;

    vec3 color = rgb * 0.5 + 0.5 * rgb * rgb * 1.2;

    //tint
    //color *= vec3(0.9, 1.0, 0.7);
    color *= vec3(0.5, 0.5, 0.5);

    color *= 1.1 - 0.6 * (dot(v_texCoords - 0.5, v_texCoords - 0.5) * 2.0);

    gl_FragColor.rgb = mix(vec3(0,0,0), color, pos1);
    gl_FragColor.a = rgba.a;
}