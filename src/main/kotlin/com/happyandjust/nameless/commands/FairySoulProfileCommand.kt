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

package com.happyandjust.nameless.commands

import com.happyandjust.nameless.core.ClientCommandBase
import com.happyandjust.nameless.devqol.sendClientMessage
import com.happyandjust.nameless.hypixel.fairysoul.FairySoulProfileCache
import net.minecraft.command.ICommandSender
import net.minecraft.util.BlockPos

class FairySoulProfileCommand : ClientCommandBase("fairysoulprofile") {

    override fun processCommand(sender: ICommandSender, args: Array<out String>) {
        when (args.size) {
            0 -> {
                sendClientMessage(
                    """
                    If you want exclude already found FairySouls from Waypoint, Please read this help-text
                    Excluding Found FairySouls is done by generating profile and store found fairysouls data in that profile
                    So to generate new profile, type /fairysoulprofile generate [profile name]
                    And then all fairysouls you found after generating profile, it'll store data in that profile
                    Then what if you change your mc account or skyblock profile?
                    Then simply create a new profile by typeing /fairysoulprofile generate [profile name]
                    And then if you change your skyblock profile back to original one, type /fairysoulprofile load [profile name]
                    To see all generated profiles, type /fairysoulprofile list
                """.trimIndent()
                )
            }
            1 -> {
                if (args[0] == "list") {
                    for (profile in FairySoulProfileCache.getProfiles()) {
                        var foundFairySouls = 0

                        for ((_, fairySouls) in profile.foundFairySouls) {
                            foundFairySouls += fairySouls.size
                        }

                        sendClientMessage("§eName: ${profile.name} Found FairySouls: $foundFairySouls")
                    }
                }
            }
            2 -> {
                when (args[0]) {
                    "generate" -> {
                        val name = args[1]
                        sendClientMessage(
                            try {
                                FairySoulProfileCache.createProfile(name)
                                "§aSuccessfully Generated Profile $name"
                            } catch (e: RuntimeException) {
                                "§c${e.message}"
                            }
                        )
                    }
                    "load" -> {
                        val name = args[1]

                        sendClientMessage(
                            try {
                                FairySoulProfileCache.currentlyLoadedProfile =
                                    FairySoulProfileCache.getProfileByName(name)
                                "§aSuccessfully Loaded Profile $name"
                            } catch (e: RuntimeException) {
                                "§c${e.message}"
                            }
                        )
                    }
                }
            }
        }
    }

    override fun addTabCompletionOptions(
        sender: ICommandSender,
        args: Array<out String>,
        pos: BlockPos
    ): MutableList<String> {
        if (args.size == 2 && args[0] == "load") {
            val list = arrayListOf<String>().also {
                for (profile in FairySoulProfileCache.getProfiles()) {
                    it.add(profile.name.takeIf { s -> s.startsWith(args[0], true) } ?: continue)
                }
            }

            return list
        }

        return mutableListOf()
    }
}