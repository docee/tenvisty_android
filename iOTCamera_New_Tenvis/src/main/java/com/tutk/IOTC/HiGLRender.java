package com.tutk.IOTC;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Administrator on 2017/10/11.
 */


public class HiGLRender implements GLSurfaceView.Renderer {
    int mHeight = 0;
    ByteBuffer mUByteBuffer = null;
    ByteBuffer mVByteBuffer = null;
    int mWidth = 0;
    ByteBuffer mYByteBuffer = null;
    FloatBuffer positionBuffer = null;
    final float[] positionBufferData;
    int positionSlot = 0;
    int programHandle = 0;
    int texRangeSlot = 0;
    int[] texture = new int[3];
    int[] textureSlot = new int[3];
    int vertexShader = 0;
    int yuvFragmentShader = 0;
    byte[] yuvData = null;
    final float[] textCoodBufferData;
    FloatBuffer textCoodBuffer = null;
    boolean bNeedSleep = true;
    private int width;
    private int height;
    private int _x;
    private int _y;

    public void setMatrix(int x, int y, int w, int h) {
        this._x = x;
        this._y = y;
        this.width = w;
        this.height = h;
    }

    public HiGLRender(GLSurfaceView paramGLSurfaceView) {
        float[] arrayOfFloat1 = new float[]{0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F, 0.0F, 1.0F};
        this.textCoodBufferData = arrayOfFloat1;
        float[] arrayOfFloat = new float[]{-1.0F, 1.0F, 0.0F, 1.0F, -1.0F, -1.0F, 0.0F, 1.0F, 1.0F, 1.0F, 0.0F, 1.0F, 1.0F, -1.0F, 0.0F, 1.0F};
        this.positionBufferData = arrayOfFloat;
        paramGLSurfaceView.setEGLContextClientVersion(2);
    }

    public static int compileShader(String paramString, int paramInt) {
        int i = GLES20.glCreateShader(paramInt);
        if(i != 0) {
            int[] compiled = new int[1];
            GLES20.glShaderSource(i, paramString);
            GLES20.glCompileShader(i);
            GLES20.glGetShaderiv(i, GLES20.GL_COMPILE_STATUS, compiled, 0);
            if(compiled[0] == 0) {
                GLES20.glDeleteShader(i);
                i = 0;
            }
        }

        return i;
    }

