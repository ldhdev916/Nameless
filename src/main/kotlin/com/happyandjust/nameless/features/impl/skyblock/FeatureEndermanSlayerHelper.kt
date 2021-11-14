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

package com.happyandjust.nameless.features.impl.skyblock

import com.google.gson.JsonElement
import com.happyandjust.nameless.config.ConfigValue
import com.happyandjust.nameless.core.Overlay
import com.happyandjust.nameless.core.toChromaColor
import com.happyandjust.nameless.devqol.*
import com.happyandjust.nameless.features.Category
import com.happyandjust.nameless.features.FeatureParameter
import com.happyandjust.nameless.features.IRelocateAble
import com.happyandjust.nameless.features.SimpleFeature
import com.happyandjust.nameless.features.listener.ClientTickListener
import com.happyandjust.nameless.features.listener.ServerChangeListener
import com.happyandjust.nameless.features.listener.WorldRenderListener
import com.happyandjust.nameless.gui.feature.components.Identifier
import com.happyandjust.nameless.gui.feature.components.VerticalPositionEditableComponent
import com.happyandjust.nameless.gui.relocate.RelocateComponent
import com.happyandjust.nameless.hypixel.GameType
import com.happyandjust.nameless.hypixel.Hypixel
import com.happyandjust.nameless.serialization.converters.*
import com.happyandjust.nameless.utils.RenderUtils
import com.happyandjust.nameless.utils.ScoreboardUtils
import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.ChildBasedMaxSizeConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.pixels
import net.minecraft.block.BlockBeacon
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.monster.EntityEnderman
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import java.awt.Color
import kotlin.math.pow

