package bagel;

/**
 * Represents information about a particular rendering job.
 */
class RenderInfo {
    final Texture tex;
    final float x;
    final float y;
    final float xOffset;
    final float yOffset;
    final float xMax;
    final float yMax;
    final float xScale;
    final float yScale;
    final float rotation;
    final float rBlend;
    final float gBlend;
    final float bBlend;
    final float aBlend;
    final Shader shader;

    RenderInfo(Texture tex,
               float x, float y,
               float xOffset, float yOffset, float xMax, float yMax,
               float xScale, float yScale,
               float rotation,
               float rBlend, float gBlend, float bBlend, float aBlend,
               Shader shader) {
        this.tex = tex;
        this.x = x;
        this.y = y;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.xMax = xMax;
        this.yMax = yMax;
        this.xScale = xScale;
        this.yScale = yScale;
        this.rotation = rotation;
        this.rBlend = rBlend;
        this.gBlend = gBlend;
        this.bBlend = bBlend;
        this.aBlend = aBlend;
        this.shader = shader;
    }

    void render() {
        tex.bind();
        shader.bind();
        shader.render(this);
    }

    @Override
    public String toString() {
        return "RenderInfo: " + tex + " (" + x + ", " + y + ")";
    }
}
