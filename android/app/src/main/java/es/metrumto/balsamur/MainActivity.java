package es.metrumto.balsamur;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {
    private static final String PREFS="balsamur_activation";
    private static final String ACTIVATED="activated";
    private static final String DEVICE="device_id";
    private static final String[] CODES={
        "CE3J7BLHJ1IN","LYMFC0DZEKQI","KUI9H27G4UJ1","W88K4TY9T7SQ","YMHDCO3BASJC",
        "KYADTN9UYYXZ","WC18EVS8C2CY","WG0Y9FCS02FN","R2UNP4FP7X0M","RC8HF8IJFFT2",
        "E5LKF6SD05Z2","E6KDV8I4AB68","43C5G82XA8RR","3PZ4VPC3BV0W","CHEPMIOC0W0O",
        "TIRV94YTSXI7","A9ILQD4CJIJE","OMQG68U2AZET","08DHCG1162SR","K5YASXX3W058",
        "ATSOLEDUQPB4","VBD4WCTH7G1I","9GRWLNVBVIEJ","C5DG9FXHQGOL","ZOU9ED447JRA",
        "S6IJ3OLNYPOC","XC2J0A7FT8NM","450BW07TCBT5","MDUSA4GDVH2F","NSEUV1UIS94P",
        "TL8VGD18LZ6P","277YNENU7ISG","20HHL7YUR6Z6","IM300EC3W8AP","YBF4LU07GI0T",
        "N95X4TN9DJ63","RISN5G70ALL5","DGHL771VQRDX","HS3XOCNW48EQ","X6WGR3V9WPEL",
        "U6ZXZ1EL5T9P","DTE1551HUYWV","914KWKEHVFDL","D4VMPIMNA495","YHB1K3ENXBTN",
        "OQWSJ5QWK0DU","O5O3FNOFQCCJ","9XWD125UJ2YB","745BEA8DR302","LVE0CD64S4XI"
    };

    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        if(!isActivated()) showActivationDialog(); else openApp();
    }

    private String deviceId(){
        return Settings.Secure.getString(getContentResolver(),Settings.Secure.ANDROID_ID);
    }

    private boolean isActivated(){
        SharedPreferences p=getSharedPreferences(PREFS,MODE_PRIVATE);
        String saved=p.getString(DEVICE,"");
        return p.getBoolean(ACTIVATED,false) && saved.equals(deviceId());
    }

    private boolean validCode(String code){
        for(String allowed:CODES) if(allowed.equals(code)) return true;
        return false;
    }

    private void showActivationDialog(){
        LinearLayout box=new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(48,8,48,8);
        TextView info=new TextView(this);
        info.setText("Introduzca su código de activación de 12 caracteres.\n\nEl código quedará asociado a este dispositivo y solo será necesario introducirlo una vez.");
        info.setGravity(Gravity.CENTER_VERTICAL);
        EditText input=new EditText(this);
        input.setHint("XXXXXXXXXXXX");
        input.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        box.addView(info); box.addView(input);
        AlertDialog d=new AlertDialog.Builder(this).setTitle("Activar BalsaMur").setView(box).setCancelable(false).setPositiveButton("Activar",null).create();
        d.setOnShowListener(x->d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{
            String code=input.getText().toString().trim().toUpperCase();
            if(code.length()!=12||!validCode(code)){input.setError("Código no válido");return;}
            getSharedPreferences(PREFS,MODE_PRIVATE).edit().putBoolean(ACTIVATED,true).putString(DEVICE,deviceId()).apply();
            d.dismiss(); openApp();
        }));
        d.show();
    }

    private void openApp(){
        WebView web=new WebView(this);
        web.setWebViewClient(new WebViewClient());
        WebSettings s=web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        web.setOverScrollMode(WebView.OVER_SCROLL_NEVER);
        setContentView(web);
        web.loadUrl("file:///android_asset/www/index.html");
    }
}
