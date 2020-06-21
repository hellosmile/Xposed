#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <jni.h>

#ifdef __cplusplus
extern "C" {
#endif


JNIEXPORT jboolean JNICALL
Java_io_github_hellosmile_xposed_detect_XposedDetect_isXposed(JNIEnv *env, jclass instance) {
    bool rel = false;

    FILE *fp = NULL;
    char *filepath = "/proc/self/maps";
    char *xposedModName = "XposedBridge.jar";
    char *modFullName = NULL;
    fp = fopen(filepath, "r");
    char strLine[1024] = {0};
    if (fp != NULL) {
        while (fgets(strLine, sizeof(strLine), fp) != NULL) {
            if (strstr(strLine, xposedModName)) {
                if (strLine[strlen(strLine) - 1] == '\n') {
                    strLine[strlen(strLine) - 1] = 0;
                }

                modFullName = strchr(strLine, '/');
                if (modFullName == NULL) {
                    continue;
                }

                rel = true;
                break;
            }
        }
        fclose(fp);
    }


    return rel ? JNI_TRUE : JNI_FALSE;
}


#ifdef __cplusplus
}
#endif