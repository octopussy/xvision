#ifdef GL_ES
precision mediump float;
#endif

varying vec4 v_pos;
varying vec4 v_color;
varying vec2 v_texCoord0;

uniform sampler2D u_texture;
uniform sampler2D u_vismap;

void main() {
    vec4 lm = texture2D(u_vismap, vec2(v_pos.x / 2.0 + .5, v_pos.y / 2.0 + .5));
    gl_FragColor = texture2D(u_texture, v_texCoord0) * lm;
}