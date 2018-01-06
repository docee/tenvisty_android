LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
 
LOCAL_MODULE    := decodeH264
LOCAL_SRC_FILES := native.c

LOCAL_C_INCLUDES := $(LOCAL_PATH)/include
LOCAL_LDLIBS := -L$(NDK_PLATFORMS_ROOT)/$(TARGET_PLATFORM)/arch-arm/usr/lib -L$(LOCAL_PATH) -lavfilter -lavformat -lavcodec  -lswresample -lswscale -lavutil -lx264 -llog -ljnigraphics -lz -ldl -lgcc
 
include $(BUILD_SHARED_LIBRARY)
