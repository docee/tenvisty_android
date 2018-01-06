LOCAL_PATH := $(call my-dir)

# prepare libX
include $(CLEAR_VARS)
LOCAL_MODULE    := avcodec
LOCAL_SRC_FILES := libavcodec-57.so
include $(PREBUILT_SHARED_LIBRARY)

# prepare libX
include $(CLEAR_VARS)
LOCAL_MODULE    := avfilter
LOCAL_SRC_FILES := libavfilter-6.so
include $(PREBUILT_SHARED_LIBRARY)

# prepare libX
include $(CLEAR_VARS)
LOCAL_MODULE    := avformat
LOCAL_SRC_FILES := libavformat-57.so
include $(PREBUILT_SHARED_LIBRARY)

# prepare libX
include $(CLEAR_VARS)
LOCAL_MODULE    := avutil
LOCAL_SRC_FILES := libavutil-55.so
include $(PREBUILT_SHARED_LIBRARY)


# prepare libX
include $(CLEAR_VARS)
LOCAL_MODULE    := swresample
LOCAL_SRC_FILES := libswresample-2.so
include $(PREBUILT_SHARED_LIBRARY)

# prepare libX
include $(CLEAR_VARS)
LOCAL_MODULE    := swscale
LOCAL_SRC_FILES := libswscale-4.so
include $(PREBUILT_SHARED_LIBRARY)


include $(CLEAR_VARS)
LOCAL_C_INCLUDES += $(LOCAL_PATH)/include
LOCAL_MODULE     := live-jni
LOCAL_SRC_FILES  := live-jni.c
LOCAL_SHARED_LIBRARIES := avfilter avformat avcodec swresample swscale avutil
LOCAL_LDLIBS     := -L$(NDK_ROOT)/platforms/$(APP_PLATFORM)/arch-arm/usr/lib -L$(LOCAL_PATH) -llog -ljnigraphics -lz -ldl
include $(BUILD_SHARED_LIBRARY)