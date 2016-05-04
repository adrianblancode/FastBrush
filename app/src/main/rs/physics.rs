#pragma version(1)
#pragma rs java_package_name(co.adrianblan.fastbrush)
#pragma rs_fp_relaxed

// Script globals
float BRISTLE_BASE_LENGTH;
float SEGMENTS_PER_BRISTLE;
float BRUSH_RADIUS_UPPER;

typedef struct ComputeParameters {

    // xyz?
    float3 brushPosition;

    float upperPathUpperControlPointLength;
    float upperPathLowerControlPointLength;
    float middlePathUpperControlPointLength;
    float middlePathLowerControlPointLength;
    float lowerPathUpperControlPointLength;
    float lowerPathLowerControlPointLength;

    float upperPathDistanceFromHandle;
    float middlePathDistanceFromHandle;
    float lowerPathDistanceFromHandle;

    // The angle of the brush rotation, and the maximum bristle spread angle (in radians)
    float brushHorizontalAngle;
    float bristleHorizontalMaxAngle;

} ComputeParameters_t;

ComputeParameters_t* computeParameters;

float* inBristlePositionTop;
float* inBristlePositionBottom;
float* outBristlePosition;

rs_script script;

void init() {}

static float interpolate(float, float, float, float);

void root(uchar4 *in, uint32_t x) {

    int outIndex = x * 2 * 3 * SEGMENTS_PER_BRISTLE;

    float3 bristlePositionTop;
    bristlePositionTop.x = inBristlePositionTop[x * 3];
    bristlePositionTop.y = inBristlePositionTop[x * 3 + 1];
    bristlePositionTop.z = inBristlePositionTop[x * 3 + 2];
    bristlePositionTop += computeParameters->brushPosition;

    float3 bristlePositionBottom;
    bristlePositionBottom.x = inBristlePositionBottom[x * 3];
    bristlePositionBottom.y = inBristlePositionBottom[x * 3 + 1];
    bristlePositionBottom.z = inBristlePositionBottom[x * 3 + 2];
    bristlePositionBottom += computeParameters->brushPosition;

    float bristleLength = fast_distance(bristlePositionTop, bristlePositionBottom);

    float bristleHorizontalRatio =
        inBristlePositionTop[x * 3] / BRUSH_RADIUS_UPPER;
    float bristleVerticalRatio =
        inBristlePositionTop[x * 3 + 1] / BRUSH_RADIUS_UPPER;

    // A vector which points to the bristle position
    float2 bristleVector;
    bristleVector.x = bristleHorizontalRatio;
    bristleVector.y = bristleVerticalRatio;
    bristleVector = normalize(bristleVector);

    // A vector which points to the orthogonal angle to where the brush is pointing
    float2 brushVector;
    brushVector.x = cos(computeParameters->brushHorizontalAngle);
    brushVector.y = sin(computeParameters->brushHorizontalAngle);
    brushVector = normalize(brushVector);

    float2 brushOrthogonalVector;
    brushOrthogonalVector.x = cos(computeParameters->brushHorizontalAngle + M_PI_4);
    brushOrthogonalVector.y = sin(computeParameters->brushHorizontalAngle + M_PI_4);
    brushOrthogonalVector = normalize(brushOrthogonalVector);

    // The alignment is how well the bristle aligns
    float bristleAlignmentMagnitude = dot(brushVector, bristleVector);
    float bristleAlignmentMagnitudeNormalized = ((bristleAlignmentMagnitude + 1) / 2.0f);

    float bristleShiftMagnitude = dot(brushOrthogonalVector, bristleVector);
    float bristleAngleShift = bristleShiftMagnitude * computeParameters->bristleHorizontalMaxAngle;

    float sinBristleHorizontalValue = sin(computeParameters->brushHorizontalAngle + bristleAngleShift);
    float cosBristleHorizontalValue = cos(computeParameters->brushHorizontalAngle + bristleAngleShift);

    float upperControlPointLength = interpolate(bristleAlignmentMagnitudeNormalized,
        computeParameters->lowerPathUpperControlPointLength, computeParameters->middlePathUpperControlPointLength, computeParameters->upperPathUpperControlPointLength);

    float lowerControlPointLength = interpolate(bristleAlignmentMagnitudeNormalized,
        computeParameters->lowerPathLowerControlPointLength, computeParameters->middlePathLowerControlPointLength, computeParameters->upperPathLowerControlPointLength);

    float pathDistanceFromHandle = interpolate(bristleAlignmentMagnitudeNormalized,
        computeParameters->lowerPathDistanceFromHandle, computeParameters->middlePathDistanceFromHandle, computeParameters->upperPathDistanceFromHandle);

    // Takes positive bottom positions
    float bottom = fmax(bristlePositionBottom.z, 0);

    float3 interpolatedPosition = bristlePositionTop;
    float scale;
    float firstFactor;
    float secondFactor;
    float thirdFactor;
    float fourthFactor;

    for(int i = 1; i <= SEGMENTS_PER_BRISTLE; i++) {

        outBristlePosition[outIndex] = interpolatedPosition.x;
        outBristlePosition[outIndex + 1] = interpolatedPosition.y;
        outBristlePosition[outIndex + 2] = interpolatedPosition.z;
        outIndex += 3;

        scale = ((float) i / SEGMENTS_PER_BRISTLE) * (bristleLength / BRISTLE_BASE_LENGTH);
        firstFactor = (1 - scale) * (1 - scale) * (1 - scale);
        secondFactor = 3 * (1 - scale) * (1 - scale) * scale;
        thirdFactor = 3 * (1 - scale) * scale * scale;
        fourthFactor = scale * scale * scale;

        // We do not perform any "rotation", we simply choose the extended bristles from their angle
        interpolatedPosition.x =
            firstFactor
                * bristlePositionTop.x
            + secondFactor
                * (bristlePositionTop.x - (bristlePositionTop.x - bristlePositionBottom.x)
                * upperControlPointLength)
            + thirdFactor
                * (bristlePositionBottom.x
                    + cosBristleHorizontalValue * (pathDistanceFromHandle - lowerControlPointLength))
            + fourthFactor
                * (bristlePositionBottom.x + cosBristleHorizontalValue * pathDistanceFromHandle);

        interpolatedPosition.y =
            firstFactor
                * bristlePositionTop.y
            + secondFactor
                * (bristlePositionTop.y - (bristlePositionTop.y - bristlePositionBottom.y)
                * upperControlPointLength)
            + thirdFactor
                * (bristlePositionBottom.y
                    + sinBristleHorizontalValue * (pathDistanceFromHandle - lowerControlPointLength))
            + fourthFactor
                * (bristlePositionBottom.y + sinBristleHorizontalValue * pathDistanceFromHandle);

        interpolatedPosition.z =
            firstFactor
                * bristlePositionTop.z
            + secondFactor
                * (bristlePositionTop.z - (bristlePositionTop.z - bottom)
                * upperControlPointLength)
            + thirdFactor
                * bottom
            + fourthFactor
                * bottom;

        outBristlePosition[outIndex] = interpolatedPosition.x;
        outBristlePosition[outIndex + 1] = interpolatedPosition.y;
        outBristlePosition[outIndex + 2] = interpolatedPosition.z;
        outIndex += 3;
    }
}

// Interpolates between three values based on a scale
static float interpolate(float scale, float firstValue, float secondValue, float thirdValue) {
    return
        (1 - scale) * (1 - scale) * firstValue
        + 2 * (1 - scale) * scale * secondValue
        + scale * scale * thirdValue;
}

void compute (rs_allocation in) {
    rs_allocation outIgnored;
    rsForEach(script, in, outIgnored);
}
