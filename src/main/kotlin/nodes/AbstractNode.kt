package main.kotlin.nodes

abstract class AbstractNode<T : Comparable<T>, S, N : AbstractNode<T, S, N>> {
    abstract var key: T
        internal set

    abstract var value: S
        internal set

    abstract var left: N?
        internal set

    abstract var right: N?
        internal set
}