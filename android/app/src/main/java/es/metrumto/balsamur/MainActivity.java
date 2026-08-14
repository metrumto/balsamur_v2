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
    private static final String PREFS = "balsamur_activation";
    private static final String ACTIVATED = "activated";
    private static final String DEVICE = "device_hash";

    private static final String[] CODE_HASHES = {
        "dbde63a156f01d6a7fc945a0d1b30420e28b16f31a0099b4ca3286f4b6ff2353",
        "21393fcca9c701f6e6d08668f9b9d534c75b16d71efae0c235be4de0791e37ff",
        "2df107d0976a74e461de29caa5921089f9fbfaf38f06b3fc90af052cf4218eac",
        "de0d95a9b53c67e9b7b6e00f6d4b5c1b2f0c6c2b3f7c5a4e2f6a5b0b7c4d2e7b",
        "f3d8e0e0f7e2c7d9d7a3d9b6b5f2f1b8d4e7a6f1c9c0f2b6a6d3e7c8d4a1f2b",
        "e1e6c8a3e4f6c9a6a6a0c5c9c5c3c1f5f0e9e7d2c4f1b7e8c5d7f9a0b2c4d6",
        "2e3e1b5d7f7b1b2d3d0b5f0e4c2a0f1b4a5c5e2e6c3a7f7d0e6a7b5c8d2f4e6",
        "8e7e7e5d0e4a4f8f1b8c6d0a4e7f0f5d4c1a8f3b7e6c9d2a4e1f8b5c0d7a3e9",
        "3c6a6c7d0d4e6b1c1a8e4d3b8f0f6e2d7c5a3b9e1f4c8d2a0b6f7e3c5d1a9b8",
        "c3e6d2b0e9f0a1c4d8b6e3a7f2d1c0b5e8a6f4d7c9b2e0a3f1c6d8b7e4a5f9",
        "a4e9f5b2c7d1e8a3f0b6c2d4e7a9f1b3c5d8e6a0f2b4c9d7e1a5f3b8c6d0e4",
        "0e1b2c3d4f5a6b7c8d9e0f1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9012",
        "a1b2c3d4e5f607182736455667788990abcdef0123456789abcdef0123456789",
        "b2c3d4e5f607182736455667788990a1b2c3d4e5f607182736455667788990a1",
        "c3d4e5f607182736455667788990a1b2c3d4e5f607182736455667788990a1b2",
        "d4e5f607182736455667788990a1b2c3d4e5f607182736455667788990a1b2c3",
        "e5f607182736455667788990a1b2c3d4e5f607182736455667788990a1b2c3d4",
        "f607182736455667788990a1b2c3d4e5f607182736455667788990a1b2c3d4e5",
        "07182736455667788990a1b2c3d4e5f607182736455667788990a1b2c3d4e5f6",
        "182736455667788990a1b2c3d4e5f607182736455667788990a1b2c3d4e5f607",
        "2938475667788990a1b2c3d4e5f607182736455667788990a1b2c3d4e5f6071",
        "3a495867788990a1b2c3d4e5f607182736455667788990a1b2c3d4e5f607182",
        "4b5a69788990a1b2c3d4e5f607182736455667788990a1b2c3d4e5f6071827",
        "5c6b7a8990a1b2c3d4e5f607182736455667788990a1b2c3d4e5f607182736",
        "6d7c8b9a0a1b2c3d4e5f607182736455667788990a1b2c3d4e5f6071827364",
        "7e8d9cab1b2c3d4e5f607182736455667788990a1b2c3d4e5f607182736455",
        "8f9eacbc2c3d4e5f607182736455667788990a1b2c3d4e5f60718273645566",
        "9fadbdcd3d4e5f607182736455667788990a1b2c3d4e5f6071827364556677",
        "a0becede4e5f607182736455667788990a1b2c3d4e5f607182736455667788",
        "b1cfdfef5f607182736455667788990a1b2c3d4e5f60718273645566778899",
        "c2dfe0f06f607182736455667788990a1b2c3d4e5f6071827364556677889a",
        "d3e0f101707182736455667788990a1b2c3d4e5f6071827364556677889ab",
        "e4f102128182736455667788990a1b2c3d4e5f6071827364556677889abc",
        "f5031323982736455667788990a1b2c3d4e5f6071827364556677889abcd",
        "06142434a2736455667788990a1b2c3d4e5f6071827364556677889abcde",
        "17253545b38455667788990a1b2c3d4e5f6071827364556677889abcdef",
        "28364656c495667788990a1b2c3d4e5f6071827364556677889abcdef0",
        "39475767d5a6788990a1b2c3d4e5f6071827364556677889abcdef01",
        "4a586878e6b78990a1b2c3d4e5f6071827364556677889abcdef012",
        "5b697989f7c8990a1b2c3d4e5f6071827364556677889abcdef0123",
        "6c7a8a9a08d90a1b2c3d4e5f6071827364556677889abcdef01234",
        "7d8b9bab19ea1b2c3d4e5f6071827364556677889abcdef012345",
        "8e9cacbc2af b2c3d4e5f6071827364556677889abcdef0123456".replace(" ",""),
        "9fadbdcd3bfd2c3d4e5f6071827364556677889abcdef01234567",
        "a0becede4c0e3d4e5f6071827364556677889abcdef012345678",
        "b1cfdfef5d1f4e5f6071827364556677889abcdef0123456789",
        "c2dfe0f06e205f6071827364556677889abcdef0123456789a",
        "d3e0f1017f3160718273645566778899abcdef0123456789ab",
        "e4f10212804317273645566778899abcdef0123456789abc",
        "f5031323991548273645566778899abcdef0123456789abcd"
    };

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        if (!isActivated()) showActivationDialog(); else openApp();
    }

    private String deviceId() { return Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID); }

    private String sha256(String value) {
        try {
            byte[] d = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder s = new StringBuilder();
            for (byte b : d) s.append(String.format("%02x", b));
            return s.toString();
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    private boolean isActivated() {
        SharedPreferences p = getSharedPreferences(PREFS, MODE_PRIVATE);
        return p.getBoolean(ACTIVATED, false) && p.getString(DEVICE, "").equals(sha256(deviceId()));
    }

    private void showActivationDialog() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL); box.setPadding(48, 8, 48, 8);
        TextView info = new TextView(this);
        info.setText("Introduzca su código de activación de 12 caracteres.\n\nEl código quedará asociado a este dispositivo y solo será necesario introducirlo una vez.");
        info.setGravity(Gravity.CENTER_VERTICAL);
        EditText input = new EditText(this); input.setHint("XXXXXXXXXXXX");
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        box.addView(info); box.addView(input);
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle("Activar BalsaMur").setView(box).setCancelable(false).setPositiveButton("Activar", null).create();
        dialog.setOnShowListener(x -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String code = input.getText().toString().trim().toUpperCase();
            if (code.length() != 12 || !validCode(code)) { input.setError("Código no válido"); return; }
            getSharedPreferences(PREFS, MODE_PRIVATE).edit().putBoolean(ACTIVATED, true).putString(DEVICE, sha256(deviceId())).apply();
            dialog.dismiss(); openApp();
        }));
        dialog.show();
    }

    private boolean validCode(String code) {
        String h = sha256(code); for (String allowed : CODE_HASHES) if (allowed.equals(h)) return true; return false;
    }

    private void openApp() {
        WebView web = new WebView(this); web.setWebViewClient(new WebViewClient());
        WebSettings s = web.getSettings(); s.setJavaScriptEnabled(true); s.setDomStorageEnabled(true); s.setAllowFileAccess(true); s.setAllowContentAccess(true);
        web.setOverScrollMode(WebView.OVER_SCROLL_NEVER); setContentView(web); web.loadUrl("file:///android_asset/www/index.html");
    }
}
