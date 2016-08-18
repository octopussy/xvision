//our attributes
attribute vec2 a_position;
attribute vec4 a_color;

//our camera matrix
uniform mat4 u_projTrans;
uniform vec2 u_centerPoint;

varying vec2 v_vertPos;
varying vec2 v_centerPoint;

void main() {
    gl_Position = u_projTrans * vec4(a_position.xy, 0.0, 1.0);

    v_vertPos = vec2(a_position.xy);
}