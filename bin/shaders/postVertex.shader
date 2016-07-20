#version 440

in vec3 position;
in vec2 texCoords;

out vec3 position_FS_in;
out vec2 texCoords_FS_in;

void main()
{
	texCoords_FS_in = texCoords;
	position_FS_in = position;
	gl_Position = vec4(position, 1);
}