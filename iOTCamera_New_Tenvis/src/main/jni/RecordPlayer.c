#include <jni.h>
#include <string.h>
#include <stdio.h>
#include <android/log.h>
#include <android/bitmap.h>

#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>
#include <libswscale/swscale.h>    

#define  LOG_TAG    "FFMPEGSample"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

/*
 ��ʼ����Ƶ����
 
 ������
 videoName:��Ƶ�����֡�������Ҫ�����3gp��ʽ,��ôVideoName = ��/sdcard/test.3pg��
 width����Ƶ�Ŀ��
 height����Ƶ�ĸ߶�
 codec_id����Ƶ�ı��뷽ʽ
  
 ˵����
 ÿ��¼����Ƶ֮ǰ����Ҫ��ʼ��һ��
*/


/*
 ����һ֡��Ƶ���ݣ����浽�ļ���
 
 ������ 
 frameData: ֡����
 length: frameData�ĳ���
 
 ˵�������һ��ʼ��ʱ�������Ƿǹؼ�֡����ô�����������֡���ݼ��뵽�ļ��У���Ϊ���Ӱ����Ƶ����
 ϵͳ��ȵ�һ���ؼ�֡��ʱ��ſ�ʼ��֡���ݼ��뵽�ļ���
 Ŀǰ�˻��ƣ�ֻ֧��H264�������Ƶ���ݡ�
*/
void addVideoFrame(uint8_t* frameData, int length);

/*
 ¼�����֮����Ҫ���ô˺������ܳɹ�����¼��
*/
void stopRecord();   
void deallocate();    

/* Cheat to keep things simple and just use some globals. */
AVFormatContext *outFormatContext;
AVCodecContext *outCodecContext; 
AVOutputFormat *pOutputFormat; 

int isKeyFrame = 0;
int isAudio = 0;//if the value =1,need to record audio.
int isVideo = 0;//if the value =1,need to record video.

/**
 * ���������videoName---�ļ�����·��+�ļ�������"E:\a.mp4"---�����ɵ����ߴ���
 * */
void initMedia(char *videoName) 
{   
	    deallocate();
		av_register_all();
		outFormatContext = avformat_alloc_context();  
		if (outFormatContext == NULL) {  
			return;
		}
		pOutputFormat = av_guess_format(NULL, videoName, NULL);
		/*AVOutputFormat *av_guess_format(const char *short_name,
		                                  const char *filename,
		                                  const char *mime_type);
		 */

		// ����һ���Ѿ�ע�������ʵ������ʽ
		// ����#include "libavformat/avformat.h"
		// ����ͨ�� const char *short_name ��ȡ,��"mpeg"
		// Ҳ����ͨ�� const char *filename ��ȡ,��"E:\a.mp4"

		if (pOutputFormat == NULL) 
		{
			 LOGE("pOutputFormat is null\n"); 
			 return;
		}
		outFormatContext->oformat = pOutputFormat;//11111
}

AVCodecContext *oAcc;
uint8_t *audio_outbuf; 
int audio_outbuf_size;
AVStream *audio_st;
//AVCodecContext *oAcc;

void initWithAudio()
{

	 isAudio = 1;

	/*
	 * audio_st = av_new_stream(outFormatContext, 1);
	audioCodecCtx =avcodec_alloc_context();
	audioCodecCtx = audio_st->codec;
	audioCodecCtx->codec_id = CODEC_ID_PCM_S16LE;
	audioCodecCtx->codec_type = AVMEDIA_TYPE_AUDIO;
	audioCodecCtx->sample_rate = 8000;
	audioCodecCtx->channels = 1;
	audioCodecCtx->sample_fmt = AV_SAMPLE_FMT_S16;
	 * */
		 	audio_st = av_new_stream(outFormatContext, 1);//��ȡ����Ƶ��
		 	oAcc=avcodec_alloc_context(); 
		 	if (oAcc == NULL)
		 	{
		 		 LOGE("oAcc is null\n");
		 		 return; 
		 	}
			oAcc=audio_st->codec;
			oAcc->codec_id=CODEC_ID_PCM_S16LE;
			oAcc->codec_type= AVMEDIA_TYPE_AUDIO;
			oAcc->sample_rate = 8000;
		    oAcc->sample_fmt = SAMPLE_FMT_S16;
			oAcc->channels=1;

				oAcc->time_base.num = 1; 
				oAcc->time_base.den = 100;

				oAcc->flags |= CODEC_FLAG_GLOBAL_HEADER;


			if (av_set_parameters(outFormatContext, NULL) < 0)
				{
					LOGE("Invalid output format parameters");
					return 0;
				}//���ñ�Ҫ���������

			//dump_format(outFormatContext,0,videoName,1);//�г�����ļ����������Ϣ

			AVCodec *oAc=avcodec_find_encoder(oAcc->codec_id);
				avcodec_register_all();
						if(!oAc)
						{
							LOGE("avcodec_find_encoder is failed\n");
									return -1;
						}//�ҵ����ʵ���Ƶ������
						if(avcodec_open(oAcc,oAc)<0)
						{
							LOGE("avcodec_open is failed\n");
									return -1;
						}//����Ƶ������

					 	audio_outbuf_size = 10000;
						audio_outbuf = (uint8_t*)av_malloc(audio_outbuf_size); 
}


