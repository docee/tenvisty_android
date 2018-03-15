#include <jni.h>
#include <string.h>
#include <stdio.h>

#define __STDC_CONSTANT_MACROS

#include <libavformat/avformat.h>
#include <android/log.h>

#define TAG "mp4record"
#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)
/*

FIX: H.264 in some container format (FLV, MP4, MKV etc.) need

"h264_mp4toannexb" bitstream filter (BSF)

  *Add SPS,PPS in front of IDR frame

  *Add start code ("0,0,0,1") in front of NALU

H.264 in some container (MPEG2TS) don't need this BSF.

*/

//'1': Use H.264 Bitstream Filter

#define USE_H264BSF 0
/*

FIX:AAC in some container format (FLV, MP4, MKV etc.) need

"aac_adtstoasc" bitstream filter (BSF)

*/

//'1': Use AAC Bitstream Filter

#define USE_AACBSF 0


#define MAX_STREAMS 20
AVFormatContext *m_pOcArray[MAX_STREAMS];
#define STREAM_FRAME_RATE 15

#define STREAM_PIX_FMT    AV_PIX_FMT_YUV420P /* default pix_fmt */

AVFrame *mAVFrameArray[MAX_STREAMS];

int ptsIncArray[MAX_STREAMS] = {0};

int viArray[MAX_STREAMS] = {-1};
int aiArray[MAX_STREAMS] = {-1};

int waitkeyArray[MAX_STREAMS] = {1};
int beginTimeStampArray[MAX_STREAMS] = {0};
int lastVideoTimeStampArray[MAX_STREAMS] = {0};
int lastAudioTimeStampArray[MAX_STREAMS] = {0};
int mBufferSizeArray[MAX_STREAMS] = {0};
int mBufferIndexArray[MAX_STREAMS] = {0};
int mAudioIndexArray[MAX_STREAMS] = {0};
uint8_t *mEncoderDataArray[MAX_STREAMS] = {0};
uint8_t *mEncoderDataEmpty;

/*-----------------
< 0 = error
   0 = I-Frame
   1 = P-Frame
   2 = B-Frame
   3 = S-Frame
--------------------*/
int getVopType(const void *p, int len) {
    if (!p || 6 >= len)
        return -1;
    unsigned char *b = (unsigned char *) p;
    // Verify NAL marker
    if (b[0] || b[1] || 0x01 != b[2]) {
        b++;
        if (b[0] || b[1] || 0x01 != b[2])
            return -1;
    } // end if
    b += 3;
    // Verify VOP id
    if (0xb6 == *b) {
        b++;
        return (*b & 0xc0) >> 6;
    } // end if
    switch (*b) {
        case 0x65 :
            return 0;
        case 0x61 :
            return 1;
        case 0x01 :
            return 2;
    } // end switch
    return -1;
}

void open_video(AVFormatContext *oc, AVCodec *codec, AVStream *st) {
    int ret;
    AVCodecContext *c = st->codec;
    /* open the codec */
    ret = avcodec_open2(c, codec, NULL);
    if (ret < 0) {
        LOGV("could not open video codec");
        //exit(1);
    }
}

