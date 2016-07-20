#version 440

in vec3 basePosition;
in float displacement;

void main(void)
{
	gl_Position = vec4(basePosition.x, basePosition.y+displacement, basePosition.z, 1);
}