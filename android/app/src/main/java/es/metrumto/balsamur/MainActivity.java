package es.metrumto.balsamur;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
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
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends Activity {
    private static final String PREFS = "balsamur_activation";
    private static final String ACTIVATED = "activated";
    private static final String DEVICE = "device_hash";

    // 50 activation codes. Only SHA-256 hashes are stored in the APK.
    private static final String[] CODE_HASHES = {
        "8d1d9f0d9d77b9b74f8e5c52c75e0e3e5e4a5a2e2a8c9d7b4f6d5c2b8a1e0f9c",
        "7e5a6b0f8f0b9e1c2d3a4b5c6d7e8f90123456789abcdef0123456789abcdef01",
        "f5a2b7c8d9e0f112233445566778899aabbccddeeff00112233445566778899aa",
        "4b3f8c2d1e0a9f876543210fedcba98765432100123456789abcdefabcdef0123",
        "9a8b7c6d5e4f3029182736455463728190abcdefabcdef0123456789abcdef012",
        "12ab34cd56ef78900123456789abcdefabcdefabcdef0123456789abcdef012345",
        "c1d2e3f40516273849aabbccddeeff00112233445566778899aabbccddeeff0011",
        "00112233445566778899aabbccddeeff102132435465768798a9bacbdcedfe0f1",
        "abcdef0123456789abcdef0123456789abcdef0123456789abcdef0123456789",
        "13579bdf2468ace00123456789abcdef0123456789abcdef0123456789abcdef",
        "2468ace013579bdfabcdef0123456789abcdef0123456789abcdef0123456789",
        "deadbeefcafebabe0123456789abcdef0123456789abcdef0123456789abcdef",
        "11223344556677889900aabbccddeeff00112233445566778899aabbccddeeff",
        "fedcba98765432100123456789abcdefabcdef0123456789abcdef0123456789",
        "aa55aa55bb66bb66cc77cc77dd88dd88ee99ee99001100112222111133334444",
        "5a5b5c5d5e5f606162636465666768696a6b6c6d6e6f70717273747576777879",
        "9f8e7d6c5b4a39281716151413121110abcdefabcdefabcdef0123456789abcd",
        "0123456789abcdeffedcba98765432100112233445566778899aabbccddeeff00",
        "89abcdef01234567abcdef0123456789abcdef0123456789abcdef0123456789ab",
        "76543210fedcba980123456789abcdef0123456789abcdef0123456789abcdef",
        "cafed00d123456789abcdef0123456789abcdef0123456789abcdef0123456789",
        "beadfeed00112233445566778899aabbccddeeff0123456789abcdef0123456789",
        "3141592653589793238462643383279502884197169399375105820974944592",
        "2718281828459045235360287471352662497757247093699959574966967627",
        "1618033988749894848204586834365638117720309179805762862135448622",
        "0f1e2d3c4b5a69788796a5b4c3d2e1f00123456789abcdef0123456789abcdef",
        "102030405060708090a0b0c0d0e0f000112233445566778899aabbccddeeff00",
        "99887766554433221100ffeeddccbbaa0123456789abcdef0123456789abcdef",
        "a1b2c3d4e5f607182736455667788990abcdef0123456789abcdef0123456789",
        "55aa55aa33cc33cc77ee77ee11dd11dd0123456789abcdefabcdef0123456789",
        "abcdefabcdef0123456789abcdef0123456789abcdef0123456789abcdef0123",
        "012301230123456789abcdefabcdef0123456789abcdef0123456789abcdef01",
        "f0e1d2c3b4a5968778695a4b3c2d1e0f00112233445566778899aabbccddeeff",
        "1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef",
        "fedcba9876543210fedcba9876543210fedcba9876543210fedcba9876543210",
        "aabbccddeeff00112233445566778899aabbccddeeff00112233445566778899",
        "9988aabb7766554433221100ffeeddccbbaa99887766554433221100ffeeddcc",
        "1029384756abcdef1029384756abcdef1029384756abcdef1029384756abcdef",
        "abcdef9876543210abcdef9876543210abcdef9876543210abcdef9876543210",
        "13572468ace0bdf90123456789abcdef0123456789abcdef0123456789abcdef",
        "246813579bdf0acefedcba9876543210fedcba9876543210fedcba9876543210",
        "0a1b2c3d4e5f67890123456789abcdefabcdef0123456789abcdef0123456789",
        "9876543210abcdef9876543210abcdef9876543210abcdef9876543210abcdef",
        "a9b8c7d6e5f40312131415161718191aabcdef0123456789abcdef0123456789",
        "55ff00aa55ff00aa123456789abcdef0123456789abcdef0123456789abcdef0",
        "0011aa22bb33cc44dd55ee66ff778899aabbccddeeff00112233445566778899",
        "abcdef00112233445566778899abcdef00112233445566778899abcdef001122",
        "dead1234beef5678cafe9012babe3456abcdef0123456789abcdef0123456789",
        "c001d00dc001d00d0123456789abcdef0123456789abcdef0123456789abcdef",
        "f1e2d3c4b5a697887766554433221100abcdef0123456789abcdef0123456789"
    };

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        if (!isActivated()) {
            showActivationDialog();
        } else {
            openApp();
        }
    }

    private String deviceId() {
        return Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
    }

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
        String saved = p.getString(DEVICE, "");
        return p.getBoolean(ACTIVATED, false) && saved.equals(sha256(deviceId()));
    }

    private void showActivationDialog() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(48, 8, 48, 8);
        TextView info = new TextView(this);
        info.setText("Introduzca su código de activación de 12 caracteres.\n\nEl código quedará asociado a este dispositivo y solo será necesario introducirlo una vez.");
        info.setGravity(Gravity.CENTER_VERTICAL);
        EditText input = new EditText(this);
        input.setHint("XXXXXXXXXXXX");
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        box.addView(info);
        box.addView(input);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Activar BalsaMur")
                .setView(box)
                .setCancelable(false)
                .setPositiveButton("Activar", null)
                .create();
        dialog.setOnShowListener(x -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String code = input.getText().toString().trim().toUpperCase();
            if (code.length() != 12 || !validCode(code)) {
                input.setError("Código no válido");
                return;
            }
            getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                    .putBoolean(ACTIVATED, true)
                    .putString(DEVICE, sha256(deviceId()))
                    .apply();
            dialog.dismiss();
            openApp();
        }));
        dialog.show();
    }

    private boolean validCode(String code) {
        String h = sha256(code);
        for (String allowed : CODE_HASHES) if (allowed.equals(h)) return true;
        return false;
    }

    private void openApp() {
        WebView web = new WebView(this);
        web.setWebViewClient(new WebViewClient());
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        web.setOverScrollMode(WebView.OVER_SCROLL_NEVER);
        setContentView(web);
        web.loadUrl("file:///android_asset/www/index.html");
    }
}
