#define __STDC_CONSTANT_MACROS


#ifdef _WIN32
//Windows
extern "C"
{
#include "include/libavcodec/avcodec.h"
#include "include/libavformat/avformat.h"
#include "include/libswscale/swscale.h"
};
#else
//Linux...
#ifdef __cplusplus
extern "C"
{
#endif
#include <include/libavcodec/avcodec.h>
#include <include/libavformat/avformat.h>
#include <include/libswscale/swscale.h>
#ifdef __cplusplus
};
#endif
#endif
