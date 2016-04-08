#pragma version(1)
#pragma rs java_package_name(co.adrianblan.fastbrush)
#pragma rs_fp_relaxed

// Script globals
float BRISTLE_BASE_LENGTH;
float SEGMENTS_PER_BRISTLE;
float BRUSH_RADIUS_UPPER;

float3 brushPosition;

float planarDistanceFromHandle;
float middlePathUpperControlPointLength;
float middlePathLowerControlPointLength;

// The angle of the brush rotation, and the maximum bristle spread angle (in radians)
float brushHorizontalAngle;
float bristleHorizontalMaxAngle;

float* inBristlePositionTop;
float* inBristlePositionBottom;
float* outBristlePosition;

rs_script script;

void init() {}

void root(uchar4 *in, uint32_t x) {

    int outIndex = x * 2 * 3 * SEGMENTS_PER_BRISTLE;

    float3 bristlePositionTop;
    bristlePositionTop.x = inBristlePositionTop[x * 3];
    bristlePositionTop.y = inBristlePositionTop[x * 3 + 1];
    bristlePositionTop.z = inBristlePositionTop[x * 3 + 2];
    bristlePositionTop += brushPosition;

    float3 bristlePositionBottom;
    bristlePositionBottom.x = inBristlePositionBottom[x * 3];
    bristlePositionBottom.y = inBristlePositionBottom[x * 3 + 1];
    bristlePositionBottom.z = inBristlePositionBottom[x * 3 + 2];
    bristlePositionBottom += brushPosition;

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

    // Degree the brush vector is offset by
    float orthogonalOffset = M_PI_4;

    // A vector which points to the orthogonal angle to where the brush is pointing
    float2 brushOrthogonalAngleVector;
    brushOrthogonalAngleVector.x = cos(brushHorizontalAngle + orthogonalOffset);
    brushOrthogonalAngleVector.y = sin(brushHorizontalAngle + orthogonalOffset);
    brushOrthogonalAngleVector = normalize(brushOrthogonalAngleVector);

    float bristleShiftMagnitude = dot(brushOrthogonalAngleVector, bristleVector);

    float bristleAngleShift = bristleShiftMagnitude * bristleHorizontalMaxAngle;

    float sinBrushHorizontalValue = sin(brushHorizontalAngle + bristleAngleShift);
    float cosBrushHorizontalValue = cos(brushHorizontalAngle + bristleAngleShift);

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
                * middlePathUpperControlPointLength)
            + thirdFactor
                * (bristlePositionBottom.x
                    + cosBrushHorizontalValue * (planarDistanceFromHandle - middlePathLowerControlPointLength))
            + fourthFactor
                * (bristlePositionBottom.x + cosBrushHorizontalValue * planarDistanceFromHandle);

        interpolatedPosition.y =
            firstFactor
                * bristlePositionTop.y
            + secondFactor
                * (bristlePositionTop.y - (bristlePositionTop.y - bristlePositionBottom.y)
                * middlePathUpperControlPointLength)
            + thirdFactor
                * (bristlePositionBottom.y
                    + sinBrushHorizontalValue * (planarDistanceFromHandle - middlePathLowerControlPointLength))
            + fourthFactor
                * (bristlePositionBottom.y + sinBrushHorizontalValue * planarDistanceFromHandle);

        interpolatedPosition.z =
            firstFactor
                * bristlePositionTop.z
            + secondFactor
                * (bristlePositionTop.z - (bristlePositionTop.z - bottom)
                * middlePathUpperControlPointLength)
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

void compute (rs_allocation in) {
    rs_allocation outIgnored;
    rsForEach(script, in, outIgnored);
}
