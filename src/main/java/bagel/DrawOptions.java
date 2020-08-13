package bagel;

import bagel.util.Colour;

import java.util.Optional;

/**
 * Allows you to specify detailed options for drawing images.
 * Used with {@link bagel.Image#draw(double, double, DrawOptions)}.
 *
 * @author Eleanor McMurtry
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class DrawOptions {
    private Optional<Float> xscale = Optional.empty();
    private Optional<Float> yscale = Optional.empty();
    private Optional<Float> xOffset = Optional.empty();
    private Optional<Float> yOffset = Optional.empty();
    private Optional<Float> xMax = Optional.empty();
    private Optional<Float> yMax = Optional.empty();
    private Optional<Float> rotation = Optional.empty();
    private Optional<Float> rBlend = Optional.empty();
    private Optional<Float> gBlend = Optional.empty();
    private Optional<Float> bBlend = Optional.empty();
    private Optional<Float> aBlend = Optional.empty();
    private Shader shader = DefaultShader.get();

    /**
     * Set the scale of the image. A value of 1.0 results in no stretching.
     */
    public DrawOptions setScale(double xscale, double yscale) {
        this.xscale = Optional.of((float) xscale);
        this.yscale = Optional.of((float) yscale);
        return this;
    }

    /**
     * Set the rotation of the image in the usual mathematical sense, measured in radians.
     */
    public DrawOptions setRotation(double rotation) {
        this.rotation = Optional.of((float) rotation);
        return this;
    }

    /**
     * Sets the colour to blend the image with, where (0.0, 0.0, 0.0) is black and (1.0, 1.0, 1.0) is white.
     */
    public DrawOptions setBlendColour(double r, double g, double b) {
        rBlend = Optional.of((float) r);
        gBlend = Optional.of((float) g);
        bBlend = Optional.of((float) b);
        return this;
    }

    /**
     * Sets the colour to blend the image with, where (0.0, 0.0, 0.0) is black and (1.0, 1.0, 1.0) is white.
     * @param a the alpha (transparency ratio) to use
     */
    public DrawOptions setBlendColour(double r, double g, double b, double a) {
        rBlend = Optional.of((float) r);
        gBlend = Optional.of((float) g);
        bBlend = Optional.of((float) b);
        aBlend = Optional.of((float) a);
        return this;
    }

    /**
     * Sets the colour to blend the image with, using the {@link Colour} class.
     */
    public DrawOptions setBlendColour(Colour colour) {
        rBlend = Optional.of((float) colour.r);
        gBlend = Optional.of((float) colour.g);
        bBlend = Optional.of((float) colour.b);
        aBlend = Optional.of((float) colour.a);
        return this;
    }

    /**
     * Sets only a section of the image to be drawn. The subimage will be drawn from (xBegin, yBegin) to
     * (xBegin + width, yBegin + height);
     */
    public DrawOptions setSection(double xBegin, double yBegin, double width, double height) {
        xOffset = Optional.of((float) xBegin);
        yOffset = Optional.of((float) yBegin);
        xMax = Optional.of((float) (xBegin + width));
        yMax = Optional.of((float) (yBegin + height));
        return this;
    }

    /**
     * <b>For very advanced users only.</b>
     *
     * Set the shader to use for this rendering job. Setting up uniforms is your problem.
     */
    public DrawOptions setShader(Shader shader) {
        this.shader = shader;
        return this;
    }

    RenderInfo toRenderInfo(Texture tex, float x, float y) {
        return new RenderInfo(tex, x - xOffset.orElse(0f), y - yOffset.orElse(0f),
                xOffset.orElse(0f) / tex.w, yOffset.orElse(0f) / tex.h,
                xMax.orElse((float) tex.w) / tex.w, yMax.orElse((float) tex.h) / tex.h,
                xscale.orElse(1f), yscale.orElse(1f),
                rotation.orElse(0f),
                rBlend.orElse(1f), gBlend.orElse(1f), bBlend.orElse(1f), aBlend.orElse(1f),
                shader);
    }
}
