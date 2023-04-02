package main.kotlin

import main.kotlin.nodes.AbstractNode

class BSTIterator<T : Comparable<T>, S, N : AbstractNode<T, S, N>>
    (node: N?) : Iterator<N> {

    private var deque = ArrayDeque<N>()

    init {
        node?.let { deque.addLast(it) }
    }

    override fun hasNext(): Boolean =
        deque.isNotEmpty()

    override fun next(): N {
        val cur = deque.removeFirst()

        cur.left?.let { deque.addLast(it) }
        cur.right?.let { deque.addLast(it) }

        return cur
    }
}