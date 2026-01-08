package com.hjhrq1991.library

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.ArrayList

/**
 * data of bridge
 * @author haoqing
 *
 */
class Message {
    var callbackId: String? = null //callbackId
    var responseId: String? = null //responseId
    var responseData: String? = null //responseData
    var data: String? = null //data of message
    var handlerName: String? = null //name of handler
    var highPriority: Boolean = false // new field for priority
    var releaseSemaphore: Boolean = false //

    fun toJson(): String? {
        val jsonObject = JSONObject()
        return try {
            jsonObject.put(Companion.CALLBACK_ID_STR, callbackId)
            jsonObject.put(Companion.DATA_STR, data)
            jsonObject.put(Companion.HANDLER_NAME_STR, handlerName)
            jsonObject.put(Companion.RESPONSE_DATA_STR, responseData)
            jsonObject.put(Companion.RESPONSE_ID_STR, responseId)
            jsonObject.put(Companion.HIGH_PRIORITY_STR, highPriority)
            jsonObject.toString()
        } catch (e: JSONException) {
            e.printStackTrace()
            null
        }
    }

    companion object {
        private const val CALLBACK_ID_STR = "callbackId"
        private const val RESPONSE_ID_STR = "responseId"
        private const val RESPONSE_DATA_STR = "responseData"
        private const val DATA_STR = "data"
        private const val HANDLER_NAME_STR = "handlerName"
        private const val HIGH_PRIORITY_STR = "highPriority"

        fun toObject(jsonStr: String?): Message {
            val m = Message()
            return try {
                val jsonObject = JSONObject(jsonStr)
                m.handlerName = jsonObject.optString(HANDLER_NAME_STR)
                m.callbackId = jsonObject.optString(CALLBACK_ID_STR)
                m.responseData = jsonObject.optString(RESPONSE_DATA_STR)
                m.responseId = jsonObject.optString(RESPONSE_ID_STR)
                m.data = jsonObject.optString(DATA_STR)
                m.highPriority = jsonObject.optBoolean(HIGH_PRIORITY_STR)
                m
            } catch (e: JSONException) {
                e.printStackTrace()
                m
            }
        }

        fun toArrayList(jsonStr: String?): List<Message> {
            val list = ArrayList<Message>()

            // Return empty list for null or empty input
            if (jsonStr == null || jsonStr.trim().isEmpty() || "null".equals(jsonStr, ignoreCase = true)) {
                return list
            }

            return try {
                val jsonArray = JSONArray(jsonStr)
                for (i in 0 until jsonArray.length()) {
                    val m = Message()
                    val jsonObject = jsonArray.getJSONObject(i)
                    m.handlerName = jsonObject.optString(HANDLER_NAME_STR)
                    m.callbackId = jsonObject.optString(CALLBACK_ID_STR)
                    m.responseData = jsonObject.optString(RESPONSE_DATA_STR)
                    m.responseId = jsonObject.optString(RESPONSE_ID_STR)
                    m.data = jsonObject.optString(DATA_STR)
                    m.highPriority = jsonObject.optBoolean(HIGH_PRIORITY_STR)
                    list.add(m)
                }
                list
            } catch (e: JSONException) {
                e.printStackTrace()
                list
            }
        }
    }
}
