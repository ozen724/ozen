package ir.ozen.smsbridge;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SmsReceiver extends BroadcastReceiver {
    private static final ExecutorService EXEC=Executors.newSingleThreadExecutor();
    @Override public void onReceive(Context context, Intent intent){ if(!Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction()))return; SmsMessage[] parts=Telephony.Sms.Intents.getMessagesFromIntent(intent);if(parts==null||parts.length==0)return;String sender=parts[0].getDisplayOriginatingAddress();if(sender==null)sender="";StringBuilder body=new StringBuilder();long ts=parts[0].getTimestampMillis();for(SmsMessage m:parts)if(m!=null&&m.getMessageBody()!=null)body.append(m.getMessageBody());String text=body.toString();if(!looksLikeCredit(text)||!senderAllowed(context,sender))return;PendingResult pending=goAsync();String finalSender=sender;EXEC.execute(()->{try{String resp=ApiClient.sendSms(context,finalSender,text,ts);AppConfig.prefs(context).edit().putString("last_result",resp).putLong("last_sync",System.currentTimeMillis()).apply();}catch(Exception e){AppConfig.prefs(context).edit().putString("last_result","خطا: "+e.getMessage()).putLong("last_sync",System.currentTimeMillis()).apply();}finally{pending.finish();}}); }
    private static boolean looksLikeCredit(String s){ if(s==null)return false;String x=normalize(s);boolean credit=x.contains("واریز")||x.contains("واريز")||x.contains("بستانکار")||x.contains("بستانكار")||x.contains("افزایش")||x.contains("افزايش")||x.contains("دریافت")||x.contains("دريافت")||x.contains("وصول");boolean money=x.contains("ریال")||x.contains("تومان")||x.contains("تومن");return credit&&money; }
    private static boolean senderAllowed(Context c,String sender){String filters=AppConfig.senders(c);if(filters.isEmpty())return true;String s=sender.toLowerCase(Locale.ROOT);for(String f:filters.split(",")){f=f.trim().toLowerCase(Locale.ROOT);if(!f.isEmpty()&&s.contains(f))return true;}return false;}
    private static String normalize(String s){return s.replace('۰','0').replace('۱','1').replace('۲','2').replace('۳','3').replace('۴','4').replace('۵','5').replace('۶','6').replace('۷','7').replace('۸','8').replace('۹','9');}
}
