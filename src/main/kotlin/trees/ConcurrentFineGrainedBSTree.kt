package main.kotlin.trees

import main.kotlin.BSTIterator
import main.kotlin.nodes.ConcurrentBSNode
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

class ConcurrentFineGrainedBSTree<T : Comparable<T>, S> : AbstractTree<T, S, ConcurrentBSNode<T, S>>() {
    private val treeLock: Lock = ReentrantLock()

    override fun insert(key: T, value: S): Boolean {
        var curr: ConcurrentBSNode<T, S>?
        var parent: ConcurrentBSNode<T, S>?
        treeLock.lock()
        if (root == null) {
            root = ConcurrentBSNode(key, value)
            treeLock.unlock()
            return true
        }
        curr = root!!
        curr.lock()
        treeLock.unlock()
        while (true) {
            if (key == curr!!.key) {
                curr.value = value
                curr.unlock()
                return true
            }
            parent = curr
            curr = if (key < curr.key) curr.left else curr.right
            if (curr == null) break
            curr.lock()
            parent.unlock()

        }

        if (key < parent!!.key) {
            parent.left = ConcurrentBSNode(key, value)
        } else {
            parent.right = ConcurrentBSNode(key, value)
        }
        parent.unlock()
        return true
    }

    override fun remove(key: T): Boolean {
        return removeRoot(key) ?: return removeNode(key)
    }

    override fun find(key: T) = findNode(key)?.value

    override fun iterator(): BSTIterator<T, S, ConcurrentBSNode<T, S>> =
        BSTIterator(root)

    private fun findNode(key: T): ConcurrentBSNode<T, S>? {
        var curr: ConcurrentBSNode<T, S>?
        var parent: ConcurrentBSNode<T, S>? = null
        treeLock.lock()
        if (root != null) {
            curr = root
            curr!!.lock()
            treeLock.unlock()
            while (curr != null) {
                parent = curr
                curr = if (curr.key > key) {
                    curr.left
                } else if (curr.key < key) {
                    curr.right
                } else {
                    curr.unlock()
                    return curr
                }
                if (curr == null) break
                curr.lock()
                parent.unlock()
            }
        } else {
            treeLock.unlock()
            return null
        }
        parent!!.unlock()
        return null
    }

    private fun removeRoot(key: T): Boolean? {
        treeLock.lock()
        if (root == null) {
            treeLock.unlock()
            return false
        }
        return if (root!!.key == key) {
            if (root!!.left != null && root!!.right != null) {
                val newRoot = findMaxNodeAndSeparate(root!!.left!!)
                newRoot.right = root!!.right
                if (newRoot !== root!!.left) {
                    newRoot.left = root!!.left
                }
                root = newRoot
                newRoot.unlock()
                treeLock.unlock()
                true
            } else if (root!!.left != null) {
                root = root!!.left
                treeLock.unlock()
                true
            } else {
                root = root!!.right
                treeLock.unlock()
                true
            }
        } else return null
    }

    private fun removeNode(key: T): Boolean {
        var curr: ConcurrentBSNode<T, S>?
        var parent: ConcurrentBSNode<T, S>?
        parent = root!!
        parent.lock()
        treeLock.unlock()
        curr = root!!
        while (true) {
            parent = curr!!
            curr = if (key < curr.key) curr.left else curr.right
            if (curr == null) {
                parent.unlock()
                return false
            }
            curr.lock()
            if (key == curr.key) break
            parent.unlock()
        }
        // Now curr is node to remove && curr and parent locked
        return if (curr!!.left != null && curr.right != null) {
            val replacementNode = findMaxNodeAndSeparate(curr.left!!)
            replacementNode.right = curr.right
            if (replacementNode !== curr.left) {
                replacementNode.left = curr.left
            }
            if (curr.key < parent!!.key) {
                parent.left = replacementNode
            } else {
                parent.right = replacementNode
            }
            replacementNode.unlock()
            curr.unlock()
            parent.unlock()
            true
        } else if (curr.left != null) {
            if (curr.key < parent!!.key) {
                parent.left = curr.left
            } else {
                parent.right = curr.left
            }
            curr.unlock()
            parent.unlock()
            true
        } else {
            if (curr.key < parent!!.key) {
                parent.left = curr.right
            } else {
                parent.right = curr.right
            }
            curr.unlock()
            parent.unlock()
            true
        }
    }

    private fun findMaxNodeAndSeparate(node: ConcurrentBSNode<T, S>): ConcurrentBSNode<T, S> {
        node.lock()
        var parent = node
        var current = node
        while (current.right != null) {
            parent = current
            current = current.right!!
            current.lock()
            if (current.right != null) {
                parent.unlock()
            }
        }

        if (current !== node) {
            parent.right = current.left
            parent.unlock()
        }
        return current
    }
}