/* Add an output stream */
AVStream *
add_stream(AVFormatContext *oc, AVCodec **codec, enum AVCodecID codec_id, int width, int height,
           int videoStreamIndex) {
    AVCodecContext *c;
    AVStream *st;
    /* find the encoder */
    *codec = avcodec_find_encoder(codec_id);
    if (!*codec) {
        LOGV("could not find encoder for '%s' \n", avcodec_get_name(codec_id));
        exit(1);
    }
    st = avformat_new_stream(oc, *codec);
    if (!st) {
        LOGV("could not allocate stream \n");
        exit(1);
    }
    st->id = oc->nb_streams - 1;
    c = st->codec;
    LOGV("input buffer %zd streamid:%zd streams:%zd", (*codec)->type, st->id, oc->nb_streams);
    switch ((*codec)->type) {
        case AVMEDIA_TYPE_AUDIO:
            aiArray[videoStreamIndex] = st->index;

            LOGV("AVMEDIA_TYPE_AUDIO %zd\n", st->index);
            c->codec_id = AV_CODEC_ID_AAC;
            c->codec_type = AVMEDIA_TYPE_AUDIO;
            c->bit_rate = 0;
            c->sample_fmt = (*codec)->sample_fmts[0];
            c->sample_rate = 8000;
            c->channel_layout = AV_CH_LAYOUT_MONO;
            c->channels = 1;
            LOGV("3AVMEDIA_TYPE_AUDIO %zd\n", st->index);
            break;
        case AVMEDIA_TYPE_VIDEO:
            viArray[videoStreamIndex] = st->index;
            LOGV("AVMEDIA_TYPE_VIDEO %zd\n", st->index);
            c->codec_id = AV_CODEC_ID_H264;
            c->bit_rate = 0;
            c->width = width;
            c->height = height;
            c->time_base.den = 30;
            c->time_base.num = 1;
            c->gop_size = 1;
            c->pix_fmt = STREAM_PIX_FMT;
            if (c->codec_id == AV_CODEC_ID_MPEG2VIDEO) {
                c->max_b_frames = 2;
            }
            if (c->codec_id == AV_CODEC_ID_MPEG1VIDEO) {
                c->mb_decision = 2;
            }
            break;
        default:
            break;
    }
    LOGV("AVMEDIA_TYPE_AUDIO %zd\n", st->index);
    if (oc->oformat->flags & AVFMT_GLOBALHEADER) {
        c->flags |= CODEC_FLAG_GLOBAL_HEADER;
    }
    LOGV("5AVMEDIA_TYPE_AUDI22O %zd\n", st->index);
    return st;
}

int CloseMp4(int videoStreamIndex) {
    int ret = 0;
    if (mBufferSizeArray[videoStreamIndex] != 0) {
        fillAudioFrame(videoStreamIndex);
        LOGV("begin Flushing encoder\n");
        ret = flush_encoder(m_pOcArray[videoStreamIndex], aiArray[videoStreamIndex]);
        if (ret < 0) {
            LOGV("Flushing encoder failed\n");
        } else {
            LOGV("Flushing encoder\n");
        }
    }
    if (m_pOcArray[videoStreamIndex]) {
        av_write_trailer(m_pOcArray[videoStreamIndex]);
        LOGV("write trailer\n");
        ret += 1;
    }
    if (mBufferSizeArray[videoStreamIndex] != 0) {
        if (m_pOcArray[videoStreamIndex]->streams[aiArray[videoStreamIndex]]) {
            avcodec_close(m_pOcArray[videoStreamIndex]->streams[aiArray[videoStreamIndex]]->codec);
            LOGV("close codec\n");
            av_free(mAVFrameArray[videoStreamIndex]);
            av_free(mEncoderDataArray[videoStreamIndex]);
            LOGV("free frame\n");
            ret += 8;
        }
    }
    if (m_pOcArray[videoStreamIndex] &&
        !(m_pOcArray[videoStreamIndex]->oformat->flags & AVFMT_NOFILE)) {
        ret += 2;
        avio_close(m_pOcArray[videoStreamIndex]->pb);
    }
    if (m_pOcArray[videoStreamIndex]) {
        ret += 4;
        avformat_free_context(m_pOcArray[videoStreamIndex]);
        m_pOcArray[videoStreamIndex] = NULL;
    }
    beginTimeStampArray[videoStreamIndex] = 0;

    ptsIncArray[videoStreamIndex] = 0;

    viArray[videoStreamIndex] = -1;
    aiArray[videoStreamIndex] = -1;
    mBufferSizeArray[videoStreamIndex] = 0;
    mEncoderDataArray[videoStreamIndex] = 0;
    waitkeyArray[videoStreamIndex] = 1;
    lastVideoTimeStampArray[videoStreamIndex] = 0;
    lastAudioTimeStampArray[videoStreamIndex] = 0;
    mBufferIndexArray[videoStreamIndex] = 0;
    mAudioIndexArray[videoStreamIndex] = 0;
    return ret;
}

