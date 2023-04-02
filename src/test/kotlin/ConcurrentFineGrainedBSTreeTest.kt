package test.kotlin

import main.kotlin.nodes.ConcurrentBSNode
import main.kotlin.trees.ConcurrentFineGrainedBSTree
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.concurrent.Executors
import kotlin.random.Random


private fun findInArray(array: Array<Int>, v: Int): Int? {
    for (i in array.indices) {
        if (array[i] == v) return i
    }
    return null
}

class ConcurrentFineGrainedBSTreeTest {
    private var tree = ConcurrentFineGrainedBSTree<Int, Int>()

    @BeforeEach
    fun makeTree() {
        tree = ConcurrentFineGrainedBSTree()
    }

    @Nested
    inner class SequentialTests {
        @Test
        fun `iterator test`() {
            var countOfNodes = 0
            tree.insert(1, 2)
            tree.insert(2, 3)

            for (node in tree) {
                countOfNodes++
            }
            assertEquals(2, countOfNodes)
        }

        //find function tests
        @Test
        fun `find in empty tree test`() {
            assertNull(tree.find(1))
        }

        @Test
        fun `find properly test`() {
            tree.insert(1, 2)
            assertEquals(2, tree.find(1))
        }

        @Test
        fun `find nonexistent key test`() {
            tree.insert(1, 2)
            assertNull(tree.find(2))
        }

        //insert function tests
        @Test
        fun `insert several times in the same key test`() {
            tree.insert(1, 2)
            tree.insert(1, 3)
            assertNotNull(tree.find(1))
            assertEquals(3, tree.find(1))
        }

        @Test
        fun `insert 2 nodes with right son test`() {
            tree.insert(1, 2)
            tree.insert(2, 3)
            assertEquals(2, tree.find(1))
            assertEquals(3, tree.find(2))
        }

        @Test
        fun `insert 2 nodes with left son test`() {
            tree.insert(1, 2)
            tree.insert(-1, 3)
            assertEquals(2, tree.find(1))
            assertEquals(3, tree.find(-1))
        }

        //remove function tests
        @Test
        fun `remove from empty tree test`() {
            assertFalse(tree.remove(1))
        }

        @Test
        fun `remove nonexistent key test`() {
            tree.insert(1, 2)
            assertFalse(tree.remove(2))
            assertEquals(2, tree.find(1))
        }

        @Test
        fun `add nodes and remove all nodes test`() {
            val nodes = mutableListOf<ConcurrentBSNode<Int, Int>>()

            tree.insert(1, 4)
            tree.insert(3, 4)
            tree.insert(2, 3)
            tree.insert(-3, 3)

            tree.remove(3)
            tree.remove(1)
            tree.remove(2)
            tree.remove(-3)

            for (node in tree) {
                nodes.add(node)
            }
            assertTrue(nodes.isEmpty())
        }

        @Test
        fun `remove root test`() {
            val nodes = mutableListOf<ConcurrentBSNode<Int, Int>>()
            tree.insert(1, 2)
            assertTrue(tree.remove(1))
            for (node in tree) {
                nodes.add(node)
            }
            assertNull(tree.find(1))
            assertTrue(nodes.isEmpty())
        }

        @Test
        fun `remove root with right son test`() {
            tree.insert(1, 1)
            tree.insert(2, 2)
            assertTrue(tree.remove(1))
            assertNull(tree.find(1))
            assertEquals(2, tree.find(2))
        }

        @Test
        fun `remove root with left son test`() {
            tree.insert(2, 2)
            tree.insert(1, 1)
            assertTrue(tree.remove(2))
            assertNull(tree.find(2))
            assertEquals(1, tree.find(1))
        }

        @Test
        fun `remove root with 2 kids test`() {
            tree.insert(2, 2)
            tree.insert(1, 1)
            tree.insert(3, 3)
            assertTrue(tree.remove(2))
            assertNull(tree.find(2))
            assertEquals(1, tree.find(1))
            assertEquals(3, tree.find(3))
        }

        @Test
        fun `remove node with left son test`() {
            var countOfNodes = 0
            tree.insert(7, 15)
            tree.insert(3, 10)
            tree.insert(2, 9)
            tree.remove(3)
            for (node in tree) {
                countOfNodes++
            }
            assertNull(tree.find(3))
            assertEquals(15, tree.find(7))
            assertEquals(9, tree.find(2))
            assertEquals(2, countOfNodes)
        }

        @Test
        fun `remove node with right son test`() {
            var countOfNodes = 0
            tree.insert(7, 15)
            tree.insert(3, 10)
            tree.insert(4, 11)
            tree.remove(3)
            for (node in tree) {
                countOfNodes++
            }
            assertNull(tree.find(3))
            assertEquals(15, tree.find(7))
            assertEquals(11, tree.find(4))
            assertEquals(2, countOfNodes)
        }

        @Test
        fun `remove node with 2 kids test`() {
            var countOfNodes = 0
            tree.insert(7, 15)
            tree.insert(3, 10)
            tree.insert(2, 9)
            tree.insert(4, 11)
            tree.remove(3)
            for (node in tree) {
                countOfNodes++
            }
            assertNull(tree.find(3))
            assertEquals(15, tree.find(7))
            assertEquals(9, tree.find(2))
            assertEquals(11, tree.find(4))
            assertEquals(3, countOfNodes)
        }

        @Test
        fun `remove node with 2 kids which have 2 kids too test`() {
            var countOfNodes = 0
            tree.insert(20, 1)
            tree.insert(15, 2)
            tree.insert(10, 3)
            tree.insert(17, 4)
            tree.insert(5, 5)
            tree.insert(12, 6)
            tree.insert(16, 7)
            tree.insert(18, 8)
            tree.remove(15)
            for (node in tree) {
                countOfNodes++
            }
            assertNull(tree.find(15))
            assertEquals(1, tree.find(20))
            assertEquals(3, tree.find(10))
            assertEquals(4, tree.find(17))
            assertEquals(5, tree.find(5))
            assertEquals(6, tree.find(12))
            assertEquals(7, tree.find(16))
            assertEquals(8, tree.find(18))
            assertEquals(7, countOfNodes)
        }
    }