    public long createShaders() {
        String fragmentShaderCode = "uniform sampler2D Ytex;\n";
        fragmentShaderCode = fragmentShaderCode + "uniform sampler2D Utex;\n";
        fragmentShaderCode = fragmentShaderCode + "uniform sampler2D Vtex;\n";
        fragmentShaderCode = fragmentShaderCode + "precision mediump float;  \n";
        fragmentShaderCode = fragmentShaderCode + "varying vec4 VaryingTexCoord0; \n";
        fragmentShaderCode = fragmentShaderCode + "vec4 color;\n";
        fragmentShaderCode = fragmentShaderCode + "void main()\n";
        fragmentShaderCode = fragmentShaderCode + "{\n";
        fragmentShaderCode = fragmentShaderCode + "float yuv0 = (texture2D(Ytex,VaryingTexCoord0.xy)).r;\n";
        fragmentShaderCode = fragmentShaderCode + "float yuv1 = (texture2D(Utex,VaryingTexCoord0.xy)).r;\n";
        fragmentShaderCode = fragmentShaderCode + "float yuv2 = (texture2D(Vtex,VaryingTexCoord0.xy)).r;\n";
        fragmentShaderCode = fragmentShaderCode + "\n";
        fragmentShaderCode = fragmentShaderCode + "color.r = yuv0 + 1.4022 * yuv2 - 0.7011;\n";
        fragmentShaderCode = fragmentShaderCode + "color.r = (color.r < 0.0) ? 0.0 : ((color.r > 1.0) ? 1.0 : color.r);\n";
        fragmentShaderCode = fragmentShaderCode + "color.g = yuv0 - 0.3456 * yuv1 - 0.7145 * yuv2 + 0.53005;\n";
        fragmentShaderCode = fragmentShaderCode + "color.g = (color.g < 0.0) ? 0.0 : ((color.g > 1.0) ? 1.0 : color.g);\n";
        fragmentShaderCode = fragmentShaderCode + "color.b = yuv0 + 1.771 * yuv1 - 0.8855;\n";
        fragmentShaderCode = fragmentShaderCode + "color.b = (color.b < 0.0) ? 0.0 : ((color.b > 1.0) ? 1.0 : color.b);\n";
        fragmentShaderCode = fragmentShaderCode + "gl_FragColor = color;\n";
        fragmentShaderCode = fragmentShaderCode + "}\n";
        String vertexShaderCode = "uniform mat4 uMVPMatrix;   \n";
        vertexShaderCode = vertexShaderCode + "attribute vec4 vPosition;  \n";
        vertexShaderCode = vertexShaderCode + "attribute vec4 myTexCoord; \n";
        vertexShaderCode = vertexShaderCode + "varying vec4 VaryingTexCoord0; \n";
        vertexShaderCode = vertexShaderCode + "void main(){               \n";
        vertexShaderCode = vertexShaderCode + "VaryingTexCoord0 = myTexCoord; \n";
        vertexShaderCode = vertexShaderCode + "gl_Position = vPosition; \n";
        vertexShaderCode = vertexShaderCode + "}  \n";
        int[] arrayOfInt = new int[1];
        int i = compileShader(vertexShaderCode, '謱');
        this.vertexShader = i;
        if(i == 0) {
            Log.e("createShaders", "failed when compileShader(vertex)");
        }

        int j = compileShader(fragmentShaderCode, '謰');
        this.yuvFragmentShader = j;
        if(j == 0) {
            Log.e("createShaders", "failed when compileShader(fragment)");
        }

        this.programHandle = GLES20.glCreateProgram();
        GLES20.glAttachShader(this.programHandle, this.vertexShader);
        GLES20.glAttachShader(this.programHandle, this.yuvFragmentShader);
        GLES20.glLinkProgram(this.programHandle);
        GLES20.glGetProgramiv(this.programHandle, '讂', arrayOfInt, 0);
        if(arrayOfInt[0] == 0) {
            Log.e("createShaders", "link program err:" + GLES20.glGetProgramInfoLog(this.programHandle));
            this.destroyShaders();
        }

        this.texRangeSlot = GLES20.glGetAttribLocation(this.programHandle, "myTexCoord");
        this.textureSlot[0] = GLES20.glGetUniformLocation(this.programHandle, "Ytex");
        this.textureSlot[1] = GLES20.glGetUniformLocation(this.programHandle, "Utex");
        this.textureSlot[2] = GLES20.glGetUniformLocation(this.programHandle, "Vtex");
        this.positionSlot = GLES20.glGetAttribLocation(this.programHandle, "vPosition");
        return 0L;
    }

    public long destroyShaders() {
        if(this.programHandle != 0) {
            GLES20.glDetachShader(this.programHandle, this.yuvFragmentShader);
            GLES20.glDetachShader(this.programHandle, this.vertexShader);
            GLES20.glDeleteProgram(this.programHandle);
            this.programHandle = 0;
        }

        if(this.yuvFragmentShader != 0) {
            GLES20.glDeleteShader(this.yuvFragmentShader);
            this.yuvFragmentShader = 0;
        }

        if(this.vertexShader != 0) {
            GLES20.glDeleteShader(this.vertexShader);
            this.vertexShader = 0;
        }

        return 0L;
    }

    public int draw(ByteBuffer paramByteBuffer1, ByteBuffer paramByteBuffer2, ByteBuffer paramByteBuffer3, int paramInt1, int paramInt2) {
        GLES20.glClear(16384);
        GLES20.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
        GLES20.glUseProgram(this.programHandle);
        paramByteBuffer1.position(0);
        GLES20.glActiveTexture('蓀');
        this.loadTexture(this.texture[0], paramInt1, paramInt2, paramByteBuffer1);
        paramByteBuffer2.position(0);
        GLES20.glActiveTexture('蓁');
        this.loadTexture(this.texture[1], paramInt1 >> 1, paramInt2 >> 1, paramByteBuffer2);
        paramByteBuffer3.position(0);
        GLES20.glActiveTexture('蓂');
        this.loadTexture(this.texture[2], paramInt1 >> 1, paramInt2 >> 1, paramByteBuffer3);
        GLES20.glUniform1i(this.textureSlot[0], 0);
        GLES20.glUniform1i(this.textureSlot[1], 1);
        GLES20.glUniform1i(this.textureSlot[2], 2);
        this.positionBuffer.position(0);
        GLES20.glEnableVertexAttribArray(this.positionSlot);
        GLES20.glVertexAttribPointer(this.positionSlot, 4, 5126, false, 0, this.positionBuffer);
        this.textCoodBuffer.position(0);
        GLES20.glEnableVertexAttribArray(this.texRangeSlot);
        GLES20.glVertexAttribPointer(this.texRangeSlot, 4, 5126, false, 0, this.textCoodBuffer);
        GLES20.glDrawArrays(5, 0, 4);
        GLES20.glDisableVertexAttribArray(this.positionSlot);
        GLES20.glDisableVertexAttribArray(this.texRangeSlot);
        return 0;
    }

