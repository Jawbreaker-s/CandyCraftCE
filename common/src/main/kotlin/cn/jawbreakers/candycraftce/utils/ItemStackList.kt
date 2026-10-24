package cn.jawbreakers.candycraftce.utils

import net.minecraft.core.NonNullList
import net.minecraft.tags.TagKey
import net.minecraft.world.ContainerHelper
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import java.util.function.Predicate
import kotlin.math.min

/**
 * Created in 2024/2/3
 * Project: candycraftce
 * 
 * Author [Bread_NiceCat](https://github.com/Bread-NiceCat)
 * 
 * Kotlinize in 2026/10/23
 */
class ItemStackList(size: Int) : NonNullList<ItemStack>(
    Array<ItemStack>(size) { ItemStack.EMPTY }.asList(),
    ItemStack.EMPTY
) {
    override fun isEmpty(): Boolean {
        for (stack in this) {
            if (!stack.isEmpty) return false
        }
        return true
    }

    fun isEmpty(index: Int): Boolean = get(index).isEmpty

    fun extract(index: Int, expectCount: Int): ItemStack {
        return ContainerHelper.removeItem(this, index, expectCount)
    }

    /**
     * 注意：会额外对参数[item]直接修改
     * 
     * @return 未被插入的部分
     */
    fun insert(index: Int, item: ItemStack): ItemStack {
        val cur = get(index)
        if (item.isEmpty) { //不能插空
            //直接返回
        } else if (cur.isEmpty) { //若空则直接放入
            set(index, item.copy())
            item.count = 0
        } else {
            val left = cur.maxStackSize - cur.count //能插的最大数量
            if (left != 0 && ItemStack.isSameItemSameTags(cur, item)) {
                val itemCnt = item.count
                if (left >= itemCnt) { //剩余 >= 外来
                    cur.grow(itemCnt)
                    item.shrink(itemCnt) //0
                } else {
                    cur.grow(left) //max
                    item.shrink(left)
                }
            }
        }
        return item
    }

    /**
     * 模拟插入
     * 插入，但是不对列表中的数据进行修改
     * 注意：仍然会对参数[item]进行修改
     * 
     * @return 未被插入的部分
     */
    fun simulateInsert(index: Int, item: ItemStack): ItemStack {
        val cur = get(index)
        if (item.isEmpty) { //不能插空
            //直接返回
        } else if (cur.isEmpty) { //若空则直接放入
            item.count = 0
        } else {
            val left = cur.maxStackSize - cur.count //能插的最大数量
            if (left != 0 && ItemStack.isSameItemSameTags(cur, item)) {
                val itemCnt = item.count
                //剩余 >= 外来
                item.shrink(min(left, itemCnt)) //0
            }
        }
        return item
    }

    override fun removeAt(index: Int): ItemStack {
        return set(index, ItemStack.EMPTY)
    }

    /**
     * 尝试从from中转移`min(count,expectedCount)`到to中
     * 
     * 
     * @param from 索引
     * @param to 索引
     * 
     * @return 转移的数量
     */
    fun transfer(from: Int, to: Int, expectedCount: Int): Int {
        val ori = get(from)
        val dest = get(to)
        if (dest.isEmpty) {
            val split = ori.split(expectedCount)
            set(to, split)
            return split.count
        } else if (dest.`is`(ori.item)) {
            val cnt = min(expectedCount, min(ori.count, dest.maxStackSize - dest.count))
            ori.shrink(cnt)
            dest.grow(cnt)
            return cnt
        }
        return 0
    }

    /**
     * 交换
     */
    fun swap(from: Int, to: Int) {
        val fr = get(from)
        val dest = get(to)
        set(from, dest)
        set(to, fr)
    }

    fun `is`(index: Int, item: Item): Boolean = get(index).`is`(item)

    fun `is`(index: Int, tag: TagKey<Item>): Boolean = get(index).`is`(tag)

    fun `is`(index: Int, item: Predicate<ItemStack?>): Boolean = item.test(get(index))
}
