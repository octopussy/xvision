#ifdef GL_ES
precision mediump float;
#endif

uniform vec2 u_centerPoint;

varying vec2 v_vertPos;

void main() {
    float max_distance = 420.0;
    float min_distance = 340.0;

    float d = (distance(u_centerPoint, v_vertPos) - min_distance) / (max_distance - min_distance);
    float a = 1.0 - smoothstep(0.0, 1.0, d);
    gl_FragColor = vec4(1, 1, 1, a);
}