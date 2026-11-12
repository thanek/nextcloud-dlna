package net.schowek.nextclouddlna.nextcloud.db

import spock.lang.Specification

import static net.schowek.nextclouddlna.nextcloud.content.MediaFormat.*

class DefaultContentItemFactoryTest extends Specification {
    static final def MIME_JPG = 3
    static final def MIME_MP3 = 5
    static final def MIME_PDF = 6
    static final def MIME_UNREGISTERED = 99

    def mimetypeResolver = Mock(MimetypeResolver)
    def sut = new DefaultContentItemFactory(mimetypeResolver)

    def setup() {
        mimetypeResolver.resolve(MIME_JPG) >> 'image/jpeg'
        mimetypeResolver.resolve(MIME_MP3) >> 'audio/mpeg'
        mimetypeResolver.resolve(MIME_PDF) >> 'application/pdf'
        mimetypeResolver.resolve(MIME_UNREGISTERED) >> null
    }

    def "maps a filecache row onto a ContentItem"() {
        given:
        def filecache = new Filecache(42, 2, "files/song.mp3", 7, "song.mp3", MIME_MP3, 1234L, 99L, 0L)

        when:
        def item = sut.create(filecache, "/data/alice/files/song.mp3")

        then:
        item.id == 42
        item.parentId == 7
        item.name == "song.mp3"
        item.path == "/data/alice/files/song.mp3"
        item.format.mime == MP3.mime
        item.fileLength == 1234L
        item.mtime == 99L
    }

    def "resolves the media format from the mimetype id"() {
        expect:
        sut.create(aFilecache(mimetypeId), "/some/path").format.mime == expectedMime

        where:
        mimetypeId || expectedMime
        MIME_JPG   || 'image/jpeg'
        MIME_MP3   || 'audio/mpeg'
    }

    def "fails for a mimetype id that is not in the mimetypes table"() {
        when:
        sut.create(aFilecache(MIME_UNREGISTERED), "/some/path")

        then:
        def e = thrown(RuntimeException)
        e.message.contains("Unable to create ContentItem")
        e.cause.message.contains("Unknown mimetype id: $MIME_UNREGISTERED")
    }

    def "fails for a mimetype that is not a playable media format"() {
        when:
        sut.create(aFilecache(MIME_PDF), "/some/path")

        then: "the cause is kept so the caller can log why the file was skipped"
        def e = thrown(RuntimeException)
        e.message.contains("Unable to create ContentItem")
        e.cause.message.contains("application/pdf")
    }

    private static def aFilecache(int mimetypeId) {
        new Filecache(1, 2, "files/f", 0, "f", mimetypeId, 0L, 0L, 0L)
    }
}