    public int loadTexture(int paramInt1, int paramInt2, int paramInt3, Buffer paramBuffer) {
        GLES20.glBindTexture(3553, paramInt1);
        GLES20.glTexParameteri(3553, 10241, 9729);
        GLES20.glTexParameteri(3553, 10240, 9729);
        GLES20.glTexParameteri(3553, 10242, '脯');
        GLES20.glTexParameteri(3553, 10243, '脯');
        GLES20.glTexImage2D(3553, 0, 6409, paramInt2, paramInt3, 0, 6409, 5121, paramBuffer);
        return 0;
    }

    public int loadVBOs() {
        this.textCoodBuffer = ByteBuffer.allocateDirect(4 * this.textCoodBufferData.length).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.textCoodBuffer.put(this.textCoodBufferData).position(0);
        this.positionBuffer = ByteBuffer.allocateDirect(4 * this.positionBufferData.length).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.positionBuffer.put(this.positionBufferData).position(0);
        return 0;
    }

    public void onDrawFrame(GL10 paramGL10) {
        GLES20.glClear(16384);
        synchronized(this) {
            if(this.mWidth != 0 && this.mHeight != 0 && this.mYByteBuffer != null && this.mUByteBuffer != null && this.mVByteBuffer != null) {
                if(this.bNeedSleep) {
                    try {
                        Thread.sleep(10L);
                    } catch (InterruptedException var4) {
                        var4.printStackTrace();
                    }
                }

                this.bNeedSleep = true;
                paramGL10.glViewport(this._x, this._y, this.width, this.height);
                this.draw(this.mYByteBuffer, this.mUByteBuffer, this.mVByteBuffer, this.mWidth, this.mHeight);
            }
        }
    }

    public void onSurfaceChanged(GL10 paramGL10, int paramInt1, int paramInt2) {
        this.width = paramInt1;
        this.height = paramInt2;
        paramGL10.glViewport(this._x, this._y, paramInt1, paramInt2);
    }

    public void onSurfaceCreated(GL10 paramGL10, EGLConfig paramEGLConfig) {
        GLES20.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
        GLES20.glGenTextures(3, this.texture, 0);
        this.createShaders();
        this.loadVBOs();
    }

    public int unloadVBOs() {
        if(this.positionBuffer != null) {
            this.positionBuffer = null;
        }

        return 0;
    }

    int writeSample(byte[] paramArrayOfByte, int width, int height) {
        synchronized(this) {
            if(width != 0 && height != 0) {
                if(width != this.mWidth || height != this.mHeight) {
                    this.mWidth = width;
                    this.mHeight = height;
                    this.mYByteBuffer = ByteBuffer.allocate(this.mWidth * this.mHeight);
                    this.mUByteBuffer = ByteBuffer.allocate(this.mWidth * this.mHeight / 4);
                    this.mVByteBuffer = ByteBuffer.allocate(this.mWidth * this.mHeight / 4);
                }

                if(this.mYByteBuffer != null) {
                    this.mYByteBuffer.position(0);
                    this.mYByteBuffer.put(paramArrayOfByte, 0, this.mWidth * this.mHeight);
                    this.mYByteBuffer.position(0);
                }

                if(this.mUByteBuffer != null) {
                    this.mUByteBuffer.position(0);
                    this.mUByteBuffer.put(paramArrayOfByte, 5 * this.mWidth * this.mHeight / 4, this.mWidth * this.mHeight / 4);
                    this.mUByteBuffer.position(0);
                }

                if(this.mVByteBuffer != null) {
                    this.mVByteBuffer.position(0);
                    this.mVByteBuffer.put(paramArrayOfByte, this.mWidth * this.mHeight, this.mWidth * this.mHeight / 4);
                    this.mVByteBuffer.position(0);
                }

                this.bNeedSleep = false;
                return 1;
            } else {
                return 0;
            }
        }
    }

    public void cleanWithRGB(float r, float g, float b) {
        GLES20.glClear(16384);
        GLES20.glClearColor(r, g, b, 1.0F);
        GLES20.glUseProgram(this.programHandle);
    }
}