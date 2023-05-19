package ca.cegepgarneau.tp4_mobile

import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsMessage
import android.util.Log
import ca.cegepgarneau.tp4_mobile.model.Marker


class MyBroadCastReceiver : android.content.BroadcastReceiver() {

    private lateinit var marker: Marker

    override fun onReceive(context: Context, intent: Intent) {
        if (!intent?.action.equals(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)) return
        val extractMessages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        extractMessages.forEach { smsMessage -> Log.v("TAG", smsMessage.displayMessageBody) }



    }
}