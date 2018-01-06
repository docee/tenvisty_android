/*
 * Copyright 2011 - Churn Labs, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * This is mostly based off of the FFMPEG tutorial:
 * http://dranger.com/ffmpeg/
 * With a few updates to support Android output mechanisms and to update
 * places where the APIs have shifted.
 */

//#include <jni.h>
//#include <string.h>
//#include <stdio.h>
//#include <android/log.h>
//#include <android/bitmap.h>

#include <jni.h>
#include <string.h>
#include <stdio.h>
#include <android/log.h>
#include <android/bitmap.h>
#include <record.c>

#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>
#include <libswscale/swscale.h>
#include <libavutil/imgutils.h>

#define  LOG_TAG    "FFMPEGSample"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

#define MAX_STREAMS 200

/* Cheat to keep things simple and just use some globals. */
AVCodecContext *pCodecCtxArray[MAX_STREAMS];
AVFrame *pFrameArray[MAX_STREAMS];
AVFrame *pFrameRGBArray[MAX_STREAMS];
struct SwsContext *img_convert_ctxArray[MAX_STREAMS];

int isRecord = 0;

/*
 * Write a frame worth of video (in pFrame) into the Android bitmap
 * described by info using the raw pixel buffer.  It's a very inefficient
 * draw routine, but it's easy to read. Relies on the format of the
 * bitmap being 8bits per color component plus an 8bit alpha channel.
 */
static void fill_bitmap(AndroidBitmapInfo*  info, void *pixels, AVFrame *pFrame)
{
    uint8_t *frameLine;

    int  yy;
    for (yy = 0; yy < info->height; yy++) {
        uint8_t*  line = (uint8_t*)pixels;
        frameLine = (uint8_t *)pFrame->data[0] + (yy * pFrame->linesize[0]);

        int xx;
        for (xx = 0; xx < info->width; xx++) {
            int out_offset = xx * 2;
            int in_offset = xx * 2;

            line[out_offset] = frameLine[in_offset];
            line[out_offset+1] = frameLine[in_offset+1];
//            line[out_offset+2] = frameLine[in_offset+2];
//            line[out_offset+3] = frameLine[in_offset+3];
        }
        pixels = (char*)pixels + info->stride;
    }
}

/**
 * 初始化播放器
 */
jint Java_com_decoder_util_VideoPlayer_initWithVideo(JNIEnv * env, jobject this,
                                                     jint video_codec)
{
//	freopen("/mnt/sdcard/log.txt", "a+", stderr);
//	freopen("/mnt/sdcard/log.txt", "a+", stderr);

    return init_video_context(env, this, video_codec);
}

int init_video_context(JNIEnv * env, jobject this, jint video_codec)
{
    int ret;
    int err;
    int videoStreamIndex = -1;;
    AVCodec *pCodec;
    //1.该函数的作用是初始化libavcodec,在使用avcodec库时，该函数必须被调用。
    //avcodec_init();
    //2.注册所有容器格式后CODEC---只需要调用一次
    //注册所有的编解码器（codecs），解析器（parsers）以及码流过滤器（bitstream filters）。
    //当然我们也可以用个别的注册函数来注册我们所要支持的格式。
    av_register_all(); //这里注册了所有的文件格式和编解码器的库，所以它们将被自动的使用在被打开的合适格式的文件上。

    int i;
    for(i = 0; i < MAX_STREAMS;i++){
        if(pCodecCtxArray[i] == NULL){
            videoStreamIndex = i;
            LOGI("videoStreamIndex == %d", videoStreamIndex);
            break;
        }
    }


    enum AVCodecID code_id;
    switch (video_codec)
    {
        case -1:
            code_id = AV_CODEC_ID_MJPEG;
            break;
        case 1:
            code_id = AV_CODEC_ID_MPEG4;
            break;
        case 2:
            code_id = AV_CODEC_ID_H264;
            break;
        default:
            break;
    }
    //3.通过code ID查找一个已经注册的音视频解码器
    // 查找成功返回解码器指针,否则返回NULL
    pCodec=avcodec_find_decoder(code_id);
    if(pCodec==NULL) {
        LOGE("Unsupported codec");
        return -1;
    }
    //3.用于分配一个AVCodecContext并设置默认值，如果失败返回NULL，并可用av_free()进行释放。
    pCodecCtxArray[videoStreamIndex] = avcodec_alloc_context3(pCodec);
    if (pCodecCtxArray[videoStreamIndex] == NULL)
    {
        LOGE("AVCodecContext allocated failure.");
        return -1;
    }

    pFrameArray[videoStreamIndex]=av_frame_alloc();//5.用于分配一个AVFrame并设置默认值，如果失败返回NULL，并可用av_free()进行释放。
    if(pFrameArray[videoStreamIndex]==NULL) {
        LOGE("pFrame allocated failure!!");
        return -1;
    }

    // 使用给定的AVCodec初始化AVCodecContext
    // 返回0时成功,打开作为输出时,参数设置不对的话,调用会失败
    // 方法: avcodec_find_decoder_by_name(), avcodec_find_encoder_by_name(), avcodec_find_decoder() and avcodec_find_encoder() 提供了快速获取一个codec的途径
    if(avcodec_open2(pCodecCtxArray[videoStreamIndex], pCodec,NULL)<0)//6.
    {
        LOGE("Unable to open codec");
        return -1;
    }

    return videoStreamIndex;
}

