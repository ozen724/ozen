package ir.ozen.smsbridge;

import android.content.Context;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public final class ApiClient {
    private ApiClient() {}
    public static String ping(Context c) throws Exception { JSONObject j=new JSONObject(); j.put("device_id",AppConfig.deviceId(c)); return post(c,"/card2card-auto/ping",j.toString()); }
    public static String sendSms(Context c,String sender,String body,long smsTimeMs) throws Exception { JSONObject j=new JSONObject();j.put("sender",sender);j.put("body",body);j.put("sms_time_ms",smsTimeMs);j.put("device_id",AppConfig.deviceId(c));return post(c,"/card2card-auto/sms",j.toString()); }
    private static String post(Context c,String path,String body) throws Exception {
        String base=AppConfig.url(c), secret=AppConfig.secret(c); if(base.isEmpty()||secret.length()<32) throw new IllegalStateException("آدرس سایت یا کلید API تنظیم نشده است.");
        long ts=System.currentTimeMillis()/1000L; String nonce=UUID.randomUUID().toString(); String data=ts+"\n"+nonce+"\n"+body; String sig=hmac(secret,data);
        HttpURLConnection con=(HttpURLConnection)new URL(base+path).openConnection(); con.setConnectTimeout(8000);con.setReadTimeout(10000);con.setRequestMethod("POST");con.setDoOutput(true);con.setRequestProperty("Content-Type","application/json; charset=utf-8");con.setRequestProperty("Accept","application/json");con.setRequestProperty("X-Ozen-Timestamp",String.valueOf(ts));con.setRequestProperty("X-Ozen-Nonce",nonce);con.setRequestProperty("X-Ozen-Signature",sig);
        byte[] bytes=body.getBytes(StandardCharsets.UTF_8);try(OutputStream os=con.getOutputStream()){os.write(bytes);}int code=con.getResponseCode();InputStream in=code>=200&&code<300?con.getInputStream():con.getErrorStream();String resp=read(in);if(code<200||code>=300)throw new IllegalStateException("HTTP "+code+": "+resp);return resp;
    }
    private static String hmac(String secret,String data) throws Exception { Mac mac=Mac.getInstance("HmacSHA256");mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),"HmacSHA256"));byte[] out=mac.doFinal(data.getBytes(StandardCharsets.UTF_8));StringBuilder sb=new StringBuilder(out.length*2);for(byte b:out)sb.append(String.format("%02x",b&0xff));return sb.toString(); }
    private static String read(InputStream in) throws Exception { if(in==null)return"";StringBuilder sb=new StringBuilder();try(BufferedReader br=new BufferedReader(new InputStreamReader(in,StandardCharsets.UTF_8))){String line;while((line=br.readLine())!=null)sb.append(line);}return sb.toString(); }
}
