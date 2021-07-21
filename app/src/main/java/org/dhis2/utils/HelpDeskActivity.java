package org.dhis2.utils;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import org.dhis2.R;

import static org.dhis2.utils.WebViewActivity.WEB_VIEW_URL;

public class HelpDeskActivity extends AppCompatActivity {

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_desk);

        WebView webView = findViewById(R.id.web_view);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String url = bundle.getString(WEB_VIEW_URL, "https://nacareke.on.spiceworks.com/portal");
            webView.getSettings().setJavaScriptEnabled(true);
            webView.setWebViewClient(new WebViewClient());
            webView.loadUrl(url);
        }

        ImageView backImg = findViewById(R.id.back);
        backImg.setOnClickListener( e -> finish());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}