int CreateMp4(const char *pszFileName, int width, int height) {
    //CloseMp4();
    int videoStreamIndex = 0;
    int ret; // 成功返回0，失败返回1
    AVOutputFormat *fmt;
    AVCodec *video_codec;
    AVCodec *audio_codec;
    AVStream *m_pVideoSt;
    AVStream *m_pAudioSt;
    av_register_all();
    int i;
    for (i = 0; i < MAX_STREAMS; i++) {
        if (m_pOcArray[i] == NULL) {
            videoStreamIndex = i;
            break;
        }
    }
    avformat_alloc_output_context2(&m_pOcArray[videoStreamIndex], NULL, NULL, pszFileName);
    if (!m_pOcArray[videoStreamIndex]) {
        LOGV("Could not deduce output format from file extension: using MPEG. \n");
        avformat_alloc_output_context2(&m_pOcArray[videoStreamIndex], NULL, "mpeg", pszFileName);
    }
    if (!m_pOcArray[videoStreamIndex]) {
        return -1;
    }
    fmt = m_pOcArray[videoStreamIndex]->oformat;
    if (fmt->video_codec != AV_CODEC_ID_NONE) {
        LOGV("add_stream\n");
        m_pVideoSt = add_stream(m_pOcArray[videoStreamIndex], &video_codec, fmt->video_codec, width,
                                height, videoStreamIndex);
        m_pAudioSt = add_stream(m_pOcArray[videoStreamIndex], &audio_codec, AV_CODEC_ID_AAC, width,
                                height, videoStreamIndex);

    }
    //in_file = fopen("/storage/emulated/0/tenvisty/Z5EW4S77VEL6FWRL111A/videorecording/Z5EW4S77VEL6FWRL111A_20180315102240264.mp4.pcm", "rb");
//    if (m_pVideoSt)
//    {
//        LOGV("open_video %zd - %zd\n",video_codec->id,AV_CODEC_ID_H264);
//        open_video(m_pOcArray[videoStreamIndex], video_codec, m_pVideoSt);
//    }
    if (m_pAudioSt) {
        LOGV("open_audio\n");
        ret = avcodec_open2(m_pAudioSt->codec, avcodec_find_encoder(m_pAudioSt->codec->codec_id),
                            NULL);
        if (ret < 0) {
            LOGV("fail to open encoder for '%s' %zd \n",
                 avcodec_get_name(m_pAudioSt->codec->codec_id), ret);
            //return NULL;
        } else {
            mAVFrameArray[videoStreamIndex] = av_frame_alloc();
            mAVFrameArray[videoStreamIndex]->nb_samples = m_pAudioSt->codec->frame_size;
            mAVFrameArray[videoStreamIndex]->format = m_pAudioSt->codec->sample_fmt;
            mAVFrameArray[videoStreamIndex]->sample_rate = m_pAudioSt->codec->sample_rate;
            mAVFrameArray[videoStreamIndex]->channels = m_pAudioSt->codec->channels;
            mAVFrameArray[videoStreamIndex]->channel_layout = m_pAudioSt->codec->channel_layout;
            mBufferSizeArray[videoStreamIndex] = av_samples_get_buffer_size(NULL,
                                                                            m_pAudioSt->codec->channels,
                                                                            m_pAudioSt->codec->frame_size,
                                                                            m_pAudioSt->codec->sample_fmt,
                                                                            1);
            mEncoderDataArray[videoStreamIndex] = (uint8_t *) av_malloc(
                    mBufferSizeArray[videoStreamIndex]);

            mEncoderDataEmpty = (uint8_t *) av_malloc(
                    mBufferSizeArray[videoStreamIndex]);
            memset(mEncoderDataEmpty, 0, mBufferSizeArray[videoStreamIndex]);
            //mAVFrameArray[videoStreamIndex]->data[0] = malloc(mEncoderDataArray[videoStreamIndex]);
            avcodec_fill_audio_frame(mAVFrameArray[videoStreamIndex], m_pAudioSt->codec->channels,
                                     m_pAudioSt->codec->sample_fmt,
                                     (const uint8_t *) mEncoderDataArray[videoStreamIndex],
                                     mBufferSizeArray[videoStreamIndex], 1);
            mBufferIndexArray[videoStreamIndex] = 0;
        }
    }
    LOGV("==========Output Information==========\n");
    av_dump_format(m_pOcArray[videoStreamIndex], 0, pszFileName, 1);
    LOGV("======================================\n");
    /* open the output file, if needed */
    if (!(fmt->flags & AVFMT_NOFILE)) {
        ret = avio_open(&m_pOcArray[videoStreamIndex]->pb, pszFileName, AVIO_FLAG_WRITE);
        if (ret < 0) {
            LOGV("could not open %s\n", pszFileName);
            return -1;
        }
    }
    /* Write the stream header, if any */
    ret = avformat_write_header(m_pOcArray[videoStreamIndex], NULL);
    if (ret < 0) {
        LOGV("Error occurred when opening output file");
        return -1;
    }
    return videoStreamIndex;
}

