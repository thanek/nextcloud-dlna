package net.schowek.nextclouddlna.nextcloud.content


enum class ContentGroup(val id: String, val itemIdPrefix: String, val humanName: String) {
    ROOT("0", "-", "Root"),

    // Root id of '0' is in the spec.
    VIDEO("1-videos", "video-", "Videos"),
    IMAGE("2-images", "image-", "Images"),
    AUDIO("3-audio", "audio-", "Audio"),
    SUBTITLES("4-subtitles", "subtitles-", "Subtitles"),
    THUMBNAIL("5-thumbnails", "thumbnail-", "Thumbnails")
}

