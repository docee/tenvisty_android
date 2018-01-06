package com.tutk.IOTC;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

/*enum StreamType
{
	STREAM_AUDIO,		// audio data
	STREAM_VIDEO_C,		// compressed video
	STREAM_VIDEO_B		// uncompressed video
}
*/
/**
 * 摄像头录像
 * @version V1.1,2012-10-30
 * @author David.chen
 *
 */
public class AVIGeneratorH {
	/*
     *  Info needed for MJPEG AVI
     *
     *  - size of file minus "RIFF & 4 byte file size"
     *
     */
    
    int width = 0;
    int height = 0;
    double framerate = 0, samplerate = 0;
    int numFrames = 0, numSamples = 0;
    File aviFile = null, indexFile = null;
    FileOutputStream aviOutput = null;
    FileChannel aviChannel = null;
    
    long riffOffset = 0;
    long aviMovieOffset = 0;
    long mainHdrOffset = 0;
    long videostarttime = 0, audiostartime = 0;
    long hdrlBeginPos = 0, hdrlEndPos = 0, fileEndPos = 0, strlBegPos = 0, strlEndPos =0;
    
    AVIIndexList indexlist = null;
    
    /**
     * Creates a new instance of MJPEGGenerator 
     * @param aviFile File
     * @param width int
     * @param height int
     * @param framerate double
     * @param numFrames int
     * @throws Exception
     */
    private AVIGeneratorH(File aviFile, int width, int height, double framerate, int numFrames) throws Exception
    {
        this.aviFile = aviFile;
        this.width = width;
        this.height = height;
        this.framerate = framerate;
        this.numFrames = numFrames;
        aviOutput = new FileOutputStream(aviFile);
        aviChannel = aviOutput.getChannel();
        
        RIFFHeader rh = new RIFFHeader();
        aviOutput.write(rh.toBytes());
        hdrlBeginPos = mainHdrOffset = aviChannel.position();        
        aviOutput.write(new AVIMainHeader().toBytes());
        strlBegPos = aviChannel.position();
        aviOutput.write(new AVIStreamVideoList().toBytes());
        aviOutput.write(new AVIStreamVideoHeader().toBytes());
        aviOutput.write(new AVIStreamVideoFormat().toBytes());
        aviOutput.write(new AVIStreamAudioList().toBytes());
        aviOutput.write(new AVIStreamAudioHeader().toBytes());
        aviOutput.write(new AVIStreamAudioFormat().toBytes());
        aviOutput.write(new AVIStreamAudioList().toBytes());
        aviOutput.write(new AVIStreamAudioHeader().toBytes());
        aviOutput.write(new AVIStreamAudioFormat().toBytes());
        strlEndPos = hdrlEndPos = aviChannel.position();
        aviOutput.write(new AVIJunk().toBytes());
        aviMovieOffset = aviChannel.position();
        aviOutput.write(new AVIMovieList().toBytes());
        indexlist = new AVIIndexList();
    }
    
    /**
     * Creates a new instance of MJPEGGenerator 
     * @param aviFile
     * @param width
     * @param height
     * @throws Exception
     */
    private AVIGeneratorH(File aviFile, int width, int height) throws Exception
    {
    	this(aviFile, width, height, (double)30, 0);
    }
    
    public AVIGeneratorH(File aviFile)
    {
    	this.aviFile = aviFile;
    }
    
    boolean mVideoStream = false;
    boolean mAudioStream = false;
    boolean mTalkStream = false;
    
    int mVideoIndex = -1;
    int mAudioIndex = -1;
    int mTalkIndex = -1;
    
    /**
     * 添加视频流
     * @param height
     * @param width
     * @return true
     */
    public boolean addVideoStream(int height, int width)
    {
    	this.height = height;
    	this.width = width;
    	mVideoStream = true;
    	return true;
    }
    
    /**
     * 添加音频输出流
     * @return true
     */
    public boolean addAudioStream()
    {
    	mAudioStream = true;
    	return true;
    }
    
