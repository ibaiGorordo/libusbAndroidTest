#include <jni.h>
#include <string>

#include "libusb_utils.h"
#include <jni.h>

std::string connect_device(int fileDescriptor)
{
    libusb_context *ctx;
    libusb_device_handle *devh;
    int r = 0;

    libusb_set_option(nullptr, LIBUSB_OPTION_NO_DEVICE_DISCOVERY, NULL);        //
    libusb_init(nullptr);
    libusb_wrap_sys_device(nullptr, (intptr_t)fileDescriptor, &devh);

    auto device = libusb_get_device(devh);
    print_device(device, devh);

    return get_device_name(device, devh);
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_libusbAndroidTest_MainActivity_initializeNativeDevice(
        JNIEnv* env,
        jobject /* this */,
        jint fileDescriptor) {


    std::string deviceName = connect_device(fileDescriptor);

    return env->NewStringUTF(deviceName.c_str());
}
