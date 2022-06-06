/*
 * Nameless - 1.8.9 Hypixel Quality Of Life Mod
 * Copyright (C) 2022 HappyAndJust
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

package com.ldhdev.ksp

import com.google.auto.service.AutoService
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.validate

annotation class DefaultSubscription(val destination: String, val kind: Kind) {
    enum class Kind {
        USER, DEFAULT
    }
}

@AutoService(SymbolProcessorProvider::class)
class DefaultSubscriptionProcessorProvider : SymbolProcessorProvider {
    @OptIn(KspExperimental::class)
    override fun create(environment: SymbolProcessorEnvironment) = with(environment) {
        object : SymbolProcessor {

            private val defaultSubscriptions = mutableListOf<KSPropertyDeclaration>()

            override fun process(resolver: Resolver): List<KSAnnotated> {

                val typeName = resolver.getKSNameFromString("com.ldhdev.socket.subscription.StompMessageHandler")
                resolver.getSymbolsWithAnnotation(DefaultSubscription::class.java.canonicalName)
                    .filterIsInstance<KSPropertyDeclaration>().filter { it.validate() }.forEach {
                        if (it.type.resolve().declaration.qualifiedName != typeName) {
                            logger.error("Not a StompMessageHandler", it)
                        }
                        if (it.parentDeclaration != null) {
                            logger.error("Not a top-level property", it)
                        }
                        defaultSubscriptions.add(it)
                    }

                return emptyList()
            }

            override fun finish() {

                val packageName = "com.ldhdev.socket"

                codeGenerator.createNewFile(
                    Dependencies(true, *defaultSubscriptions.map { it.containingFile!! }.toTypedArray()),
                    packageName,
                    "defaultSubscriptionsRegistration"
                ).buffered().use {

                    val convert: (KSPropertyDeclaration) -> String = { declaration ->
                        val defaultSubscription = declaration.getAnnotationsByType(DefaultSubscription::class).single()
                        val param = """
                            "${defaultSubscription.destination}", ${declaration.qualifiedName?.asString()}
                        """.trimIndent()
                        val function = when (defaultSubscription.kind) {
                            DefaultSubscription.Kind.USER -> "user"
                            DefaultSubscription.Kind.DEFAULT -> "default"
                        }
                        "subscribe($function($param))"
                    }

                    it.write(
                        """
                            package $packageName
                            
                            import com.ldhdev.socket.subscription.StompSubscription
                            
                            fun StompClient.registerDefaultSubscriptions(): Unit = with(StompSubscription) {
                                ${defaultSubscriptions.joinToString("\n", transform = convert)}
                            }
                        """.trimIndent().toByteArray()
                    )
                }

                super.finish()
            }
        }
    }
}