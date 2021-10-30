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

package com.happyandjust.nameless.features.impl.settings

import com.happyandjust.nameless.devqol.matchesMatcher
import com.happyandjust.nameless.devqol.mc
import com.happyandjust.nameless.devqol.sendClientMessage
import com.happyandjust.nameless.features.SettingFeature
import com.happyandjust.nameless.features.listener.ChatListener
import com.happyandjust.nameless.features.listener.ClientTickListener
import net.minecraftforge.client.event.ClientChatReceivedEvent
import java.util.*
import java.util.regex.Pattern

object FeatureReparty : SettingFeature("reparty", "Reparty", ""), ChatListener, ClientTickListener {

    val commandScheduler = LinkedList<String>()
    var doingReparty = false
    private var commandTick = 0
    private val DASH = "-----------------------------"
    var currentPartyPhase: Phase? = null

    private val prefix = "§6[§3Nameless§6]"
    private val RANK = "(\\[.+])?"
    private val NAME_CAPTURE = "(?<name>\\w+)".toRegex()
    private val PLAYER_JOIN_PARTY = Pattern.compile("$RANK $NAME_CAPTURE joined the party\\.")
    private val INVITE_PARTY =
        Pattern.compile("$RANK \\w+ invited $RANK $NAME_CAPTURE to the party! They have 60 seconds to accept.")
    private val PARTY_MEMBERS = Pattern.compile("Party Members \\((?<num>\\d)+\\)")
    private val DISBAND_PARTY = Pattern.compile("$RANK \\w+ has disbanded the party!")

    private var currentPartyInfo: PartyInfo? = null
    private var stopRepartyingAfterDash = false
    private var doPartyAfterDash = false

    override fun onChatReceived(e: ClientChatReceivedEvent) {
        if (!doingReparty) return
        if (e.type.toInt() == 2) return

        var msg = e.message.unformattedText

        if (msg == DASH) {

            if (stopRepartyingAfterDash) {
                stopRepartying()
                stopRepartyingAfterDash = false
            }

            if (doPartyAfterDash) {

                if (currentPartyInfo!!.partyMembers[mc.thePlayer.name]!!.status != Status.LEADER) {
                    sendClientMessage("$prefix §cYou're not party leader")

                    stopRepartyingAfterDash = true

                    return
                }

                currentPartyInfo!!.partyMembers.remove(mc.thePlayer.name)

                val memberTextList = currentPartyInfo!!.partyMembers.keys.joinToString(", ")

                sendClientMessage(
                    "$prefix §aPartying ${currentPartyInfo!!.membersExceptLeader} Members: ($memberTextList)"
                )

                currentPartyPhase = Phase.PARTYING

                commandScheduler.add("/p disband")

                for ((nickname, info) in currentPartyInfo!!.partyMembers) {

                    info.alreadyInvited = true
                    commandScheduler.add("/p $nickname")
                }
                doPartyAfterDash = false
            }

            e.isCanceled = true
            return
        }

        if (msg.isBlank()) {
            e.isCanceled = true
            return
        }

        when (currentPartyPhase) {
            Phase.CHECKING_MEMBERS -> {

                if (msg == "You are not currently in a party.") {
                    sendClientMessage("$prefix §cYou're not in party")

                    e.isCanceled = true

                    stopRepartyingAfterDash = true

                    return
                }

                PARTY_MEMBERS.matchesMatcher(msg) {

                    currentPartyInfo = PartyInfo(it.group("num").toInt() - 1, hashMapOf(), System.currentTimeMillis())

                    e.isCanceled = true

                    return
                }

                if (!msg.contains("●")) return

                msg = msg.replace("\\[.+]".toRegex(), "")

                val status: Status

                val members = when {
                    msg.startsWith("Party Leader: ") -> {

                        e.isCanceled = true

                        doPartyAfterDash = true

                        status = Status.LEADER
                        msg.split("Party Leader: ")[1]

                    }
                    msg.startsWith("Party Moderators: ") -> {

                        e.isCanceled = true

                        status = Status.MEMBER
                        msg.split("Party Moderators: ")[1]
                    }
                    msg.startsWith("Party Members: ") -> {

                        e.isCanceled = true

                        status = Status.MEMBER
                        msg.split("Party Members: ")[1]
                    }
                    else -> return
                }.split("●")

                for (member in members) {
                    if (member.isBlank()) continue
                    currentPartyInfo!!.partyMembers[member.replace(" ", "")] = MemberInfo(status)
                }
            }
            Phase.PARTYING -> {

                if (msg == "You cannot invite that player since they're not online.") {
                    e.isCanceled = true

                    return
                }

                DISBAND_PARTY.matchesMatcher(msg) {

                    e.isCanceled = true

                    return
                }

                INVITE_PARTY.matchesMatcher(msg) {
                    currentPartyInfo!!.partyMembers[it.group("name")]?.run {
                        successfullyInvited = true
                    }

                    e.isCanceled = true

                    if (getInvitedMemberNumber() == currentPartyInfo!!.membersExceptLeader) {
                        val successfullyInvitedMembers = arrayListOf<String>()
                        val failedInvitedMembers = arrayListOf<String>()

                        for ((nickname, info) in currentPartyInfo!!.partyMembers) {
                            if (info.successfullyInvited) {
                                successfullyInvitedMembers.add(nickname)
                            } else {
                                failedInvitedMembers.add(nickname)
                            }
                        }

                        if (successfullyInvitedMembers.isNotEmpty()) {
                            sendClientMessage(
                                "$prefix §aSuccessfully Invited Players: (${
                                    successfullyInvitedMembers.joinToString(
                                        " "
                                    )
                                })"
                            )
                        }
                        if (failedInvitedMembers.isNotEmpty()) {
                            sendClientMessage(
                                "$prefix §cFailed to invite these players: (${
                                    failedInvitedMembers.joinToString(
                                        " "
                                    )
                                })"
                            )
                        }
                    }

