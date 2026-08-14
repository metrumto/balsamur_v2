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
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class MainActivity extends Activity {
 private static final String PREFS="balsamur_activation", ACTIVATED="activated", DEVICE="device_hash";
 private static final String[] CODE_HASHES={
"dbde63a156f01d6a7fc945a0d1b30420e28b16f31a0099b4ca3286f4b6ff2353","21393fcca9c701f6e6d08668f9b9d534c75b16d71efae0c235be4de0791e37ff","2df107d0976a74e461de29caa5921089f9fbfaf38f06b3fc90af052cf4218eac","c4c43c3b4a53d0d25b8e1f3e0a3d6b5d4a3b4a7e6d2d2c5b6a1e0c5c8b4a7b2f3","d8f0c8f3a1f3f9f4b2d7a7c2f2a6d8c1b5c2d0e1f8e5a5a1b3c5d7e9f0a2b4","e3f3b3d2f5c9a0e4f0c1b6d3a4e9f7c8b2a1d6e5f4c3b2a1e0d9c8b7a6f5e4","a0c5e4f9b2d6c7a1e3f8d5b4c2a7e6f0d1c9b8a7f6e5d4c3b2a1e0f9d8c7","b1d2e3f40516273849aabbccddeeff00112233445566778899aabbccddeeff00","3e2b4a6c8d0f1e3c5b7a9d8e6f4c2b0a1d3f5e7c9b8a6d4f2e0c1b3a5d7f9e8","c9e8d7f6a5b4c3d2e1f00112233445566778899aabbccddeeff102132435465","1a2b3c4d5e6f708192a3b4c5d6e7f8091a2b3c4d5e6f708192a3b4c5d6e7f809","2b3c4d5e6f708192a3b4c5d6e7f8091a2b3c4d5e6f708192a3b4c5d6e7f8091","3c4d5e6f708192a3b4c5d6e7f8091a2b3c4d5e6f708192a3b4c5d6e7f80912","4d5e6f708192a3b4c5d6e7f8091a2b3c4d5e6f708192a3b4c5d6e7f809123","5e6f708192a3b4c5d6e7f8091a2b3c4d5e6f708192a3b4c5d6e7f80912345","6f708192a3b4c5d6e7f8091a2b3c4d5e6f708192a3b4c5d6e7f809123456","708192a3b4c5d6e7f8091a2b3c4d5e6f708192a3b4c5d6e7f80912345678","8192a3b4c5d6e7f8091a2b3c4d5e6f708192a3b4c5d6e7f809123456789","92a3b4c5d6e7f8091a2b3c4d5e6f708192a3b4c5d6e7f809123456789a","a3b4c5d6e7f8091a2b3c4d5e6f708192a3b4c5d6e7f809123456789ab","b4c5d6e7f8091a2b3c4d5e6f708192a3b4c5d6e7f809123456789abc","c5d6e7f8091a2b3c4d5e6f708192a3b4c5d6e7f809123456789abcd","d6e7f8091a2b3c4d5e6f708192a3b4c5d6e7f809123456789abcde","e7f8091a2b3c4d5e6f708192a3b4c5d6e7f809123456789abcdef","f8091a2b3c4d5e6f708192a3b4c5d6e7f809123456789abcdef0","091a2b3c4d5e6f708192a3b4c5d6e7f809123456789abcdef01","1a2b3c4d5e6f708192a3b4c5d6e7f809123456789abcdef012","2b3c4d5e6f708192a3b4c5d6e7f809123456789abcdef0123","3c4d5e6f708192a3b4c5d6e7f809123456789abcdef01234","4d5e6f708192a3b4c5d6e7f809123456789abcdef012345","5e6f708192a3b4c5d6e7f809123456789abcdef0123456","6f708192a3b4c5d6e7f809123456789abcdef01234567","708192a3b4c5d6e7f809123456789abcdef012345678","8192a3b4c5d6e7f809123456789abcdef0123456789","92a3b4c5d6e7f809123456789abcdef0123456789a","a3b4c5d6e7f809123456789abcdef0123456789ab","b4c5d6e7f809123456789abcdef0123456789abc","c5d6e7f809123456789abcdef0123456789abcd","d6e7f809123456789abcdef0123456789abcde","e7f809123456789abcdef0123456789abcdef","f809123456789abcdef0123456789abcdef0","09123456789abcdef0123456789abcdef01","123456789abcdef0123456789abcdef012","23456789abcdef0123456789abcdef0123","3456789abcdef0123456789abcdef01234","456789abcdef0123456789abcdef012345","56789abcdef0123456789abcdef0123456","6789abcdef0123456789abcdef01234567"
 };
 @Override public void onCreate(Bundle s){super.onCreate(s);if(!isActivated())showActivationDialog();else openApp();}
 private String deviceId(){return Settings.Secure.getString(getContentResolver(),Settings.Secure.ANDROID_ID);}
 private String sha256(String v){try{byte[] d=MessageDigest.getInstance("SHA-256").digest(v.getBytes(StandardCharsets.UTF_8));StringBuilder s=new StringBuilder();for(byte b:d)s.append(String.format("%02x",b));return s.toString();}catch(Exception e){throw new RuntimeException(e);}}
 private boolean isActivated(){SharedPreferences p=getSharedPreferences(PREFS,MODE_PRIVATE);return p.getBoolean(ACTIVATED,false)&&p.getString(DEVICE,"").equals(sha256(deviceId()));}
 private void showActivationDialog(){LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(48,8,48,8);TextView info=new TextView(this);info.setText("Introduzca su código de activación de 12 caracteres.\n\nEl código quedará asociado a este dispositivo y solo será necesario introducirlo una vez.");info.setGravity(Gravity.CENTER_VERTICAL);EditText input=new EditText(this);input.setHint("XXXXXXXXXXXX");input.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);box.addView(info);box.addView(input);AlertDialog d=new AlertDialog.Builder(this).setTitle("Activar BalsaMur").setView(box).setCancelable(false).setPositiveButton("Activar",null).create();d.setOnShowListener(x->d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{String c=input.getText().toString().trim().toUpperCase();if(c.length()!=12||!validCode(c)){input.setError("Código no válido");return;}getSharedPreferences(PREFS,MODE_PRIVATE).edit().putBoolean(ACTIVATED,true).putString(DEVICE,sha256(deviceId())).apply();d.dismiss();openApp();}));d.show();}
 private boolean validCode(String c){String h=sha256(c);for(String a:CODE_HASHES)if(a.equals(h))return true;return false;}
 private void openApp(){WebView web=new WebView(this);web.setWebViewClient(new WebViewClient());WebSettings s=web.getSettings();s.setJavaScriptEnabled(true);s.setDomStorageEnabled(true);s.setAllowFileAccess(true);s.setAllowContentAccess(true);web.setOverScrollMode(WebView.OVER_SCROLL_NEVER);setContentView(web);web.loadUrl("file:///android_asset/www/index.html");}
}
