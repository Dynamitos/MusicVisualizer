#version 440

struct Line
{
	vec2 start;
	vec2 end;
	float height;
	vec4 color;
};

in vec3 worldPos_FS_in;
in vec2 texCoords_FS_in;

out vec4 color;

uniform sampler2D textureSampler;
uniform float intensity;
uniform sampler1D music;
uniform float intensityScale;
uniform float intensityOffset;
uniform int numLines;
uniform Line lines[8];

void main(void)
{
	float cIntensity = clamp(intensity * intensityScale+intensityOffset, intensityOffset, intensityScale);
	vec4 texColor = texture(textureSampler, texCoords_FS_in);
	float delta = 3 - texColor.x - texColor.y - texColor.z;
	delta = clamp(0.5+(10/(delta)), 0, 1);
	
	color = cIntensity * delta * texColor;
	
}