int fillAudioFrame(int videoStreamIndex){
    int ret = 0;
    if(mBufferIndexArray[videoStreamIndex] != 0){
        AVStream *pst = m_pOcArray[videoStreamIndex]->streams[aiArray[videoStreamIndex]];
        //printf("vi=====%d\n",vi);
        // Init packet
        int got_frame = 0;
        // 我的添加，为了计算pts
        AVCodecContext *c = pst->codec;
        int pts = lastAudioTimeStampArray[videoStreamIndex] - 20*(double)mBufferIndexArray[videoStreamIndex]/320;
        int remainAudioData = mBufferSizeArray[videoStreamIndex] - mBufferIndexArray[videoStreamIndex];
            memset(&mEncoderDataArray[videoStreamIndex][mBufferIndexArray[videoStreamIndex]],
                   0, remainAudioData);
        mAVFrameArray[videoStreamIndex]->pkt_size = mBufferSizeArray[videoStreamIndex];
        // memset(mEncoderDataArray[videoStreamIndex], 0, mBufferSizeArray[videoStreamIndex]);
        //memcpy(mEncoderDataArray[videoStreamIndex],data,nLen);
        mAVFrameArray[videoStreamIndex]->data[0] = mEncoderDataArray[videoStreamIndex];
        //mAVFrameArray[videoStreamIndex]->pkt_size = nLen;
        mAVFrameArray[videoStreamIndex]->pts =
                (pts - beginTimeStampArray[videoStreamIndex] -
                 20 * ((double)mBufferSizeArray[videoStreamIndex] / 320 - 1)) /
                (double) (av_q2d(pst->time_base) * 1000);
        AVPacket pkt;
        av_new_packet(&pkt, mBufferSizeArray[videoStreamIndex]);
        ret = avcodec_encode_audio2(c, &pkt,
                                    mAVFrameArray[videoStreamIndex], &got_frame);
        if (ret < 0) {
            LOGV("Failed to encode! %zd\n", ret);
            av_packet_unref(&pkt);
        }
        if (got_frame == 1) {
            pkt.stream_index = m_pOcArray[videoStreamIndex]->streams[aiArray[videoStreamIndex]]->index;
        } else {
            LOGV("fail to encode 1 frame! \tsize:%zd\tgot_frame:%zd\n", pkt.size,
                 got_frame);
        }
        mAudioIndexArray[videoStreamIndex]++;
        if (got_frame == 1) {
            ret = av_interleaved_write_frame(m_pOcArray[videoStreamIndex], &pkt);
            if (ret < 0) {
                LOGV("cannot write audio frame");
            }
            av_packet_unref(&pkt);
        }

    }
}