jint Java_com_decoder_util_VideoPlayer_decode(JNIEnv * env, jobject this,
                                              jbyteArray data,jint length,jintArray pictureParam,jint videoStreamIndex)
{

//	LOGE("start videoDrawFrameAt ");
//	LOGE("pCodecCtx11 == %p %d", pCodecCtxArray[videoStreamIndex],videoStreamIndex);
    jint * pictureParamInt = (jbyte*)(*env)->GetIntArrayElements(env, pictureParam, 0);

    int result = decode(env, data, length, pictureParamInt,videoStreamIndex);

    (*env)->ReleaseIntArrayElements(env, pictureParam,pictureParamInt,0);
//	LOGE("pCodecCtx22 == %p", pCodecCtxArray[videoStreamIndex]);
    return result;
}


int decode(JNIEnv * env, jbyteArray data,jint length,jint* pictureParam,jint videoStreamIndex)
{
//	LOGE("start decode");

    int err;
    int frameFinished = 0;
    AVPacket packet;// AVPacket是个很重要的结构,该结构在读媒体源文件和写输出文件时都需要用到
    av_init_packet(&packet);// 定义AVPacket对象后,请使用av_init_packet进行初始化

    int64_t seek_target;

    jbyte * tmpdata = (jbyte*)(*env)->GetByteArrayElements(env, data, 0);
    packet.data = tmpdata; //包数据
    packet.size = length;   //包数据长度

//    LOGE("pCodecCtx == %p", pCodecCtxArray[videoStreamIndex]);

//    LOGE("start avcodec_decode_video2");

    int decode_result = avcodec_decode_video2(pCodecCtxArray[videoStreamIndex], pFrameArray[videoStreamIndex], &frameFinished, &packet);// 解码视频流AVPacket
    if (decode_result < 0)
    {
        LOGE("decode failure.....");
        av_free_packet(&packet);// 释放AVPacket对象
        (*env)->ReleaseByteArrayElements(env, data, tmpdata, 0);

        return 0;
    }
//    LOGE("pCodecCtx->width == %d", pCodecCtxArray[videoStreamIndex]->width);
    pictureParam[2] = pCodecCtxArray[videoStreamIndex]->width;
    pictureParam[3] = pCodecCtxArray[videoStreamIndex]->height;

//    LOGE("end avcodec_decode_video2 %d",frameFinished);

    av_free_packet(&packet);
    (*env)->ReleaseByteArrayElements(env, data, tmpdata, 0);

    return frameFinished;
}

jint Java_com_decoder_util_VideoPlayer_getBitmap(JNIEnv * env, jobject this,
                                                 jstring bitmap,jint videoStreamIndex)
{
    getBitmap(env, bitmap,videoStreamIndex);
}
jint Java_com_decoder_util_VideoPlayer_getYuvData(JNIEnv * env, jobject this,
                                                  jbyteArray data,jint videoStreamIndex)
{

    if(pCodecCtxArray[videoStreamIndex] == NULL  || pFrameArray[videoStreamIndex] == NULL){
        return -1;
    }

    getYuvData(env, data,videoStreamIndex);
    return 1;
}