    /**
     * 添加麦克风输入流
     * @return true
     */
    public boolean addTalkStream()
    {
    	mTalkStream = true;
    	return true;
    }
    
    /**
     * 开始录像
     * @return boolean
     * @throws Exception
     */
    public boolean startAVI() throws Exception
    {
        aviOutput = new FileOutputStream(aviFile);
        aviChannel = aviOutput.getChannel();
        
        RIFFHeader rh = new RIFFHeader();
        aviOutput.write(rh.toBytes());
        hdrlBeginPos = mainHdrOffset = aviChannel.position();        
        aviOutput.write(new AVIMainHeader().toBytes());
        strlBegPos = aviChannel.position();
        int index = 0;
        if(mVideoStream)
        {
        	mVideoIndex = index++;
	        aviOutput.write(new AVIStreamVideoList().toBytes());
	        aviOutput.write(new AVIStreamVideoHeader().toBytes());
	        aviOutput.write(new AVIStreamVideoFormat().toBytes());
        }
        
        if(mAudioStream)
        {
        	mAudioIndex = index++;
	        aviOutput.write(new AVIStreamAudioList().toBytes());
	        aviOutput.write(new AVIStreamAudioHeader().toBytes());
	        aviOutput.write(new AVIStreamAudioFormat().toBytes());
        }
        
        if(mTalkStream)
        {
        	mTalkIndex = index++;
	        aviOutput.write(new AVIStreamAudioList().toBytes());
	        aviOutput.write(new AVIStreamAudioHeader().toBytes());
	        aviOutput.write(new AVIStreamAudioFormat().toBytes());
        }
        
        strlEndPos = hdrlEndPos = aviChannel.position();
        aviOutput.write(new AVIJunk().toBytes());
        aviMovieOffset = aviChannel.position();
        aviOutput.write(new AVIMovieList().toBytes());
        indexlist = new AVIIndexList();
        
        indexFile = new File(aviFile.getAbsolutePath()+".index");        
        
    	return (indexFile != null);
    }

    /**
     * 添加图像
     * @param imagedata byte[]
     * @throws Exception
     */
	public void addImage(byte[] imagedata) throws Exception
    {
		if(!mVideoStream)
			return;
		
        byte[] fcc = new byte[]{'0',(byte) (mVideoIndex + 0x30),'d','b'};
        //byte[] imagedata = writeImageToBytes(image);
        int useLength = imagedata.length;
        long position = aviChannel.position();
        int extra = (useLength+(int)position) % 4;
        if(extra > 0)
            useLength = useLength + extra;
        
        indexlist.addAVIIndex(mVideoIndex, StreamType.STREAM_VIDEO_B, (int)position,useLength);
        
        aviOutput.write(fcc);
        aviOutput.write(intBytes(swapInt(useLength)));
        aviOutput.write(imagedata);
        if(extra > 0)
        {
            for(int i = 0; i < extra; i++)
                aviOutput.write(0);
        }
        
        numFrames++;
        if(videostarttime == 0)
        {
        	videostarttime = Calendar.getInstance().getTimeInMillis();
        }
        
        imagedata = null;
    }
	
	/**
	 * 添加音频
	 * @param audiodata byte[]
	 * @param offset int
	 * @param length int
	 * @throws Exception
	 */
	public void addAudio(byte[] audiodata, int offset, int length) throws Exception
    {
		if(!mAudioStream){
			return;
		}
		
        byte[] fcc = new byte[]{'0',(byte) (mAudioIndex + 0x30),'w','b'};
        //byte[] imagedata = writeImageToBytes(image);
        int useLength = length;
        long position = aviChannel.position();
        int extra = (useLength+(int)position) % 4;
        if(extra > 0)
            useLength = useLength + extra;
        
        indexlist.addAVIIndex(mAudioIndex, StreamType.STREAM_AUDIO, (int)position,useLength);
        
        aviOutput.write(fcc);
        aviOutput.write(intBytes(swapInt(useLength)));
        aviOutput.write(audiodata, offset, length);
        if(extra > 0)
        {
            for(int i = 0; i < extra; i++)
                aviOutput.write(0);
        }
        if(audiostartime == 0)
        	audiostartime = Calendar.getInstance().getTimeInMillis();
        numSamples++;
        audiodata = null;
    }
	
