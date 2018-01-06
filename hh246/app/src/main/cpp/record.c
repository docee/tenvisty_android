#include <stdio.h>
#define __STDC_CONSTANT_MACROS
#include <libavformat/avformat.h>
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


static AVFormatContext *m_pOc;
#define STREAM_FRAME_RATE 15

#define STREAM_PIX_FMT    AV_PIX_FMT_YUV420P /* default pix_fmt */



static int ptsInc = 0;

static int vi = -1;

static int waitkey = 1;
/*-----------------
< 0 = error
   0 = I-Frame
   1 = P-Frame
   2 = B-Frame
   3 = S-Frame
--------------------*/
int getVopType( const void *p, int len )
{
    if ( !p || 6 >= len )
        return -1;
    unsigned char *b = (unsigned char*)p;
    // Verify NAL marker
    if ( b[ 0 ] || b[ 1 ] || 0x01 != b[ 2 ] )
    {   b++;
        if ( b[ 0 ] || b[ 1 ] || 0x01 != b[ 2 ] )
            return -1;
    } // end if
    b += 3;
    // Verify VOP id
    if ( 0xb6 == *b )
    {
        b++;
        return ( *b & 0xc0 ) >> 6;
    } // end if
    switch( *b )
    {
        case 0x65 : return 0;
        case 0x61 : return 1;
        case 0x01 : return 2;
    } // end switch
    return -1;
}
void open_video(AVFormatContext *oc, AVCodec *codec, AVStream *st)
{
    int ret;
    AVCodecContext *c = st->codec;
    /* open the codec */
    ret = avcodec_open2(c, codec, NULL);
    if (ret < 0)
    {
        printf("could not open video codec");
        //exit(1);
    }
}
/* Add an output stream */
AVStream *add_stream(AVFormatContext *oc, AVCodec **codec, enum AVCodecID codec_id,int width,int height)
{
    AVCodecContext *c;
    AVStream *st;
    /* find the encoder */
    *codec = avcodec_find_encoder(codec_id);
    if (!*codec)
    {
        printf("could not find encoder for '%s' \n", avcodec_get_name(codec_id));
        exit(1);
    }
    st = avformat_new_stream(oc, *codec);
    if (!st)
    {
        printf("could not allocate stream \n");
        exit(1);
    }
    st->id = oc->nb_streams-1;
    c = st->codec;
    vi = st->index;
    switch ((*codec)->type)
    {
        case AVMEDIA_TYPE_AUDIO:
            printf("AVMEDIA_TYPE_AUDIO\n");
            c->sample_fmt = (*codec)->sample_fmts ? (*codec)->sample_fmts[0] : AV_SAMPLE_FMT_FLTP;
            c->bit_rate = 64000;
            c->sample_rate = 44100;
            c->channels = 2;
            break;
        case AVMEDIA_TYPE_VIDEO:
            printf("AVMEDIA_TYPE_VIDEO\n");
            c->codec_id = AV_CODEC_ID_H264;
            c->bit_rate = 0;
            c->width = width;
            c->height = height;
            c->time_base.den = 50;
            c->time_base.num = 1;
            c->gop_size = 1;
            c->pix_fmt = STREAM_PIX_FMT;
            if (c->codec_id == AV_CODEC_ID_MPEG2VIDEO)
            {
                c->max_b_frames = 2;
            }
            if (c->codec_id == AV_CODEC_ID_MPEG1VIDEO)
            {
                c->mb_decision = 2;
            }
            break;
        default:
            break;
    }
    if (oc->oformat->flags & AVFMT_GLOBALHEADER)
    {
        c->flags |= CODEC_FLAG_GLOBAL_HEADER;
    }
    return st;
}

void CloseMp4()
{
    waitkey = -1;
    vi = -1;
    if (m_pOc)
        av_write_trailer(m_pOc);
    if (m_pOc && !(m_pOc->oformat->flags & AVFMT_NOFILE))
        avio_close(m_pOc->pb);
    if (m_pOc)
    {
        avformat_free_context(m_pOc);
        m_pOc = NULL;
    }
}

