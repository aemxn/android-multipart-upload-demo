package com.aimanbaharum.camerademo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.aimanbaharum.camerademo.helper.RealPathUtil;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Created by cliqers on 30/6/2016.
 */
public class AnotherActivity extends Activity implements View.OnClickListener {

    TextView txtSDK;
    Button btnSelectImage;
    TextView txtUriPath,txtRealPath;
    ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_another);

        // get reference to views
        txtSDK = (TextView) findViewById(R.id.txtSDK);
        btnSelectImage = (Button) findViewById(R.id.btnSelectImage);
        txtUriPath = (TextView) findViewById(R.id.txtUriPath);
        txtRealPath = (TextView) findViewById(R.id.txtRealPath);
        imageView = (ImageView) findViewById(R.id.imgView);

        // add click listener to button
        btnSelectImage.setOnClickListener(this);


    }

    @Override
    public void onClick(View view) {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, 0);
    }


    @Override
    protected void onActivityResult(int reqCode, int resCode, Intent data) {
        if(resCode == Activity.RESULT_OK && data != null){
            String realPath;
            if (Build.VERSION.SDK_INT < 11)
                realPath = RealPathUtil.getRealPathFromURI_BelowAPI11(this, data.getData());
            else if (Build.VERSION.SDK_INT < 19)
                realPath = RealPathUtil.getRealPathFromURI_API11to18(this, data.getData());
            else
                realPath = RealPathUtil.getRealPathFromURI_API19(this, data.getData());
            setTextViews(Build.VERSION.SDK_INT, data.getData().getPath(),realPath);
        }
    }

    private void setTextViews(int sdk, String uriPath,String realPath){

        this.txtSDK.setText("Build.VERSION.SDK_INT: "+sdk);
        this.txtUriPath.setText("URI Path: "+uriPath);
        this.txtRealPath.setText("Real Path: "+realPath);

        Uri uriFromPath = Uri.fromFile(new File(realPath));

        // you have two ways to display selected image

        // ( 1 ) imageView.setImageURI(uriFromPath);

        // ( 2 ) imageView.setImageBitmap(bitmap);
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uriFromPath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        imageView.setImageBitmap(bitmap);

        Log.d("HMKCODE", "Build.VERSION.SDK_INT:"+sdk);
        Log.d("HMKCODE", "URI Path:"+uriPath);
        Log.d("HMKCODE", "Real Path: "+realPath);
    }

}