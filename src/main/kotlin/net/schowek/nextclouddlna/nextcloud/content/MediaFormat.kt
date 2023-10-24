package net.schowek.nextclouddlna.nextcloud.content

import net.schowek.nextclouddlna.nextcloud.content.ContentGroup.*
import org.jupnp.util.MimeType
import java.util.Arrays.stream


enum class MediaFormat(
    val ext: String,
    val mime: String,
    val contentGroup: ContentGroup
) {
    AVI("avi", "video/avi", VIDEO),
    FLV("flv", "video/x-flv", VIDEO),
    M4V("m4v", "video/mp4", VIDEO),
    MKV("mkv", "video/x-matroska", VIDEO),
    MOV("mov", "video/quicktime", VIDEO),
    MP4("mp4", "video/mp4", VIDEO),
    MPEG("mpeg", "video/mpeg", VIDEO),
    MPG("mpg", "video/mpeg", VIDEO),
    OGM("ogm", "video/ogg", VIDEO),
    OGV("ogv", "video/ogg", VIDEO),
    RMVB("rmvb", "application/vnd.rn-realmedia-vbr", VIDEO),
    WEBM("webm", "video/webm", VIDEO),
    WMV("wmv", "video/x-ms-wmv", VIDEO),
    _3GP("3gp", "video/3gpp", VIDEO),
    GIF("gif", "image/gif", IMAGE),
    JPEG("jpeg", "image/jpeg", IMAGE),
    JPG("jpg", "image/jpeg", IMAGE),
    PNG("png", "image/png", IMAGE),
    WEBP("webp", "image/webp", IMAGE),
    AAC("aac", "audio/aac", AUDIO),
    AC3("ac3", "audio/ac3", AUDIO),
    FLAC("flac", "audio/flac", AUDIO),
    M4A("m4a", "audio/mp4", AUDIO),
    MP3("mp3", "audio/mpeg", AUDIO),
    MPGA("mpga", "audio/mpeg", AUDIO),
    OGA("oga", "audio/ogg", AUDIO),
    OGG("ogg", "audio/ogg", AUDIO),
    RA("ra", "audio/vnd.rn-realaudio", AUDIO),
    WAV("wav", "audio/vnd.wave", AUDIO),
    WMA("wma", "audio/x-ms-wma", AUDIO),
    SRT("srt", "text/srt", SUBTITLES),
    SSA("ssa", "text/x-ssa", SUBTITLES),
    ASS("ass", "text/x-ass", SUBTITLES);

    fun asMimetype(): MimeType {
        val slash = mime.indexOf('/')
        return MimeType(mime.substring(0, slash), mime.substring(slash + 1))
    }

    companion object {
        fun fromMimeType(mimetype: String): MediaFormat {
            return stream(values()).filter { i -> i.mime == mimetype }
                .findFirst()
                .orElseThrow { RuntimeException("Unknown mime type $mimetype") }
        }
    }
}

