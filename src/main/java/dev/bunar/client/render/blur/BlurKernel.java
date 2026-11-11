package dev.bunar.client.render.blur;

/** Immutable normalized Gaussian weights. No OpenGL or Minecraft dependency. */
public final class BlurKernel {
    public static final int RADIUS = 6;
    private final float[] weights;
    private BlurKernel(float[] weights) { this.weights = weights; }
    public static BlurKernel gaussian(float sigma) {
        if (!Float.isFinite(sigma) || sigma <= 0) throw new IllegalArgumentException("sigma must be positive and finite");
        float[] weights = new float[RADIUS + 1];
        double sum = 0;
        for (int i = 0; i <= RADIUS; i++) { weights[i] = (float) Math.exp(-(i * i) / (2.0 * sigma * sigma)); sum += weights[i] * (i == 0 ? 1 : 2); }
        for (int i = 0; i <= RADIUS; i++) weights[i] /= (float) sum;
        return new BlurKernel(weights);
    }
    public float[] weights() { return weights.clone(); }
}