int getYuvData(JNIEnv * env, jbyteArray data,jint videoStreamIndex){

    jbyte* yuvData = (jbyte*)(*env)->GetByteArrayElements(env, data, 0);
    //申请内存
    int height = pCodecCtxArray[videoStreamIndex]->height;
    int width = pCodecCtxArray[videoStreamIndex]->width;

    //写入数据
    int a=0,i;
    //YUV420P
    for (i=0; i<height; i++)
    {
        memcpy(yuvData+a,pFrameArray[videoStreamIndex]->data[0] + i * pFrameArray[videoStreamIndex]->linesize[0], width);
        a+=width;
    }
    for (i=0; i<height/2; i++)
    {
        memcpy(yuvData+a,pFrameArray[videoStreamIndex]->data[2] + i * pFrameArray[videoStreamIndex]->linesize[2], width/2);
        a+=width/2;
    }
    for (i=0; i<height/2; i++)
    {
        memcpy(yuvData+a,pFrameArray[videoStreamIndex]->data[1] + i * pFrameArray[videoStreamIndex]->linesize[1], width/2);
        a+=width/2;
    }
    (*env)->ReleaseByteArrayElements(env, data, yuvData, 0);
}
int getBitmap(JNIEnv * env, jstring bitmap,jint videoStreamIndex){
    int ret;
    AndroidBitmapInfo info;
    void* pixels;

    //a) 用 AndroidBitmap_getInfo() 函数从位图句柄（从JNI得到）获得信息（宽度、高度、像素格式）,判断是图片
    if ((ret = AndroidBitmap_getInfo(env, bitmap, &info)) < 0) {
        LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
        return -1;
    }

    // b) 用 AndroidBitmap_lockPixels() 对像素缓存上锁，即获得该缓存的指针。 获取bmp图像指针，已方便操作图像
    if ((ret = AndroidBitmap_lockPixels(env, bitmap, &pixels)) < 0) {
        LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
        return -1;
    }

//		LOGE("getBitmap start");
    int pix_fmt = AV_PIX_FMT_RGB565;
    if (pFrameRGBArray[videoStreamIndex] == NULL) {
        //分配目标帧（RGB）空间
        pFrameRGBArray[videoStreamIndex]=av_frame_alloc();

        //计算目标图像大小
        int numBytes = av_image_get_buffer_size(pix_fmt, pCodecCtxArray[videoStreamIndex]->width,
                                          pCodecCtxArray[videoStreamIndex]->height,1);

        //分配目标图像空间
        uint8_t *buffer = (uint8_t *) av_malloc(numBytes * sizeof(uint8_t));

        av_image_fill_arrays ( pFrameRGBArray[videoStreamIndex]->data,pFrameRGBArray[videoStreamIndex]->linesize, buffer, pix_fmt,
                       pCodecCtxArray[videoStreamIndex]->width, pCodecCtxArray[videoStreamIndex]->height,1);

    }

    int target_width = pCodecCtxArray[videoStreamIndex]->width;
    int target_height = pCodecCtxArray[videoStreamIndex]->height;

//		LOGE("getBitmap start0");
    img_convert_ctxArray[videoStreamIndex] = sws_getContext(pCodecCtxArray[videoStreamIndex]->width, pCodecCtxArray[videoStreamIndex]->height,
                                                            pCodecCtxArray[videoStreamIndex]->pix_fmt, target_width, target_height, pix_fmt,
                                                            SWS_BICUBIC, NULL, NULL, NULL);
//		LOGE("getBitmap start1");
    if (img_convert_ctxArray[videoStreamIndex] == NULL) {
        LOGE("could not initialize conversion context == %d\n", target_width);
        return -1;
    }

    sws_scale(img_convert_ctxArray[videoStreamIndex], (const uint8_t* const *) pFrameArray[videoStreamIndex]->data,
              pFrameArray[videoStreamIndex]->linesize, 0, pCodecCtxArray[videoStreamIndex]->height, pFrameRGBArray[videoStreamIndex]->data,
              pFrameRGBArray[videoStreamIndex]->linesize);

    fill_bitmap(&info, pixels, pFrameRGBArray[videoStreamIndex]);

    // d) 用 AndroidBitmap_unlockPixels() 解锁
    AndroidBitmap_unlockPixels(env, bitmap);

//		LOGE("getBitmap end");
}

void Java_com_decoder_util_VideoPlayer_dealloc(JNIEnv * env, jobject this,jint video_stream_index)
{
    if(img_convert_ctxArray[video_stream_index]){
        sws_freeContext(img_convert_ctxArray[video_stream_index]);
        img_convert_ctxArray[video_stream_index] = NULL;
    }

    if(pFrameArray[video_stream_index]){
        av_free(pFrameArray[video_stream_index]);
        pFrameArray[video_stream_index] = NULL;
    }

    if(pFrameRGBArray[video_stream_index]){
        av_free(pFrameRGBArray[video_stream_index]);
        pFrameRGBArray[video_stream_index] = NULL;
    }

    if (pCodecCtxArray[video_stream_index]){
        avcodec_close(pCodecCtxArray[video_stream_index]);
        pCodecCtxArray[video_stream_index] = NULL;
    }
}

JNIEXPORT jint JNICALL
Java_com_decoder_util_VideoPlayer_RecordMp4init(JNIEnv *env, jclass type,
                                                                jint width, jint height,
                                                                jstring videoPath_) {
    const char *videoPath = (*env)->GetStringUTFChars(env, videoPath_, 0);
    int ret = CreateMp4(videoPath,width,height);
    // TODO

    (*env)->ReleaseStringUTFChars(env, videoPath_, videoPath);
    return ret;
}

JNIEXPORT jint JNICALL
Java_com_decoder_util_VideoPlayer_RecordMp4write(JNIEnv *env, jclass type,
                                                                 jbyteArray data_, jint length, jint pts) {
    jbyte *data = (*env)->GetByteArrayElements(env, data_, NULL);
    WriteVideo(data,length,pts);
    // TODO

    (*env)->ReleaseByteArrayElements(env, data_, data, 0);
    return 0;
}

JNIEXPORT jint JNICALL
Java_com_decoder_util_VideoPlayer_RecordMp4deinit(JNIEnv *env, jclass type) {
    CloseMp4();
    // TODO

}