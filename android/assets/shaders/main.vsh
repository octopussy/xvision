//our attributes
attribute vec2 a_position;
attribute vec2 a_texCoord0;

//our camera matrix
uniform mat4 u_projTrans;
uniform float u_width;
uniform float u_height;

//send the color out to the fragment shader
varying vec4 v_pos;
varying vec4 v_color;
varying vec2 v_texCoord0;

void main() {
    v_pos = u_projTrans * vec4(a_position.xy, 0.0, 1.0);
    v_texCoord0 = a_texCoord0;
    gl_Position = v_pos;
}