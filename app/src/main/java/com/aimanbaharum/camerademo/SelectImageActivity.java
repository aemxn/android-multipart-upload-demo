package com.aimanbaharum.camerademo;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.aimanbaharum.camerademo.helper.ImageInputHelper;
import com.aimanbaharum.camerademo.helper.RealPathUtil;

import java.io.File;
import java.io.IOException;

/**
 * Created by cliqers on 1/7/2016.
 */
public class SelectImageActivity extends ActionBarActivity implements ImageInputHelper.ImageActionListener {

    private static final String TAG = SelectImageActivity.class.getSimpleName();

    private ImageInputHelper imageInputHelper;
    private static final int REQUEST_CAMERA = 1001;
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final int REQUEST_STORAGE_PERMISSION = 2;
    private static final int REQUEST_GALLERY = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_image);

        imageInputHelper = new ImageInputHelper(this);
        imageInputHelper.setImageActionListener(this);

        findViewById(R.id.select_photo_from_gallery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT < 23) {
                    imageInputHelper.selectImageFromGallery();
                } else {
                    initGalleryPermission();
                }
            }
        });

        findViewById(R.id.take_picture_with_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT < 23) {
                    imageInputHelper.takePhotoWithCamera();
                } else {
                    initCameraPermission();
                }
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void initCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                Toast.makeText(this, "Permission to use Camera", Toast.LENGTH_SHORT).show();
            }
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            imageInputHelper.takePhotoWithCamera();
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void initGalleryPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Toast.makeText(this, "Permission to read Storage", Toast.LENGTH_SHORT).show();
            }
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
        } else {
            imageInputHelper.selectImageFromGallery();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                imageInputHelper.takePhotoWithCamera();
            } else {
                Toast.makeText(this, "Permission denied by user", Toast.LENGTH_SHORT).show();
            }
        } else if(requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                imageInputHelper.selectImageFromGallery();
            } else {
                Toast.makeText(this, "Permission denied by user", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        imageInputHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onImageSelectedFromGallery(Uri uri, File imageFile) {
        // cropping the selected image. crop intent will have aspect ratio 16/9 and result image
        // will have size 800x450
//        imageInputHelper.requestCropImage(uri, 800, 450, 16, 9);
        displayImage(uri);
    }

    @Override
    public void onImageTakenFromCamera(Uri uri, File imageFile) {
        // cropping the taken photo. crop intent will have aspect ratio 16/9 and result image
        // will have size 800x450
//        imageInputHelper.requestCropImage(uri, 800, 450, 16, 9);
        displayImage(uri);
    }

    @Override
    public void onImageCropped(Uri uri, File imageFile) {
        displayImage(uri);
    }

    private void displayImage(Uri uri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);

            String realPath = "";

            if (Build.VERSION.SDK_INT < 11) {
                realPath = RealPathUtil.getRealPathFromURI_BelowAPI11(this, uri);
            } else if (Build.VERSION.SDK_INT < 19) {
                realPath = RealPathUtil.getRealPathFromURI_API11to18(this, uri);
            } else {
                realPath = RealPathUtil.getRealPathFromURI_API19(this, uri);
            }

            // showing bitmap in image view
            ((ImageView) findViewById(R.id.image)).setImageBitmap(bitmap);

            Log.d(TAG, "realPath: " + realPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}