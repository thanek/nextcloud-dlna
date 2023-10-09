package net.schowek.nextclouddlna.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

interface Logging {
    val logger: Logger
        get() = LoggerFactory.getLogger(classNameOf(this::class))
}

fun classNameOf(ownerClass: KClass<*>): String =
    if (ownerClass.isCompanion) ownerClass.java.enclosingClass.name else ownerClass.java.name

