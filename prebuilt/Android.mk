LOCAL_PATH := $(call my-dir)


include $(CLEAR_VARS)
LOCAL_MODULE := RemovePackages
LOCAL_MODULE_CLASS := APPS
LOCAL_MODULE_TAGS := optional
LOCAL_OVERRIDES_PACKAGES := arcore AudioFX Snap GoogleTTS talkback webview Camera Camera2 Snap
LOCAL_OVERRIDES_PACKAGES := AmbientSensePrebuilt Camera Camera2 CarrierSetup Drive
LOCAL_OVERRIDES_PACKAGES += PrebuiltGmail Maps MyVerizonServices Music MusicFX AudioFX
LOCAL_OVERRIDES_PACKAGES += OBDM_Permissions OPScreenRecord RecorderPrebuilt SoundPickerPrebuilt SoundAmplifierPrebuilt
LOCAL_OVERRIDES_PACKAGES += SafetyHubPrebuilt SprintDM SprintHM Snap AndroidAutoStubPrebuilt RecorderPrebuilt
LOCAL_OVERRIDES_PACKAGES += VzwOmaTrigger YouTube YouTubeMusicPrebuilt Flipendo
LOCAL_UNINSTALLABLE_MODULE := true
LOCAL_CERTIFICATE := PRESIGNED
LOCAL_SRC_FILES := /dev/null
include $(BUILD_PREBUILT)
