#version 440

layout(location = 0) in vec2 vertex_VS_in;
layout(location = 1) in vec3 position_VS_in;
layout(location = 2) in vec3 rotation_VS_in;
layout(location = 3) in vec2 atlasCoords_VS_in;

layout(location = 0) out vec3 texCoords_FS_in;

uniform mat4 projectionMatrix;

void main(void)
{
	texCoords_FS_in = atlasCoords_VS_in;
	gl_Position = vec4(vertex_VS_in, 0, 1);
}