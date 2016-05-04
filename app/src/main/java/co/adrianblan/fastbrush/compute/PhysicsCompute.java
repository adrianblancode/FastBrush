package co.adrianblan.fastbrush.compute;

import android.content.Context;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.Float3;
import android.support.v8.renderscript.RenderScript;

import co.adrianblan.fastbrush.ScriptC_physics;
import co.adrianblan.fastbrush.ScriptField_ComputeParameters;
import co.adrianblan.fastbrush.database.BristleParameters;
import co.adrianblan.fastbrush.globject.Bristle;
import co.adrianblan.fastbrush.globject.Brush;
import co.adrianblan.fastbrush.vector.Vector3;


/**
 * Class which computes physics with RenderScript
 */
public class PhysicsCompute {

    private RenderScript renderScript;
    private ScriptC_physics script;

    private Allocation inBristleIndices;
    private Allocation inAllocationTop;
    private Allocation inAllocationBottom;
    private Allocation outAllocation;

    private ScriptField_ComputeParameters computeParameters;

    private Brush brush;

    private float [] out;
    private float [] inTop;
    private float [] inBottom;

    public PhysicsCompute(Context context, Brush brush) {

        this.brush = brush;
        Bristle[] bristleArray = brush.getBristles();

        renderScript = RenderScript.create(context);
        script = new ScriptC_physics(renderScript);

        script.set_BRISTLE_BASE_LENGTH(Bristle.BASE_LENGTH);
        script.set_SEGMENTS_PER_BRISTLE(Brush.SEGMENTS_PER_BRISTLE);
        script.set_BRUSH_RADIUS_UPPER(Bristle.radiusUpper);
        script.set_script(script);

        int numBristles = brush.getBristles().length;
        int numSegments = Brush.SEGMENTS_PER_BRISTLE;

        int[] bristleIndices = new int[numBristles];
        for ( int i = 0; i < numBristles; i++) {
            bristleIndices[i] = i * 3;
        }

        inBristleIndices = Allocation.createSized(renderScript, Element.I32(renderScript), numBristles, Allocation.USAGE_SCRIPT | Allocation.USAGE_SHARED);
        inBristleIndices.copyFrom(bristleIndices);

        inTop = new float[3 * numBristles];
        inBottom  = new float[3 * numBristles];

        for(int i = 0; i < bristleArray.length; i++) {
            inTop[i * 3] = bristleArray[i].top.vector[0];
            inTop[i * 3 + 1] = bristleArray[i].top.vector[1];
            inTop[i * 3 + 2] = bristleArray[i].top.vector[2];

            inBottom[i * 3] = bristleArray[i].bottom.vector[0];
            inBottom[i * 3 + 1] = bristleArray[i].bottom.vector[1];
            inBottom[i * 3 + 2] = bristleArray[i].bottom.vector[2];
        }

        inAllocationTop = Allocation.createSized(renderScript, Element.F32(renderScript), 3 * numBristles, Allocation.USAGE_SCRIPT | Allocation.USAGE_SHARED);
        inAllocationBottom = Allocation.createSized(renderScript, Element.F32(renderScript), 3 * numBristles, Allocation.USAGE_SCRIPT | Allocation.USAGE_SHARED);

        inAllocationTop.copyFrom(inTop);
        inAllocationBottom.copyFrom(inBottom);

        outAllocation = Allocation.createSized(renderScript, Element.F32(renderScript), 3 * 2 * numSegments * numBristles, Allocation.USAGE_SCRIPT | Allocation.USAGE_SHARED);
        out = new float[3 * 2 * numSegments * numBristles];

        script.bind_inBristlePositionTop(inAllocationTop);
        script.bind_inBristlePositionBottom(inAllocationBottom);
        script.bind_outBristlePosition(outAllocation);

        computeParameters = new ScriptField_ComputeParameters(renderScript, 1);
        script.bind_computeParameters(computeParameters);
    }

