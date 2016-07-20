#version 440

in vec3 position_FS_in;
in vec2 texCoords_FS_in;

out vec4 out_Color;

uniform sampler2D textureSampler;
uniform sampler2D overlay;
uniform float time;

void main()
{
	vec2 texCoords = vec2(texCoords_FS_in.x, -texCoords_FS_in.y);
	vec4 texColor = texture(textureSampler, texCoords);
	vec4 overlayColor = texture(overlay, texCoords_FS_in);
	
	out_Color = overlayColor*texColor;//+0*(sin(time * 10 + (3.1415 * texCoords_FS_in.y * 0.5 + 0.5))*2);
}