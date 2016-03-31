package co.adrianblan.fastbrush;

import android.content.Context;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;

import co.adrianblan.fastbrush.database.BristleParameters;
import co.adrianblan.fastbrush.globject.Bristle;
import co.adrianblan.fastbrush.globject.Brush;


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
    private ScriptField_ComputeParameters.Item computeParameterItem;


    private Brush brush;

    float [] out;
    float [] inTop;
    float [] inBottom;

    public PhysicsCompute(Context context, Brush brush) {

        this.brush = brush;
        Bristle[] bristleArray = brush.getBristles();

        renderScript = RenderScript.create(context);
        script = new ScriptC_physics(renderScript);

        script.set_BRUSH_BASE_LENGTH(Bristle.BASE_LENGTH);
        script.set_SEGMENTS_PER_BRISTLE(Brush.SEGMENTS_PER_BRISTLE);
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

        inAllocationTop = Allocation.createSized(renderScript, Element.F32(renderScript), 3 * numBristles, Allocation.USAGE_SCRIPT);
        inAllocationBottom = Allocation.createSized(renderScript, Element.F32(renderScript), 3 * numBristles, Allocation.USAGE_SCRIPT);

        inAllocationTop.copyFrom(inTop);
        inAllocationBottom.copyFrom(inBottom);

        outAllocation = Allocation.createSized(renderScript, Element.F32(renderScript), 3 * 2 * numSegments * numBristles, Allocation.USAGE_SCRIPT);
        out = new float[3 * 2 * numSegments * numBristles];

        computeParameters = new ScriptField_ComputeParameters(renderScript, 1);
        computeParameterItem = new ScriptField_ComputeParameters.Item();

        script.bind_inBristlePositionTop(inAllocationTop);
        script.bind_inBristlePositionBottom(inAllocationBottom);
        script.bind_outBristlePosition(outAllocation);
        script.bind_computeParameters(computeParameters);
    }

    public float[] computeVertexData() {

        //Set all parameters for compute script
        computeParameterItem.brushPositionx = brush.getPosition().vector[0];
        computeParameterItem.brushPositiony = brush.getPosition().vector[1];
        computeParameterItem.brushPositionz = brush.getPosition().vector[2];

        BristleParameters bristleParameters = brush.getBristleParameters();

        computeParameterItem.planarDistanceFromHandle = bristleParameters.planarDistanceFromHandle;
        computeParameterItem.upperControlPointLength = bristleParameters.upperControlPointLength;
        computeParameterItem.lowerControlPointLength = bristleParameters.lowerControlPointLength;

        computeParameterItem.cosHorizontalAngle = (float) Math.cos(Math.toRadians(brush.getHorizontalAngle()));
        computeParameterItem.sinHorizontalAngle = (float) Math.sin(Math.toRadians(brush.getHorizontalAngle()));

        computeParameters.set(computeParameterItem, 0, true);

        computeParameters.copyAll();

        // Computes all positions
        script.invoke_compute(inBristleIndices);

        // Wait for script to complete before continuing
        renderScript.finish();

        outAllocation.copyTo(out);
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