int CreateMp4(const char *pszFileName,int width, int height)
{
    //CloseMp4();
    int ret; // 成功返回0，失败返回1
    AVOutputFormat *fmt;
    AVCodec *video_codec;
    AVStream *m_pVideoSt;
    av_register_all();
    avformat_alloc_output_context2(&m_pOc, NULL, NULL, "/storage/emulated/0/tenvisty/videorecording/7FEEGFU8Z3AEMTDC111A/7FEEGFU8Z3AEMTDC111A_20171022090251.mp4");
    if (!m_pOc)
    {
        printf("Could not deduce output format from file extension: using MPEG. \n");
        avformat_alloc_output_context2(&m_pOc, NULL, "mpeg", pszFileName);
    }
    if (!m_pOc)
    {
        return 1;
    }
    fmt = m_pOc->oformat;
    if (fmt->video_codec != AV_CODEC_ID_NONE)
    {
        printf("1111111111111111add_stream\n");
        m_pVideoSt = add_stream(m_pOc, &video_codec, fmt->video_codec,width,height);
    }
    if (m_pVideoSt)
    {
        printf("1111111111111111open_video\n");
        open_video(m_pOc, video_codec, m_pVideoSt);
    }
    printf("==========Output Information==========\n");
    av_dump_format(m_pOc, 0, pszFileName, 1);
    printf("======================================\n");
    /* open the output file, if needed */
    if (!(fmt->flags & AVFMT_NOFILE))
    {
        ret = avio_open(&m_pOc->pb, pszFileName, AVIO_FLAG_WRITE);
        if (ret < 0)
        {
            printf("could not open %s\n", pszFileName);
            return 1;
        }
    }
    /* Write the stream header, if any */
    ret = avformat_write_header(m_pOc, NULL);
    if (ret < 0)
    {
        printf("Error occurred when opening output file");
        return 1;
    }
    return 0;
}

/* 创建mp4文件返回2；写入数据帧返回0 */
void WriteVideo(void* data, int nLen,int pts)
{
    int ret;
    if ( 0 > vi )
    {
        printf("vi less than 0");
        //return -1;
    }
    AVStream *pst = m_pOc->streams[ vi ];
    //printf("vi=====%d\n",vi);
    // Init packet
    AVPacket pkt;
    // 我的添加，为了计算pts
    AVCodecContext *c = pst->codec;
    av_init_packet( &pkt );
    pkt.flags |= ( 0 >= getVopType( data, nLen ) ) ? AV_PKT_FLAG_KEY : 0;
    pkt.stream_index = pst->index;
    pkt.data = (uint8_t*)data;
    pkt.size = nLen;
    // Wait for key frame
    if ( waitkey )
        if ( 0 == ( pkt.flags & AV_PKT_FLAG_KEY ) )
            return ;
        else
            waitkey = 0;
    pkt.pts = (ptsInc++) * (90000/STREAM_FRAME_RATE);
    pkt.pts = av_rescale_q((ptsInc++)*2, pst->codec->time_base,pst->time_base);
    //pkt.dts = (ptsInc++) * (90000/STREAM_FRAME_RATE);
    //  pkt.pts=av_rescale_q_rnd(pkt.pts, pst->time_base,pst->time_base,(AVRounding)(AV_ROUND_NEAR_INF|AV_ROUND_PASS_MINMAX));
    pkt.dts=av_rescale_q_rnd(pkt.dts, pst->time_base,pst->time_base,(AV_ROUND_NEAR_INF|AV_ROUND_PASS_MINMAX));
    pkt.duration = av_rescale_q(pkt.duration,pst->time_base, pst->time_base);
    pkt.pos = -1;
    printf("pkt.size=%d\n",pkt.size);
    ret = av_interleaved_write_frame( m_pOc, &pkt );
    if (ret < 0)
    {
        printf("cannot write frame");
    }
}
int main(int argc, char* argv[])