    @Nested
    inner class ParallelTests {
        /**
         * A sequential insertion of vertices into the tree is done and
         * the subsequent parallel use of the find function in 4 threads
         * to the inserted vertices, also the find function is checked on
         * non-existing vertices
         */
        @Test
        fun `parallel find test`() {
            tree.insert(5, 5)
            tree.insert(12, 6)
            tree.insert(16, 7)
            tree.insert(9, 9)
            tree.insert(15, 2)
            tree.insert(10, 17)
            tree.insert(8, 4)
            tree.insert(90, 12)
            tree.insert(6, 10)
            tree.insert(23, 19)
            tree.insert(20, 11)
            tree.insert(2, 31)

            val threadPool = Executors.newFixedThreadPool(4)
            val task1 = Runnable {
                assertEquals(9, tree.find(9))
                assertEquals(6, tree.find(12))
                assertEquals(11, tree.find(20))
            }
            val task2 = Runnable {
                assertEquals(5, tree.find(5))
                assertEquals(7, tree.find(16))
                assertNull(tree.find(100))
            }
            val task3 = Runnable {
                assertEquals(12, tree.find(90))
                assertEquals(4, tree.find(8))
                assertEquals(10, tree.find(6))
            }
            val task4 = Runnable {
                assertEquals(11, tree.find(20))
                assertEquals(6, tree.find(12))
                assertEquals(10, tree.find(6))
                assertNull(tree.find(200))
            }
            threadPool.execute(task1)
            threadPool.execute(task2)
            threadPool.execute(task3)
            threadPool.execute(task4)
            threadPool.shutdown()
            while (!threadPool.isTerminated) {
            }
        }

        /**
         * Parallel insertion of vertices in 4 threads, and then checking
         * the inserted vertices for compliance with their keys and values,
         * also counting the number of vertices that were inserted (the number
         * of vertices after insertion must coincide with the number of
         * inserted vertices), a sequential tree traversal is also performed
         * (keys must strictly increase with such a traversal)
         */
        @Test
        fun `parallel insert test on correct building tree`() {
            var countOfNodes = 0
            val arrayOfKeys = arrayOf(9, 15, 10, 23, 5, 12, 16, 8, 90, 6, 20, 2)
            val arrayOfValues = arrayOf(9, 2, 17, 19, 5, 6, 7, 4, 12, 10, 11, 31)

            val threadPool = Executors.newFixedThreadPool(4)
            val task1 = Runnable {
                tree.insert(9, 9)
                tree.insert(15, 2)
                tree.insert(10, 17)
            }
            val task2 = Runnable {
                tree.insert(5, 5)
                tree.insert(12, 6)
                tree.insert(16, 7)
            }
            val task3 = Runnable {
                tree.insert(8, 4)
                tree.insert(90, 12)
                tree.insert(6, 10)
            }
            val task4 = Runnable {
                tree.insert(23, 19)
                tree.insert(20, 11)
                tree.insert(2, 31)
            }
            threadPool.execute(task1)
            threadPool.execute(task2)
            threadPool.execute(task3)
            threadPool.execute(task4)
            threadPool.shutdown()
            while (!threadPool.isTerminated) {
            }

            for (node in tree) {
                when (val index = findInArray(arrayOfKeys, node.key)) {
                    null -> assertEquals(
                        "check error message", "Error! have found non-existent key" +
                                " = ${node.key}"
                    )
                    else -> if (arrayOfValues[index] != node.value) {
                        assertEquals(
                            "check error message", "Error! ${node.key} " +
                                    "== ${arrayOfKeys[index]}, but ${node.value} != ${arrayOfValues[index]}"
                        )
                    }
                }
                countOfNodes++
            }
            val listOfKeys = tree.makeInorderTreeMutableList()
            var firstElement = listOfKeys[0]
            listOfKeys.remove(firstElement)
            for (el in listOfKeys) {
                if (firstElement < el) firstElement = el
                else {
                    assertEquals(
                        "check error message", "Error! the tree is built incorrectly" +
                                " the key $firstElement is not less than $el"
                    )
                }
            }
            assertEquals(12, countOfNodes)
        }

        /**
         * A sequential insertion of vertices into the tree and subsequent parallel
         * use of the remove function in 4 threads is performed, it is also
         * checked that the remaining set of vertices after deletion is equal
         * to the complement of the deleting set of vertices, the correspondence
         * of the remaining vertices to their keys and value is also checked,
         * the tree is also traversed in the order of the tree and the strict
         * increase in key values is checked with such a traversal
         */
        @Test
        fun `parallel remove test on correct building tree`() {
            val arrayOfNotRemovedKeys = arrayOf(20, 5, 12, 16, 35)
            val arrayOfNotRemovedValues = arrayOf(1, 5, 6, 7, 7)
            val checkListOfKeys = arrayOfNotRemovedKeys.toMutableList()

            tree.insert(20, 1)
            tree.insert(15, 2)
            tree.insert(10, 3)
            tree.insert(17, 4)
            tree.insert(5, 5)
            tree.insert(12, 6)
            tree.insert(16, 7)
            tree.insert(18, 8)
            tree.insert(35, 7)
            tree.insert(9, 2)
            tree.insert(7, 15)
            tree.insert(3, 10)
            tree.insert(2, 9)
            tree.insert(4, 11)

            val threadPool = Executors.newFixedThreadPool(4)
            val task1 = Runnable {
                tree.remove(15)
                tree.remove(10)
                tree.remove(2)
            }
            val task2 = Runnable {
                assertFalse(tree.remove(1))
                tree.remove(3)
                tree.remove(7)

            }
            val task3 = Runnable {
                tree.remove(18)
                tree.remove(4)
            }
            val task4 = Runnable {
                tree.remove(17)
                tree.remove(9)
                assertFalse(tree.remove(100))
            }
            threadPool.execute(task1)
            threadPool.execute(task2)
            threadPool.execute(task3)
            threadPool.execute(task4)
            threadPool.shutdown()
            while (!threadPool.isTerminated) {
            }

            for (node in tree) {
                when (val index = findInArray(arrayOfNotRemovedKeys, node.key)) {
                    null -> assertEquals(
                        "check error message", "Error! have found non-existent key" +
                                " = ${node.key}"
                    )
                    else -> if (arrayOfNotRemovedValues[index] != node.value) {
                        assertEquals(
                            "check error message", "Error! ${node.key} " +
                                    "== ${arrayOfNotRemovedKeys[index]}, but ${node.value} != ${arrayOfNotRemovedValues[index]}"
                        )
                    }
                }
                checkListOfKeys.remove(node.key)
            }
            assertTrue(checkListOfKeys.isEmpty())

            val listOfKeys = tree.makeInorderTreeMutableList()
            var firstElement = listOfKeys[0]
            listOfKeys.remove(firstElement)
            for (el in listOfKeys) {
                if (firstElement < el) firstElement = el
                else {
                    assertEquals(
                        "check error message", "Error! the tree is built incorrectly" +
                                " the key $firstElement is not less than $el"
                    )
                }
            }
        }

        /**
         * Parallel insertion and deletion and finding of vertices in 4 threads, and
         * then checking the inserted vertices for compliance with their
         * keys and values, a sequential traversal of the tree is also
         * performed (the keys must strictly increase with such a traversal)
         */
        @Test
        fun `parallel insert and remove test on correct building tree`() {
            val arrayOfKeys = arrayOf(20, 15, 10, 17, 9, 23, 5, 12, 16, 18, 3, 4, 8, 90, 6, 1, 2)
            val arrayOfValues = arrayOf(1, 2, 17, 4, 12, 19, 5, 6, 7, 8, 2, 14, 4, 12, 10, 4, 31)

            val threadPool = Executors.newFixedThreadPool(4)
            val task1 = Runnable {
                tree.insert(20, 1)
                tree.insert(15, 2)
                tree.insert(10, 17)
                tree.insert(17, 4)
                tree.insert(9, 12)
                tree.insert(23, 19)
            }
            val task2 = Runnable {
                tree.insert(5, 5)
                tree.insert(12, 6)
                tree.insert(16, 7)
                tree.insert(18, 8)
                tree.remove(15)
                tree.insert(3, 2)
                tree.insert(4, 14)
            }
            val task3 = Runnable {
                tree.insert(8, 4)
                tree.insert(90, 12)
                tree.remove(20)
                tree.remove(12)
                tree.insert(6, 10)
                assertEquals(7, tree.find(16))
                tree.remove(8)
            }
            val task4 = Runnable {
                tree.remove(1)
                tree.remove(5)
                tree.insert(1, 4)
                tree.insert(2, 31)
                tree.remove(20)
                assertEquals(31, tree.find(2))
            }
            threadPool.execute(task1)
            threadPool.execute(task2)
            threadPool.execute(task3)
            threadPool.execute(task4)
            threadPool.shutdown()
            while (!threadPool.isTerminated) {
            }

            for (node in tree) {
                when (val index = findInArray(arrayOfKeys, node.key)) {
                    null -> assertEquals(
                        "check error message", "Error! have found non-existent key" +
                                " = ${node.key}"
                    )
                    else -> if (arrayOfValues[index] != node.value) {
                        assertEquals(
                            "check error message", "Error! ${node.key} " +
                                    "== ${arrayOfKeys[index]}, but ${node.value} != ${arrayOfValues[index]}"
                        )
                    }
                }
            }
            val listOfKeys = tree.makeInorderTreeMutableList()
            var firstElement = listOfKeys[0]
            listOfKeys.remove(firstElement)
            for (el in listOfKeys) {
                if (firstElement < el) firstElement = el
                else {
                    assertEquals(
                        "check error message", "Error! the tree is built incorrectly" +
                                " the key $firstElement is not less than $el"
                    )
                }
            }
        }

        /**
         * Parallel insertion and deletion of 2000 vertices in 4 threads,
         * and then checking the inserted vertices for compliance with their
         * keys and values, a sequential traversal of the tree is also
         * performed (the keys must strictly increase with such a traversal)
         */
        @Test
        fun `high contention parallel insert and remove test on correct building tree`() {
            val from = -10000
            val to = 10000
            val numberNodes = 2000
            val mapKeysAndValues = hashMapOf<Int, Int>()
            for (i in 0 until numberNodes) {
                mapKeysAndValues[Random.nextInt(from, to)] = Random.nextInt(from, to)
            }
            val arrayOfKeys = mapKeysAndValues.keys.toIntArray()
            val arrayOfValues = mapKeysAndValues.values.toIntArray()
            val sizeOfArray = arrayOfKeys.size

            val threadPool = Executors.newFixedThreadPool(4)
            val task1 = Runnable {
                for (i in 0 until sizeOfArray / 4) {
                    tree.insert(arrayOfKeys[i], arrayOfValues[i])
                    if (i % 7 == 0) tree.remove(arrayOfKeys[Random.nextInt(0, sizeOfArray)])
                }
            }
            val task2 = Runnable {
                for (i in sizeOfArray / 4 until sizeOfArray / 2) {
                    tree.insert(arrayOfKeys[i], arrayOfValues[i])
                    if (i % 19 == 0) tree.remove(arrayOfKeys[Random.nextInt(0, sizeOfArray)])
                }
            }
            val task3 = Runnable {
                for (i in sizeOfArray / 2 until sizeOfArray / 4 * 3) {
                    tree.insert(arrayOfKeys[i], arrayOfValues[i])
                    if (i % 21 == 0) tree.remove(arrayOfKeys[Random.nextInt(0, sizeOfArray)])
                }
            }
            val task4 = Runnable {
                for (i in sizeOfArray / 4 * 3 until sizeOfArray) {
                    tree.insert(arrayOfKeys[i], arrayOfValues[i])
                    if (i % 5 == 0) tree.remove(arrayOfKeys[Random.nextInt(0, sizeOfArray)])
                }
            }
            threadPool.execute(task1)
            threadPool.execute(task2)
            threadPool.execute(task3)
            threadPool.execute(task4)
            threadPool.shutdown()
            while (!threadPool.isTerminated) {
            }

            for (node in tree) {
                when (val value = mapKeysAndValues[node.key]) {
                    null -> assertEquals(
                        "check error message", "Error! have found non-existent key" +
                                " = ${node.key}"
                    )
                    else -> if (value != node.value) {
                        assertEquals(
                            "check error message", "Error! ${node.key} ==" +
                                    " ${node.key}, but ${node.value} != $value"
                        )
                    }
                }
            }
            val listOfKeys = tree.makeInorderTreeMutableList()
            var firstElement = listOfKeys[0]
            listOfKeys.remove(firstElement)
            for (el in listOfKeys) {
                if (firstElement < el) firstElement = el
                else {
                    assertEquals(
                        "check error message", "Error! the tree is built incorrectly" +
                                " the key $firstElement is not less than $el"
                    )
                }
            }
        }
    }
}