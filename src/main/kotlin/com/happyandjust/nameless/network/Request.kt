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

package com.happyandjust.nameless.network

import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

object Request {

    fun get(url: String, headers: Map<String, String> = mapOf()): String {
        val connection = connect(url)
        try {
            connection.requestMethod = "GET"
            headers.forEach(connection::setRequestProperty)
            return readBody(connection.inputStream)
        } catch (e: Exception) {
            throw RuntimeException(e)
        } finally {
            connection.disconnect()
        }
    }

    fun post(url: String, requestHeaders: Map<String, String>, params: Map<String, String>): String {
        val connection = connect(url)
        try {
            connection.requestMethod = "POST"
            requestHeaders.forEach(connection::setRequestProperty)

            val paramString = params.entries.joinToString("") { "&${it.key}=${it.value}" }

            connection.doOutput = true

            DataOutputStream(connection.outputStream).use {
                it.write(paramString.substring(1).toByteArray())
                it.flush()
            }

            return readBody(connection.inputStream)

        } catch (e: IOException) {
            throw RuntimeException(e)
        } finally {
            connection.disconnect()
        }
    }

    private fun connect(url: String): HttpURLConnection = try {
        URL(url).openConnection() as HttpURLConnection
    } catch (e: MalformedURLException) {
        throw RuntimeException(e)
    } catch (e: IOException) {
        throw RuntimeException(e)
    }

    private fun readBody(body: InputStream): String {
        return try {
            body.bufferedReader().readLines().joinToString("\n")
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

}
