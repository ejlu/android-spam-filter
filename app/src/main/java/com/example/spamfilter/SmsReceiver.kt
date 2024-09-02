package com.example.spamfilter

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.widget.Toast

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            for (sms in messages) {
                val sender = sms.originatingAddress
                val messageBody = sms.messageBody

                if (isSpam(context, messageBody)) {
                    // Block the message or perform any other action
                    Toast.makeText(context, "Spam message blocked from $sender", Toast.LENGTH_SHORT).show()
                    abortBroadcast()
                }
            }
        }
    }

    private fun isSpam(context: Context, messageBody: String): Boolean {
        val sharedPrefs = context.getSharedPreferences("SpamFilter", Context.MODE_PRIVATE)
        val keywordsSet = sharedPrefs.getStringSet("keywords", emptySet()) ?: emptySet()
        return keywordsSet.any { keyword -> messageBody.contains(keyword, ignoreCase = true) }
    }
}