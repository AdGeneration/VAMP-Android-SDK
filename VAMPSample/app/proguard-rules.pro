# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in ${sdk.dir}/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

#AdGeneration(VAMP)

-keepclasseswithmembers class jp.supership.vamp.** { *; }

#AdMob

-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

#AppLovin

-keep class com.applovin.** { *; }

#maio

-keep class jp.maio.sdk.android.** { *; }

#UnityAds

-keep class com.unity3d.ads.android.** { *; }

#FAN

-keep class com.facebook.ads.** { *; }

#nend

-keep class net.nend.android.** { *; }
-dontwarn net.nend.android.**

# Vungle

-keep class com.vungle.warren.** { *; }

## Evernote
-dontwarn com.evernote.android.job.gcm.**
-dontwarn com.evernote.android.job.GcmAvailableHelper

-dontwarn com.google.android.gms.ads.identifier.**

-keep public class com.evernote.android.job.v21.PlatformJobService
-keep public class com.evernote.android.job.v14.PlatformAlarmService
-keep public class com.evernote.android.job.v14.PlatformAlarmReceiver
-keep public class com.evernote.android.job.JobBootReceiver
-keep public class com.evernote.android.job.JobRescheduleService

-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-keep class com.google.android.gms.internal.** { *; }

## Moat SDK
-keep class com.moat.** { *; }
-dontwarn com.moat.**

#Tapjoy

-keep class com.tapjoy.** { *; }
#-keep class com.moat.** { *; }     Vungleでも設定してるのでこっちはコメントアウト
-keepattributes JavascriptInterface
-keep class * extends java.util.ListResourceBundle {
protected Object[][] getContents();
}
-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
public static final *** NULL;
}
-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
@com.google.android.gms.common.annotation.KeepName *;
}
-keepnames class * implements android.os.Parcelable {
public static final ** CREATOR;
}
-keep class com.google.android.gms.ads.identifier.** { *; }
-dontwarn com.tapjoy.**
