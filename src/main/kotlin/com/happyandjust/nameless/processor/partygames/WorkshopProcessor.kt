/*
 * Nameless - 1.8.9 Hypixel Quality Of Life Mod
 * Copyright (C) 2021 HappyAndJust
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.happyandjust.nameless.processor.partygames

import com.happyandjust.nameless.core.TickTimer
import com.happyandjust.nameless.core.value.Pos
import com.happyandjust.nameless.dsl.*
import com.happyandjust.nameless.events.PartyGameChangeEvent
import com.happyandjust.nameless.events.SpecialOverlayEvent
import com.happyandjust.nameless.events.SpecialTickEvent
import com.happyandjust.nameless.features.impl.qol.FeaturePartyGamesHelper
import com.happyandjust.nameless.hypixel.PartyGamesType
import com.happyandjust.nameless.mixins.accessors.AccessorGuiContainer
import com.happyandjust.nameless.mixins.accessors.AccessorShapedOreRecipe
import com.happyandjust.nameless.processor.Processor
import com.happyandjust.nameless.utils.RenderUtils
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import net.minecraft.block.*
import net.minecraft.block.state.IBlockState
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.gui.inventory.GuiCrafting
import net.minecraft.client.gui.inventory.GuiFurnace
import net.minecraft.entity.item.EntityItemFrame
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.CraftingManager
import net.minecraft.item.crafting.IRecipe
import net.minecraft.item.crafting.ShapedRecipes
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraftforge.client.event.GuiScreenEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.oredict.ShapedOreRecipe
import java.awt.Color
import java.util.*
import kotlin.math.ceil
import kotlin.reflect.KClass

@OptIn(DelicateCoroutinesApi::class)
object WorkshopProcessor : Processor() {
    //39 ~ 45

    private var prevOutputItemStack: ItemStack? = null
    private val scanTimer = TickTimer(3)
    private val tasks = arrayListOf<WorkshopTask>()
    private var currentWorkshopRoom: WorkshopRoom? = null
    private val toWorkshopBlockType: BlockPos.() -> WorkshopBlockType? = {
        when (mc.theWorld.getBlockAtPos(this)) {
            is BlockFurnace -> WorkshopBlockType.FURNACE
            Blocks.crafting_table -> WorkshopBlockType.CRAFTING_TABLE
            Blocks.stone, Blocks.obsidian, Blocks.iron_ore, Blocks.redstone_ore, Blocks.gold_ore, Blocks.log, Blocks.log2, Blocks.web, Blocks.melon_block, is BlockOre, Blocks.bookshelf -> {
                if (arrayOf(
                        mc.theWorld.getBlockAtPos(up()),
                        mc.theWorld.getBlockAtPos(down())
                    ).all { it is BlockSlab || it is BlockStairs || it is BlockDoubleWoodSlab }
                ) WorkshopBlockType.BREAKABLE else null
            }
            else -> null
        }
    }

    private val itemRecipeCached = hashMapOf<Item, IRecipe?>()
    override val filter = FeaturePartyGamesHelper.getFilter(this)

    private inline fun firstTask(action: WorkshopTask.() -> Unit) {
        tasks.firstOrNull()?.action()
    }

    init {
        request<SpecialTickEvent>().filter { scanTimer.update().check() }.subscribe {
            firstTask { if (isCompleted()) tasks.remove(this) }

            currentWorkshopRoom?.let {
                val output = it.outputItemFrame.displayedItem

                if (output != prevOutputItemStack && it.blockPosList.filter { pos -> pos.toWorkshopBlockType() == WorkshopBlockType.BREAKABLE }.size == 100) {
                    tasks.clear()
                    prevOutputItemStack = output
                    GlobalScope.launch {
                        scanRecipes(it)
                    }
                }
                return@subscribe
            }

            val outputItemFrame = mc.theWorld.loadedEntityList
                .filterIsInstance<EntityItemFrame>()
                .find { mc.thePlayer.getDistanceToEntity(it) <= 10 && it.hangingPosition.y == 39 } ?: return@subscribe

            currentWorkshopRoom = WorkshopRoom(outputItemFrame, scanRoom(outputItemFrame.hangingPosition))

        }
    }

    private suspend fun scanRecipes(
        workshopRoom: WorkshopRoom,
    ) = coroutineScope {
        val outputItem = workshopRoom.outputItemFrame.displayedItem ?: return@coroutineScope
        val sortedPosList = workshopRoom.blockPosList.sortedBy { mc.thePlayer.getDistanceSq(it) }

        val breakAbleBlocks =
            workshopRoom.blockPosList.filter { it.toWorkshopBlockType() == WorkshopBlockType.BREAKABLE }
                .sortedWith(compareBy({ if (it.y in 40 to 42) 41 else 47 }, { mc.thePlayer.getDistanceSq(it) }))
                .associateWith { mc.theWorld.getBlockState(it) }
                .toList()
                .toMutableList()

        val existingBlocks = breakAbleBlocks.map { it.second }.toHashSet()

        val recipeItems = outputItem.item.toRecipeItems(existingBlocks)

        var needPlanksCount = 0

        fun RecipeItem.processLogs() {
            val logs =
                recipesOfItem.filter { it.item in Item.getItemFromBlock(Blocks.log) to Item.getItemFromBlock(Blocks.log2) }
                    .toSet()

            needPlanksCount += logs.size

            recipesOfItem.removeAll(logs)

            recipesOfItem.forEach(RecipeItem::processLogs)
        }
        recipeItems.forEach(RecipeItem::processLogs)

        val finalRecipes = recipeItems.flatMap { it.getFinalChildRecipeItems() }.toMutableList()

        val needLogsCount = ceil(needPlanksCount / 4.0).toInt()

        val log =
            Item.getItemFromBlock(existingBlocks.find { it.block == Blocks.log || it.block == Blocks.log2 }?.block)
        if (needLogsCount != 0 && log == null) return@coroutineScope

        tasks.add(BreakTask(finalRecipes + List(needLogsCount) { RecipeItem(Pos.TOP_LEFT, log) }, breakAbleBlocks))

        val finalRecipeItems = finalRecipes.map { it.item }

        if (finalRecipeItems.any { it in Items.iron_ingot to Items.gold_ingot }) {
            tasks.add(
                BlockClickTask(
                    sortedPosList.first { it.toWorkshopBlockType() == WorkshopBlockType.FURNACE },
                    GuiFurnace::class
                )
            )

            for ((ingot, ore) in arrayOf(
                Items.iron_ingot to Item.getItemFromBlock(Blocks.iron_ore),
                Items.gold_ingot to Item.getItemFromBlock(Blocks.gold_ore)
            )) {
                if (ingot in finalRecipeItems) {
                    val count = finalRecipeItems.count { it == ingot }
                    tasks.add(
                        WindowClickTask(
                            arrayListOf(0 to CustomItemStack(ore, count)),
                            CustomItemStack(ingot, count),
                            GuiFurnace::class
                        )
                    )
                }
            }
        }

        tasks.add(
            BlockClickTask(
                sortedPosList.first { it.toWorkshopBlockType() == WorkshopBlockType.CRAFTING_TABLE },
                GuiCrafting::class
            )
        )
        val stack = Stack<WindowClickTask>()
        stack.push(
            WindowClickTask(
                recipeItems.map { it.pos.ordinal + 1 to CustomItemStack(it.item) },
                CustomItemStack(outputItem.item, 1),
                GuiCrafting::class
            )
        )

        fun RecipeItem.digChildren() {
            if (recipesOfItem.isEmpty()) return
            stack.push(
                WindowClickTask(
                    recipesOfItem.map { it.pos.ordinal + 1 to CustomItemStack(it.item) },
                    CustomItemStack(item, 1),
                    GuiCrafting::class
                )
            )

            recipesOfItem.forEach { it.digChildren() }
        }

        recipeItems.forEach { it.digChildren() }

        if (needLogsCount > 0) {
            stack.push(
                WindowClickTask(
                    arrayListOf(1 to CustomItemStack(log, needLogsCount)),
                    CustomItemStack(Item.getItemFromBlock(Blocks.planks), needPlanksCount),
                    GuiCrafting::class
                )
            )
        }

        var lastAdded: WindowClickTask? = null

        while (stack.isNotEmpty()) {
            val clickTask = stack.pop()

            if (clickTask != lastAdded) {
                lastAdded = clickTask
                tasks.add(clickTask)
            }
        }

    }

    private fun Item.toRecipeItems(existingBlocks: Set<IBlockState>): List<RecipeItem> {
        val recipe = getRecipe()?.getInputRecipe() ?: return emptyList()

        val random = Random()

        fun IBlockState.getItemDrop() = when (block) {
            Blocks.gold_ore -> Items.gold_ingot
            Blocks.iron_ore -> Items.iron_ingot
            else -> block.getItemDropped(this, random, 1)
        }

        if (this in existingBlocks.map { it.getItemDrop() }) return emptyList() // don't need recipe

        return recipe.map {
            RecipeItem(it.key, it.value).apply { recipesOfItem.addAll(item.toRecipeItems(existingBlocks)) }
        }
    }

    private fun Item.getRecipe(): IRecipe? {
        return itemRecipeCached.getOrPut(this) {
            when (val recipe = CraftingManager.getInstance().recipeList.find { it.recipeOutput?.item == this }) {
                is ShapedOreRecipe, is ShapedRecipes -> recipe
                else -> null
            }
        }
    }

    private fun IRecipe.getInputRecipe(): Map<Pos, Item> {
        val map = hashMapOf<Pos, Item>()
        val width = if (this is ShapedRecipes) recipeWidth else (this as AccessorShapedOreRecipe).width
        val height = if (this is ShapedRecipes) recipeHeight else (this as AccessorShapedOreRecipe).height
        val input = if (this is ShapedRecipes) recipeItems else (this as ShapedOreRecipe).input

        fun Any.getItem(): Item? = when (this) {
            is Item -> this
            is ItemStack -> item
            is List<*> -> mapNotNull { it?.getItem() }.firstOrNull()
            else -> null
        }

        val values = Pos.values()

        for (y in 0 until height) {
            for (x in 0 until width) {
                val pos = values.find { it.ordinal == x + y * 3 }!!
                val inputItem = input[x + y * width]

                map[pos] = inputItem?.getItem() ?: continue
            }
        }

        return map
    }

    private fun scanRoom(initial: BlockPos): List<BlockPos> {

        fun BlockPos.isAir() = mc.theWorld.getBlockAtPos(this) is BlockAir

        val queue = LinkedList<BlockPos>()
        val visited = hashMapOf<BlockPos, Boolean>()
        val facings = EnumFacing.values()

        val list = arrayListOf<BlockPos>()

        queue.add(initial)

        while (queue.isNotEmpty()) {
            val pos = queue.poll()

            list.add(pos)

            queue.addAll(
                facings
                    .map { pos.offset(it) }
                    .filterNot { visited[it] ?: false }
                    .filter { it.y in 39..48 }
                    .filterNot { it.isAir() && it.up().isAir() }
                    .onEach { visited[it] = true }
            )
        }

        return list
    }

    init {
        on<PartyGameChangeEvent>().filter { from == PartyGamesType.WORKSHOP || to == PartyGamesType.WORKSHOP }
            .subscribe {
                currentWorkshopRoom = null
                tasks.clear()
                prevOutputItemStack = null
            }

        request<RenderWorldLastEvent>().subscribe {
            firstTask { renderWorld(partialTicks) }
        }

        request<GuiScreenEvent.BackgroundDrawnEvent>().filter { gui is GuiContainer }.subscribe {
            firstTask { renderInGui() }
        }

        request<SpecialOverlayEvent>().subscribe {
            firstTask { renderOverlay(partialTicks) }
        }
    }

    data class WorkshopRoom(
        val outputItemFrame: EntityItemFrame,
        val blockPosList: List<BlockPos>,
    )

    enum class WorkshopBlockType {
        BREAKABLE, FURNACE, CRAFTING_TABLE
    }

    data class RecipeItem(val pos: Pos, val item: Item) {
        val recipesOfItem = arrayListOf<RecipeItem>()

        fun getFinalChildRecipeItems(): List<RecipeItem> =
            if (recipesOfItem.isEmpty()) arrayListOf(this) else recipesOfItem.flatMap { it.getFinalChildRecipeItems() }
    }

}

sealed class WorkshopTask {
    abstract fun isCompleted(): Boolean

    open fun renderWorld(partialTicks: Float) {}

    open fun renderOverlay(partialTicks: Float) {}

    open fun renderInGui() {}
}

data class BreakTask(
    val recipeItems: List<WorkshopProcessor.RecipeItem>,
    val breakAbleBlocks: MutableList<Pair<BlockPos, IBlockState>>,
) : WorkshopTask() {

    private fun Item.getCorrespondingBlock() = when (this) {
        Items.gold_ingot -> breakAbleBlocks.find { it.second.block == Blocks.gold_ore }
        Items.iron_ingot -> breakAbleBlocks.find { it.second.block == Blocks.iron_ore }
        else -> breakAbleBlocks.find { it.second.block.getItemDropped(it.second, Random(), 1) == this }
    }

    private val targets = recipeItems.mapNotNull {
        val pair = it.item.getCorrespondingBlock()

        breakAbleBlocks.remove(pair)

        pair?.first?.getAxisAlignedBB()
    }

    override fun renderWorld(partialTicks: Float) {
        targets.forEach {
            RenderUtils.drawBox(it, 0x40FF0000, partialTicks)
        }
    }

    override fun isCompleted(): Boolean {
        val itemsInInventory = mc.thePlayer.inventory.mainInventory
            .filterNotNull()
            .flatMap { List(it.stackSize) { _ -> it.item } }
            .toMutableList()

        for (item in recipeItems.filterNot { it.item == Item.getItemFromBlock(Blocks.planks) }.map {
            when (it.item) {
                Items.iron_ingot -> Item.getItemFromBlock(Blocks.iron_ore)
                Items.gold_ingot -> Item.getItemFromBlock(Blocks.gold_ore)
                else -> it.item
            }
        }) {
            if (item !in itemsInInventory) return false
            itemsInInventory.remove(item)
        }

        return true
    }
}

data class BlockClickTask(val target: BlockPos, val targetGui: KClass<out GuiScreen>) : WorkshopTask() {

    override fun renderWorld(partialTicks: Float) {
        RenderUtils.drawBox(target.getAxisAlignedBB(), 0x40FF0000, partialTicks)
    }

    override fun renderOverlay(partialTicks: Float) {
        RenderUtils.drawDirectionArrow(target.toVec3(), Color.red.rgb)
    }

    override fun isCompleted(): Boolean {
        return targetGui.isInstance(mc.currentScreen)
    }
}

data class WindowClickTask(
    val allClickItems: List<Pair<Int, CustomItemStack>>,
    val needItem: CustomItemStack,
    val needGui: KClass<out GuiScreen>,
) :
    WorkshopTask() {


    override fun renderInGui() {
        val gui = mc.currentScreen
        if (!needGui.isInstance(gui)) return

        if (gui is GuiContainer && gui is AccessorGuiContainer) {

            for ((slotId, customItemStack) in allClickItems) {
                val slot = gui.inventorySlots.getSlot(slotId)

                gui.drawOnSlot(slot, 0x4000FF00)
                mc.renderItem.renderItemIntoGUI(
                    customItemStack.itemStack,
                    gui.guiLeft + slot.xDisplayPosition,
                    gui.guiTop + slot.yDisplayPosition
                )
                mc.renderItem.renderItemOverlayIntoGUI(
                    mc.fontRendererObj,
                    customItemStack.itemStack,
                    gui.guiLeft + slot.xDisplayPosition,
                    gui.guiTop + slot.yDisplayPosition,
                    null
                )
            }
        }
    }

    override fun isCompleted(): Boolean {
        val gui = mc.currentScreen
        if (gui !is GuiContainer || !needGui.isInstance(gui)) return false

        //    return allClickItems.all { (slotId, itemStack) ->
        //      gui.inventorySlots.getSlot(slotId).stack?.let { it.item == itemStack.item && it.stackSize >= itemStack.stackSize } == true
        //   }
        return mc.thePlayer.inventory.mainInventory.mapNotNull {
            it?.let { itemStack ->
                CustomItemStack(
                    itemStack.item,
                    it.stackSize
                )
            }
        }.any { it.item == needItem.item && it.stackSize >= needItem.stackSize }
    }
}

data class CustomItemStack(val item: Item, val stackSize: Int = 1) {
    val itemStack = ItemStack(item, stackSize)
}