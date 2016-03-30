#pragma version(1)
#pragma rs java_package_name(co.adrianblan.fastbrush)
#pragma rs_fp_relaxed

// Script globals
float BRUSH_BASE_LENGTH;
float SEGMENTS_PER_BRISTLE;

float* inBristlePositionTop;
float* inBristlePositionBottom;
float* outBristlePosition;

typedef struct ComputeParameters {
    float brushPositionx;
    float brushPositiony;
    float brushPositionz;

    float planarDistanceFromHandle;
    float upperControlPointLength;
    float lowerControlPointLength;

    float cosHorizontalAngle;
    float sinHorizontalAngle;
} ComputeParameters_t;

ComputeParameters_t* computeParameters;

rs_script script;

void init() {
}

void root(uchar4 *in, uint32_t x) {

    int outIndex = x * 2 * 3 * SEGMENTS_PER_BRISTLE;

    float3 bristlePositionTop;
    bristlePositionTop.x = inBristlePositionTop[x * 3] + computeParameters->brushPositionx;
    bristlePositionTop.y = inBristlePositionTop[x * 3 + 1] + computeParameters->brushPositiony;
    bristlePositionTop.z = inBristlePositionTop[x * 3 + 2] + computeParameters->brushPositionz;

    float3 bristlePositionBottom;
    bristlePositionBottom.x = inBristlePositionBottom[x * 3] + computeParameters->brushPositionx;
    bristlePositionBottom.y = inBristlePositionBottom[x * 3 + 1] + computeParameters->brushPositiony;
    bristlePositionBottom.z = inBristlePositionBottom[x * 3 + 2] + computeParameters->brushPositionz;

    float bottom = bristlePositionBottom.z;

    if(bottom < 0) {
        bottom = 0;
    }

    float3 interpolatedPosition = bristlePositionTop;
    float scale;
    float firstFactor;
    float secondFactor;
    float thirdFactor;
    float fourthFactor;

    float length = distance(bristlePositionTop, bristlePositionBottom);

    for(int i = 1; i <= SEGMENTS_PER_BRISTLE; i++) {

        outBristlePosition[outIndex] = interpolatedPosition.x;
        outBristlePosition[outIndex + 1] = interpolatedPosition.y;
        outBristlePosition[outIndex + 2] = interpolatedPosition.z;
        outIndex += 3;

        scale = ((float) i / SEGMENTS_PER_BRISTLE) * (length / BRUSH_BASE_LENGTH);
        firstFactor = (1 - scale) * (1 - scale) * (1 - scale);
        secondFactor = 3 * (1 - scale) * (1 - scale) * scale;
        thirdFactor = 3 * (1 - scale) * scale * scale;
        fourthFactor = scale * scale * scale;

        interpolatedPosition.x =
                firstFactor
                        * bristlePositionTop.x
                + secondFactor
                        * (bristlePositionTop.x - (bristlePositionTop.x - bristlePositionBottom.x)
                        * computeParameters->upperControlPointLength)
                + thirdFactor
                        * (bristlePositionBottom.x
                        + computeParameters->cosHorizontalAngle * computeParameters->planarDistanceFromHandle
                        - computeParameters->cosHorizontalAngle * computeParameters->lowerControlPointLength)
                + fourthFactor
                        * (bristlePositionBottom.x
                        + computeParameters->cosHorizontalAngle * computeParameters->planarDistanceFromHandle);

        interpolatedPosition.y =
                firstFactor
                    * bristlePositionTop.y
                + secondFactor
                    * (bristlePositionTop.y - (bristlePositionTop.y - bristlePositionBottom.y)
                    * computeParameters->upperControlPointLength)
                + thirdFactor
                    * (bristlePositionBottom.y
                    + computeParameters->sinHorizontalAngle * computeParameters->planarDistanceFromHandle
                    - computeParameters->sinHorizontalAngle * computeParameters->lowerControlPointLength)
                + fourthFactor
                    * (bristlePositionBottom.y
                    + computeParameters->sinHorizontalAngle * computeParameters->planarDistanceFromHandle);

        interpolatedPosition.z =
                firstFactor
                    * bristlePositionTop.z
                + secondFactor
                    * (bristlePositionTop.z - (bristlePositionTop.z - bottom)
                    * computeParameters->upperControlPointLength)
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