/* 创建mp4文件返回2；写入数据帧返回0 */
void WriteVideo(int videoStreamIndex, uint8_t *data, int nLen, int pts, int frameType) {
    int ret;
    if (0 > viArray[videoStreamIndex]) {
        LOGV("vi less than 0");
        //return -1;
    }
    AVStream *pst = m_pOcArray[videoStreamIndex]->streams[frameType != 2 ? viArray[videoStreamIndex]
                                                                         : aiArray[videoStreamIndex]];
    //printf("vi=====%d\n",vi);
    // Init packet
    int got_frame = 0;
    // 我的添加，为了计算pts
    AVCodecContext *c = pst->codec;
    if (beginTimeStampArray[videoStreamIndex] == 0) {
        beginTimeStampArray[videoStreamIndex] = pts;
    }
    if (frameType == 2) {
        int remainLength = 0;
        if (mBufferSizeArray[videoStreamIndex] != 0) {
            if (mBufferIndexArray[videoStreamIndex] + nLen <= mBufferSizeArray[videoStreamIndex]) {
                memcpy(&mEncoderDataArray[videoStreamIndex][mBufferIndexArray[videoStreamIndex]],
                       data, nLen);
                mBufferIndexArray[videoStreamIndex] += nLen;

                if (mBufferIndexArray[videoStreamIndex] == mBufferSizeArray[videoStreamIndex]) {
                    mBufferIndexArray[videoStreamIndex] = 0;
                }
            } else {
                LOGV("remainLength:%zd",remainLength);
                remainLength = mBufferIndexArray[videoStreamIndex] + nLen -
                               mBufferSizeArray[videoStreamIndex];
                //memcpy(&mEncoderDataArray[videoStreamIndex][mBufferIndexArray[videoStreamIndex]],data,nLen-remainLength);
                memset(&mEncoderDataArray[videoStreamIndex][mBufferIndexArray[videoStreamIndex]], 0,
                       nLen - remainLength);
                mBufferIndexArray[videoStreamIndex] = 0;
                LOGV("end remainLength:%zd",remainLength);
            }
            if (mBufferIndexArray[videoStreamIndex] == 0) {
                mAVFrameArray[videoStreamIndex]->pkt_size = mBufferSizeArray[videoStreamIndex];
                // memset(mEncoderDataArray[videoStreamIndex], 0, mBufferSizeArray[videoStreamIndex]);
                //memcpy(mEncoderDataArray[videoStreamIndex],data,nLen);
                mAVFrameArray[videoStreamIndex]->data[0] = mEncoderDataArray[videoStreamIndex];
                //mAVFrameArray[videoStreamIndex]->pkt_size = nLen;
                mAVFrameArray[videoStreamIndex]->pts =
                        (pts - beginTimeStampArray[videoStreamIndex] -
                         20 * ((double)mBufferSizeArray[videoStreamIndex] / 320 - 1)) /
                        (double) (av_q2d(pst->time_base) * 1000);
                AVPacket pkt;
                av_new_packet(&pkt, mBufferSizeArray[videoStreamIndex]);
                ret = avcodec_encode_audio2(c, &pkt,
                                            mAVFrameArray[videoStreamIndex], &got_frame);
                if (ret < 0) {
                    LOGV("Failed to encode! %zd\n", ret);
                    av_packet_unref(&pkt);
                    return;
                }
                if (got_frame == 1) {
                    pkt.stream_index = m_pOcArray[videoStreamIndex]->streams[aiArray[videoStreamIndex]]->index;
                } else {
                    LOGV("fail to encode 1 frame! \tsize:%zd\tgot_frame:%zd\n", pkt.size,
                         got_frame);
                }
                mAudioIndexArray[videoStreamIndex]++;
                if (got_frame == 1) {
                    ret = av_interleaved_write_frame(m_pOcArray[videoStreamIndex], &pkt);
                    if (ret < 0) {
                        LOGV("cannot write audio frame");
                    }
                    av_packet_unref(&pkt);
                }
                if (remainLength != 0) {
                    mBufferIndexArray[videoStreamIndex] = remainLength;
                    memcpy(mEncoderDataArray[0], &data[nLen - remainLength], remainLength);
                }
            }
        }
    } else {
        AVPacket pkt;
        av_init_packet(&pkt);
        pkt.flags |= (0 >= getVopType(data, nLen)) ? AV_PKT_FLAG_KEY : 0;
        LOGV("flags");
        pkt.stream_index = pst->index;
        pkt.data = (uint8_t *) data;
        pkt.size = nLen;
        // Wait for key frame
        if (frameType != 2) {
            if (waitkeyArray[videoStreamIndex])
                if (0 == (pkt.flags & AV_PKT_FLAG_KEY))
                    return;
                else
                    waitkeyArray[videoStreamIndex] = 0;
        }
        int oneAudioFrameDuration = 20*(double)mBufferSizeArray[videoStreamIndex]/ 320;
        pkt.pts = (pts - beginTimeStampArray[videoStreamIndex]) /
                  (double) (av_q2d(pst->time_base) * 1000);
        if(lastAudioTimeStampArray[videoStreamIndex] == 0 && pts - beginTimeStampArray[videoStreamIndex] >  oneAudioFrameDuration){
            LOGV("pts:%zd\tbeginTimeStampArray[videoStreamIndex]:%zd\toneAudioFrameDuration:%zd",pts,beginTimeStampArray[videoStreamIndex],oneAudioFrameDuration);
            //writeEmptyAudioData(videoStreamIndex,(int)((pts - beginTimeStampArray[videoStreamIndex]- (20*mBufferSizeArray[videoStreamIndex]/320)) /(double) (av_q2d(pst->time_base) * 1000)) );
            int ptss = (int)((pts - beginTimeStampArray[videoStreamIndex]- oneAudioFrameDuration) /(double) (av_q2d(pst->time_base) * 1000));
            writeEmptyAudioData(videoStreamIndex,ptss);
            lastAudioTimeStampArray[videoStreamIndex] = pts - oneAudioFrameDuration;
        }
        if(lastAudioTimeStampArray[videoStreamIndex] != 0 && pts - lastAudioTimeStampArray[videoStreamIndex]>oneAudioFrameDuration){
            LOGV("lastAudioTimeStampArray:%zd\tpts:%zd\tlastAudioTimeStampArray[videoStreamIndex]:%zd\toneAudioFrameDuration:%zd",lastAudioTimeStampArray[videoStreamIndex],pts,lastAudioTimeStampArray[videoStreamIndex],oneAudioFrameDuration);
            int ptss = pkt.pts;
            writeEmptyAudioData(videoStreamIndex,ptss);
            //writeEmptyAudioData(videoStreamIndex,(int)((pts - lastAudioTimeStampArray[videoStreamIndex]- (20*mBufferSizeArray[videoStreamIndex]/320)) /(double) (av_q2d(pst->time_base) * 1000)));
            lastAudioTimeStampArray[videoStreamIndex] = pts;
        }
//    pkt.pts = (ptsIncArray[videoStreamIndex]++) * (90000/STREAM_FRAME_RATE);
//    pkt.pts = av_rescale_q((ptsIncArray[videoStreamIndex]++)*2, pst->codec->time_base,pst->time_base);
//    //pkt.dts = (ptsInc++) * (90000/STREAM_FRAME_RATE);
//    //  pkt.pts=av_rescale_q_rnd(pkt.pts, pst->time_base,pst->time_base,(AVRounding)(AV_ROUND_NEAR_INF|AV_ROUND_PASS_MINMAX));
//    pkt.dts=av_rescale_q_rnd(pkt.dts, pst->time_base,pst->time_base,(AV_ROUND_NEAR_INF|AV_ROUND_PASS_MINMAX));

        pkt.duration = 0;//av_rescale_q(pkt.duration,pst->time_base, pst->time_base);
        pkt.pos = -1;
        got_frame = 1;
        LOGV("pkt.size=%d\n", pkt.size);
        ret = av_interleaved_write_frame(m_pOcArray[videoStreamIndex], &pkt);
        if (ret < 0) {
            LOGV("cannot write frame");
        }
        av_packet_unref(&pkt);
    }

    if (frameType != 2) {
        lastVideoTimeStampArray[videoStreamIndex] = pts;
    } else {
        lastAudioTimeStampArray[videoStreamIndex] = pts;
    }
}

