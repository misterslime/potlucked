// ENHANCED 2D liquid inside 'sphere' shader.

// Author: Mirza Beig

// https://twitter.com/TheMirzaBeig
// https://www.youtube.com/@MirzaBeig

// Feel free to use this however you want.
// Modify, learn from it, copy-paste, etc...

// Modified by Azzyberries/misterslime/auberries.

#version 150

#define PI 3.14159
#define easeInCirc(a) 1. - sqrt(1. - pow(a, 2.))

uniform sampler2D Sampler0;

uniform float uTime;

uniform vec4 ColorModulator;
uniform vec3 FrontFillColour;
uniform vec3 BackFillColour;
uniform vec2 ImageResolution;
uniform float FillAnimationFrequency;
uniform float FillAnimationAmplitude;
uniform float WaveFrequency;
uniform float WaveAmplitude;
uniform float WaveAnimation;
uniform float AnimatedVerticalBandStrength;
uniform float FillProgressAnimationFrequency;
uniform float FillProgressAnimationAmplitude;
uniform float FresnelPower;
uniform float FresnelOffset;
uniform float FillProgress;

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

void main() {

    // Centered, aspect ratio-corrected, normalized pixel coordinates [0.0, 1.0].
    vec2 uv = (texCoord0 - 0.5) * 2.0;

    // Pixelation.
    uv = floor(uv * ImageResolution) / ImageResolution;

    // Flip vertically.
    uv.y *= -1.0;

    // Progress/fill. Animated.
    float fillAnimation = sin(uTime * FillAnimationFrequency) * FillAnimationAmplitude; // for back wave set FillAnimationAmplitude to a negative number

    // Waves as repeating sine/band offsets to the horizontal gradient.
    float frontWavePhase = (uTime * WaveAnimation) + uv.x; // for back wave set WaveAnimation to a negative number
    float backWavePhase = (uTime * -WaveAnimation) + uv.x;

    float frontWaves = sin(frontWavePhase * WaveFrequency) * WaveAmplitude; // for back wave set WavePhase to PI
    float backWaves = sin(backWavePhase * WaveFrequency + PI) * WaveAmplitude;

    float verticalBand = sin(uv.x + (PI * 0.5)) - 0.3;
    verticalBand = smoothstep(0.2, 1.2, verticalBand);

    // Stretch waves up/down near center, synced as they bob up/down.
    float animatedVerticalBand = verticalBand * AnimatedVerticalBandStrength; // for back wave set AnimatedVerticalBandStrength to a negative number

    animatedVerticalBand *= sin(uTime * FillAnimationFrequency);

    frontWaves += animatedVerticalBand;
    backWaves -= animatedVerticalBand;

    // Pinch sides (mask by the vertical gradient band) so they don't move
    fillAnimation *= verticalBand;

    // Centered fill progress.
    float progress = FillProgress + sin((uTime * FillProgressAnimationFrequency) * PI) * FillProgressAnimationAmplitude;
    progress = (progress - 0.5) * 2.0;

    float frontFill = step(uv.y, (fillAnimation + frontWaves) + progress);
    float backFill = step(uv.y, (-fillAnimation + backWaves) + progress);

    // Mask back fill to only parts that would be visible separate from frontFill.
    backFill = clamp(backFill - frontFill, 0.0, 1.0);
    float fill = frontFill + backFill;

    // Apply fresnel shading to the front layer to imply a cylindrical shape.
    float fresnel = (easeInCirc(abs(uv.x)) + FresnelOffset) * (fill);
    fresnel = clamp(pow(fresnel, FresnelPower), 0.0, 1.0);

    vec4 color = vec4((frontFill * mix(FrontFillColour, vec3(0.0), fresnel)) + (backFill * BackFillColour), fill);

    vec4 texColor = texture(Sampler0, texCoord0) * vertexColor;

    fragColor = color * ColorModulator;
}