    public float[] computeVertexData() {

        // Set all parameters for compute script
        computeParameters.set_brushPosition(0, brush.getPosition().getFloat3(), false);

        BristleParameters bristleParameters = brush.getBristleParameters();

        computeParameters.set_upperPathUpperControlPointLength(0, bristleParameters.upperPathUpperControlPointLength, false);
        computeParameters.set_upperPathLowerControlPointLength(0, bristleParameters.upperPathLowerControlPointLength, false);
        computeParameters.set_middlePathUpperControlPointLength(0, bristleParameters.middlePathUpperControlPointLength, false);
        computeParameters.set_middlePathLowerControlPointLength(0, bristleParameters.middlePathLowerControlPointLength, false);
        computeParameters.set_lowerPathUpperControlPointLength(0, bristleParameters.lowerPathUpperControlPointLength, false);
        computeParameters.set_lowerPathLowerControlPointLength(0, bristleParameters.lowerPathLowerControlPointLength, false);

        computeParameters.set_upperPathDistanceFromHandle(0, bristleParameters.upperPathDistanceFromHandle, false);
        computeParameters.set_middlePathDistanceFromHandle(0, bristleParameters.middlePathDistanceFromHandle, false);
        computeParameters.set_lowerPathDistanceFromHandle(0, bristleParameters.lowerPathDistanceFromHandle, false);

        computeParameters.set_brushHorizontalAngle(0, (float) Math.toRadians(brush.getHorizontalAngle()), false);
        computeParameters.set_bristleHorizontalMaxAngle(0, (float) Math.toRadians(bristleParameters.bristleHorizontalAngle), false);

        // Copy all parameters at once for reduced overhead
        computeParameters.copyAll();

        // Computes all positions
        script.invoke_compute(inBristleIndices);

        // Wait for script to complete before continuing
        renderScript.finish();

        outAllocation.copyTo(out);
        return out;
    }

