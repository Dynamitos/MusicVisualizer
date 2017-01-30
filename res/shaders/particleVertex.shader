#version 440

in vec3 position;

out vec3 position_FS_in;

uniform mat4 projectionMatrix;

void main(void)
{
	vec4 temp = vec4(position, 1);
	position_FS_in = temp.xyz;
	gl_Position = temp;
}