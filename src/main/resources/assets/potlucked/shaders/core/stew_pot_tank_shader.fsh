// ENHANCED 2D liquid inside 'sphere' shader.

// Author: Mirza Beig

// https://twitter.com/TheMirzaBeig
// https://www.youtube.com/@MirzaBeig

// Feel free to use this however you want.
// Modify, learn from it, copy-paste, etc...

// Modified by Azzyberries/misterslime/auberries.

#version 150

#define PI 3.14159
#define antialiasedstep(b, a) smoothstep(a, a - (fwidth(b) * 2.0), b)
#define easeInCirc(a) 1. - sqrt(1. - pow(a, 2.))

uniform sampler2D Sampler0;

uniform float uTime;

uniform vec4 ColorModulator;
uniform vec3 FillColour;
uniform vec2 ImageResolution;
uniform float FillAnimationFrequency;
uniform float FillAnimationAmplitude;
uniform float FillAnimationMultiplier;
uniform float WavePhase;
uniform float WaveFrequency;
uniform float WaveAmplitude;
uniform float WaveAnimation;
uniform float AnimatedVerticalBandStrength;
uniform float FillProgressAnimationFrequency;
uniform float FillProgressAnimationAmplitude;
uniform float FresnelPower;
uniform float FresnelOffset;
uniform float DrawAlpha;
uniform float FillProgress;

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

void main() {

    // Pixelation.
    vec2 modUv = floor(texCoord0 * ImageResolution) / ImageResolution;

    // Centered, aspect ratio-corrected, normalized pixel coordinates [0.0, 1.0].
    vec2 uv = (modUv - 0.5) * 2.0;

    // Flip vertically.
    uv.y *= -1.0;


    // Pixelation.
    //uv = floor(uv * ImageResolution) / ImageResolution;

    // Progress/fill. Animated.
    float fillAnimation = sin(uTime * FillAnimationFrequency) * FillAnimationAmplitude; // for back wave set FillAnimationAmplitude to a negative number

    // Waves as repeating sine/band offsets to the horizontal gradient.
    float wavePhase = (uTime * WaveAnimation) + uv.x; // for back wave set WaveAnimation to a negative number
    float waves = sin(wavePhase * WaveFrequency + WavePhase) * WaveAmplitude; // for back wave set WavePhase to PI

    float verticalBand = sin(uv.x + (PI * 0.5)) - 0.3;
    verticalBand = smoothstep(0.2, 1.2, verticalBand);

    // Stretch waves up/down near center, synced as they bob up/down.
    float animatedVerticalBand = verticalBand * AnimatedVerticalBandStrength; // for back wave set AnimatedVerticalBandStrength to a negative number

    animatedVerticalBand *= sin(uTime * FillAnimationFrequency);

    waves += animatedVerticalBand;

    // Pinch sides (mask by the vertical gradient band) so they don't move
    fillAnimation *= verticalBand * FillAnimationMultiplier; // set FillAnimationMultiplier to -1 or 1 depending on which side the layer is

    // Centered fill progress.
    float progress = FillProgress + sin((uTime * FillProgressAnimationFrequency) * PI) * FillProgressAnimationAmplitude;
    progress = (progress - 0.5) * 2.0;

    float fill = antialiasedstep(uv.y, (fillAnimation + waves) + progress);
    fill = clamp(fill, 0.0, 1.0);

    // Apply fresnel shading to the front layer to imply a cylindrical shape.
    float fresnel = (easeInCirc(abs(uv.x)) + FresnelOffset) * fill; // set FresnelOffset to -1 to remove the fresnel
    fresnel = clamp(pow(fresnel, FresnelPower), 0.0, 1.0);

    vec4 color = vec4(fill * mix(FillColour, vec3(0.0), fresnel), fill * DrawAlpha);

    if (fill == 0.0) {
        discard;
    }

    vec4 texColor = texture(Sampler0, texCoord0) * vertexColor;

    fragColor = color * ColorModulator;
}