    public float[] computeVertexDataSingleThread() {

        float[] positionVector = brush.getPosition().vector;
        float brushAngle = (float) Math.toRadians(brush.getHorizontalAngle());

        // A vector which points to the orthogonal angle to where the brush is pointing
        //float2 brushVector;
        float brushVectorX = (float) Math.cos(brushAngle);
        float brushVectorY = (float) Math.sin(brushAngle);
        //brushVector = normalize(brushVector);

        //float2 brushOrthogonalVector;
        float brushOrthogonalVectorX = (float) Math.cos(brushAngle + Math.PI / 4);
        float brushOrthogonalVectorY = (float) Math.sin(brushAngle + Math.PI / 4);
        //brushOrthogonalVector = normalize(brushOrthogonalVector);

        for(int i = 0; i < brush.numBristles; i++) {

            Bristle bristle = brush.bristles[i];

            BristleParameters bristleParameters = brush.getBristleParameters();
            int outIndex = i * 2 * 3 * Brush.SEGMENTS_PER_BRISTLE;


            float bristlePositionTopX = bristle.top.vector[0] + positionVector[0];
            float bristlePositionTopY = bristle.top.vector[1] + positionVector[1];
            float bristlePositionTopZ = bristle.top.vector[2] + positionVector[2];

            float bristlePositionBottomX = bristle.bottom.vector[0] + positionVector[0];
            float bristlePositionBottomY = bristle.bottom.vector[1] + positionVector[1];
            float bristlePositionBottomZ = bristle.bottom.vector[2] + positionVector[2];

            float bristleLength = bristle.bottom.distance(bristle.top);

            float bristleHorizontalRatio =
                    bristle.top.vector[0] / Bristle.BRUSH_RADIUS_UPPER;
            float bristleVerticalRatio =
                    bristle.top.vector[1] / Bristle.BRUSH_RADIUS_UPPER;

            // A vector which points to the bristle position
            float bristleVectorX = bristleHorizontalRatio;
            float bristleVectorY = bristleVerticalRatio;
            //bristleVector = normalize(bristleVector);


            // dot
            float bristleAlignmentMagnitude = bristlePositionTopX * brushVectorX + bristlePositionTopY * brushVectorY;
            float bristleAlignmentMagnitudeNormalized = ((bristleAlignmentMagnitude + 1) / 2.0f);

            // dot
            float bristleShiftMagnitude = bristlePositionTopX * brushOrthogonalVectorX + bristlePositionTopY * brushOrthogonalVectorY;
            float bristleAngleShift = bristleShiftMagnitude * (float) Math.toRadians(bristleParameters.bristleHorizontalAngle);

            float sinBristleHorizontalValue = (float) Math.sin(brushAngle + bristleAngleShift);
            float cosBristleHorizontalValue = (float) Math.cos(brushAngle + bristleAngleShift);


            float upperControlPointLength = interpolate(bristleAlignmentMagnitudeNormalized,
                    bristleParameters.lowerPathUpperControlPointLength, bristleParameters.middlePathUpperControlPointLength,
                    bristleParameters.upperPathUpperControlPointLength);

            float lowerControlPointLength = interpolate(bristleAlignmentMagnitudeNormalized,
                    bristleParameters.lowerPathLowerControlPointLength, bristleParameters.middlePathLowerControlPointLength,
                    bristleParameters.middlePathLowerControlPointLength);

            float pathDistanceFromHandle = interpolate(bristleAlignmentMagnitudeNormalized,
                    bristleParameters.lowerPathDistanceFromHandle, bristleParameters.middlePathDistanceFromHandle,
                    bristleParameters.upperPathDistanceFromHandle);

            // Takes positive bottom positions
            float bottom = Math.max(bristlePositionBottomZ, 0);

            float scale;
            float firstFactor;
            float secondFactor;
            float thirdFactor;
            float fourthFactor;

            float interpolatedPositionX = positionVector[0];
            float interpolatedPositionY = positionVector[1];
            float interpolatedPositionZ = positionVector[2];

            for (int segment = 1; segment <= Brush.SEGMENTS_PER_BRISTLE; segment++) {

                out[outIndex] = interpolatedPositionX;
                out[outIndex + 1] = interpolatedPositionY;
                out[outIndex + 2] = interpolatedPositionZ;
                outIndex += 3;

                scale = ((float) segment / Brush.SEGMENTS_PER_BRISTLE) * (bristleLength / Brush.SEGMENTS_PER_BRISTLE);
                firstFactor = (1 - scale) * (1 - scale) * (1 - scale);
                secondFactor = 3 * (1 - scale) * (1 - scale) * scale;
                thirdFactor = 3 * (1 - scale) * scale * scale;
                fourthFactor = scale * scale * scale;

                // We do not perform any "rotation", we simply choose the extended bristles from their angle
                interpolatedPositionX =
                        firstFactor
                                * bristlePositionTopX
                                + secondFactor
                                * (bristlePositionTopX - (bristlePositionTopX - bristlePositionBottomX)
                                * upperControlPointLength)
                                + thirdFactor
                                * (bristlePositionBottomX
                                + cosBristleHorizontalValue * (pathDistanceFromHandle - lowerControlPointLength))
                                + fourthFactor
                                * (bristlePositionBottomX + cosBristleHorizontalValue * pathDistanceFromHandle);

                interpolatedPositionY =
                        firstFactor
                                * bristlePositionTopY
                                + secondFactor
                                * (bristlePositionTopY - (bristlePositionTopY - bristlePositionBottomY)
                                * upperControlPointLength)
                                + thirdFactor
                                * (bristlePositionBottomY
                                + sinBristleHorizontalValue * (pathDistanceFromHandle - lowerControlPointLength))
                                + fourthFactor
                                * (bristlePositionBottomY + sinBristleHorizontalValue * pathDistanceFromHandle);

                interpolatedPositionZ =
                        firstFactor
                                * bristlePositionTopZ
                                + secondFactor
                                * (bristlePositionTopZ- (bristlePositionTopZ - bottom)
                                * upperControlPointLength)
                                + thirdFactor
                                * bottom
                                + fourthFactor
                                * bottom;

                out[outIndex] = interpolatedPositionX;
                out[outIndex + 1] = interpolatedPositionY;
                out[outIndex + 2] = interpolatedPositionZ;
                outIndex += 3;
            }

        }

        return out;
    }

    // Interpolates between three values based on a scale
    static float interpolate(float scale, float firstValue, float secondValue, float thirdValue) {
        return
                (1 - scale) * (1 - scale) * firstValue
                        + 2 * (1 - scale) * scale * secondValue
                        + scale * scale * thirdValue;
    }


    // Warning might be uninitialized
    public float[] getCurrentVertexData() {
        return out;
    }

    public void destroy() {
        inAllocationTop.destroy();
        inAllocationBottom.destroy();
        outAllocation.destroy();
        script.destroy();
        renderScript.destroy();
    }
}
