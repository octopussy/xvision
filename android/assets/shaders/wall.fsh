#ifdef GL_ES
precision mediump float;
#endif

varying vec4 v_pos;
varying vec2 v_vertPos;
varying vec4 v_color;
varying vec2 v_texCoord0;

uniform vec2 u_centerPoint;
uniform sampler2D u_sampled2D;

void main() {

    float max_distance = 37.0;
    float min_distance = 25.0;

    float d = (distance(u_centerPoint, v_vertPos) - min_distance) / (max_distance - min_distance);
    float distAlpha = 1.0 - smoothstep(0.0, 1.0, d);
    vec4 color = texture2D(u_sampled2D, v_texCoord0);
    float a = 1.0 - smoothstep(.7, 1.0, v_texCoord0.y);

    gl_FragColor = vec4(color.rgb, a * distAlpha);
}