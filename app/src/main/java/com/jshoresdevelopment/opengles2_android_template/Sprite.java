package com.jshoresdevelopment.opengles2_android_template;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.opengl.GLUtils;
import android.util.Log;
import lombok.Getter;
import lombok.Setter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

@Getter
@Setter
public class Sprite {
    private float xIncrement = 0, yIncrement = 0;
    private float centerX = 0, centerY = 0;
    private float rotationSpeed = 0;
    private float rotationPosition = 0;
    private float radius = 0;
    private float speedMultiplier = 1.0f;
    private int angle = 0;
    private int initialAngle = 0;
    private double angleIncrement = 0;

    // Translate params.
    private float x = 0;
    private float y = 0;
    private float z = 0;
    private float initialX = 0;
    private float initialY = 0;
    private float initialCenterX = 0;
    private float initialCenterY = 0;

    // Rotate params.
    private float rx = 0;
    private float ry = 0;
    private float rz = 0;

    private FloatBuffer verticesBuffer = null;
    private ShortBuffer indicesBuffer = null;
    private FloatBuffer colorBuffer = null;
    private FloatBuffer mTextureBuffer;

    // Our texture id.
    private int mTextureId = -1;
    private int numOfIndices = -1;

    private float[] rgba = new float[]{1.0f, 1.0f, 1.0f, 1.0f};
    private float[] vertices;

    // The bitmap we want to load as a texture.
    private Bitmap objectImage;
    private boolean mShouldLoadTexture = false;

    public Sprite() {
        // Mapping coordinates for the vertices
        float textureCoordinates[] = { 0.0f, 1.0f, //
                1.0f, 1.0f, //
                0.0f, 0.0f, //
                1.0f, 0.0f, //
        };

        short[] indices = new short[] { 0, 1, 2, 1, 3, 2 };

        vertices = new float[] {
                -0.25f, -0.5f, 0.0f,
                0.25f, -0.5f, 0.0f,
                -0.25f,  0.5f, 0.0f,
                0.25f, 0.5f, 0.0f };

        setIndices(indices);
        setVertices(vertices);
        setTextureCoordinates(textureCoordinates);
    }

    public void moveObject() {
        rz += rotationSpeed;
        if (radius == 0) {
            moveObjectLinear();
        } else {
            moveObjectCurve();
        }
    }

    public void moveObjectLinear() {
        x += xIncrement;
        y += yIncrement;
    }

    private void moveObjectCurve() {
        x = (float) (centerX + (radius * Math.cos(Math.toRadians(angle))));
        y = (float) (centerY + (radius * Math.sin(Math.toRadians(angle))));
        angle += angleIncrement;
        centerX += xIncrement;
        centerY += yIncrement;
    }

    /** Set the bitmap to load into a texture. */
    public void loadBitmap(Bitmap bitmap) {
        this.objectImage = bitmap;
        mShouldLoadTexture = true;
    }

    /** Loads the texture. */
    private void loadGLTexture(GL10 gl) {
        setColor(0.5f, 0.5f, 0.5f, 0.0f);
        // Generate one texture pointer...
        int[] textures = new int[1];
        gl.glGenTextures(1, textures, 0);
        mTextureId = textures[0];

        // ...and bind it to our array
        gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureId);

        // Create Nearest Filtered Texture
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_NEAREST);

        // Different possible texture parameters, e.g. GL10.GL_CLAMP_TO_EDGE
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

        // Use the Android GLUtils to specify a two-dimensional texture image
        // from our bitmap
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, objectImage, 0);
    }

    /** Set the texture coordinates. */
    protected void setTextureCoordinates(float[] textureCoords) {
        ByteBuffer byteBuf = ByteBuffer.allocateDirect(textureCoords.length * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        mTextureBuffer = byteBuf.asFloatBuffer();
        mTextureBuffer.put(textureCoords);
        mTextureBuffer.position(0);
    }

    public void draw(GL10 gl) {
        gl.glEnable(GL10.GL_BLEND);
        // Counter-clockwise winding.
        gl.glFrontFace(GL10.GL_CCW);
        // Enable face culling.
        gl.glEnable(GL10.GL_CULL_FACE);
        // What faces to remove with the face culling.
        gl.glCullFace(GL10.GL_BACK);
        // Enabled the vertices buffer for writing and to be used during
        // rendering.
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        // Specifies the location and data format of an array of vertex
        // coordinates to use when rendering.
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, verticesBuffer);

        if (mShouldLoadTexture) {
            loadGLTexture(gl);
            mShouldLoadTexture = false;
        }

        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

        if (mTextureId != -1 && mTextureBuffer != null) {
            gl.glEnable(GL10.GL_TEXTURE_2D);
            // Enable the texture state
            gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

            // Point to our buffers
            gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mTextureBuffer);
            gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureId);
        }

        gl.glTranslatef(x, y, z);
        gl.glRotatef(rx, 1, 0, 0);
        gl.glRotatef(ry, 0, 1, 0);
        gl.glRotatef(rz, 0, 0, 1);

        // Point out the where the color buffer is.
        gl.glDrawElements(GL10.GL_TRIANGLES, numOfIndices, GL10.GL_UNSIGNED_SHORT, indicesBuffer);

        if (mTextureId != -1 && mTextureBuffer != null) {
            gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        }

        // Disable the vertices buffer.
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        // Disable face culling.
        gl.glDisable(GL10.GL_CULL_FACE);
    }

    protected void setVertices(float[] vertices) {
        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
        vbb.order(ByteOrder.nativeOrder());
        verticesBuffer = vbb.asFloatBuffer();
        verticesBuffer.put(vertices);
        verticesBuffer.position(0);
    }

    protected void setIndices(short[] indices) {
        ByteBuffer ibb = ByteBuffer.allocateDirect(indices.length * 2);
        ibb.order(ByteOrder.nativeOrder());
        indicesBuffer = ibb.asShortBuffer();
        indicesBuffer.put(indices);
        indicesBuffer.position(0);
        numOfIndices = indices.length;
    }

    protected void setColor(float red, float green, float blue, float alpha) {
        rgba[0] = red;
        rgba[1] = green;
        rgba[2] = blue;
        rgba[3] = alpha;
    }

    protected void setColors(float[] colors) {
        ByteBuffer cbb = ByteBuffer.allocateDirect(colors.length * 4);
        cbb.order(ByteOrder.nativeOrder());
        colorBuffer = cbb.asFloatBuffer();
        colorBuffer.put(colors);
        colorBuffer.position(0);
    }

    public void setX(float x) {
        this.x = x;
        this.initialX = x;
    }

    public void setY(float y) {
        this.y = y;
        this.initialY = y;
    }

    public void setCenterX(float centerX) {
        this.centerX = centerX;
        this.initialCenterX = centerX;
    }

    public void setCenterY(float centerY) {
        this.centerY = centerY;
        this.initialCenterY = centerY;
    }

    public void setAngle(int angle) {
        this.angle = angle;
        this.initialAngle = angle;
    }

    public void resetObjectPositions() {
        x = initialX;
        y = initialY;
        centerX = initialCenterX;
        centerY = initialCenterY;
        angle = initialAngle;
    }

    public float getSizeX() {
        return vertices[3] / 1.5f;
    }

    public float getSizeY() {
        return  vertices[7] / 1.5f;
    }

    public void setIncrements(float xIncrement, float yIncrement) {
        this.xIncrement = xIncrement;
        this.yIncrement = yIncrement;
    }

}