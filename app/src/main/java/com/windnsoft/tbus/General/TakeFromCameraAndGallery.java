package com.windnsoft.tbus.General;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TakeFromCameraAndGallery {

    public static final int RequestCode_Camera = 13431;
    public static final int RequestCode_Gallery = 13432;
    public static final int RequestCode_Gallery_Kitkat = 13433;
    public static final int RequestCode_Crop = 13434;
    public static final int RequestCode_CameraCrop = 13435;
    public static final int RequestCode_GalleryCrop = 13436;
    public static final int RequestCode_GalleryOnly = 13437;

    public static final int MaxPictureHeight = 800; //최대 해상도 높이

    private static final String Instance_SaveFile = "Instance_SaveFile";
    private static final String Instance_TempFile = "Instance_TempFile";

    private Activity context;
    private Fragment fragment;
    private String saveFile;
    private String tempFile;

    public TakeFromCameraAndGallery(Activity context) {
        this.context = context;
    }

    public TakeFromCameraAndGallery(Fragment fragment) {
        this.fragment = fragment;
        this.context = fragment.getActivity();
    }

    public void setSaveFilePath(String path) {
        saveFile = path;
    }

    public String getSavedFile() {
        return saveFile;
    }

    public String getTempFile() {
        return tempFile;
    }

    public void startIntentCameraWithCrop() {
        //		Uri uri = Uri.fromFile((null!=saveFile && !"".equals(saveFile))? new File(saveFile): generateTempFilePath());
        Uri uri = Uri.fromFile((null != tempFile && !"".equals(tempFile)) ? new File(tempFile) : generateTempFilePath());

        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra("crop", "true");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        intent.putExtra("outputFormat", CompressFormat.PNG.toString());
        intent.putExtra("return-data", true); //용량이 크면 오류난다
        if (fragment != null)
            fragment.startActivityForResult(intent, RequestCode_CameraCrop);
        else if (context != null)
            context.startActivityForResult(intent, RequestCode_CameraCrop);
    }

    public void startIntentGalleryWithCrop() {
        Uri uri = Uri.fromFile((null != tempFile && !"".equals(tempFile)) ? new File(tempFile) : generateTempFilePath());
        // pick from file
        /**
         * To select an image from existing files, use
         * Intent.createChooser to open image chooser. Android will
         * automatically display a TypeList of supported applications,
         * such as image gallery or file manager.
         */
        Intent intent = new Intent();

        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.putExtra("crop", "true");
        intent.putExtra("outputFormat", CompressFormat.PNG.toString());
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        //		intent.putExtra("return-data", true); //용량이 크면 오류난다

        if (fragment != null)
            fragment.startActivityForResult(Intent.createChooser(intent, "Choice"), RequestCode_GalleryCrop);
        else if (context != null)
            context.startActivityForResult(Intent.createChooser(intent, "Choice"), RequestCode_GalleryCrop);
    }

    public void startIntentCamera() {
//		Uri uri = Uri.fromFile((null!=tempFile && !"".equals(tempFile))? new File(tempFile): generateTempFilePath());

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //		intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        if (fragment != null)
            fragment.startActivityForResult(intent, RequestCode_Camera);
        else if (context != null)
            context.startActivityForResult(intent, RequestCode_Camera);
    }

    public void startIntentGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        if (fragment != null)
            fragment.startActivityForResult(intent, RequestCode_Gallery);
        else if (context != null)
            context.startActivityForResult(intent, RequestCode_Gallery);
    }

    public void startIntentCrop(Uri orgUri, Map<String, Integer> extraSet) {

        Uri uri = Uri.fromFile((null != tempFile && !"".equals(tempFile)) ? new File(tempFile) : generateTempFilePath());

        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(orgUri, "image/*");
        intent.putExtra("crop", "true");

        if(extraSet!=null){
            for(String key: extraSet.keySet()){
                intent.putExtra(key, extraSet.get(key));
            }
        }
        intent.putExtra("scale", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        intent.putExtra("return-data", true);
        if (fragment != null)
            fragment.startActivityForResult(intent, RequestCode_Crop);
        else if (context != null)
            context.startActivityForResult(intent, RequestCode_Crop);
    }

    public Uri getResultUri(Intent data) {
        Uri ret = null;
        String savedFileString = null;

        if (null != tempFile && !"".equals(tempFile)) {
            // save된 file이 있다면
            File temp = new File(tempFile);
            if (temp.exists()) {
                savedFileString = tempFile;
            }
            tempFile = null;
        }

        if (null == savedFileString && null != data) {
            // data로 uri 가 반환된다면
            Uri uriData = data.getData();
            if (null != uriData && new File(uriData.getPath()).length() > 0) {
                savedFileString = uriData.getPath();
            }
        }

        if (null == savedFileString) {
            // 삼성폰에서.
            Uri temp = getSamsungPhoneUri();
            if (null != temp && new File(temp.getPath()).length() > 0) {
                savedFileString = temp.getPath();
            }
        }

        // bitmap 이 반환된다면
        Bundle extras;
        Bitmap photo;

        if (null == savedFileString && null != (extras = data.getExtras()) && null != (photo = extras.getParcelable("data"))) {
            if (photo.getWidth() > 0 && photo.getHeight() > 0) {
                File temp = generateTempFilePath();
                WsBitmapTools.saveBitmap(photo, temp.getAbsolutePath());

                savedFileString = temp.getAbsolutePath();
            }
        }

        File file = new File(savedFileString);
        if (file.exists() && file.length() > 0)
            ret = Uri.fromFile(file);

        return ret;
    }

    @SuppressWarnings("deprecation")
    private Uri getSamsungPhoneUri() {
        Uri imageCaptureUri = null;

        try {
            Log.i("TAG", "inside Samsung Phones");
            String[] projection = {
                    MediaStore.Images.Thumbnails._ID, // The columns we want
                    MediaStore.Images.Thumbnails.IMAGE_ID,
                    MediaStore.Images.Thumbnails.KIND,
                    MediaStore.Images.Thumbnails.DATA};
            String selection = MediaStore.Images.Thumbnails.KIND + "=" + MediaStore.Images.Thumbnails.MINI_KIND;

            String sort = MediaStore.Images.Thumbnails._ID + " DESC";

            Cursor myCursor = context.managedQuery(
                    MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,
                    projection, selection, null, sort);

            long imageId = 0l;
            long thumbnailImageId = 0l;
            String thumbnailPath = "";

            try {
                myCursor.moveToFirst();
                imageId = myCursor.getLong(myCursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails.IMAGE_ID));
                thumbnailImageId = myCursor.getLong(myCursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails._ID));
                thumbnailPath = myCursor.getString(myCursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails.DATA));
            } finally {
                // myCursor.close();
            }

            String[] largeFileProjection = {
                    MediaStore.Images.ImageColumns._ID,
                    MediaStore.Images.ImageColumns.DATA};

            String largeFileSort = MediaStore.Images.ImageColumns._ID + " DESC";
            myCursor = context.managedQuery(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    largeFileProjection, null, null, largeFileSort);

            try {
                myCursor.moveToFirst();

                String largeImagePath = myCursor.getString(myCursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA));
                imageCaptureUri = Uri.fromFile(new File(largeImagePath));
            } finally {
                // myCursor.close();
            }

            Uri uriLargeImage = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, String.valueOf(imageId));
            Uri uriThumbnailImage = Uri.withAppendedPath(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, String.valueOf(thumbnailImageId));

            if (imageCaptureUri == null) imageCaptureUri = uriLargeImage;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return imageCaptureUri;
    }

    private Uri getResultUriFromMediaStore() {
        File toFile = new File(tempFile);
        File fromFile = null;
        FileInputStream inputStream = null;
        FileOutputStream outputStream = null;
        //		if (saveFile != null) {
        //			File f = new File(saveFile);
        //			if (f.exists()) {
        //				f.delete();
        //			}
        //		}

        Uri uriImages = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        int id = -1;

        String[] IMAGE_PROJECTION = {
                MediaStore.Images.ImageColumns.DATA, //데이터의 파일 절대 경로(문자열)
                MediaStore.Images.ImageColumns._ID, //데이터의 테이블상의 아이디(정수)
        };
        try {
            Cursor cursorImages = context.getContentResolver().query(uriImages, IMAGE_PROJECTION, null, null, null);
            if (cursorImages != null && cursorImages.moveToLast()) {
                fromFile = new File(cursorImages.getString(0)); //이 것이 경로
                id = cursorImages.getInt(1); //이 것이 아이디
                cursorImages.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 임시파일의 경로로서, 본인은 원본과 겹치지 않게 아래와 같이 작성하였다.
        toFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + System.currentTimeMillis() + ".jpg");

        try {
            // 이 부분이 파일을 복사하는 부분이다.
            // 이 부분이 없으면 CROP할 때 SK W폰에서 다른 이미지를 불러온다.
            // 이유는 모르지만 원본이 소실되거나 DB에서 소실되는 지도 모르겠다.
            inputStream = new FileInputStream(fromFile);
            outputStream = new FileOutputStream(toFile);
            FileChannel fcin = inputStream.getChannel();
            FileChannel fcout = outputStream.getChannel();

            long size = fcin.size();

            fcin.transferTo(0, size, fcout);

            fcout.close();
            fcin.close();
            outputStream.close();
            inputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 그리고 원본은 지운다.
        fromFile.delete();
        // DB상에 남아있는 원본 레코드도 지운다.
        context.getContentResolver().delete(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                MediaStore.Images.ImageColumns._ID + "=" + id,
                null
        );
        return Uri.fromFile(toFile);
    }

    public File generateTempFilePath() {
        File parentDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        //		File parentDir = new File(WsMemoryUtil.getUserContentsTemp()+File.separator+DicKey.Dir_UserContentsFace);
        if (!parentDir.exists()) parentDir.mkdirs();
        File file = new File(parentDir, System.currentTimeMillis() + ".png");
        tempFile = file.getAbsolutePath();
        return file;
    }

    public boolean isRequested(int requestCode) {
        return (requestCode == RequestCode_CameraCrop || requestCode == RequestCode_GalleryCrop || requestCode == RequestCode_Camera || requestCode == RequestCode_Gallery || requestCode == RequestCode_Crop);
    }

    public void onSaveInstanceState(Bundle outState) {
        if (outState == null) return;
        outState.putString(Instance_SaveFile, saveFile);
        outState.putString(Instance_TempFile, tempFile);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState == null) return;
        saveFile = savedInstanceState.getString(Instance_SaveFile);
        tempFile = savedInstanceState.getString(Instance_TempFile);
    }

    public void onDestroy() {
        context = null;
        fragment = null;
    }

    class CropOption {
        public CharSequence title;
        public Drawable icon;
        public Intent appIntent;
    }

    private boolean doCrop() {

        final ArrayList<CropOption> cropOptions = new ArrayList<CropOption>();
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setType("image/*");
        List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent, 0);

        int size = list.size();

        if (size == 0) {
            //crop 수행할 api가 없다.
            return false;
        } else {
            intent.setData(Uri.fromFile(new File(tempFile)));

            intent.putExtra("scale", true);
            intent.putExtra("return-data", true);

            Intent i = new Intent(intent);
            ResolveInfo res = list.get(0);

            i.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            if (fragment != null)
                fragment.startActivityForResult(i, RequestCode_Crop);
            else if (context != null)
                context.startActivityForResult(i, RequestCode_Crop);
        }
        return true;
    }

    @SuppressLint("NewApi")
    public static String getRealPathFromURI(Context context, Intent data) {

        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(data.getData(), proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public File createTempFile4SaveCrop() {
        File tempFile = null;
        try {
            File outputDir = context.getCacheDir();
            tempFile = File.createTempFile("temp", ".png", outputDir);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return tempFile;
    }


}
