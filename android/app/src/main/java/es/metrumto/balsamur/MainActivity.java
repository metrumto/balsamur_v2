package es.metrumto.balsamur;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    private static final String PREFS="balsamur_activation";
    private static final String ACTIVATED="activated";
    private static final String DEVICE="device_id";
    private static final String ACTIVATION_URL="https://balsamur-activation.onrender.com/activate";
    private final ExecutorService executor=Executors.newSingleThreadExecutor();

    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        if(isActivated()) openApp(); else showActivationDialog();
    }

    private String deviceId(){
        String id=Settings.Secure.getString(getContentResolver(),Settings.Secure.ANDROID_ID);
        return id == null ? "unknown-device" : id;
    }

    private boolean isActivated(){
        SharedPreferences p=getSharedPreferences(PREFS,MODE_PRIVATE);
        return p.getBoolean(ACTIVATED,false) && deviceId().equals(p.getString(DEVICE,""));
    }

    private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+0.5f);}

    private void showActivationDialog(){
        LinearLayout box=new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        int p=dp(24); box.setPadding(p,0,p,0);
        TextView info=new TextView(this);
        info.setText("Introduzca su código de activación de 12 caracteres.\n\nEl código quedará asociado a este dispositivo y solo será necesario introducirlo una vez.");
        EditText input=new EditText(this);
        input.setHint("XXXXXXXXXXXX");
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        box.addView(info,new LinearLayout.LayoutParams(-1,-2));
        box.addView(input,new LinearLayout.LayoutParams(-1,-2));
        AlertDialog d=new AlertDialog.Builder(this).setTitle("Activar BalsaMur").setView(box).setCancelable(false).setNegativeButton("Salir",(x,w)->finish()).setPositiveButton("Activar",null).create();
        d.setOnShowListener(x->d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{
            String code=input.getText().toString().trim().toUpperCase();
            if(!code.matches("[A-Z0-9]{12}")){input.setError("El código debe tener exactamente 12 caracteres alfanuméricos");return;}
            d.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            activate(code,d,input);
        }));
        d.show();
    }

    private void activate(String code, AlertDialog dialog, EditText input){
        executor.execute(()->{
            int status=-1; String body="";
            try{
                JSONObject payload=new JSONObject();
                payload.put("code",code); payload.put("deviceId",deviceId());
                HttpURLConnection c=(HttpURLConnection)new URL(ACTIVATION_URL).openConnection();
                c.setRequestMethod("POST"); c.setConnectTimeout(15000); c.setReadTimeout(15000);
                c.setRequestProperty("Content-Type","application/json; charset=UTF-8"); c.setDoOutput(true);
                try(OutputStream o=c.getOutputStream()){o.write(payload.toString().getBytes(StandardCharsets.UTF_8));}
                status=c.getResponseCode();
                InputStream in=status>=400?c.getErrorStream():c.getInputStream();
                if(in!=null){try(BufferedReader r=new BufferedReader(new InputStreamReader(in,StandardCharsets.UTF_8))){String l;StringBuilder b=new StringBuilder();while((l=r.readLine())!=null)b.append(l);body=b.toString();}}
                c.disconnect();
            }catch(Exception e){body=e.getMessage()==null?"network_error":e.getMessage();}
            final int st=status; final String res=body;
            runOnUiThread(()->{
                if(st==200 && res.contains("\"ok\":true")){
                    getSharedPreferences(PREFS,MODE_PRIVATE).edit().putBoolean(ACTIVATED,true).putString(DEVICE,deviceId()).apply();
                    dialog.dismiss(); openApp();
                }else{
                    String msg=st==409?"Este código ya está asociado a otro dispositivo.":st==403?"Código no válido.":st==400?"El código debe tener 12 caracteres alfanuméricos.":"No se ha podido conectar con el servidor de activación.";
                    Toast.makeText(this,msg,Toast.LENGTH_LONG).show(); dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }
            });
        });
    }

    private void openApp(){
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL);
        WebView web=new WebView(this); web.setWebViewClient(new WebViewClient());
        WebSettings s=web.getSettings(); s.setJavaScriptEnabled(true); s.setDomStorageEnabled(true); s.setAllowFileAccess(true); s.setAllowContentAccess(true);
        web.setOverScrollMode(WebView.OVER_SCROLL_NEVER);
        root.addView(web,new LinearLayout.LayoutParams(-1,0,1));
        ImageView footer=new ImageView(this); footer.setImageResource(R.drawable.footer_icon); footer.setScaleType(ImageView.ScaleType.FIT_CENTER); footer.setPadding(dp(8),dp(4),dp(8),dp(4));
        root.addView(footer,new LinearLayout.LayoutParams(-1,dp(72)));
        setContentView(root); web.loadUrl("file:///android_asset/www/index.html");
    }

    @Override protected void onDestroy(){executor.shutdownNow();super.onDestroy();}
}
