package main.kotlin.nodes

import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

class ConcurrentBSNode<T : Comparable<T>, S>(override var key: T, override var value: S) :
    AbstractNode<T, S, ConcurrentBSNode<T, S>>() {

    override var left: ConcurrentBSNode<T, S>? = null
    override var right: ConcurrentBSNode<T, S>? = null

    private val lock: Lock = ReentrantLock()

    fun lock() = lock.lock()
    fun unlock() = lock.unlock()
}