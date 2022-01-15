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
import com.happyandjust.nameless.core.TickTimer
import com.happyandjust.nameless.core.VOIDGLOOM_SKULL
import com.happyandjust.nameless.core.value.Overlay
import com.happyandjust.nameless.core.value.toChromaColor
import com.happyandjust.nameless.dsl.*
import com.happyandjust.nameless.events.HypixelServerChangeEvent
import com.happyandjust.nameless.events.SpecialTickEvent
import com.happyandjust.nameless.features.Category
import com.happyandjust.nameless.features.OverlayFeature
import com.happyandjust.nameless.features.SubParameterOf
import com.happyandjust.nameless.features.base.FeatureParameter
import com.happyandjust.nameless.gui.feature.components.Identifier
import com.happyandjust.nameless.gui.feature.components.VerticalPositionEditableComponent
import com.happyandjust.nameless.gui.fixed
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
import gg.essential.elementa.dsl.basicTextScaleConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.utils.withAlpha
import net.minecraft.block.BlockBeacon
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.monster.EntityEnderman
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.RenderWorldLastEvent
import java.awt.Color
import kotlin.math.pow

object EndermanSlayerHelper :
    OverlayFeature(Category.SKYBLOCK, "endermanslayerhelper", "Enderman Slayer Helper", "Display Voidgloom Info") {

    private val scanTimer = TickTimer.withSecond(0.5)
    override var overlayPoint by ConfigValue("endermanslayer", "overlay", Overlay.DEFAULT, COverlay)
    private var currentVoidgloomCache: VoidgloomCache? = null
    private val findArmorStand: (EntityEnderman) -> EntityArmorStand? = {
        val aabb = it.entityBoundingBox
        val axisAlignedBB =
            AxisAlignedBB(aabb.minX, aabb.maxY, aabb.minZ, aabb.maxX, aabb.maxY + 1, aabb.maxZ)

        it.worldObj.getEntitiesWithinAABB(EntityArmorStand::class.java, axisAlignedBB)
            .sortedBy { entityArmorStand ->
                (it.posX - entityArmorStand.posX).pow(2) + (it.posZ - entityArmorStand.posZ).pow(2)
            }
            .find { entityArmorStand -> "Voidgloom Seraph" in entityArmorStand.displayName.unformattedText }
    }

    private var highlightBeacon by FeatureParameter(
        0,
        "endermanslayer",
        "highlightbeacon",
        "Highlight Beacon",
        "",
        true,
        CBoolean
    )

    @SubParameterOf("highlightBeacon")
    private var beaconColor by FeatureParameter(
        0,
        "endermanslayer",
        "color",
        "Highlight Color",
        "",
        Color.red.withAlpha(0.5f).toChromaColor(),
        CChromaColor
    )

    private var directionArrow by FeatureParameter(
        0,
        "endermanslayer",
        "directionarrow",
        "Render Direction Arrow on Screen",
        "Render arrow pointing to beacon",
        false,
        CBoolean
    )

    private var notifyBeacon by FeatureParameter(
        0,
        "endermanslayer",
        "beaconnotify",
        "Notify Beacon",
        "Display title and play sound when beacon is placed",
        false,
        CBoolean
    )

    private var highlightSkull by FeatureParameter(
        1,
        "endermanslayer",
        "highlightskull",
        "Highligh Skulls",
        "",
        true,
        CBoolean
    )

    private var skullColor by FeatureParameter(
        0,
        "endermanslayer",
        "skullcolor",
        "Highlight Color",
        "",
        Color.red.withAlpha(0.5f).toChromaColor(),
        CChromaColor
    )

    private var order by FeatureParameter(
        2,
        "endermanslayer",
        "order",
        "Information List",
        "",
        VoidgloomInformation.values().map { VoidgloomIdentifier(it) },
        CList(VoidgloomIdentifier::serialize, VoidgloomIdentifier::deserialize)
    ).apply {
        allIdentifiers = VoidgloomInformation.values().map { VoidgloomIdentifier(it) }
    }

    override fun getRelocateComponent(relocateComponent: RelocateComponent): UIComponent {
        val container = UIContainer().constrain {
            width = ChildBasedMaxSizeConstraint()
            height = ChildBasedSizeConstraint()
        }

        for (text in VoidgloomInformation.values().map { it.dummyText }) {
            UIText(text).constrain {

                y = SiblingConstraint()

                textScale = basicTextScaleConstraint { relocateComponent.currentScale.toFloat() }.fixed()
            } childOf container
        }

        return container
    }

    override fun shouldDisplayInRelocateGui(): Boolean {
        return checkForRequirement()
    }

    override fun renderOverlay0(partialTicks: Float) {
        if (checkForRequirement()) {
            currentVoidgloomCache?.let {
                matrix {
                    setup(overlayPoint)
                    var y = 0
                    for (identifier in order) {
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

                if (directionArrow) {
                    val pos = getBeaconPos()?.toVec3() ?: return

                    RenderUtils.drawDirectionArrow(pos, 0xFFFF0000.toInt())
                }
            }

        }
    }

    init {
        on<SpecialTickEvent>().filter { checkForRequirement() && scanTimer.update().check() }.subscribe {
            currentVoidgloomCache =
                if (ScoreboardUtils.getSidebarLines(true).any { "Slay the boss!" in it }) {
                    val pair = mc.theWorld.loadedEntityList
                        .asSequence()
                        .filterIsInstance<EntityEnderman>()
                        .filter { it.getDistanceToEntity(mc.thePlayer) <= 10 }
                        .sortedBy { it.getDistanceToEntity(mc.thePlayer) }
                        .mapNotNull {
                            val armorStand = findArmorStand(it)
                            if (armorStand != null) it to armorStand else null
                        }
                        .firstOrNull()

                    if (pair != null) {
                        VoidgloomCache(
                            pair.first,
                            calculate(pair.first, pair.second)
                        )
                    } else {
                        currentVoidgloomCache?.let {
                            VoidgloomCache(
                                it.enderman,
                                calculate(
                                    it.enderman,
                                    it.stat[VoidgloomInformation.NAME]!! as EntityArmorStand
                                )
                            )
                        }
                    }
                } else {
                    null
                }
        }
    }


    /**
     * [VoidgloomInformation.NAME] to [EntityArmorStand]
     *
     * [VoidgloomInformation.HIT_PHASE] to [Boolean]
     *
     * [VoidgloomInformation.HOLDING_BEACON] to [Boolean]
     *
     * [VoidgloomInformation.BEACON_PLACED] to [BeaconInfo]
     *
     * [VoidgloomInformation.SKULLS_NEARBY] to [List]
     */
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
                mc.theWorld.loadedEntityList.filterIsInstance<EntityArmorStand>()
                    .filter {
                        it.getDistanceToEntity(enderman) <= 12 &&
                                it.getEquipmentInSlot(4)?.getSkullOwner()?.getMD5() == VOIDGLOOM_SKULL
                    }
            }
        )
    }

    private fun checkForRequirement() =
        enabled && Hypixel.currentGame == GameType.SKYBLOCK && Hypixel.locrawInfo?.mode == "combat_3"

    init {
        on<RenderWorldLastEvent>().filter { checkForRequirement() }.subscribe {
            if (highlightBeacon) {
                run {
                    val aabb = getBeaconPos()?.getAxisAlignedBB() ?: return@run

                    RenderUtils.drawBox(
                        aabb,
                        beaconColor.rgb,
                        partialTicks
                    )
                }
            }

            if (highlightSkull) {
                for (aabb in getSkullPos()) {
                    RenderUtils.drawBox(aabb, skullColor.rgb, partialTicks)
                }
            }
        }

        on<HypixelServerChangeEvent>().subscribe {
            BeaconInfo.clear()
            currentVoidgloomCache = null
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
                    if (notifyBeacon) {
                        mc.thePlayer.playSound("random.successful_hit", 1F, 0.5F)
                        with(mc.ingameGUI) {
                            displayTitle(null, null, 0, 20, 0)
                            displayTitle("§cBeacon Placed!", null, 0, 0, 0)
                        }
                    }
                    createdBeaconInstances[pos] ?: BeaconInfo(pos, placeTime, true)
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
            return voidgloomInformationConverter.serialize(information)
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

            private val voidgloomInformationConverter = getEnumConverter<VoidgloomInformation>()

            fun deserialize(jsonElement: JsonElement) =
                VoidgloomIdentifier(voidgloomInformationConverter.deserialize(jsonElement))
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