int writeEmptyAudioData(int videoStreamIndex, int pts) {
    LOGV("add empty audio dataq! %zd\n", pts);
    int ret = 0;
    int got_frame = 0;
    AVCodecContext *c = m_pOcArray[videoStreamIndex]->streams[aiArray[videoStreamIndex]]->codec;
    mAVFrameArray[videoStreamIndex]->pkt_size = mBufferSizeArray[videoStreamIndex];
    // memset(mEncoderDataArray[videoStreamIndex], 0, mBufferSizeArray[videoStreamIndex]);
    //memcpy(mEncoderDataArray[videoStreamIndex],data,nLen);
    mAVFrameArray[videoStreamIndex]->data[0] = mEncoderDataEmpty;
    mAVFrameArray[videoStreamIndex]->pts =pts* av_q2d(m_pOcArray[videoStreamIndex]->streams[viArray[videoStreamIndex]]->time_base)/av_q2d(m_pOcArray[videoStreamIndex]->streams[aiArray[videoStreamIndex]]->time_base);
    AVPacket pkt;
    av_new_packet(&pkt, mBufferSizeArray[videoStreamIndex]);
    ret = avcodec_encode_audio2(c, &pkt,
                                mAVFrameArray[videoStreamIndex], &got_frame);
    if (ret < 0) {
        LOGV("Failed to encode! %zd\n", ret);
        av_packet_unref(&pkt);
        return -1;
    }
    if (got_frame == 1) {
        pkt.stream_index = m_pOcArray[videoStreamIndex]->streams[aiArray[videoStreamIndex]]->index;
    } else {
        LOGV("fail to encode 1 frame! \tsize:%zd\tgot_frame:%zd\n", pkt.size,
             got_frame);
    }
    mAudioIndexArray[videoStreamIndex]++;
    if (got_frame == 1) {
        ret = av_interleaved_write_frame(m_pOcArray[videoStreamIndex], &pkt);
        if (ret < 0) {
            LOGV("cannot write frame");
        }
        av_packet_unref(&pkt);
    }
    return ret;
}