{

    AVOutputFormat *ofmt = NULL;
    //Input AVFormatContext and Output AVFormatContext
    AVFormatContext *ifmt_ctx_v = NULL, *ifmt_ctx_a = NULL,*ofmt_ctx = NULL;
    AVPacket pkt;
    int ret, i;
    int videoindex_v=0,videoindex_out=0;
    int frame_index=0;
    int64_t cur_pts_v=0,cur_pts_a=0;
    //const char *in_filename_v = "cuc_ieschool.ts";//Input file URL
    const char *in_filename_v = "vpu.h264";
    //const char *in_filename_a = "cuc_ieschool.mp3";
    //const char *in_filename_a = "gowest.m4a";
    //const char *in_filename_a = "gowest.aac";
    const char *in_filename_a = "huoyuanjia.mp3";
    const char *out_filename = "vpu.mp4";//Output file URL
    av_register_all();
    //Input
    if ((ret = avformat_open_input(&ifmt_ctx_v, in_filename_v, 0, 0)) < 0) {
        printf( "Could not open input file.");
        goto end;

    }
    if ((ret = avformat_find_stream_info(ifmt_ctx_v, 0)) < 0) {
        printf( "Failed to retrieve input stream information");
        goto end;
    }
    /*if ((ret = avformat_open_input(&ifmt_ctx_a, in_filename_a, 0, 0)) < 0) {
        printf( "Could not open input file.");
        goto end;
    }
    if ((ret = avformat_find_stream_info(ifmt_ctx_a, 0)) < 0) {
        printf( "Failed to retrieve input stream information");
        goto end;
    }*/
    printf("===========Input Information==========\n");
    av_dump_format(ifmt_ctx_v, 0, in_filename_v, 0);
    //av_dump_format(ifmt_ctx_a, 0, in_filename_a, 0);
    printf("======================================\n");
    //Output
    avformat_alloc_output_context2(&ofmt_ctx, NULL, NULL, out_filename);
    if (!ofmt_ctx) {
        printf( "Could not create output context\n");
        ret = AVERROR_UNKNOWN;
        goto end;
    }
    ofmt = ofmt_ctx->oformat;
    printf("ifmt_ctx_v->nb_streams=%d\n",ifmt_ctx_v->nb_streams);
    for (i = 0; i < ifmt_ctx_v->nb_streams; i++) {
        //Create output AVStream according to input AVStream
        //if(ifmt_ctx_v->streams[i]->codec->codec_type==AVMEDIA_TYPE_VIDEO)
        {
            AVStream *in_stream = ifmt_ctx_v->streams[i];
            AVStream *out_stream = avformat_new_stream(ofmt_ctx, in_stream->codec->codec);
            videoindex_v=i;
            if (!out_stream) {
                printf( "Failed allocating output stream\n");
                ret = AVERROR_UNKNOWN;
                goto end;
            }
            videoindex_out=out_stream->index;
            //Copy the settings of AVCodecContext
            if (avcodec_copy_context(out_stream->codec, in_stream->codec) < 0) {
                printf( "Failed to copy context from input to output stream codec context\n");
                goto end;
            }
            out_stream->codec->codec_tag = 0;
            if (ofmt_ctx->oformat->flags & AVFMT_GLOBALHEADER)
                out_stream->codec->flags |= CODEC_FLAG_GLOBAL_HEADER;
            //break;
        }
    }
/*
	for (i = 0; i < ifmt_ctx_a->nb_streams; i++) {
		//Create output AVStream according to input AVStream
		if(ifmt_ctx_a->streams[i]->codec->codec_type==AVMEDIA_TYPE_AUDIO){
			AVStream *in_stream = ifmt_ctx_a->streams[i];
			AVStream *out_stream = avformat_new_stream(ofmt_ctx, in_stream->codec->codec);
			audioindex_a=i;
			if (!out_stream) {
				printf( "Failed allocating output stream\n");
				ret = AVERROR_UNKNOWN;
				goto end;
			}
			audioindex_out=out_stream->index;
			//Copy the settings of AVCodecContext
			if (avcodec_copy_context(out_stream->codec, in_stream->codec) < 0) {
				printf( "Failed to copy context from input to output stream codec context\n");
				goto end;
			}
			out_stream->codec->codec_tag = 0;
			if (ofmt_ctx->oformat->flags & AVFMT_GLOBALHEADER)
				out_stream->codec->flags |= CODEC_FLAG_GLOBAL_HEADER;
			break;
		}
	}
*/
    printf("==========Output Information==========\n");
    av_dump_format(ofmt_ctx, 0, out_filename, 1);
    printf("======================================\n");
    //Open output file
    if (!(ofmt->flags & AVFMT_NOFILE)) {
        if (avio_open(&ofmt_ctx->pb, out_filename, AVIO_FLAG_WRITE) < 0) {
            printf( "Could not open output file '%s'", out_filename);
            goto end;
        }
    }
    //Write file header
    if (avformat_write_header(ofmt_ctx, NULL) < 0) {
        printf( "Error occurred when opening output file\n");
        goto end;
    }
    //FIX
#if USE_H264BSF
    AVBitStreamFilterContext* h264bsfc =  av_bitstream_filter_init("h264_mp4toannexb");
#endif
#if USE_AACBSF
    AVBitStreamFilterContext* aacbsfc =  av_bitstream_filter_init("aac_adtstoasc");
#endif
    while (1) {
        AVFormatContext *ifmt_ctx;
        int stream_index=0;
        AVStream *in_stream, *out_stream;
        //Get an AVPacket
        //if(av_compare_ts(cur_pts_v,ifmt_ctx_v->streams[videoindex_v]->time_base,cur_pts_a,ifmt_ctx_a->streams[audioindex_a]->time_base) <= 0)
        {
            ifmt_ctx=ifmt_ctx_v;
            stream_index=videoindex_out;
            if(av_read_frame(ifmt_ctx, &pkt) >= 0){
                do{
                    in_stream  = ifmt_ctx->streams[pkt.stream_index];
                    out_stream = ofmt_ctx->streams[stream_index];
                    printf("stream_index==%d,pkt.stream_index==%d,videoindex_v=%d\n", stream_index,pkt.stream_index,videoindex_v);
                    if(pkt.stream_index==videoindex_v){
                        //FIX：No PTS (Example: Raw H.264)
                        //Simple Write PTS
                        if(pkt.pts==AV_NOPTS_VALUE){
                            printf("frame_index==%d\n",frame_index);
                            //Write PTS
                            AVRational time_base1=in_stream->time_base;
                            //Duration between 2 frames (us)
                            int64_t calc_duration=(double)AV_TIME_BASE/av_q2d(in_stream->r_frame_rate);
                            //Parameters
                            pkt.pts=(double)(frame_index*calc_duration)/(double)(av_q2d(time_base1)*AV_TIME_BASE);
                            pkt.dts=pkt.pts;
                            pkt.duration=(double)calc_duration/(double)(av_q2d(time_base1)*AV_TIME_BASE);
                            frame_index++;
                        }
                        cur_pts_v=pkt.pts;
                        break;
                    }
                }while(av_read_frame(ifmt_ctx, &pkt) >= 0);
            }else{
                break;
            }
        }
        /*else
        {
            ifmt_ctx=ifmt_ctx_a;
            stream_index=audioindex_out;
            if(av_read_frame(ifmt_ctx, &pkt) >= 0){
                do{
                    in_stream  = ifmt_ctx->streams[pkt.stream_index];
                    out_stream = ofmt_ctx->streams[stream_index];
                    if(pkt.stream_index==audioindex_a){
                        //FIX：No PTS
                        //Simple Write PTS
                        if(pkt.pts==AV_NOPTS_VALUE){
                            //Write PTS
                            AVRational time_base1=in_stream->time_base;
                            //Duration between 2 frames (us)
                            int64_t calc_duration=(double)AV_TIME_BASE/av_q2d(in_stream->r_frame_rate);
                            //Parameters
                            pkt.pts=(double)(frame_index*calc_duration)/(double)(av_q2d(time_base1)*AV_TIME_BASE);
                            pkt.dts=pkt.pts;
                            pkt.duration=(double)calc_duration/(double)(av_q2d(time_base1)*AV_TIME_BASE);
                            frame_index++;
                        }
                        cur_pts_a=pkt.pts;
                        break;
                    }
                }while(av_read_frame(ifmt_ctx, &pkt) >= 0);
            }else{
                break;
            }
        }*/
        //FIX:Bitstream Filter
#if USE_H264BSF
        av_bitstream_filter_filter(h264bsfc, in_stream->codec, NULL, &pkt.data, &pkt.size, pkt.data, pkt.size, 0);
#endif
#if USE_AACBSF
        av_bitstream_filter_filter(aacbsfc, out_stream->codec, NULL, &pkt.data, &pkt.size, pkt.data, pkt.size, 0);
#endif
        //Convert PTS/DTS
        pkt.pts = av_rescale_q_rnd(pkt.pts, in_stream->time_base, out_stream->time_base, (AV_ROUND_NEAR_INF|AV_ROUND_PASS_MINMAX));
        pkt.dts = av_rescale_q_rnd(pkt.dts, in_stream->time_base, out_stream->time_base, (AV_ROUND_NEAR_INF|AV_ROUND_PASS_MINMAX));
        pkt.duration = av_rescale_q(pkt.duration, in_stream->time_base, out_stream->time_base);
        pkt.pos = -1;
        pkt.stream_index=stream_index;
        printf("Write 1 Packet. size:%5d\tpts:%lld\n",pkt.size,pkt.pts);
        //Write
        if (av_interleaved_write_frame(ofmt_ctx, &pkt) < 0) {
            printf( "Error muxing packet\n");
            break;
        }
        av_free_packet(&pkt);
    }
    //Write file trailer
    av_write_trailer(ofmt_ctx);
#if USE_H264BSF
    av_bitstream_filter_close(h264bsfc);
#endif
#if USE_AACBSF
    av_bitstream_filter_close(aacbsfc);
#endif
    end:
    avformat_close_input(&ifmt_ctx_v);
    //avformat_close_input(&ifmt_ctx_a);
    /* close output */
    if (ofmt_ctx && !(ofmt->flags & AVFMT_NOFILE))
        avio_close(ofmt_ctx->pb);
    avformat_free_context(ofmt_ctx);
    if (ret < 0 && ret != AVERROR_EOF) {
        printf( "Error occurred.\n");
        return -1;
    }
    return 0;
}