	/**
	 * 添加麦克风输入流
	 * @param audiodata byte[]
	 * @param offset int
	 * @param length int
	 * @throws Exception
	 */
	public void addTalk(byte[] audiodata, int offset, int length) throws Exception
    {
		if(!mTalkStream)
			return;
		
        byte[] fcc = new byte[]{'0',(byte) (mTalkIndex + 0x30),'w','b'};
        //byte[] imagedata = writeImageToBytes(image);
        int useLength = length;
        long position = aviChannel.position();
        int extra = (useLength+(int)position) % 4;
        if(extra > 0)
            useLength = useLength + extra;
        
        indexlist.addAVIIndex(mTalkIndex, StreamType.STREAM_AUDIO, (int)position,useLength);
        
        aviOutput.write(fcc);
        aviOutput.write(intBytes(swapInt(useLength)));
        aviOutput.write(audiodata, offset, length);
        if(extra > 0)
        {
            for(int i = 0; i < extra; i++)
                aviOutput.write(0);
        }

        audiodata = null;
    }
    
	/**
	 * 录像完成
	 * @throws Exception
	 */
    public void finishAVI() throws Exception
    {
        //byte[] indexlistBytes = indexlist.toBytes();
        FileOutputStream fos;
		try {
			fos = new FileOutputStream(indexFile);
			fos.write(indexlist.toBytes());
			fos.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
        long endtime = Calendar.getInstance().getTimeInMillis();
        byte[] fcc_index = new byte[]{'i','d','x','1'};
        aviOutput.write(fcc_index);
        aviOutput.write(swapInt((int)indexFile.length()));
        FileInputStream fis = new FileInputStream(indexFile);
        byte[] buffer = new byte[4096];
        int nRead;
        while(true)
        {
	        nRead = fis.read(buffer);
	        if(nRead > 0)
	        {
	        	aviOutput.write(buffer, 0, nRead);
	        }
	        else
	        {
	        	break;
	        }
        }
        
        //aviOutput.write(indexlistBytes);
        fis.close();
        aviOutput.close();
        long size = aviFile.length();
        fileEndPos = size;
        RandomAccessFile raf = new RandomAccessFile(aviFile, "rw");
        this.framerate = this.numFrames*1000/(endtime - videostarttime);
        this.samplerate = this.numSamples * 1000 / (endtime - videostarttime);
        raf.write(new RIFFHeader().toBytes());        
        raf.write(new AVIMainHeader().toBytes());
        if(mVideoStream)
        {
	        raf.write(new AVIStreamVideoList().toBytes());
	        raf.write(new AVIStreamVideoHeader().toBytes());
	        raf.write(new AVIStreamVideoFormat().toBytes());
	        mVideoStream = false;
        }
        
        if(mAudioStream)
        {
	        raf.write(new AVIStreamAudioList().toBytes());
	        raf.write(new AVIStreamAudioHeader().toBytes());
	        raf.write(new AVIStreamAudioFormat().toBytes());
	        mAudioStream = false;
        }
        
        if(mTalkStream)
        {
	        raf.write(new AVIStreamAudioList().toBytes());
	        raf.write(new AVIStreamAudioHeader().toBytes());
	        raf.write(new AVIStreamAudioFormat().toBytes());
	        mTalkStream = false;
        }
        raf.seek(aviMovieOffset+4);
        raf.write(intBytes(swapInt((int)(size - 8 - aviMovieOffset - (indexFile.length() + 8)))));
        raf.close();
        indexFile.delete();
    }
    
//    public void writeAVI(File file) throws Exception
//    {
//        OutputStream os = new FileOutputStream(file);
//        
//        // RIFFHeader
//        // AVIMainHeader
//        // AVIStreamList
//        // AVIStreamHeader
//        // AVIStreamFormat
//        // write 00db and image bytes...
//    }
    
    public static int swapInt(int v)
    {
        return  (v >>> 24) | (v << 24) |
                ((v << 8) & 0x00FF0000) | ((v >> 8) & 0x0000FF00);
    }
    
    public static short swapShort(short v)
    {
        return (short)((v >>> 8) | (v << 8));
    }
    
    public static byte[] intBytes(int i)
    {
        byte[] b = new byte[4];
        b[0] = (byte)(i >>> 24);
        b[1] = (byte)((i >>> 16) & 0x000000FF);
        b[2] = (byte)((i >>> 8) & 0x000000FF);
        b[3] = (byte)(i & 0x000000FF);
        
        return b;
    }
    
    public static byte[] shortBytes(short i)
    {
        byte[] b = new byte[2];
        b[0] = (byte)(i >>> 8);
        b[1] = (byte)(i & 0x000000FF);
        
        return b;
    }
    
    /**
     * RIFF文件头
     * @author David.Chen
     *
     */
    private class RIFFHeader
    {
        public byte[] fcc = new byte[]{'R','I','F','F'};
        public int fileSize = (int) fileEndPos - 8;
        public byte[] fcc2 = new byte[]{'A','V','I',' '};
        public byte[] fcc3 = new byte[]{'L','I','S','T'};
        public int listSize = (int) (hdrlEndPos - hdrlBeginPos + 4);
        public byte[] fcc4 = new byte[]{'h','d','r','l'};
        
        public RIFFHeader()
        {
            
        }
        
        public byte[] toBytes() throws Exception
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(fcc);
            baos.write(intBytes(swapInt(fileSize)));
            baos.write(fcc2);
            baos.write(fcc3);
            baos.write(intBytes(swapInt(listSize)));
            baos.write(fcc4);
            baos.close();
            
            return baos.toByteArray();
        }
    }
    
