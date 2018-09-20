#ifdef GL_ES
    precision mediump float;
    precision mediump int;
#endif

uniform sampler2D u_texture;
varying vec2 v_texCoords;

float amount = 1.0;
float power = 0.7;

void main(){
    vec3 color = texture2D(u_texture, v_texCoords.xy).xyz;
    float alpha = texture2D(u_texture, v_texCoords).a;
    vec4 sum = vec4(0);
    vec3 bloom;

    for(int i=-3; i<3; i++){
        sum += texture2D(u_texture, v_texCoords + vec2(-1, i)*0.004) * amount;
        sum += texture2D(u_texture, v_texCoords + vec2(0, i)*0.004) * amount;
        sum += texture2D(u_texture, v_texCoords + vec2(1, i)*0.004) * amount;
    }

    if(color.r < 0.3 && color.g < 0.3 && color.b < 0.3){
        bloom = sum.xyz*sum.xyz*0.012 + color;
    }
    else{
        if(color.r < 0.5 && color.g < 0.5 && color.b < 0.5){
            bloom = sum.xyz*sum.xyz*0.009 + color;
        }
        else{
            bloom = sum.xyz*sum.xyz*0.0075 + color;
        }
    }

    bloom = mix(color, bloom, power);
    gl_FragColor.rgb = bloom;
    gl_FragColor.a = alpha;
}