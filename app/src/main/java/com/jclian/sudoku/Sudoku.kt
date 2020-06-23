package com.jclian.sudoku

import android.util.Log
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.HashSet


/**
 * 该算法根据知乎大神写的生成算法详解
 * https://zhuanlan.zhihu.com/p/67447747
 * 数独的求解用上了Dance Link X算法，知乎大神也做了解释
 * https://zhuanlan.zhihu.com/p/67324277
 * *
 * 下面使用kotlin，转写了一下
 */
object Sudoku {
    fun initLocationDict(initCount: Int): HashMap<String, Int> {
        val dict = HashMap<String, Int>()
        val s = HashSet<Int>()
        while (dict.keys.size < initCount) {
            val i: Int = (Math.random() * 9).toInt()
            val j: Int = (Math.random() * 9).toInt()
            val k: Int = 1 + (Math.random() * 9).toInt()

            val a = i * 9 + j
            if (s.contains(a)) {
                continue
            }
            val b = i * 9 + k + 80
            if (s.contains(b)) {
                continue
            }
            val c = j * 9 + k + 161
            if (s.contains(c)) {
                continue
            }
            val d = ((i / 3).toInt() * 3 + (j / 3).toInt()) * 9 + k + 242
            if (s.contains(d)) {
                continue
            }
            s.add(a)
            s.add(b)
            s.add(c)
            s.add(d)
            dict["$i,$j"] = k
        }
        return dict
    }

    fun getFormattedAnswer(ans: ArrayList<String>): Array<IntArray> {
        ans.sort()
        val arr = Array(9) { IntArray(9) }
        for (row_id in ans) {
            val loc = row_id.toInt() / 9
            val i = (loc / 9).toInt()
            val j = loc % 9
            val k = row_id.toInt() % 9 + 1
            arr[i][j] = k
        }
        return arr
    }

    fun getSudokuMap(ans: ArrayList<String>): HashMap<String, Int> {
        val map = HashMap<String, Int>()
        ans.sort()
        val arr = Array(9) { IntArray(9) }
        for (row_id in ans) {
            val loc = row_id.toInt() / 9
            val i = (loc / 9).toInt()
            val j = loc % 9
            val k = row_id.toInt() % 9 + 1
            map["$i,$j"] = k
        }

        // 随机挖空
        for (count in 0..30) {
            val i: Int = (Math.random() * 9).toInt()
            val j: Int = (Math.random() * 9).toInt()
            val key = "$i,$j"
            map.remove(key)
        }

        return map
    }

    fun getSudokuLinkedList(map: HashMap<String, Int>): CrossCycleLinkNode<String> {
        val head = initCol(324)
        for (i in 0..8) {
            for (j in 0..8) {
                val key = "$i,$j"
                if (map.contains(key)) {

                    val k = map[key]!!
                    // 条件一：max 8×9+8
                    val a = i * 9 + j
                    // 所以这里加 80,实际上共81个条件
                    //
                    val b = i * 9 + k + 80
                    val c = j * 9 + k + 161
                    val d = ((i / 3) * 3 + (j / 3)) * 9 + k + 242
                    val rowId = (i * 9 + j) * 9 + k - 1
                    appendRow(
                        head,
                        rowId.toString(),
                        arrayListOf(a.toString(), b.toString(), c.toString(), d.toString())
                    )
                } else {

                    for (k in 1..9) {
                        val a = i * 9 + j
                        val b = i * 9 + k + 80
                        val c = j * 9 + k + 161
                        val d = ((i / 3) * 3 + (j / 3)) * 9 + k + 242
                        val rowId = (i * 9 + j) * 9 + k - 1
                        appendRow(
                            head,
                            rowId.toString(),
                            arrayListOf(a.toString(), b.toString(), c.toString(), d.toString())
                        )
                    }
                }
            }

        }
        return head
    }

    fun initCol(col_count: Int): CrossCycleLinkNode<String> {
        val head = CrossCycleLinkNode("head", "column")
        for (i in 0 until col_count) {
            val colNode = CrossCycleLinkNode(i.toString(), head.row)
            colNode.right = head
            colNode.left = head.left
            colNode.right.left = colNode
            colNode.left.right = colNode
        }
        return head
    }


    fun appendRow(head: CrossCycleLinkNode<String>, row_id: String, list: List<String>) {

        var last: CrossCycleLinkNode<String>? = null
        var col = head.right
        for (num in list) {
            while (col != head) {
                if (col.value == num) {
                    val node = CrossCycleLinkNode(1.toString(), row_id)
                    node.col = col
                    node.down = col
                    node.up = col.up
                    node.down.up = node
                    node.up.down = node
                    if (last != null) {
                        node.left = last
                        node.right = last.right
                        node.left.right = node
                        node.right.left = node

                    }
                    last = node
                    break
                }

                col = col.right
            }

        }

    }

    fun danceLinkX(head: CrossCycleLinkNode<String>, answers: ArrayList<String>): Boolean {
//        Log.d("sudikuview#dance_link_x", "head=$head, ans=$answers")
        if (head.right == head) return true

        var node = head.right
        while (node != head) {
            if (node.down == node) return false
            node = node.right
        }

        val restores = ArrayList<(() -> Unit)>()
        val firstCol = head.right
        firstCol.removeColumn()
        val restoreColumn: () -> Unit = firstCol::restoreColumn
        restores.add(restoreColumn)

        node = firstCol.down
        while (node != firstCol) {
            if (node.right != node) {
                node.right.removeRow()
                restores.add(node.right::restoreRow)
            }
            node = node.down
        }
        val curRestoresCount = restores.size
        var selectedRow = firstCol.down
        while (selectedRow != firstCol) {
            answers.add(selectedRow.row)
            if (selectedRow.right != selectedRow) {
                var rowNode = selectedRow.right
                while (true) {
                    var colNode = rowNode.col
                    colNode.removeColumn()
                    restores.add(colNode::restoreColumn)
                    colNode = colNode.down
                    while (colNode != colNode.col) {
                        if (colNode.right != colNode) {
                            colNode.right.removeRow()
                            restores.add(colNode.right::restoreRow)
                        }
                        colNode = colNode.down
                    }
                    rowNode = rowNode.right
                    if (rowNode == selectedRow.right) {
                        break
                    }
                }
            }
            if (danceLinkX(head, answers)) {
//            #while len(restores): restores.pop()()
                return true
            }
            answers.removeAt(answers.size - 1)
            while (restores.size > curRestoresCount) {
                val method = restores[restores.size - 1]
                method.invoke()
                restores.remove(method)
            }
            selectedRow = selectedRow.down
        }
        while (restores.size > 0) {
            val method = restores[restores.size - 1]
            method.invoke()
            restores.remove(method)
        }
        return false
    }


    fun gen(): HashMap<String, Int> {
        val initData = initLocationDict(11)
        var head = getSudokuLinkedList(initData)
        val ans = ArrayList<String>()
        danceLinkX(head, ans)
        if (ans.size > 0) {
            Log.d("SudokuView", "$ans")
            val map: HashMap<String, Int> = getSudokuMap(ans)
            head = getSudokuLinkedList(initData)
            ans.clear()
            danceLinkX(head, ans)
            if (ans.size > 0) {
                return map
            }
            return gen()
        }
        return gen()

    }

    fun check(data: HashMap<String, Int>): Boolean {
        val head = getSudokuLinkedList(data)
        val ans = ArrayList<String>()
        danceLinkX(head, ans)
        return ans.size > 0
    }


}