int flush_encoder(AVFormatContext *fmt_ctx, unsigned int stream_index) {
    int ret;
    int got_frame;
    AVPacket enc_pkt;
    if (!(fmt_ctx->streams[stream_index]->codec->codec->capabilities &
          CODEC_CAP_DELAY))
        return 0;
    while (1) {
        enc_pkt.data = NULL;
        enc_pkt.size = 0;
        av_init_packet(&enc_pkt);
        LOGV("avcodec_encode_audio2");
        ret = avcodec_encode_audio2(fmt_ctx->streams[stream_index]->codec, &enc_pkt,
                                    NULL, &got_frame);
        av_frame_free(NULL);
        if (ret < 0)
            break;
        if (!got_frame) {
            ret = 0;
            break;
        }
        LOGV("Flush Encoder: Succeed to encode 1 frame!\tsize:%5d\n", enc_pkt.size);
        /* mux encoded frame */
        ret = av_write_frame(fmt_ctx, &enc_pkt);
        if (ret < 0)
            break;
    }
    return ret;
}

void short2float(short *in, void *out, int len) {
    register int i;
    for (i = 0; i < len; i++)
        ((float *) out)[i] = ((float) (in[i])) / 32767.0;
}


JNIEXPORT jint JNICALL
Java_com_recorder_util_mp4Recorder_initMp4(JNIEnv *env, jclass type, jint width,
                                           jint height, jstring videoPath_) {
    const char *videoPath = (*env)->GetStringUTFChars(env, videoPath_, 0);
    int videoStreamIndex = CreateMp4(videoPath, width, height);
    // TODO

    (*env)->ReleaseStringUTFChars(env, videoPath_, videoPath);
    return videoStreamIndex;
}

JNIEXPORT jint JNICALL
Java_com_recorder_util_mp4Recorder_uninitMp4(JNIEnv *env, jclass type,
                                             jint videoStreamIndex) {
    int ret = CloseMp4(videoStreamIndex);
    // TODO
    return ret;
}

JNIEXPORT jint JNICALL
Java_com_recorder_util_mp4Recorder_writeData(JNIEnv *env, jclass type,
                                             jbyteArray data_, jint size,
                                             jint frameType, jint pts,
                                             jint videoStreamIndex) {
    jbyte *data = (*env)->GetByteArrayElements(env, data_, NULL);

    // TODO
    WriteVideo(videoStreamIndex, data, size, pts, frameType);
    (*env)->ReleaseByteArrayElements(env, data_, data, 0);
    return 0;
}


