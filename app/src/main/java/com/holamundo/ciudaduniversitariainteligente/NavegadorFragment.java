package com.holamundo.ciudaduniversitariainteligente;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class NavegadorFragment extends Fragment {

    private String link;

    public void setLink(String link){
        this.link = link;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view1= inflater.inflate(R.layout.webview, null);
        WebView navegador= (WebView) view1.findViewById(R.id.webView);

        //client
        navegador.setWebViewClient(new WebViewClient());

        //settings
        navegador.getSettings().setJavaScriptEnabled(true);
//        navegador.getSettings().setUserAgentString("Android");

        navegador.getSettings().setLoadWithOverviewMode(true);
        navegador.getSettings().setUseWideViewPort(true);

        navegador.getSettings().setSupportZoom(true);
        navegador.getSettings().setBuiltInZoomControls(true);
        navegador.getSettings().setDisplayZoomControls(false);




        //cargar link
        navegador.loadUrl(link);

        return view1;
    }
}
