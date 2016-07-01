package com.aimanbaharum.camerademo.helper;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;

import java.io.File;
import java.io.IOException;

/**
 * Class that helps, for Android App, selecting image from gallery, getting image from camera and
 * cropping image.
 * <p/>
 * <p/>
 * IMPORTANT: The Activity, that contains this object or that contains the fragment that uses this
 * object, must handle the orientation change during taking photo. Either lock the orientation of
 * the Activity or handle orientation changes. Otherwise taking photo feature will not work since
 * the new instance of this object will be created when device rotates.
 */
public class ImageInputHelper {

    public static final int REQUEST_PICTURE_FROM_GALLERY = 23;
    public static final int REQUEST_PICTURE_FROM_CAMERA = 24;
    public static final int REQUEST_CROP_PICTURE = 25;
    private static final String TAG = "ImageInputHelper";

    private File tempFileFromSource = null;
    private Uri tempUriFromSource = null;

    private File tempFileFromCrop = null;
    private Uri tempUriFromCrop = null;

    /**
     * Activity object that will be used while calling startActivityForResult(). Activity then will
     * receive the callbacks to its own onActivityResult() and is responsible of calling the
     * onActivityResult() of the ImageInputHelper for handling result and being notified.
     */
    private Activity mContext;

    /**
     * Fragment object that will be used while calling startActivityForResult(). Fragment then will
     * receive the callbacks to its own onActivityResult() and is responsible of calling the
     * onActivityResult() of the ImageInputHelper for handling result and being notified.
     */
    private Fragment fragment;

    /**
     * Listener instance for callbacks on user events. It must be set to be able to use
     * the ImageInputHelper object.
     */
    private ImageActionListener imageActionListener;

    public ImageInputHelper(Activity mContext) {
        this.mContext = mContext;
    }

    public ImageInputHelper(Fragment fragment) {
        this.fragment = fragment;
        this.mContext = fragment.getActivity();
    }

    public void setImageActionListener(ImageActionListener imageActionListener) {
        this.imageActionListener = imageActionListener;
    }

    /**
     * Handles the result of events that the Activity or Fragment receives on its own
     * onActivityResult(). This method must be called inside the onActivityResult()
     * of the container Activity or Fragment.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if ((requestCode == REQUEST_PICTURE_FROM_GALLERY) && (resultCode == Activity.RESULT_OK)) {

            Log.d(TAG, "Image selected from gallery");
            imageActionListener.onImageSelectedFromGallery(data.getData(), tempFileFromSource);

        } else if ((requestCode == REQUEST_PICTURE_FROM_CAMERA) && (resultCode == Activity.RESULT_OK)) {

            Log.d(TAG, "Image selected from camera");
            imageActionListener.onImageTakenFromCamera(tempUriFromSource, tempFileFromSource);

        } else if ((requestCode == REQUEST_CROP_PICTURE) && (resultCode == Activity.RESULT_OK)) {

            Log.d(TAG, "Image returned from crop");
            imageActionListener.onImageCropped(tempUriFromCrop, tempFileFromCrop);
        }
    }

    /**
     * Starts an intent for selecting image from gallery. The result is returned to the
     * onImageSelectedFromGallery() method of the ImageSelectionListener interface.
     */
    public void selectImageFromGallery() {
        checkListener();

        if (tempFileFromSource == null) {
            try {
                tempFileFromSource = File.createTempFile("choose", "png", mContext.getExternalCacheDir());
                tempUriFromSource = Uri.fromFile(tempFileFromSource);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Using native image explorer
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, tempUriFromSource);
        if (fragment != null) {
            fragment.startActivityForResult(intent, REQUEST_PICTURE_FROM_GALLERY);
        } else {
            mContext.startActivityForResult(intent, REQUEST_PICTURE_FROM_GALLERY);
        }

        // Using intent picker
//        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, tempUriFromSource);
//        if (fragment != null) {
//            fragment.startActivityForResult(intent, REQUEST_PICTURE_FROM_GALLERY);
//        } else {
//            mContext.startActivityForResult(intent, REQUEST_PICTURE_FROM_GALLERY);
//        }
    }

    /**
     * Starts an intent for taking photo with camera. The result is returned to the
     * onImageTakenFromCamera() method of the ImageSelectionListener interface.
     */
    public void takePhotoWithCamera() {
        checkListener();

        if (tempFileFromSource == null) {
            try {
                tempFileFromSource = File.createTempFile("choose", "png", mContext.getExternalCacheDir());
                tempUriFromSource = Uri.fromFile(tempFileFromSource);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, tempUriFromSource);
        if (fragment == null) {
            mContext.startActivityForResult(intent, REQUEST_PICTURE_FROM_CAMERA);
        } else {
            fragment.startActivityForResult(intent, REQUEST_PICTURE_FROM_CAMERA);
        }
    }

    /**
     * Starts an intent for cropping an image that is saved in the uri. The result is
     * returned to the onImageCropped() method of the ImageSelectionListener interface.
     *
     * @param uri     uri that contains the data of the image to crop
     * @param outputX width of the result image
     * @param outputY height of the result image
     * @param aspectX horizontal ratio value while cutting the image
     * @param aspectY vertical ratio value of while cutting the image
     */
    public void requestCropImage(Uri uri, int outputX, int outputY, int aspectX, int aspectY) {
        checkListener();

        if (tempFileFromCrop == null) {
            try {
                tempFileFromCrop = File.createTempFile("crop", "png", mContext.getExternalCacheDir());
                tempUriFromCrop = Uri.fromFile(tempFileFromCrop);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // open crop intent when user selects image
        final Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("output", tempUriFromCrop);
        intent.putExtra("outputX", outputX);
        intent.putExtra("outputY", outputY);
        intent.putExtra("aspectX", aspectX);
        intent.putExtra("aspectY", aspectY);
        intent.putExtra("scale", true);
        intent.putExtra("noFaceDetection", true);
        if (fragment == null) {
            mContext.startActivityForResult(intent, REQUEST_CROP_PICTURE);
        } else {
            fragment.startActivityForResult(intent, REQUEST_CROP_PICTURE);
        }
    }

    private void checkListener() {
        if (imageActionListener == null) {
            throw new RuntimeException("ImageSelectionListener must be set before calling openGalleryIntent(), openCameraIntent() or requestCropImage().");
        }
    }

    /**
     * Listener interface for receiving callbacks from the ImageInputHelper.
     */
    public interface ImageActionListener {
        void onImageSelectedFromGallery(Uri uri, File imageFile);

        void onImageTakenFromCamera(Uri uri, File imageFile);

        void onImageCropped(Uri uri, File imageFile);
    }
}