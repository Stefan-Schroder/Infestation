#ifdef GL_ES
    precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform mat4 u_projTrans;

float rand(vec2 co){
    return fract(sin(dot(co.xy ,vec2(12.9898,78.233))) * 43758.5453);
}

void main() {
        vec2 cord = gl_FragCoord.xy;
        vec4 color = texture2D(u_texture, v_texCoords).rgba;
        float gray = (color.r + color.g + color.b + 0.1) / 3.0;
        vec3 grayscale = vec3(gray);

        gl_FragColor = vec4(grayscale, color.a);
}