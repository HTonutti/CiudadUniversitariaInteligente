package com.holamundo.ciudaduniversitariainteligente;

import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class news_adapter extends BaseAdapter {
    private ArrayList<Noticia> listadoNoticias;
    private LayoutInflater lInflater;
    private Context context;
    private MainActivity oMainActivity = null;

    public news_adapter(Context context, ArrayList<Noticia> listadoNoticias, MainActivity oMainActivity) {
        this.listadoNoticias = listadoNoticias;
        this.lInflater = LayoutInflater.from(context);
        this.context= context;
        this.oMainActivity = oMainActivity;
    }

    @Override
    public int getCount() {
        return listadoNoticias.size();
    }

    @Override
    public Noticia getItem(int i) {
        return listadoNoticias.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        final Noticia noti= (Noticia) getItem(i);
        if(view==null) {
            view = lInflater.inflate(R.layout.news_item, null);
        }

        TextView fecha= (TextView) view.findViewById(R.id.fecha_news);
        fecha.setText(noti.getFecha());

        ImageView logo= (ImageView) view.findViewById(R.id.logo);
        final Bitmap bm= noti.getBitmapImage();
        if(bm!=null) logo.setImageBitmap(bm);
        else logo.setImageResource(R.drawable.unl_logo);

        final int width;
        if(viewGroup.getWidth()<viewGroup.getHeight()) width= (int) (viewGroup.getWidth()*0.9);
        else width= (int) (viewGroup.getHeight()*0.9);


        //agrego click en la imagen para ampliarla
        logo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog settingsDialog= new Dialog(context);
                settingsDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);

                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(width, RelativeLayout.LayoutParams.WRAP_CONTENT);
                lp.addRule(RelativeLayout.CENTER_IN_PARENT);
                ImageView iv = new ImageView(context);
                iv.setLayoutParams(lp);
                if(bm!=null) iv.setImageBitmap(bm);
                else iv.setImageResource(R.drawable.unl_logo);

                iv.setAdjustViewBounds(true);
                iv.setScaleType(ImageView.ScaleType.FIT_XY);

                settingsDialog.addContentView(iv,lp);
                settingsDialog.show();
            }
        });

        //fin agrego


        TextView titulo= (TextView) view.findViewById(R.id.titulo_news);
        titulo.setText(noti.getTitulo());

        TextView descrip= (TextView) view.findViewById(R.id.descripcion_news);
        descrip.setText(noti.getDescrip());

        TextView link= (TextView) view.findViewById(R.id.hyperlink_news);
        if(noti.getLink()!="") {
//            Spanned html = Html.fromHtml("<a href=" + noti.getLink() + ">Link</a>", 0);
//            link.setMovementMethod(LinkMovementMethod.getInstance());
//            link.setText(html);

            //para que se abra en el navegador dentro de la app
            link.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    NavegadorFragment navegador = new NavegadorFragment();
                    navegador.setLink(noti.getLink());

                    //hacer visible
                    oMainActivity.addToFMstack(navegador);
                }
            });


        }else
            link.setText("");

        return view;
    }
}
