package co.adrianblan.fastbrush;

import android.content.Context;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;

import co.adrianblan.fastbrush.globject.Bristle;


/**
 * Class which computes physics with RenderScript
 */
public class PhysicsCompute {

    private RenderScript mRenderScript;
    private Allocation mInAllocationTop;
    private Allocation mInAllocationBottom;
    private Allocation mOutAllocation;
    private ScriptC_physics script;

    float [] out;

    private void createScript(Context context, int numBristles, int numSegments) {
        mRenderScript = RenderScript.create(context);
        script = new ScriptC_physics(mRenderScript);

        mInAllocationTop = Allocation.createSized(mRenderScript, Element.F32_3(mRenderScript), 2 * numBristles, Allocation.USAGE_SCRIPT);
        mInAllocationBottom = Allocation.createSized(mRenderScript, Element.F32_3(mRenderScript), 2 * numBristles, Allocation.USAGE_SCRIPT);
        mOutAllocation = Allocation.createSized(mRenderScript, Element.F32_3(mRenderScript), 2 * numSegments * numBristles, Allocation.USAGE_SCRIPT);

        out = new float[3 * 2 * numSegments * numBristles];
    }

    public float[] compute(Bristle[] bristleArray) {

        for(int i = 0; i < bristleArray.length; i++) {
            mInAllocationTop.copy1DRangeFrom(0, 3, bristleArray[i].absoluteTop.vector);
            mInAllocationBottom.copy1DRangeFrom(0, 3, bristleArray[i].absoluteBottom.vector);
        }

        script.forEach_root(mOutAllocation);
        mRenderScript.finish();

        mOutAllocation.copyTo(out);
        return out;
    }
}
