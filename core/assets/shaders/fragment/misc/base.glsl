#ifdef GL_ES
    precision mediump float;
    precision mediump int;
#endif

uniform sampler2D u_texture;
varying vec2 v_texCoords;

void main() {
    vec4 rgba = texture2D(u_texture, v_texCoords).rgba;
    vec3 rgb = rgba.rgb;
    float alpha = rgba.a;

    float luma = dot(rgb, vec3(0.299, 0.587, 0.114));
    vec3 gray = vec3(luma, luma, luma) - 0.5;
    rgb -= vec3(0.5, 0.5, 0.5);

    gl_FragColor = vec4(mix(rgb, gray, 2.0) + 0.5, alpha);
}