    /**
     * AVI文件头
     * @author Administrator
     *
     */
    private class AVIMainHeader
    {
        /*
         *
         FOURCC fcc;
    DWORD  cb;
    DWORD  dwMicroSecPerFrame;
    DWORD  dwMaxBytesPerSec;
    DWORD  dwPaddingGranularity;
    DWORD  dwFlags;
    DWORD  dwTotalFrames;
    DWORD  dwInitialFrames;
    DWORD  dwStreams;
    DWORD  dwSuggestedBufferSize;
    DWORD  dwWidth;
    DWORD  dwHeight;
    DWORD  dwReserved[4];
         */
        
        public byte[] fcc = new byte[]{'a','v','i','h'};
        public int cb = 56;
        public int dwMicroSecPerFrame = 0; //  (1 / frames per sec) * 1,000,000
        public int dwMaxBytesPerSec = 10000000;
        public int dwPaddingGranularity = 0;
        public int dwFlags =  65552;
        public int dwTotalFrames = 0;  // replace with correct value
        public int dwInitialFrames = 0;
        public int dwStreams = (mVideoStream?1:0) + (mAudioStream?1:0) + (mTalkStream?1:0);
        public int dwSuggestedBufferSize = 0;
        public int dwWidth = 0;  // replace with correct value
        public int dwHeight = 0; // replace with correct value
        public int[] dwReserved = new int[4];
        
        public AVIMainHeader()
        {
            dwMicroSecPerFrame = (int)((1.0/framerate)*1000000.0);
            dwWidth = width;
            dwHeight = height;
            dwTotalFrames = numFrames;
        }
        
