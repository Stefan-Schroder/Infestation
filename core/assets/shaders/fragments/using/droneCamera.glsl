#ifdef GL_ES
    precision mediump float;
#endif

#define PI 3.1415926

uniform sampler2D u_texture;
uniform float u_time;
varying vec2 v_texCoords;

float bloomAmount = 1.0;
float bloomPower = 0.7;

float scanAmount = 0.6;
float scanPower = 1.0;

float rand(vec2 co){//noise
    return fract(sin(dot(co.xy ,vec2(12.9898,78.233))) * 43758.5453);
}

void main(){
    //initialise
    vec3 color = texture2D(u_texture, v_texCoords.xy).xyz;
    float alpha = texture2D(u_texture, v_texCoords).a;
    vec2 cord = gl_FragCoord.xy;


    //bloom
    vec4 sum = vec4(0);
    vec3 bloom;

    for(int i=-3; i<3; i++){
        sum += texture2D(u_texture, v_texCoords + vec2(-1, i)*0.004) * bloomAmount;
        sum += texture2D(u_texture, v_texCoords + vec2(0, i)*0.004) * bloomAmount;
        sum += texture2D(u_texture, v_texCoords + vec2(1, i)*0.004) * bloomAmount;
    }

    /*
    float greyBloom = (sum.r + sum.g + sum.b + 0.1) / 3.0;
    sum = vec4(greyBloom, greyBloom, greyBloom, sum.a);
    */

    if(color.r < 0.3 && color.g < 0.3 && color.b < 0.3){
        bloom = sum.xyz*sum.xyz*0.012 + color;
    }
    else{
        //bloom = sum.xyz*sum.xyz+ color;
        if(color.r < 0.5 && color.g < 0.5 && color.b < 0.5){
            bloom = sum.xyz*sum.xyz*0.009 + color;
        }
        else{
            bloom = sum.xyz*sum.xyz*0.0075 + color;
        }
    }

    bloom = mix(color, bloom, bloomPower);

    //grey
    float grey = (color.r + color.g + color.b + 0.1) / 3.0;
    color = vec3(grey);

    //scan
    /* old scan
    float pos0 = ((v_texCoords.y + 1.0) * 170.0 * scanAmount);
    float pos1 = cos((fract(pos0) - 0.5) * PI * scanPower) * 1.5;

    vec3 scan = color * 0.5 + 0.5 * color * color * 1.2;

    scan *= 1.1 - 0.6 * (dot(v_texCoords - 0.5, v_texCoords - 0.5) * 2.0);

    scan = mix(vec3(0,0,0), scan, pos1);
    */ 

    //a mod b = a - (b * floor(a/b))

    float scanFlicker = 2.*PI*u_time;
    //if(scanFlicker-(PI * floor(scanFlicker/(PI)) ) < 0.01) scanFlicker = 0.1;

    float scanMovement = 5.*sin(scanFlicker/10.+PI/2.);

    vec3 scan = color*((cos(cord.y*PI/3.0 + scanMovement)+1.)/2.0);
    scan = mix(color, scan, 0.8);

    //noise
    /*
    if(rand(cord + u_time)>=0.85){
        finalColor *= rand(cord + u_time);
    }
    */

    //final
    vec3 finalColor = mix(scan, bloom, 0.5);


    //vec3 finalColor = mix(finalColor0, greyScale, 0.5);
    /*
    float anim=(sin(u_time*1.0)+0.9)*0.55;
    if(v_texCoords.x<anim){
        finalColor *= 0.0;
    }
    */
    gl_FragColor.rgb = finalColor;
    gl_FragColor.a = alpha;
}