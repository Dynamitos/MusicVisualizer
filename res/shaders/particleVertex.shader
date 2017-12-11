#version 440

layout(location = 0) in vec2 vertex_VS_in;
layout(location = 1) in vec3 position_VS_in;
layout(location = 2) in vec3 rotation_VS_in;
layout(location = 3) in vec2 atlasCoords_VS_in;
layout(location = 4) in float scale_VS_in;

layout(location = 0) out vec2 texCoords_FS_in;
layout(location = 1) out vec3 position_FS_in;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;

void main(void)
{
	texCoords_FS_in = vertex_VS_in + vec2(0.5, 0.5);
    mat3 mx, my, mz;

	// rotate around x
	float s = sin(rotation_VS_in.x);
	float c = cos(rotation_VS_in.x);

	mx[0] = vec3(c, s, 0.0);
	mx[1] = vec3(-s, c, 0.0);
	mx[2] = vec3(0.0, 0.0, 1.0);

	// rotate around y
	s = sin(rotation_VS_in.y);
	c = cos(rotation_VS_in.y);

	my[0] = vec3(c, 0.0, s);
	my[1] = vec3(0.0, 1.0, 0.0);
	my[2] = vec3(-s, 0.0, c);

	// rot around z
	s = sin(rotation_VS_in.z);
	c = cos(rotation_VS_in.z);

	mz[0] = vec3(1.0, 0.0, 0.0);
	mz[1] = vec3(0.0, c, s);
	mz[2] = vec3(0.0, -s, c);

    mat3 rotMat = mz * my * mx;
    mat3 posMat;
    posMat[0] = vec3(1, 0, 0);
    posMat[1] = vec3(0, 1, 0);
    posMat[2] = vec3(position_VS_in.x, position_VS_in.y, position_VS_in.z);

    vec3 worldPos = rotMat * vec3(vertex_VS_in*scale_VS_in, -1);
	gl_Position = projectionMatrix * viewMatrix * vec4(worldPos, 1);
}