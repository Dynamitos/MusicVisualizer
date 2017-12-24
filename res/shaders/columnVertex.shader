#version 450

layout(location = 0) in vec2 basePosition;
layout(location = 1) in vec2 position;
layout(location = 2) in float displacement;

void main()
{
	gl_Position = vec4(basePosition.x+position.x, (basePosition.y*displacement)+position.y, 0, 1);
}