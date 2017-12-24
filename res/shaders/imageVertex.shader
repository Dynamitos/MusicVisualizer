#version 440

in vec3 position;
in vec2 texCoords;

out vec2 texCoords_FS_in;
out vec3 worldPos_FS_in;

uniform mat4 transformationMatrix;
uniform mat4 viewMatrix;
uniform mat4 projection;
uniform float intensity;

void main(void)
{
	texCoords_FS_in = texCoords;
	vec4 worldPos = transformationMatrix * vec4(position, 1);
	worldPos_FS_in = worldPos.xyz;
	gl_Position = viewMatrix * worldPos;
}