void pcmConvertMp2(uint16_t* frameData)
{
	LOGE("pcmConvertMp2");

	int length = avcodec_encode_audio(oAcc, audio_outbuf, audio_outbuf_size, frameData);
	LOGE("avcodec_encode_audio");


	LOGE("pkt.data");

}
void initWithVideo( int v_rate,int width, int height, enum CodecID codec_id)
{
	isVideo = 1; 

	AVStream *video_st1 = av_new_stream(outFormatContext, 0);
	outCodecContext = video_st1->codec;  
	outCodecContext->me_range = 16;
	outCodecContext->max_qdiff = 4;
	outCodecContext->qmin = 10;
	outCodecContext->qmax = 51;
	outCodecContext->qcompress = 0.6;

	outCodecContext->codec_id = codec_id;
	outCodecContext->codec_type = AVMEDIA_TYPE_VIDEO;
	outCodecContext->bit_rate = v_rate;
	LOGE("outCodecContext->bit_rate === %d",outCodecContext->bit_rate);
	outCodecContext->time_base.num = 1;
	outCodecContext->time_base.den = 100;
	outCodecContext->width = width;
	LOGE("outCodecContext->width === %d",outCodecContext->width);
	outCodecContext->height = height;
	LOGE("outCodecContext->height === %d",outCodecContext->height);
	//outCodecContext->gop_size = 50;	//����֡��ѹ��ʱ������12����ָ12��ͼƬ��֡��Ԥ��
	if (codec_id == CODEC_ID_H264 ||codec_id == CODEC_ID_MJPEG ) {
		outCodecContext->pix_fmt = PIX_FMT_YUVJ420P;	//���ظ�ʽ����ʾ��Ļ����ʾ��ʽ
	}else {
		outCodecContext->pix_fmt = PIX_FMT_YUV420P;	//���ظ�ʽ����ʾ��Ļ����ʾ��ʽ
	}


	AVCodec *codec;
	avcodec_register_all();
	LOGE("avcodec_register_all");
	codec = avcodec_find_encoder(outCodecContext->codec_id);
	LOGE("codec");
	if(NULL == codec){
		LOGE("codec is failed\n");  
		return -1;
	}

	LOGE("codec is build\n"); 
	if(avcodec_open(outCodecContext, codec) < 0){//3333
		LOGE("faile avcodec_open  == %d",outCodecContext->codec_id);
		return -1;
	} 
	
	LOGE("avcodec_open");

}

int initOver(char *videoName)
{
	if(!(pOutputFormat->flags & AVFMT_NOFILE))
		{
			if(url_fopen(&outFormatContext->pb, videoName, URL_WRONLY)< 0)
			{
				LOGE("pOutputFormat");
				return -1;
			}
		}
		LOGE("pOutputFormat over");
		int length = av_write_header(outFormatContext); 
		LOGE("av_write_header == %d", length); 
		
		return length;

}

void addFrame(uint8_t* frameData, int length,int streamType,int tick) 
{
		AVPacket packet;
		av_init_packet(&packet);
		packet.pts = tick;

		packet.stream_index = streamType;//���ֻ������ƵӦ�ð�����ע�ͣ�������

		packet.data = frameData; 
		packet.size = length;
		LOGE("write_frame start ");
		av_write_frame(outFormatContext, &packet);//key
		LOGE("write_frame  over"); 
}
 
void stopRecord() 
{
	av_write_trailer(outFormatContext);
	 //avformat_free_context(outFormatContext); 
}

void findKeyFrame(char *frameData)
{
	//ͨ���ж�֡����ͷ�����õ��Ƿ�Ϊ�ؼ�֡
	if (frameData[2] == 1 && (frameData[3]&0x1F) == 7) {
		isKeyFrame = 1;
	}else if (frameData[3] == 1 && (frameData[4]&0x1F) == 7) {
		isKeyFrame = 1;
	}
}

void deallocate() 
{  
	 if (outFormatContext) 
	 {
	 	  avformat_free_context(outFormatContext);   
	 	  outFormatContext = NULL;  
	 }
	    
	 if (audio_outbuf)        
	 {
	 	  av_freep(audio_outbuf);          
	 	  audio_outbuf = NULL;
	 } 
}
