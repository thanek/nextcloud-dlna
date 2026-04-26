package net.schowek.nextclouddlna.dlna.media

import net.schowek.nextclouddlna.nextcloud.content.ContentItem
import net.schowek.nextclouddlna.nextcloud.content.ContentNode
import net.schowek.nextclouddlna.nextcloud.content.ContentTreeProvider
import net.schowek.nextclouddlna.nextcloud.content.MediaFormat
import org.jupnp.support.model.BrowseFlag
import org.jupnp.support.model.DIDLObject
import org.jupnp.support.model.Res
import org.jupnp.support.model.SortCriterion
import org.jupnp.support.model.container.Container
import org.jupnp.support.model.item.VideoItem
import org.jupnp.util.MimeType
import spock.lang.Specification

import static org.jupnp.support.model.BrowseFlag.DIRECT_CHILDREN
import static org.jupnp.support.model.BrowseFlag.METADATA

class ContentDirectoryServiceTest extends Specification {
    def contentTreeProvider = Mock(ContentTreeProvider)
    def nodeConverter = Mock(NodeConverter)
    def sut = new ContentDirectoryService(contentTreeProvider, nodeConverter)

    def "browse node with DIRECT_CHILDREN returns all children"() {
        given:
        def child1 = new ContentNode(2, 1, "Sub1")
        def child2 = new ContentNode(3, 1, "Sub2")
        def item1 = new ContentItem(4, 1, "a.mp4", "/a.mp4", MediaFormat.MP4, 100L, 0L)
        def item2 = new ContentItem(5, 1, "b.mp4", "/b.mp4", MediaFormat.MP4, 100L, 0L)
        def node = new ContentNode(1, 0, "Videos")
        node.addNode(child1)
        node.addNode(child2)
        node.addItem(item1)
        node.addItem(item2)
        contentTreeProvider.getNode("1") >> node
        nodeConverter.makeContainerWithoutSubContainers(child1) >> container("2", "1")
        nodeConverter.makeContainerWithoutSubContainers(child2) >> container("3", "1")
        nodeConverter.makeItem(item1) >> item("4", "1")
        nodeConverter.makeItem(item2) >> item("5", "1")

        when:
        def result = sut.browse("1", DIRECT_CHILDREN, "*", 0, 0, [] as SortCriterion[])

        then:
        result.countLong == 4
        result.totalMatchesLong == 4
    }

    def "browse node with METADATA returns single container entry"() {
        given:
        def node = new ContentNode(1, 0, "Videos")
        contentTreeProvider.getNode("1") >> node
        nodeConverter.makeContainerWithoutSubContainers(node) >> container("1", "0")

        when:
        def result = sut.browse("1", METADATA, "*", 0, 0, [] as SortCriterion[])

        then:
        result.countLong == 1
        result.totalMatchesLong == 1
    }

    def "browse unknown objectID returns empty result"() {
        given:
        contentTreeProvider.getNode("999") >> null
        contentTreeProvider.getItem("999") >> null

        when:
        def result = sut.browse("999", DIRECT_CHILDREN, "*", 0, 0, [] as SortCriterion[])

        then:
        result.countLong == 0
        result.totalMatchesLong == 0
    }

    def "browse item ID returns item metadata"() {
        given:
        def contentItem = new ContentItem(4, 1, "movie.mp4", "/path/movie.mp4", MediaFormat.MP4, 1000L, 0L)
        contentTreeProvider.getNode("4") >> null
        contentTreeProvider.getItem("4") >> contentItem
        nodeConverter.makeItem(contentItem) >> item("4", "1")

        when:
        def result = sut.browse("4", DIRECT_CHILDREN, "*", 0, 0, [] as SortCriterion[])

        then:
        result.countLong == 1
        result.totalMatchesLong == 1
    }

    def "browse with pagination returns correct slice"() {
        given: "node with 3 containers and 2 items"
        def node = new ContentNode(1, 0, "All")
        def childNodes = (1..3).collect { new ContentNode(it, 1, "Node $it") }
        def childItems = (4..5).collect { new ContentItem(it, 1, "item${it}.mp4", "/item${it}.mp4", MediaFormat.MP4, 100L, 0L) }
        childNodes.each { node.addNode(it) }
        childItems.each { node.addItem(it) }
        contentTreeProvider.getNode("1") >> node
        childNodes.each { n -> nodeConverter.makeContainerWithoutSubContainers(n) >> container("$n.id", "1") }
        childItems.each { i -> nodeConverter.makeItem(i) >> item("$i.id", "1") }

        when:
        def result = sut.browse("1", DIRECT_CHILDREN, "*", firstResult, maxResults, [] as SortCriterion[])

        then:
        result.countLong == expectedCount
        result.totalMatchesLong == 5

        where:
        firstResult | maxResults || expectedCount
        0           | 0          || 5   // all results
        0           | 2          || 2   // first 2 containers
        2           | 2          || 2   // last container + first item
        4           | 2          || 1   // only last item
        5           | 2          || 0   // past end
    }

    private static Container container(String id, String parentId) {
        new Container().tap {
            it.id = id
            it.parentID = parentId
            it.title = "Container $id"
            it.clazz = new DIDLObject.Class("object.container")
            it.childCount = 0
        }
    }

    private static VideoItem item(String id, String parentId) {
        new VideoItem(id, parentId, "Video $id", "",
                new Res(new MimeType("video", "mp4"), 1000L, "http://localhost/c/$id"))
    }
}
