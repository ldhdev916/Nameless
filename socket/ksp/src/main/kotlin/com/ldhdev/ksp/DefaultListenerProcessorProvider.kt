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
import com.google.devtools.ksp.KSTypeNotPresentException
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class DefaultListener(val listener: KClass<*>)

@AutoService(SymbolProcessorProvider::class)
class DefaultListenerProcessorProvider : SymbolProcessorProvider {
    @OptIn(KspExperimental::class)
    override fun create(environment: SymbolProcessorEnvironment) = with(environment) {
        object : SymbolProcessor {

            private val defaultListeners = mutableListOf<KSClassDeclaration>()

            override fun process(resolver: Resolver): List<KSAnnotated> {

                resolver.getSymbolsWithAnnotation(DefaultListener::class.java.canonicalName)
                    .filterIsInstance<KSClassDeclaration>().filter { it.validate() }.forEach {
                        if (it.classKind != ClassKind.OBJECT) {
                            logger.error("Not an OBJECT", it)
                        }


                        defaultListeners.add(it)
                    }

                return emptyList()
            }

            override fun finish() {
                val packageName = "com.ldhdev.socket"
                codeGenerator.createNewFile(
                    Dependencies(
                        true,
                        *defaultListeners.map { it.containingFile!! }.toTypedArray()
                    ),
                    packageName,
                    "defaultListeners"
                ).buffered().use {

                    val registerListener: (KSClassDeclaration) -> String = { declaration ->
                        val defaultListener = declaration.getAnnotationsByType(DefaultListener::class).single()
                        val listenerName = try {
                            defaultListener.listener.qualifiedName
                        } catch (e: KSTypeNotPresentException) {
                            e.ksType.declaration.qualifiedName?.asString()
                        }
                        "setListener<${listenerName}>{${declaration.qualifiedName?.asString()}}"
                    }

                    it.write(
                        """
                            package $packageName
        
                            fun StompClient.registerDefaultListeners() {
                                ${defaultListeners.joinToString("\n", transform = registerListener)}
                            }
                        """.trimIndent().toByteArray()
                    )
                }
            }
        }
    }
}