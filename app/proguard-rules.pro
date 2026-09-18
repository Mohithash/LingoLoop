# kotlinx.serialization: keep the generated serializers for our @Serializable models.
-keepclassmembers class com.mohithash.lingoloop.**.** {
    *** Companion;
    *** serializer(...);
}
-keepclasseswithmembers class com.mohithash.lingoloop.**.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.mohithash.lingoloop.**.**$$serializer { *; }
-dontwarn org.slf4j.**