int sssss(int argc, char *argv[]) {
    AVFormatContext *pFormatCtx;
    AVOutputFormat *fmt;
    AVStream *audio_st;
    AVCodecContext *pCodecCtx;
    AVCodec *pCodec;

    uint8_t *frame_buf;
    AVFrame *pFrame;
    AVPacket pkt;

    int got_frame = 0;
    int ret = 0;
    int size = 0;

    FILE *in_file = NULL;                            //Raw PCM data
    int framenum = 1000;                          //Audio frame number
    const char *out_file = "/storage/emulated/0/20180315102240264.mp4.pcm.aac";          //Output URL
    int i;

    in_file = fopen("/storage/emulated/0/20180314060115264.mp4.pcm", "rb");
    av_register_all();

    //Method 1.
    pFormatCtx = avformat_alloc_context();
    fmt = av_guess_format(NULL, out_file, NULL);
    pFormatCtx->oformat = fmt;


    //Method 2.
    //avformat_alloc_output_context2(&pFormatCtx, NULL, NULL, out_file);
    //fmt = pFormatCtx->oformat;

    //Open output URL
    ret = avio_open(&pFormatCtx->pb, out_file, AVIO_FLAG_READ_WRITE);
    if (ret < 0) {
        LOGV("Failed to open output file!%zd %s\n", ret, strerror(errno));
        return -1;
    }

    AVCodec *audio_codec;
    audio_st = add_stream(pFormatCtx, &audio_codec, AV_CODEC_ID_AAC, 0, 0, 0);
    LOGV("add stream\n");
    if (audio_st) {
        //avcodec_find_encoder(audio_st->codec->codec_id)
        int ret = avcodec_open2(audio_st->codec, avcodec_find_encoder(AV_CODEC_ID_AAC), NULL);
        if (ret < 0) {
            LOGV("Failed to open encoder!%zd\n", ret);
            return NULL;
        } else {
            LOGV("succeed to open encoder!%zd\n", ret);
        }
        mAVFrameArray[0] = av_frame_alloc();
        mAVFrameArray[0]->nb_samples = audio_st->codec->frame_size;
        mAVFrameArray[0]->format = audio_st->codec->sample_fmt;
        mAVFrameArray[0]->sample_rate = audio_st->codec->sample_rate;
        mAVFrameArray[0]->channels = audio_st->codec->channels;
        mAVFrameArray[0]->channel_layout = audio_st->codec->channel_layout;
        mBufferSizeArray[0] = av_samples_get_buffer_size(NULL, audio_st->codec->channels,
                                                         audio_st->codec->frame_size,
                                                         audio_st->codec->sample_fmt, 1);
        LOGV("mBufferSizeArray!%zd\n", mBufferSizeArray[0]);
        mEncoderDataArray[0] = (uint8_t *) av_malloc(mBufferSizeArray[0]);
        avcodec_fill_audio_frame(mAVFrameArray[0], audio_st->codec->channels,
                                 audio_st->codec->sample_fmt,
                                 (const uint8_t *) mEncoderDataArray[0], mBufferSizeArray[0], 1);
        LOGV("avcodec_fill_audio_frame!%zd\n", mBufferSizeArray[0]);

    }
    pCodecCtx = audio_st->codec;
    //Write Header
    LOGV("avformat_write_header!%zd\n", pFormatCtx->nb_streams);
//    ret = avformat_write_header(pFormatCtx,NULL);
//    if(ret>=0){
//
//        LOGV("succeed to avformat_write_header!%zd\n",ret);
//    }
//    else{
//
//        LOGV("fail to avformat_write_header!%zd\n",ret);
//    }
    av_new_packet(&pkt, mBufferSizeArray[0]);

    for (i = 0; i < framenum; i++) {
        LOGV("encode!\n");

        //Read PCM
        ret = fread(mEncoderDataArray[0], 1, mBufferSizeArray[0], in_file);
        if (ret <= 0) {
            LOGV("Failed to read raw data! \n");
            return -1;
        } else if (feof(in_file)) {
            LOGV("end file  to read raw data! \n");
            break;
        }
        LOGV("success to read raw data! \n");
        //memset(&mEncoderDataArray[0][0], 0, mBufferSizeArray[0]);
        mAVFrameArray[0]->data[0] = mEncoderDataArray[0];  //PCM Data

        mAVFrameArray[0]->pts = i * 1024;
        got_frame = 0;
        mAVFrameArray[0]->pkt_size = mBufferSizeArray[0];
        //Encode
        LOGV("begin encode! %s\n", strerror(errno));
        ret = avcodec_encode_audio2(pCodecCtx, &pkt, mAVFrameArray[0], &got_frame);
        if (ret < 0) {
            LOGV("Failed to encode!%s\n", strerror(errno));
            return -1;
        }
        if (got_frame == 1) {
            LOGV("Succeed to encode 1 frame! \tsize:%5d\n", pkt.size);
            pkt.stream_index = audio_st->index;
            // ret = av_write_frame(pFormatCtx, &pkt);
            av_free_packet(&pkt);
        } else {
            LOGV("Failed to encode 1 frame! %s\n", strerror(errno));
        }
    }

    //Flush Encoder
    ret = flush_encoder(pFormatCtx, 0);
    if (ret < 0) {
        LOGV("Flushing encoder failed\n");
        return -1;
    }

    //Write Trailer
    av_write_trailer(pFormatCtx);

    //Clean
    if (audio_st) {
        avcodec_close(audio_st->codec);
        av_free(mAVFrameArray[0]);
        av_free(mEncoderDataArray[0]);
    }
    avio_close(pFormatCtx->pb);
    avformat_free_context(pFormatCtx);

    fclose(in_file);

    return 0;
}

JNIEXPORT jint JNICALL
Java_com_recorder_util_mp4Recorder_writeDataTest(JNIEnv *env, jclass type) {
    sssss(0, 0);
    // TODO

}