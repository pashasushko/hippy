#version 300 es

precision mediump float;

out vec4 FragColor;

uniform vec2 u_resolution;
uniform float u_time;
uniform float u_hue;
uniform float u_speed;
uniform float u_scale;
uniform float u_magic_number;

float colormap_red(float x) {
    if (x < 0.09963276982307434) {
        return 5.56064615384614E+02 * x + 6.34200000000000E+01;
    } else if (x < 0.4070911109447479) {
        return ((-1.64134573262743E+03 * x + 1.26126075878571E+03) * x + 8.30228593437549E+01) * x + 9.96536529647110E+01;
    } else if (x < 0.5013306438922882) {
        return 1.64123076923114E+02 * x + 1.64926153846145E+02;
    } else if (x < 0.9049346148967743) {
        return ((((-4.17783076764345E+04 * x + 1.55735420068591E+05) * x - 2.27018068370619E+05) * x + 1.61149115838926E+05) * x - 5.60588504546212E+04) * x + 7.93919652573346E+03;
    } else {
        return -2.67676923076906E+02 * x + 2.68590769230752E+02;
    }
}

float colormap_green(float x) {
    if (x < 0.1011035144329071) {
        return 4.30627692307691E+02 * x - 1.56923076923038E-01;
    } else if (x < 0.5013851821422577) {
        return ((2.21240993583109E+02 * x - 7.23499016773187E+02) * x + 8.74292145995924E+02) * x - 3.78460594811949E+01;
    } else {
        return ((((-8.82260255008935E+03 * x + 3.69735516719018E+04) * x - 5.94940784200438E+04) * x + 4.54236515662453E+04) * x - 1.66043372157228E+04) * x + 2.59449114260420E+03;
    }
}

float colormap_blue(float x) {
    if (x < 0.50031378865242) {
        return ((((1.32543265346288E+04 * x - 1.82876583834065E+04) * x + 9.17087085537958E+03) * x - 2.45909850441496E+03) * x + 7.42893247681885E+02) * x + 7.26668497072812E+01;
    } else if (x < 0.609173446893692) {
        return -3.50141636141726E+02 * x + 4.22147741147797E+02;
    } else {
        return ((1.79776073728688E+03 * x - 3.80142452792079E+03) * x + 2.10214624189039E+03) * x - 6.74426111651015E+01;
    }
}

vec3 sat(vec3 c, float adj) {
    return mix(vec3(dot(c, vec3(0.2125, 0.7154, 0.0721))), c, adj);
}

// bezier fade
float bezier(float t) {
    return t * t * (3. - 2. * t);
}

vec3 colormap(float x) {
    return vec3(x, x, x);
    x = x*.25 - .025;
    float r = clamp(colormap_red(x) / 255.0, 0.0, 1.0);
    float g = clamp(colormap_green(x) / 255.0, 0.0, 1.0);
    float b = clamp(colormap_blue(x) / 255.0, 0.0, 1.0);
    vec3 c = vec3(r, g, b);
    c = sat(c, mix(0.5, 2., bezier(clamp(0., 1., 2. / 2.))));
    return vec3(c);
}

#define PI 3.141592654
vec2 rot(vec2 p, float a) {
    float c=sin(a*35.83);
    float s=cos(a*35.83);
    return p*mat2(s, c, c, -s);
}

vec3 hsv2rgb(vec3 c) {
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

void main() {
    vec2 uv = gl_FragCoord.xy;
    uv/=u_resolution.xy;
    uv=vec2(.125, .75)+(uv-vec2(.125, .5))*.003 * u_scale;
    float T = u_time * 0.25 * u_speed;

    vec3 c = clamp(1.-.7*vec3(
    length(uv-vec2(1.1, 1)),
    length(uv-vec2(1.1, 1)),
    length(uv-vec2(1.1, 1))
    ), 0., 1.)*2.-1.;
    vec3 c0=vec3(0);
    float w0=0.;
    float N = u_magic_number;
    for (float i=0.;i<N;i++) {
        float wt=(i*i/N/N-.2)*.15;
        float wp=0.5+(i+1.)*(i+1.5)*0.000001;
        float wb=.05+i/N*0.1;
        c.zx=rot(c.zx, 1.6+T*0.65*wt+(uv.x+.7)*23.*wp);
        c.xy=rot(c.xy, c.z*c.x*wb+1.7+T*wt+(uv.y+1.1)*15.*wp);
        c.yz=rot(c.yz, c.x*c.y*wb+2.4-T*0.79*wt+(uv.x+uv.y*(fract(i/2.)-0.25)*4.)*17.*wp);
        c.zx=rot(c.zx, c.y*c.z*wb+1.6-T*0.65*wt+(uv.x+.7)*23.*wp);
        c.xy=rot(c.xy, c.z*c.x*wb+1.7-T*wt+(uv.y+1.1)*15.*wp);
        float w=(1.5-i/N);
        c0+=c*w;
        w0+=w;
    }
    c0=c0/w0*2.+.5;
    c0*=.5+dot(c0, vec3(1))/sqrt(3.)*.5;
    c0+=pow(length(sin(c0*PI*4.))/sqrt(3.)*1.0, 20.)*(.3+.7*c0);
    vec3 hsvColor = vec3(u_hue, 0.9, 0.7);
    vec3 rgbColor = hsv2rgb(hsvColor);
    FragColor = vec4(colormap(c0.b*0.85+-0.075) + rgbColor, 1.0);
}
