package ir.ozen.smsbridge;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    private static final int REQ_SMS=2001; private final ExecutorService exec=Executors.newSingleThreadExecutor();
    private EditText url,secret,senders;private TextView status;
    @Override protected void onCreate(Bundle b){super.onCreate(b);setContentView(ir.ozen.smsbridge.R.layout.activity_main);url=findViewById(R.id.baseUrl);secret=findViewById(R.id.apiSecret);senders=findViewById(R.id.senders);status=findViewById(R.id.status);Button save=findViewById(R.id.saveBtn),test=findViewById(R.id.testBtn);url.setText(AppConfig.url(this));secret.setText(AppConfig.secret(this));senders.setText(AppConfig.senders(this));showLast();save.setOnClickListener(v->{save();requestSms();});test.setOnClickListener(v->{save();testConnection();});requestSms();}
    private void save(){AppConfig.prefs(this).edit().putString("url",AppConfig.trimSlash(url.getText().toString())).putString("secret",secret.getText().toString().trim()).putString("senders",senders.getText().toString().trim()).apply();status.setText("تنظیمات ذخیره شد. منتظر پیامک واریز بانک هستم.");}
    private void requestSms(){if(android.os.Build.VERSION.SDK_INT>=23&&checkSelfPermission(Manifest.permission.RECEIVE_SMS)!=PackageManager.PERMISSION_GRANTED)requestPermissions(new String[]{Manifest.permission.RECEIVE_SMS},REQ_SMS);}
    @Override public void onRequestPermissionsResult(int r,String[] p,int[] g){super.onRequestPermissionsResult(r,p,g);if(r==REQ_SMS)status.setText(g.length>0&&g[0]==PackageManager.PERMISSION_GRANTED?"مجوز SMS فعال شد. اپ آماده است.":"مجوز دریافت SMS داده نشد؛ بدون این مجوز تایید خودکار کار نمی‌کند.");}
    private void testConnection(){status.setText("در حال تست ارتباط امن...");exec.execute(()->{try{String resp=ApiClient.ping(this);String msg=resp;try{JSONObject j=new JSONObject(resp);msg=j.optString("message",resp);}catch(Exception ignored){}String finalMsg=msg;runOnUiThread(()->status.setText("موفق: "+finalMsg));}catch(Exception e){runOnUiThread(()->status.setText("ناموفق: "+e.getMessage()));}});}
    private void showLast(){long t=AppConfig.prefs(this).getLong("last_sync",0);String r=AppConfig.prefs(this).getString("last_result","");if(t>0){String d=new SimpleDateFormat("yyyy/MM/dd HH:mm:ss",Locale.US).format(new Date(t));status.setText("آخرین ارسال: "+d+"\n"+r);} }
}