                    return
                }

                PLAYER_JOIN_PARTY.matchesMatcher(msg) {

                    currentPartyInfo!!.partyMembers[it.group("name")]?.joined = true

                    e.isCanceled = true

                    if (currentPartyInfo!!.membersExceptLeader == getJoinedMembers()) {
                        sendClientMessage("$prefix §aAll ${currentPartyInfo!!.membersExceptLeader} Players joined the Party.")

                        stopRepartyingAfterDash = true
                    }
                }
            }
        }

    }

    private fun getJoinedMembers(): Int {
        var a = 0

        for ((_, info) in currentPartyInfo!!.partyMembers) {
            if (info.joined) a++
        }

        return a
    }

    private fun stopRepartying() {
        currentPartyInfo = null
        currentPartyPhase = null
        doingReparty = false
    }

    private fun getInvitedMemberNumber(): Int {
        var a = 0

        for ((_, info) in currentPartyInfo!!.partyMembers) {
            if (info.alreadyInvited) a++
        }

        return a
    }

    override fun tick() {
        if (!doingReparty) return

        currentPartyInfo?.let {
            if (it.startTime + 10000L < System.currentTimeMillis()) {

                val unJoinedMembers = arrayListOf<String>()

                for ((nickname, info) in it.partyMembers) {
                    if (!info.joined) {
                        unJoinedMembers.add(nickname)
                    }
                }

                sendClientMessage(
                    "$prefix 10 Seconds passed still these players didn't join: (${
                        unJoinedMembers.joinToString(
                            " "
                        )
                    })"
                )

                stopRepartying()
            }
        }

        if (commandScheduler.isEmpty()) return

        commandTick = (commandTick + 1) % 13

        if (commandTick == 0) {
            mc.thePlayer.sendChatMessage(commandScheduler.poll())
        }
    }

    enum class Phase {
        CHECKING_MEMBERS, PARTYING
    }

    data class MemberInfo(val status: Status) {
        var successfullyInvited = false
        var alreadyInvited = false
        var joined = false
    }

    enum class Status {
        LEADER, MEMBER
    }

    data class PartyInfo(
        val membersExceptLeader: Int,
        val partyMembers: HashMap<String, MemberInfo>,
        val startTime: Long
    )
}