package net.schowek.nextclouddlna.nextcloud.content

import spock.lang.Specification

import static net.schowek.nextclouddlna.nextcloud.content.MediaFormat.*

class ContentItemThumbnailTest extends Specification {

    def "keeps the smallest thumbnail that still covers the DLNA thumbnail size"() {
        given:
        def item = anItem()

        when:
        offered.each { w, h -> item.offerThumbnail(aThumb(w, h)) }

        then:
        [item.thumb.width, item.thumb.height] == expected

        where:
        offered                                    || expected
        [[4096, 2304], [1024, 576], [455, 256]]    || [455, 256]
        [[455, 256], [4096, 2304]]                 || [455, 256]
        [[256, 256], [160, 90], [64, 64]]          || [160, 90]
        [[64, 64], [128, 72]]                      || [128, 72]
        [[64, 64], [128, 72], [1024, 1024]]        || [1024, 1024]
        [[90, 160], [256, 256]]                    || [90, 160]
    }

    def "ignores thumbnails in formats without a DLNA thumbnail profile"() {
        given:
        def item = anItem()

        when:
        item.offerThumbnail(aThumb(256, 256, WEBP))
        item.offerThumbnail(aThumb(256, 256, GIF))

        then:
        item.thumb == null

        when:
        item.offerThumbnail(aThumb(1024, 1024, PNG))
        item.offerThumbnail(aThumb(256, 256, WEBP))

        then:
        item.thumb.format == PNG
    }

    private static ContentItem anItem() {
        new ContentItem(1, 0, "photo.jpg", "/photo.jpg", JPEG, 100L, 0L)
    }

    private static Thumbnail aThumb(int width, int height, MediaFormat format = JPEG) {
        new Thumbnail(1, "/preview/${width}-${height}", format, 10L, width, height)
    }
}
