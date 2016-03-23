package com.aimanbaharum.camerademo;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int REQUEST_CAMERA = 1001;
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final int REQUEST_GALLERY = 1002;
    private static final String API_URL = "http://api.aimanbaharum.com/shoppermate-test/upload.php";

    private Bitmap mBitmap;
    private Button btn_upload;
    private Button btn_camera;

    private final CharSequence[] items = {"Take Photo", "From Gallery"};

    private ImageView iv_image;
    private TextView tv_url;
    private EditText et_response;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        iv_image = (ImageView) findViewById(R.id.iv_image);
        btn_upload = (Button) findViewById(R.id.btn_upload);
        btn_camera = (Button) findViewById(R.id.btn_camera);
        et_response = (EditText) findViewById(R.id.et_response);
        tv_url = (TextView) findViewById(R.id.tv_url);
        btn_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooserDialog();
            }
        });

        if (selectedImageUri == null) {
            btn_upload.setClickable(false);
        }

    }

    View.OnClickListener upload = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            /** Multipart upload */
            try {
                execMultipartPost();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private void execMultipartPost() throws Exception {
        File file = new File(selectedImageUri.getPath());
        String contentType = file.toURL().openConnection().getContentType();
        RequestBody fileBody = RequestBody.create(MediaType.parse(contentType), file);

        final String filename = "file_" + System.currentTimeMillis() / 1000L;

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("fileUploadType", "1")
                .addFormDataPart("miniType", contentType)
                .addFormDataPart("ext", file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf(".")))
                .addFormDataPart("fileTypeName", "img")
                .addFormDataPart("clientFilePath", selectedImageUri.getPath())
                .addFormDataPart("filedata", filename + ".png", fileBody)
                // timestamp
                .build();

        Request request = new Request.Builder()
                .url(API_URL)
                .post(requestBody)
                .build();

        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        et_response.setText(e.getMessage());
                        Toast.makeText(MainActivity.this, "nah", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            et_response.setText(response.body().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        tv_url.setVisibility(View.VISIBLE);
                        tv_url.setText(Html.fromHtml("<a href=\"http://api.aimanbaharum.com/shoppermate-test/uploads/" + filename + ".png\">See image in browser</a>"));
                        tv_url.setMovementMethod(LinkMovementMethod.getInstance());
                        Toast.makeText(MainActivity.this, "response: " + response, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void openFileChooserDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Picture");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        initCameraPermission();
                        break;
                    case 1:
                        initGalleryIntent();
                        break;
                    default:
                }
            }
        });
        builder.show();
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void initCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                Toast.makeText(this, "Permission to use Camera", Toast.LENGTH_SHORT).show();
            }
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            initCameraIntent();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initCameraIntent();
            } else {
                Toast.makeText(this, "Permission denied by user", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void initGalleryIntent() {
        Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(
                Intent.createChooser(galleryIntent, "Select File"),
                REQUEST_GALLERY
        );
    }

    private void initCameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File file = getOutputMediafile(1);
        selectedImageUri = Uri.fromFile(file);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, selectedImageUri);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_CAMERA);
        }
    }

    private File getOutputMediafile(int type) {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), getResources().getString(R.string.app_name)
        );
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }
        String timeStamp = new SimpleDateFormat("yyyyHHdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == 1) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".png");
        } else {
            return null;
        }

        return mediaFile;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CAMERA) {
                //
            } else if (requestCode == REQUEST_GALLERY) {
                selectedImageUri = data.getData();
            }
            mBitmap = ImageUtils.getScaledImage(selectedImageUri, this);
            setImageBitmap(mBitmap);
//            try {
//                InputStream inputStream;
//                inputStream = getContentResolver().openInputStream(selectedImageUri);
//                mBitmap = BitmapFactory.decodeStream(inputStream);
//                setImageBitmap(mBitmap);
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
        }

    }

    private void setImageBitmap(Bitmap bm) {
        iv_image.setImageBitmap(bm);
        btn_upload.setClickable(true);
        btn_upload.setOnClickListener(upload);
    }

}
