package net.schowek.nextclouddlna.nextcloud.db

interface PathBuilder {
    fun build(filecache: Filecache): String
}
