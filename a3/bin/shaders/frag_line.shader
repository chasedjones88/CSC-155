#version 430 core

flat in int index;
out vec4 color;

uniform mat4 mv_matrix;
uniform mat4 proj_matrix;

void main(void) {
    color = vec4(1.0, 0.0, 0.0, 1.0);
    if(index > 1) {
    	color = vec4(0.0, 1.0, 0.0, 1.0);
    	if(index > 3) {
    		color = vec4(0.0, 0.0, 1.0, 1.0);
    	}
    }
}