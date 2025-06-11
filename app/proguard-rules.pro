# 保留 Android 组件
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class * extends android.view.View

# 保留注解和反射使用的类
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# 保留你的数据模型类
-keep class com.hsbc.github.model.** { *; }

# 保留网络接口类
-keep interface com.hsbc.github.network.** { *; }
-keep class retrofit2.** { *; }
-keepattributes Exceptions

# 保留 Gson 相关
-keep class com.google.gson.** { *; }
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class java.lang.reflect.Type { *; }

# 保留 Parcelable 类
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

# 保留 Serializable 类
-keepnames class * implements java.io.Serializable

# 保留 Room 相关
-keep class androidx.room.** { *; }

# 保留 ViewModel 和 LiveData
-keep class androidx.lifecycle.ViewModel { *; }
-keep class androidx.lifecycle.LiveData { *; }