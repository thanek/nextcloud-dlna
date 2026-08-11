package net.schowek.nextclouddlna.dlna.media

import net.schowek.nextclouddlna.nextcloud.content.ContentItem
import net.schowek.nextclouddlna.nextcloud.content.ContentNode
import net.schowek.nextclouddlna.nextcloud.content.MediaFormat
import org.jupnp.support.model.SortCriterion
import spock.lang.Specification

class DefaultResultSorterTest extends Specification {
    static final SortCriterion[] NO_CRITERIA = [] as SortCriterion[]

    def nodes = [node(3, "Videos"), node(1, "photos"), node(2, "Music")]
    def items = [
            item(3, "b.mp4", MediaFormat.MP4, 200L),
            item(1, "C.png", MediaFormat.PNG, 100L),
            item(2, "a.mp4", MediaFormat.MP4, 300L)
    ]

    def "sorts by the configured default criteria when the client requests no order"() {
        given:
        def sut = new DefaultResultSorter("+dc:title")

        expect:
        sut.sortNodes(nodes, NO_CRITERIA).collect { it.name } == ["Music", "photos", "Videos"]
        sut.sortItems(items, NO_CRITERIA).collect { it.name } == ["a.mp4", "b.mp4", "C.png"]
    }

    def "supports multiple default criteria"() {
        given:
        def sut = new DefaultResultSorter("+upnp:class,-dc:title")

        expect:
        sut.sortItems(items, NO_CRITERIA).collect { it.name } == ["C.png", "b.mp4", "a.mp4"]
    }

    def "falls back to title ascending for containers when the default criteria do not apply to them"() {
        given:
        def sut = new DefaultResultSorter("-dc:date")

        expect:
        sut.sortItems(items, NO_CRITERIA).collect { it.name } == ["a.mp4", "b.mp4", "C.png"]
        sut.sortNodes(nodes, NO_CRITERIA).collect { it.name } == ["Music", "photos", "Videos"]
    }

    def "criteria requested by the client take precedence over the default ones"() {
        given:
        def sut = new DefaultResultSorter("+dc:title")
        def orderby = SortCriterion.valueOf("-dc:title")

        expect:
        sut.sortNodes(nodes, orderby).collect { it.name } == ["Videos", "photos", "Music"]
        sut.sortItems(items, orderby).collect { it.name } == ["C.png", "b.mp4", "a.mp4"]
    }

    def "keeps the original order when no default criteria are configured"() {
        given:
        def sut = new DefaultResultSorter(defaultSortCriteria)

        expect:
        sut.sortNodes(nodes, NO_CRITERIA).collect { it.name } == ["Videos", "photos", "Music"]
        sut.sortItems(items, NO_CRITERIA).collect { it.name } == ["b.mp4", "C.png", "a.mp4"]

        where:
        defaultSortCriteria << ["", "   "]
    }

    def "ignores unsupported default properties"() {
        given:
        def sut = new DefaultResultSorter("+dc:creator,+dc:title")

        expect:
        sut.sortItems(items, NO_CRITERIA).collect { it.name } == ["a.mp4", "b.mp4", "C.png"]
    }

    def "falls back to title ascending when the configured default criteria are malformed"() {
        given:
        def sut = new DefaultResultSorter("dc:title")

        expect:
        sut.sortItems(items, NO_CRITERIA).collect { it.name } == ["a.mp4", "b.mp4", "C.png"]
    }

    def "orders equally ranked elements by id so that the order does not change between tree rebuilds"() {
        given:
        def sut = new DefaultResultSorter("+dc:date")
        def sameDate = [item(7, "x.mp4", MediaFormat.MP4, 100L), item(5, "y.mp4", MediaFormat.MP4, 100L)]

        expect:
        sut.sortItems(sameDate, NO_CRITERIA).collect { it.id } == [5, 7]
        sut.sortItems(sameDate.reverse(), NO_CRITERIA).collect { it.id } == [5, 7]
    }

    private static ContentNode node(int id, String name) {
        new ContentNode(id, 0, name)
    }

    private static ContentItem item(int id, String name, MediaFormat format, long mtime) {
        new ContentItem(id, 1, name, "/$name", format, 1000L, mtime)
    }
}
