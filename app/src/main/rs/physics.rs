#pragma version(1)
#pragma rs java_package_name(co.adrianblan.fastbrush)
#pragma rs_fp_relaxed

// Script globals
float BRUSH_LENGTH;
float SEGMENTS_PER_BRISTLE;

float3 brushPosition;

float3 bristleTop;
float3 bristleBottom;
float bristleLength;

rs_script script;

void init() {
}

void root(uchar4 *out, uint32_t x) {
}

void compute (rs_allocation out) {
    rs_allocation inIgnored;
    rsForEach(script, inIgnored, out);
}