        public byte[] toBytes() throws Exception
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(fcc);
            baos.write(intBytes(swapInt(cb)));
            baos.write(intBytes(swapInt(dwMicroSecPerFrame)));
            baos.write(intBytes(swapInt(dwMaxBytesPerSec)));
            baos.write(intBytes(swapInt(dwPaddingGranularity)));
            baos.write(intBytes(swapInt(dwFlags)));
            baos.write(intBytes(swapInt(dwTotalFrames)));
            baos.write(intBytes(swapInt(dwInitialFrames)));
            baos.write(intBytes(swapInt(dwStreams)));
            baos.write(intBytes(swapInt(dwSuggestedBufferSize)));
            baos.write(intBytes(swapInt(dwWidth)));
            baos.write(intBytes(swapInt(dwHeight)));
            baos.write(intBytes(swapInt(dwReserved[0])));
            baos.write(intBytes(swapInt(dwReserved[1])));
            baos.write(intBytes(swapInt(dwReserved[2])));
            baos.write(intBytes(swapInt(dwReserved[3])));
            baos.close();
            
            return baos.toByteArray();
        }
    }
    
    private class AVIStreamAudioList
    {
        public byte[] fcc = new byte[]{'L','I','S','T'};
        public int size = 104;
        public byte[] fcc2 = new byte[]{'s','t','r','l'};
        
        public AVIStreamAudioList()
        {
            
        }
        
        public byte[] toBytes() throws Exception
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(fcc);
            baos.write(intBytes(swapInt(size)));
            baos.write(fcc2);
            baos.close();
            
            return baos.toByteArray();
        }
    }
    
    private class AVIStreamVideoList
    {
        public byte[] fcc = new byte[]{'L','I','S','T'};
        public static final int size = 124;
        public byte[] fcc2 = new byte[]{'s','t','r','l'};
        
        public AVIStreamVideoList()
        {
            
        }
        
        public byte[] toBytes() throws Exception
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(fcc);
            baos.write(intBytes(swapInt(size)));
            baos.write(fcc2);
            baos.close();
            
            return baos.toByteArray();
        }
    }
    
    /**
     * AVI流视频头部
     * @author David.Chen
     *
     */
    private class AVIStreamVideoHeader
    {
        /*
         FOURCC fcc;
     DWORD  cb;
     FOURCC fccType;
     FOURCC fccHandler;
     DWORD  dwFlags;
     WORD   wPriority;
     WORD   wLanguage;
     DWORD  dwInitialFrames;
     DWORD  dwScale;
     DWORD  dwRate;
     DWORD  dwStart;
     DWORD  dwLength;
     DWORD  dwSuggestedBufferSize;
     DWORD  dwQuality;
     DWORD  dwSampleSize;
     struct {
         short int left;
         short int top;
         short int right;
         short int bottom;
     }  rcFrame;
         */
        
        public byte[] fcc = new byte[]{'s','t','r','h'};
        public static final int cb = 64;
        public byte[] fccType = new byte[]{'v','i','d','s'};
        //public byte[] fccHandler = new byte[]{'M','J','P','G'};
        public byte[] fccHandler = new byte[]{'H','2','6','4'};
        public int dwFlags = 0;
        public short wPriority = 0;
        public short wLanguage = 0;
        public int dwInitialFrames = 0;
        public int dwScale = 0; // microseconds per frame
        public int dwRate = 1000000; // dwRate / dwScale = frame rate
        public int dwStart = 0;
        public int dwLength = 0; // num frames
        public int dwSuggestedBufferSize = 0;
        public int dwQuality = -1;
        public int dwSampleSize = 0;
        public int left = 0;
        public int top = 0;
        public int right = 0;
        public int bottom = 0;
        
        public AVIStreamVideoHeader()
        {
            dwScale = (int)((1.0/framerate)*1000000.0);
            dwLength = numFrames;
        }
        
        public byte[] toBytes() throws Exception
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(fcc);
            baos.write(intBytes(swapInt(cb)));
            baos.write(fccType);
            baos.write(fccHandler);
            baos.write(intBytes(swapInt(dwFlags)));
            baos.write(shortBytes(swapShort(wPriority)));
            baos.write(shortBytes(swapShort(wLanguage)));
            baos.write(intBytes(swapInt(dwInitialFrames)));
            baos.write(intBytes(swapInt(dwScale)));
            baos.write(intBytes(swapInt(dwRate)));
            baos.write(intBytes(swapInt(dwStart)));
            baos.write(intBytes(swapInt(dwLength)));
            baos.write(intBytes(swapInt(dwSuggestedBufferSize)));
            baos.write(intBytes(swapInt(dwQuality)));
            baos.write(intBytes(swapInt(dwSampleSize)));
            baos.write(intBytes(swapInt(left)));
            baos.write(intBytes(swapInt(top)));
            baos.write(intBytes(swapInt(right)));
            baos.write(intBytes(swapInt(bottom)));
            baos.close();
            
            return baos.toByteArray();
        }
    }
    
    /**
     * AVI流音频头部
     * @author Administrator
     *
     */
    private class AVIStreamAudioHeader
    {
        /*
         FOURCC fcc;
     DWORD  cb;
     FOURCC fccType;
     FOURCC fccHandler;
     DWORD  dwFlags;
     WORD   wPriority;
     WORD   wLanguage;
     DWORD  dwInitialFrames;
     DWORD  dwScale;
     DWORD  dwRate;
     DWORD  dwStart;
     DWORD  dwLength;
     DWORD  dwSuggestedBufferSize;
     DWORD  dwQuality;
     DWORD  dwSampleSize;
     struct {
         short int left;
         short int top;
         short int right;
         short int bottom;
     }  rcFrame;
         */
        
        public byte[] fcc = new byte[]{'s','t','r','h'};
        public static final int cb = 64;        
        public byte[] fccType = new byte[]{'a','u','d','s'};
        public byte[] fccHandler = new byte[]{1, 0, 0, 0};
        public int dwFlags = 0;
        public short wPriority = 0;
        public short wLanguage = 0;
        public int dwInitialFrames = 0;
        public int dwScale = 0; // microseconds per frame
        public int dwRate = 1000000; // dwRate / dwScale = frame rate
        public int dwStart = 0;
        public int dwLength = 0; // num frames
        public int dwSuggestedBufferSize = 0;
        public int dwQuality = -1;
        public int dwSampleSize = 0;
        public int left = 0;
        public int top = 0;
        public int right = 0;
        public int bottom = 0;
        
        public AVIStreamAudioHeader()
        {
        	samplerate = 8000/320;
            dwScale = (int)((1.0/samplerate)*1000000.0);
            dwLength = numFrames;
        }
        
        public byte[] toBytes() throws Exception
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(fcc);
            baos.write(intBytes(swapInt(cb)));
            baos.write(fccType);
            baos.write(fccHandler);
            baos.write(intBytes(swapInt(dwFlags)));
            baos.write(shortBytes(swapShort(wPriority)));
            baos.write(shortBytes(swapShort(wLanguage)));
            baos.write(intBytes(swapInt(dwInitialFrames)));
            baos.write(intBytes(swapInt(dwScale)));
            baos.write(intBytes(swapInt(dwRate)));
            baos.write(intBytes(swapInt(dwStart)));
            baos.write(intBytes(swapInt(dwLength)));
            baos.write(intBytes(swapInt(dwSuggestedBufferSize)));
            baos.write(intBytes(swapInt(dwQuality)));
            baos.write(intBytes(swapInt(dwSampleSize)));
            baos.write(intBytes(swapInt(left)));
            baos.write(intBytes(swapInt(top)));
            baos.write(intBytes(swapInt(right)));
            baos.write(intBytes(swapInt(bottom)));
            baos.close();
            
            return baos.toByteArray();
        }
    }
    
    private class AVIStreamVideoFormat
    {
        /*
         FOURCC fcc;
     DWORD  cb;
     DWORD  biSize;
    LONG   biWidth;
    LONG   biHeight;
    WORD   biPlanes;
    WORD   biBitCount;
    DWORD  biCompression;
    DWORD  biSizeImage;
    LONG   biXPelsPerMeter;
    LONG   biYPelsPerMeter;
    DWORD  biClrUsed;
    DWORD  biClrImportant;
         */
        
        public byte[] fcc = new byte[]{'s','t','r','f'};
        public static final int cb = 40;
        public int biSize = 40; // same as cb
        public int biWidth = 0;
        public int biHeight = 0;
        public short biPlanes = 1;
        public short biBitCount = 24;
        //public byte[] biCompression = new byte[]{'M','J','P','G'};
        public byte[] biCompression = new byte[]{'H','2','6','4'};
        public int biSizeImage = 0; // width x height in pixels
        public int biXPelsPerMeter = 0;
        public int biYPelsPerMeter = 0;
        public int biClrUsed = 0;
        public int biClrImportant = 0;
        
        
        public AVIStreamVideoFormat()
        {
            biWidth = width;
            biHeight = height;
            biSizeImage = width * height;
        }
        
        public byte[] toBytes() throws Exception
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(fcc);
            baos.write(intBytes(swapInt(cb)));
            baos.write(intBytes(swapInt(biSize)));
            baos.write(intBytes(swapInt(biWidth)));
            baos.write(intBytes(swapInt(biHeight)));
            baos.write(shortBytes(swapShort(biPlanes)));
            baos.write(shortBytes(swapShort(biBitCount)));
            baos.write(biCompression);
            baos.write(intBytes(swapInt(biSizeImage)));
            baos.write(intBytes(swapInt(biXPelsPerMeter)));
            baos.write(intBytes(swapInt(biYPelsPerMeter)));
            baos.write(intBytes(swapInt(biClrUsed)));
            baos.write(intBytes(swapInt(biClrImportant)));
            baos.close();
            
            return baos.toByteArray();
        }
    }
    
    private class AVIStreamAudioFormat
    {
        /*
         FOURCC fcc;
     DWORD  cb;
     WORD  wFormatTag;
  WORD  nChannels;
  DWORD nSamplesPerSec;
  DWORD nAvgBytesPerSec;
  WORD  nBlockAlign;
  WORD  wBitsPerSample;
  WORD  cbSize;
         */
        
        public byte[] fcc = new byte[]{'s','t','r','f'};
        public static final int cb = 20;
        public short wFormatTag = 0x11; 	// WAVE_FORMAT_PCM
        public short nChannels = 1;		// 1
        public int nSamplesPerSec = 8000;	// 25
        public short wBitsPerSample = 4; // width x height in pixels
        public short nBlockAlign = 164;//(short) (nChannels * wBitsPerSample / 8);        
        public int nAvgBytesPerSec = 4100;//nSamplesPerSec * nBlockAlign;	//
        public short nSamplesPerBlock = 320;
        
        public short cbSize = 2;        
        
        public AVIStreamAudioFormat()
        {
        }
        
        public byte[] toBytes() throws Exception
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(fcc);
            baos.write(intBytes(swapInt(cb)));
            baos.write(shortBytes(swapShort(wFormatTag)));
            baos.write(shortBytes(swapShort(nChannels)));
            baos.write(intBytes(swapInt(nSamplesPerSec)));
            baos.write(intBytes(swapInt(nAvgBytesPerSec)));
            baos.write(shortBytes(swapShort(nBlockAlign)));
            baos.write(shortBytes(swapShort(wBitsPerSample)));
            baos.write(shortBytes(swapShort(cbSize)));
            baos.write(shortBytes(swapShort(nSamplesPerBlock)));
            baos.close();
            
            return baos.toByteArray();
        }
    }
    
    private class AVIMovieList
    {
        public byte[] fcc = new byte[]{'L','I','S','T'};
        public int listSize = 0;
        public byte[] fcc2 = new byte[]{'m','o','v','i'};
        // 00db size jpg image data ...
        
        public AVIMovieList()
        {
            
        }
        
        public byte[] toBytes() throws Exception
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(fcc);
            baos.write(intBytes(swapInt(listSize)));
            baos.write(fcc2);
            baos.close();
            
            return baos.toByteArray();
        }
    }
    
    private class AVIIndexList
    {
        public byte[] fcc = new byte[]{'i','d','x','1'};
        public int cb = 0;
        public ArrayList ind = new ArrayList();
        long lastWriteTime = System.currentTimeMillis();
        
        public AVIIndexList()
        {
            
        }
        
        public void addAVIIndex(AVIIndex ai)
        {
            ind.add(ai);
        }
        
        public void addAVIIndex(int nStream, StreamType type, int dwOffset, int dwSize)
        {
            ind.add(new AVIIndex(nStream, type, dwOffset, dwSize));
            long nowTime = System.currentTimeMillis(); 
            if((nowTime - lastWriteTime) > 1000)
            {
            	lastWriteTime = nowTime;
            	FileOutputStream fos;
				try {
					fos = new FileOutputStream(indexFile);
					fos.write(this.toBytes());
					fos.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            	this.ind.clear();
            }
        }
        
        public byte[] toBytes() throws Exception
        {
            cb = 16 * ind.size();
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            baos.write(fcc);
//            baos.write(intBytes(swapInt(cb)));
            for(int i = 0; i < ind.size(); i++)
            {
                AVIIndex in = (AVIIndex)ind.get(i);
                baos.write(in.toBytes());
            }
            
            baos.close();
            
            return baos.toByteArray();
        }
    }
    
    
    private class AVIIndex
    {
        public byte[] fcc = new byte[4];// = new byte[]{'0','0','d','b'};
        public int dwFlags = 16;
        public int dwOffset = 0;
        public int dwSize = 0;  
        
        public AVIIndex(int nStream, StreamType type, int dwOffset, int dwSize)
        {
        	fcc[0] = '0';
        	fcc[1] = (byte) (nStream + 0x30);
        	switch(type)
        	{
        	case STREAM_AUDIO:
        		fcc[2] = 'w';
        		fcc[3] = 'b';
        		break;
        		
        	case STREAM_VIDEO_C:
        		fcc[2] = 'd';
        		fcc[3] = 'c';
        		break;
        	
        	case STREAM_VIDEO_B:
        		fcc[2] = 'd';
        		fcc[3] = 'b';
        		break;        		
        	}
        	
            this.dwOffset = dwOffset;
            this.dwSize = dwSize;
        }
        
        public byte[] toBytes() throws Exception
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(fcc);
            baos.write(intBytes(swapInt(dwFlags)));
            baos.write(intBytes(swapInt(dwOffset)));
            baos.write(intBytes(swapInt(dwSize)));
            baos.close();
            
            return baos.toByteArray();
        }
    }
    
    private class AVIJunk
    {
        public byte[] fcc = new byte[]{'J','U','N','K'};
        public int size = 1808;
        public byte[] data = new byte[size];
        
        public AVIJunk()
        {
            Arrays.fill(data,(byte)0);
        }
        
        public byte[] toBytes() throws Exception
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(fcc);
            baos.write(intBytes(swapInt(size)));
            baos.write(data);
            baos.close();
            
            return baos.toByteArray();
        }
    }
    
//    private byte[] writeImageToBytes(Image image) throws Exception
//    {
//        BufferedImage bi = new BufferedImage(width, height,BufferedImage.TYPE_INT_RGB);
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        Graphics2D g = bi.createGraphics();
//        g.drawImage(image,0,0,width,height,null);
//        ImageIO.write(bi,"jpg",baos);
//        baos.close();
//        bi = null;
//        g = null;
//        
//        return baos.toByteArray();
//    }
}
