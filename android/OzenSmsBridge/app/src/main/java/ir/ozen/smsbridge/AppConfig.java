package ir.ozen.smsbridge;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.UUID;

public final class AppConfig {
    public static final String DEFAULT_URL = "https://ozen.ir";
    public static final String DEFAULT_SECRET = "";
    private static final String PREF = "ozen_sms_bridge";
    private AppConfig() {}
    public static SharedPreferences prefs(Context c){ return c.getSharedPreferences(PREF, Context.MODE_PRIVATE); }
    public static String url(Context c){ return trimSlash(prefs(c).getString("url", DEFAULT_URL)); }
    public static String secret(Context c){ return prefs(c).getString("secret", DEFAULT_SECRET).trim(); }
    public static String senders(Context c){ return prefs(c).getString("senders", "").trim(); }
    public static String deviceId(Context c){ SharedPreferences p=prefs(c); String v=p.getString("device_id",""); if(v.isEmpty()){v=UUID.randomUUID().toString();p.edit().putString("device_id",v).apply();}return v; }
    public static String trimSlash(String s){ s=s==null?"":s.trim(); while(s.endsWith("/"))s=s.substring(0,s.length()-1); return s; }
}
