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
#include <RecordPlayer.c>

#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>
#include <libswscale/swscale.h>

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
 * ��ʼ��������
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
	    //1.�ú����������ǳ�ʼ��libavcodec,��ʹ��avcodec��ʱ���ú������뱻���á�
	    avcodec_init();
	    //2.ע������������ʽ��CODEC---ֻ��Ҫ����һ��
	    //ע�����еı��������codecs������������parsers���Լ�������������bitstream filters����
	    //��Ȼ����Ҳ�����ø����ע�ắ����ע��������Ҫ֧�ֵĸ�ʽ��
	    av_register_all(); //����ע�������е��ļ���ʽ�ͱ�������Ŀ⣬�������ǽ����Զ���ʹ���ڱ��򿪵ĺ��ʸ�ʽ���ļ��ϡ�
	    
	    int i;
	    for(i = 0; i < MAX_STREAMS;i++){
	    	if(pCodecCtxArray[i] == NULL){
	    		videoStreamIndex = i;
	    		LOGI("videoStreamIndex == %d", videoStreamIndex);
	    		break;
	    	}
	    }

	    //3.���ڷ���һ��AVCodecContext������Ĭ��ֵ�����ʧ�ܷ���NULL��������av_free()�����ͷš�
	    pCodecCtxArray[videoStreamIndex] = avcodec_alloc_context();
	    if (pCodecCtxArray[videoStreamIndex] == NULL)
	    {
	    	 LOGE("AVCodecContext allocated failure.");    
	    	 return -1;
	    }
	    
	    enum CodecID code_id; 
	    switch (video_codec)
	    {
	    		case -1:
	    			code_id = CODEC_ID_MJPEG;
	    			break;
	    		case 1:
	    			code_id = CODEC_ID_MPEG4;
	    			break;
	    		case 2:
	    			code_id = CODEC_ID_H264; 
	    			break;
	    		default:
	    			break;
	    }
	    //3.ͨ��code ID����һ���Ѿ�ע�������Ƶ������
	    // ���ҳɹ����ؽ�����ָ��,���򷵻�NULL
	    pCodec=avcodec_find_decoder(code_id); 
	    if(pCodec==NULL) {
	        LOGE("Unsupported codec");  
	        return -1;
	    } 
	     
	    pFrameArray[videoStreamIndex]=avcodec_alloc_frame();//5.���ڷ���һ��AVFrame������Ĭ��ֵ�����ʧ�ܷ���NULL��������av_free()�����ͷš�
	    if(pFrameArray[videoStreamIndex]==NULL) {
	        LOGE("pFrame allocated failure!!");   
	        return -1;
	    } 
	    
	    // ʹ�ø�����AVCodec��ʼ��AVCodecContext
	    // ����0ʱ�ɹ�,����Ϊ���ʱ,�������ò��ԵĻ�,���û�ʧ��
	    // ����: avcodec_find_decoder_by_name(), avcodec_find_encoder_by_name(), avcodec_find_decoder() and avcodec_find_encoder() �ṩ�˿��ٻ�ȡһ��codec��;��
	    if(avcodec_open(pCodecCtxArray[videoStreamIndex], pCodec)<0)//6.
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
    AVPacket packet;// AVPacket�Ǹ�����Ҫ�Ľṹ,�ýṹ�ڶ�ý��Դ�ļ���д����ļ�ʱ����Ҫ�õ�
    av_init_packet(&packet);// ����AVPacket�����,��ʹ��av_init_packet���г�ʼ��

    int64_t seek_target;
    
    jbyte * tmpdata = (jbyte*)(*env)->GetByteArrayElements(env, data, 0);   
  	packet.data = tmpdata; //������
  	packet.size = length;   //�����ݳ���
   
//    LOGE("pCodecCtx == %p", pCodecCtxArray[videoStreamIndex]);

//    LOGE("start avcodec_decode_video2");
    
    int decode_result = avcodec_decode_video2(pCodecCtxArray[videoStreamIndex], pFrameArray[videoStreamIndex], &frameFinished, &packet);// ������Ƶ��AVPacket
    if (decode_result < 0)  
    {
    		LOGE("decode failure.....");
    		av_free_packet(&packet);// �ͷ�AVPacket����
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
	getBitmap(env, data,videoStreamIndex);
}

int getYuvData(JNIEnv * env, jbyteArray data,jint videoStreamIndex){
		int ret;
		AndroidBitmapInfo info;
		void* pixels;
        jbyte* yuvData = (jbyte*)(*env)->GetByteArrayElements(env, data, 0);
		//a) �� AndroidBitmap_getInfo() ������λͼ�������JNI�õ��������Ϣ����ȡ��߶ȡ����ظ�ʽ��
		if ((ret = AndroidBitmap_getInfo(env, bitmap, &info)) < 0) {
			LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
			return -1;
		}

		// b) �� AndroidBitmap_lockPixels() �����ػ�������������øû����ָ�롣
		if ((ret = AndroidBitmap_lockPixels(env, bitmap, &pixels)) < 0) {
			LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
			return -1;
		}

//		LOGE("getBitmap start");
		int pix_fmt = PIX_FMT_RGB565;
		if (pFrameRGBArray[videoStreamIndex] == NULL) {
		    pFrameRGBArray[videoStreamIndex]=avcodec_alloc_frame();

			int numBytes = avpicture_get_size(pix_fmt, pCodecCtxArray[videoStreamIndex]->width,
					pCodecCtxArray[videoStreamIndex]->height);

			uint8_t *buffer = (uint8_t *) av_malloc(numBytes * sizeof(uint8_t));

			avpicture_fill((AVPicture *) pFrameRGBArray[videoStreamIndex], buffer, pix_fmt,
					pCodecCtxArray[videoStreamIndex]->width, pCodecCtxArray[videoStreamIndex]->height);

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

		// d) �� AndroidBitmap_unlockPixels() ����
		AndroidBitmap_unlockPixels(env, bitmap);

//		LOGE("getBitmap end");
}
int getBitmap(JNIEnv * env, jstring bitmap,jint videoStreamIndex){
		int ret;
		AndroidBitmapInfo info;
		void* pixels;

		//a) �� AndroidBitmap_getInfo() ������λͼ�������JNI�õ��������Ϣ����ȡ��߶ȡ����ظ�ʽ��
		if ((ret = AndroidBitmap_getInfo(env, bitmap, &info)) < 0) {
			LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
			return -1;
		}

		// b) �� AndroidBitmap_lockPixels() �����ػ�������������øû����ָ�롣
		if ((ret = AndroidBitmap_lockPixels(env, bitmap, &pixels)) < 0) {
			LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
			return -1;
		}

//		LOGE("getBitmap start");
		int pix_fmt = PIX_FMT_RGB565;
		if (pFrameRGBArray[videoStreamIndex] == NULL) {
		    pFrameRGBArray[videoStreamIndex]=avcodec_alloc_frame();

			int numBytes = avpicture_get_size(pix_fmt, pCodecCtxArray[videoStreamIndex]->width,
					pCodecCtxArray[videoStreamIndex]->height);

			uint8_t *buffer = (uint8_t *) av_malloc(numBytes * sizeof(uint8_t));

			avpicture_fill((AVPicture *) pFrameRGBArray[videoStreamIndex], buffer, pix_fmt,
					pCodecCtxArray[videoStreamIndex]->width, pCodecCtxArray[videoStreamIndex]->height);

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

		// d) �� AndroidBitmap_unlockPixels() ����
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
