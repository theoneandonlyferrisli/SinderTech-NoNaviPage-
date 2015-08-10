package jerei_digitals.sindertechnonavipage;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class web extends AppCompatActivity {
    private WebView webView;
    private static final String TARGET_URL = "http://192.168.50.200:8877/";
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        // 监听手机网络变化；警告用户如无网络连接。
        BroadcastReceiver mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean noConnectivity = intent.
                        getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
                if (noConnectivity) {
                        new AlertDialog.Builder(web.this)
                                .setTitle("警告")
                                .setMessage("当前无网络连接,是否前往设置？")
                                .setPositiveButton("设置", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                                    }
                                })
                                .setNegativeButton("忽略", null)
                                .show();

                } else {
                    Toast.makeText(web.this, "网络已链接！", Toast.LENGTH_SHORT).show();
                }
            }
        };

        // 注册网络变化监听器至本页面。
        registerReceiver(mReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        // 初始化WebView
        webView = (WebView) findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.requestFocus();
        webView.loadUrl(TARGET_URL);

        // 覆盖“shouldOverrideUrlLoading”以在WebView内部打开链接
        webView.setWebViewClient( new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView webView, String url) {
                webView.loadUrl(url);
                return false;
            }

            @Override
            // 当页面加载失败时显示错误信息
            public void onReceivedError(WebView webView, int errorCode,
                                        String description, String faillingUrl) {
                new AlertDialog.Builder(web.this)
                        .setTitle("网页加载错误")
                        .setMessage("页面加载失败，请下拉页面刷新尝试再次加载！")
                        .setPositiveButton("确定", null)
                        .show();
                super.onReceivedError(webView, errorCode, description, faillingUrl);
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            // 显示载入进度
            @Override
            public void onProgressChanged(WebView view, int progress) {
                if (progress == 100) {
                    swipeRefreshLayout.setRefreshing(false);
                } else {
                    if (!swipeRefreshLayout.isRefreshing())
                        swipeRefreshLayout.setRefreshing(true);
                }

                super.onProgressChanged(webView, progress);
            }
        });

        // 设置下拉刷新功能
        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.holo_blue_bright,
                R.color.holo_green_light, R.color.holo_orange_light,
                R.color.holo_red_light);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                webView.loadUrl(webView.getUrl());
            }
        });
    }

    // 覆盖onKeyDown函数，当有可返回的网页浏览记录时将返回键功能改为后退至上一网页
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (webView.canGoBack() && event.getKeyCode() == KeyEvent.KEYCODE_BACK &&
                event.getRepeatCount() == 0) {
            webView.goBack();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        webView.saveState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_web, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
