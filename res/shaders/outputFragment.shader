#version 440

in vec3 position_FS_in;
in vec2 texCoords_FS_in;

layout(location = 0) out vec4 out_Color;

uniform sampler2D textureSampler;

void main()
{
	vec4 texColor = texture(textureSampler, vec2(texCoords_FS_in.x, -texCoords_FS_in.y));
	out_Color = texColor;
}