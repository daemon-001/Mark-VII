package com.daemon.markvii.data

import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * @author Nitesh
 */
object ChatData {


//    _____Faster Response lite computation_____
//    Latest: gemini-1.5-flash-8b-latest
//    Latest stable: gemini-1.5-flash-8b
//    Stable: gemini-1.5-flash-8b-001

//    ______Balanced Computation_____
//    Latest: gemini-1.5-flash-latest
//    Latest_stable: gemini-1.5-flash
//    Stable:
//    gemini-1.5-flash-001
//    gemini-1.5-flash-002
//    Experimental:
//    gemini-1.5-flash-8b-exp-0924
//    gemini-1.5-flash-8b-exp-0827
//    gemini-1.5-flash-exp-0827

//    _____Hard Computation____
//    Latest: gemini-1.5-pro-latest
//    Latest stable: gemini-1.5-pro
//    Stable:
//    gemini-1.5-pro-001
//    gemini-1.5-pro-002
//    Experimental:
//    gemini-1.5-pro-exp-0827
    val gemini_api_key = "YOUR API KEY"
//    val gpt_api_key = ""

    var gemini_api_model = ""
//    var gpt_api_model = ""


    suspend fun getResponse(prompt: String): Chat {
        val generativeModel = GenerativeModel(
            modelName = gemini_api_model, apiKey = gemini_api_key
        )

        try {
            val response = withContext(Dispatchers.IO) {
                generativeModel.generateContent(prompt)
            }

            return Chat(
                prompt = response.text ?: "error",
                bitmap = null,
                isFromUser = false
            )

        } catch (e: Exception) {
            return Chat(
                prompt = e.message ?: "error",
                bitmap = null,
                isFromUser = false
            )
        }

    }

    suspend fun getResponseWithImage(prompt: String, bitmap: Bitmap): Chat {
        val generativeModel = GenerativeModel(

            modelName = "gemini-1.5-flash", apiKey = gemini_api_key
        )


        try {

            val inputContent = content {
                image(bitmap)
                text(prompt)
            }

            val response = withContext(Dispatchers.IO) {
                generativeModel.generateContent(inputContent)
            }

            return Chat(
                prompt = response.text ?: "error",
                bitmap = null,
                isFromUser = false
            )

        } catch (e: Exception) {
            return Chat(
                prompt = e.message ?: "error",
                bitmap = null,
                isFromUser = false
            )
        }

    }

}




















