package com.jclian.sudoku


class CrossCycleLinkNode<T>(val value: T, val row: String) {
    var up: CrossCycleLinkNode<T> = this
    var down: CrossCycleLinkNode<T> = this
    var left: CrossCycleLinkNode<T> = this
    var right: CrossCycleLinkNode<T> = this
    var col: CrossCycleLinkNode<T> = this

    init {
        this.col = this
        this.up = this
        this.down = this
        this.left = this
        this.right = this
    }

    fun removeColumn() {
        var node = this
        while (true) {
            node.left.right = node.right
            node.right.left = node.left
            node = node.down
            if (node == this) break
        }
    }

    fun restoreColumn(){
        var node = this
        while (true) {
            node.left.right = node
            node.right.left = node
            node = node.down
            if (node == this) {
                break
            }
        }
    }

    fun removeRow() {
        var node = this
        while (true) {
            node.up?.down = node.down
            node.down.up = node.up
            node = node.right
            if (node == this) {
                break
            }
        }
    }


    fun restoreRow() {
        var node = this
        while (true) {
            node.up.down = node
            node.down.up = node
            node = node.right
            if (node == this) break
        }
    }

    override fun toString(): String {
        return super.toString()
    }
}