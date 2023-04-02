package main.kotlin.trees

import main.kotlin.nodes.AbstractNode

abstract class AbstractTree<T : Comparable<T>, S, N : AbstractNode<T, S, N>> : Iterable<N> {
    protected var root: N? = null

    private fun inorderTreeWalk(node: N?, list: MutableList<T>) {
        if (node != null) {
            inorderTreeWalk(node.left, list)
            list.add(node.key)
            inorderTreeWalk(node.right, list)
        }
    }

    fun makeInorderTreeMutableList(): MutableList<T> {
        val ListOfOrderedNodes = mutableListOf<T>()
        inorderTreeWalk(root, ListOfOrderedNodes)
        return ListOfOrderedNodes
    }

    abstract fun find(key: T): S?

    abstract fun remove(key: T): Boolean

    abstract fun insert(key: T, value: S): Boolean
}