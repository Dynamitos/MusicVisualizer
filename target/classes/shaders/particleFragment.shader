#version 440

in vec3 position_FS_in;

out vec4 out_Color;

uniform vec4 color;

void main(void)
{
	out_Color = color;
}