object FeatureEndermanSlayerHelper :
    SimpleFeature(Category.SKYBLOCK, "endermanslayerhelper", "Enderman Slayer Helper", "Display Voidgloom Info"),
    ClientTickListener, IRelocateAble, WorldRenderListener, ServerChangeListener {

    private var scanTick = 0
    override val overlayPoint = ConfigValue("endermanslayer", "overlay", Overlay.DEFAULT, COverlay)
    var currentVoidgloomCache: VoidgloomCache? = null
    private val findArmorStand: (EntityEnderman) -> EntityArmorStand? = {
        val aabb = it.entityBoundingBox
        val axisAlignedBB =
            AxisAlignedBB(aabb.minX, aabb.maxY, aabb.minZ, aabb.maxX, aabb.maxY + 1, aabb.maxZ)

        it.worldObj.getEntitiesWithinAABB(EntityArmorStand::class.java, axisAlignedBB)
            .sortedBy { entityArmorStand ->
                (it.posX - entityArmorStand.posX).pow(2) + (it.posZ - entityArmorStand.posZ).pow(2)
            }
            .firstOrNull { entityArmorStand -> entityArmorStand.displayName.unformattedText.contains("Voidgloom Seraph") }
    }

    init {

        parameters["beacon"] = FeatureParameter(
            0,
            "endermanslayer",
            "highlightbeacon",
            "Highlight Beacon",
            "",
            true,
            CBoolean
        ).also {
            it.parameters["color"] = FeatureParameter(
                0,
                "endermanslayer",
                "color",
                "Highlight Color",
                "",
                Color.red.toChromaColor(),
                CChromaColor
            )
        }

        parameters["arrow"] = FeatureParameter(
            0,
            "endermanslayer",
            "directionarrow",
            "Render Direction Arrow on Screen",
            "Render arrow pointing to beacon",
            false,
            CBoolean
        )

        parameters["notify"] = FeatureParameter(
            0,
            "endermanslayer",
            "beaconnotify",
            "Notify Beacon",
            "Display title and play sound when beacon is placed",
            false,
            CBoolean
        )

        parameters["skull"] = FeatureParameter(
            1,
            "endermanslayer",
            "highlightskull",
            "Highligh Skulls",
            "",
            true,
            CBoolean
        ).also {
            it.parameters["color"] = FeatureParameter(
                0,
                "endermanslayer",
                "skullcolor",
                "Highlight Color",
                "",
                Color.red.toChromaColor(),
                CChromaColor
            )
        }

        val informations = VoidgloomInformation.values().map { VoidgloomIdentifier(it) }

        parameters["order"] = FeatureParameter(
            2,
            "endermanslayer",
            "order",
            "Information List",
            "",
            informations,
            CIdentifierList { VoidgloomIdentifier.deserialize(it) }
        ).also {
            it.allIdentifiers = informations
        }
    }

    override fun getRelocateComponent(relocateComponent: RelocateComponent): UIComponent {
        val container = UIContainer().constrain {
            width = ChildBasedMaxSizeConstraint()
            height = ChildBasedSizeConstraint()
        }

        for (text in VoidgloomInformation.values().map { it.dummyText }) {
            UIText(text).constrain {

                y = SiblingConstraint()

                textScale = relocateComponent.currentScale.pixels()

                relocateComponent.onScaleChange {
                    textScale = it.pixels()
                }
            } childOf container
        }

        return container
    }

    override fun renderOverlay(partialTicks: Float) {
        if (checkForRequirement()) {
            currentVoidgloomCache?.let {
                matrix {
                    setup(overlayPoint.value)
                    val identifiers = getParameterValue<List<Identifier>>("order").map { it as VoidgloomIdentifier }

                    var y = 0
                    for (identifier in identifiers) {
                        val information = identifier.information

                        mc.fontRendererObj.drawStringWithShadow(
                            information.getTextForRender(it.stat[information] ?: continue),
                            0f,
                            y.toFloat(),
                            Color.white.rgb
                        )
                        y += mc.fontRendererObj.FONT_HEIGHT
                    }
                }

                if (getParameterValue("arrow")) {
                    val pos = getBeaconPos()?.toVec3() ?: return

                    RenderUtils.drawDirectionArrow(pos, 0xFFFF0000.toInt())
                }
            }

        }
    }

    override fun tick() {
        if (!checkForRequirement()) return
        scanTick = (scanTick + 1) % 10
        if (scanTick != 0) return

        currentVoidgloomCache =
            if (ScoreboardUtils.getSidebarLines(true).any { it.contains("Slay the boss!") }) {
                val cache = hashMapOf<EntityEnderman, EntityArmorStand?>()
                val enderman = mc.theWorld.loadedEntityList.filterIsInstance<EntityEnderman>()
                    .filter { it.getDistanceToEntity(mc.thePlayer) <= 10 }
                    .sortedBy { it.getDistanceToEntity(mc.thePlayer) }
                    .firstOrNull {
                        cache[it] = findArmorStand(it)
                        cache[it] != null
                    }

                if (enderman != null) {
                    VoidgloomCache(
                        enderman,
                        calculate(enderman, cache[enderman]!!)
                    )
                } else {
                    currentVoidgloomCache?.let { voidGloomCache ->
                        VoidgloomCache(
                            voidGloomCache.enderman,
                            calculate(
                                voidGloomCache.enderman,
                                voidGloomCache.stat[VoidgloomInformation.NAME]!! as EntityArmorStand
                            )
                        )
                    }
                }
            } else {
                null
            }
    }

    private fun calculate(
        enderman: EntityEnderman,
        armorStand: EntityArmorStand
    ): HashMap<VoidgloomInformation, Any> {
        return hashMapOf(
            VoidgloomInformation.NAME to armorStand,
            VoidgloomInformation.HIT_PHASE to armorStand.displayName.unformattedText.contains("Hits"),
            VoidgloomInformation.HOLDING_BEACON to (enderman.heldBlockState?.block is BlockBeacon),
            VoidgloomInformation.BEACON_PLACED to run {
                val from = BlockPos(enderman.posX - 15, enderman.posY - 3, enderman.posZ - 15)
                val to = BlockPos(enderman.posX + 15, enderman.posY + 3, enderman.posZ + 15)
                val blocks = BlockPos.getAllInBox(from, to)
                    .map {
                        BeaconInfo.createInstance(
                            it,
                            System.currentTimeMillis(),
                            mc.theWorld.getBlockAtPos(it) is BlockBeacon
                        )
                    }
                    .sortedWith(compareBy({ !it.isBeacon }, { enderman.getDistanceSq(it.pos) }))
                blocks[0]
            },
            VoidgloomInformation.SKULLS_NEARBY to run {
                val list = mc.theWorld.loadedEntityList.filterIsInstance<EntityArmorStand>()
                    .filter { it.getDistanceToEntity(enderman) <= 12 }
                    .filter {
                        it.getEquipmentInSlot(4)?.getSkullOwner()?.getMD5() == "159dcb0174e3282cc7d63afa022fb379"
                    }

                list
            }
        )
    }

    private fun checkForRequirement() =
        enabled && Hypixel.currentGame == GameType.SKYBLOCK && Hypixel.locrawInfo?.mode == "combat_3"


    override fun renderWorld(partialTicks: Float) {

        if (!checkForRequirement()) return
        run {
            val parameter = getParameter<Boolean>("beacon")
            if (!parameter.value) return@run
            val aabb = getBeaconPos()?.getAxisAlignedBB() ?: return@run

            RenderUtils.drawBox(
                aabb,
                parameter.getParameterValue<Color>("color").rgb and 0x80FFFFFF.toInt(),
                partialTicks
            )
        }

        run {
            val parameter = getParameter<Boolean>("skull")
            if (!parameter.value) return@run
            val color = parameter.getParameterValue<Color>("color").rgb and 0x80FFFFFF.toInt()

            for (aabb in getSkullPos()) {
                RenderUtils.drawBox(aabb, color, partialTicks)
            }
        }
    }

    private fun EntityArmorStand.getSkullAxisAlignedBB(): AxisAlignedBB {
        val width = 0.4

        val top = posY + eyeHeight + 1.6
        return AxisAlignedBB(
            posX - width,
            top - 0.8,
            posZ - width,
            posX + width,
            top,
            posZ + width
        )
    }

    override fun onServerChange(server: String) {
        BeaconInfo.clear()
        currentVoidgloomCache = null
    }

    private fun getBeaconPos() =
        (currentVoidgloomCache?.stat?.get(VoidgloomInformation.BEACON_PLACED) as? BeaconInfo)?.takeIf { it.isBeacon }?.pos

    private fun getSkullPos() =
        (currentVoidgloomCache?.stat?.get(VoidgloomInformation.SKULLS_NEARBY) as? List<EntityArmorStand>)
            ?.map { it.getSkullAxisAlignedBB() } ?: emptyList()

    data class VoidgloomCache(val enderman: EntityEnderman, val stat: HashMap<VoidgloomInformation, Any>)

    data class BeaconInfo(val pos: BlockPos, val placeTime: Long, val isBeacon: Boolean) {

        init {
            if (isBeacon) {
                createdBeaconInstances[pos] = this
            }
        }

        companion object {
            private val createdBeaconInstances = hashMapOf<BlockPos, BeaconInfo>()

            fun createInstance(pos: BlockPos, placeTime: Long, isBeacon: Boolean): BeaconInfo {
                return if (isBeacon) {
                    createdBeaconInstances[pos]?.takeIf { it.isBeacon } ?: BeaconInfo(pos, placeTime, true).also {
                        if (getParameterValue("notify")) {
                            mc.thePlayer.playSound("random.successful_hit", 1F, 0.5F)
                            with(mc.ingameGUI) {
                                displayTitle(null, null, 0, 20, 0)
                                displayTitle("§cBeacon Placed!", null, 0, 0, 0)
                            }
                        }
                    }
                } else {
                    createdBeaconInstances.remove(pos)
                    BeaconInfo(pos, placeTime, false)
                }
            }

            fun clear() = createdBeaconInstances.clear()
        }
    }

    class VoidgloomIdentifier(val information: VoidgloomInformation) : Identifier {
        override fun toUIComponent(gui: VerticalPositionEditableComponent): UIComponent {
            return UIText(information.prettyName).constrain {
                textScale = 2.pixels()
            }
        }

        override fun serialize(): JsonElement {
            return CVoidgloomInformation.serialize(information)
        }

        override fun areEqual(other: Identifier) = this == other

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as VoidgloomIdentifier

            if (information != other.information) return false

            return true
        }

        override fun hashCode(): Int {
            return information.hashCode()
        }

        companion object {
            fun deserialize(jsonElement: JsonElement): Identifier {
                return VoidgloomIdentifier(CVoidgloomInformation.deserialize(jsonElement))
            }
        }

    }

    enum class VoidgloomInformation(val prettyName: String, val dummyText: String) {
        NAME("Name", "§c☠ §bVoidgloom Seraph §f§l15 Hits") {
            override fun getTextForRender(value: Any): String =
                (value as EntityArmorStand).displayName.unformattedText
        },
        HIT_PHASE("Hit Phase", "§4Hit Phase: §cNO") {
            override fun getTextForRender(value: Any): String {
                return "§4Hit Phase: ${getText(value)}"
            }
        },
        HOLDING_BEACON("Holding Beacon", "§aHolding Beacon: §cNO") {
            override fun getTextForRender(value: Any): String {
                return "§aHolding Beacon: ${getText(value)}"
            }
        },
        BEACON_PLACED("Beacon Placed", "§aBeacon Placed: §cNO") {
            override fun getTextForRender(value: Any): String {
                val isBeacon = (value as BeaconInfo).isBeacon
                var basicText = "§aBeacon Placed: ${getText(isBeacon)}"

                if (isBeacon) {
                    val timePassed = (System.currentTimeMillis() - value.placeTime) / 1000.0

                    val timeLeft = (5 - timePassed).coerceAtLeast(0.0)

                    basicText += " §5${timeLeft.transformToPrecisionString(3)}s"
                }

                return basicText
            }
        },
        SKULLS_NEARBY("Skulls Nearby", "§eSkulls Nearby: §cNO") {
            override fun getTextForRender(value: Any): String {
                return "§eSkulls Nearby: ${getText((value as List<*>).isNotEmpty())}"
            }
        };

        abstract fun getTextForRender(value: Any): String

        companion object {
            fun getText(value: Any) = if (value as Boolean) "§6YES" else "§cNO"
        }
    }
}