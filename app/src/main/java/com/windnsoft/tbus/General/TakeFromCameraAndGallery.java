package com.windnsoft.tbus.General;

import android.Manifest;
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
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

    private Activity getActiveActivity() {
        if (fragment != null && fragment.getActivity() != null) {
            return fragment.getActivity();
        }
        return context;
    }

    public static void runWithMediaPermission(final Context ctx, final Runnable onGranted) {
        String mediaPermission = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                ? Manifest.permission.READ_MEDIA_IMAGES
                : Manifest.permission.READ_EXTERNAL_STORAGE;

        TedPermission.create()
                .setPermissionListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        if (onGranted != null) {
                            onGranted.run();
                        }
                    }

                    @Override
                    public void onPermissionDenied(List<String> deniedPermissions) {
                        if (ctx != null) {
                            Toast.makeText(ctx, "사진 접근 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setDeniedMessage("사진을 첨부하기 위해 사진/미디어 접근 권한이 필요합니다.\n\n[설정] > [권한]에서 권한을 허용해주세요.")
                .setPermissions(mediaPermission)
                .check();
    }

    public static void runWithCameraPermission(final Context ctx, final Runnable onGranted) {
        TedPermission.create()
                .setPermissionListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        if (onGranted != null) {
                            onGranted.run();
                        }
                    }

                    @Override
                    public void onPermissionDenied(List<String> deniedPermissions) {
                        if (ctx != null) {
                            Toast.makeText(ctx, "카메라 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setDeniedMessage("사진 촬영을 위해 카메라 권한이 필요합니다.\n\n[설정] > [권한]에서 권한을 허용해주세요.")
                .setPermissions(Manifest.permission.CAMERA)
                .check();
    }

    private Uri getSafeUriForFile(File file) {
        Activity act = getActiveActivity();
        if (act == null) {
            return Uri.fromFile(file);
        }
        try {
            return FileProvider.getUriForFile(act, act.getPackageName() + ".fileprovider", file);
        } catch (Exception e) {
            return Uri.fromFile(file);
        }
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
        runWithCameraPermission(getActiveActivity(), new Runnable() {
            @Override
            public void run() {
                File out = (null != tempFile && !"".equals(tempFile)) ? new File(tempFile) : generateTempFilePath();
                Uri uri = getSafeUriForFile(out);

                Intent intent = new Intent();
                intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra("crop", "true");
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                intent.putExtra("outputFormat", CompressFormat.PNG.toString());
                intent.putExtra("return-data", true);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                if (fragment != null)
                    fragment.startActivityForResult(intent, RequestCode_CameraCrop);
                else if (context != null)
                    context.startActivityForResult(intent, RequestCode_CameraCrop);
            }
        });
    }

    public void startIntentGalleryWithCrop() {
        runWithMediaPermission(getActiveActivity(), new Runnable() {
            @Override
            public void run() {
                File out = (null != tempFile && !"".equals(tempFile)) ? new File(tempFile) : generateTempFilePath();
                Uri uri = getSafeUriForFile(out);

                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.putExtra("crop", "true");
                intent.putExtra("outputFormat", CompressFormat.PNG.toString());
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                if (fragment != null)
                    fragment.startActivityForResult(Intent.createChooser(intent, "Choice"), RequestCode_GalleryCrop);
                else if (context != null)
                    context.startActivityForResult(Intent.createChooser(intent, "Choice"), RequestCode_GalleryCrop);
            }
        });
    }

    public void startIntentCamera() {
        runWithCameraPermission(getActiveActivity(), new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (fragment != null)
                    fragment.startActivityForResult(intent, RequestCode_Camera);
                else if (context != null)
                    context.startActivityForResult(intent, RequestCode_Camera);
            }
        });
    }

    public void startIntentGallery() {
        runWithMediaPermission(getActiveActivity(), new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                if (fragment != null)
                    fragment.startActivityForResult(intent, RequestCode_Gallery);
                else if (context != null)
                    context.startActivityForResult(intent, RequestCode_Gallery);
            }
        });
    }

    public void startIntentCrop(Uri orgUri, Map<String, Integer> extraSet) {
        File out = (null != tempFile && !"".equals(tempFile)) ? new File(tempFile) : generateTempFilePath();
        Activity act = getActiveActivity();
        if (act != null && orgUri != null) {
            copyUriToFile(act, orgUri, out);
        }
        Uri uri = getSafeUriForFile(out);

        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(orgUri, "image/*");
        intent.putExtra("crop", "true");

        if (extraSet != null) {
            for (String key : extraSet.keySet()) {
                intent.putExtra(key, extraSet.get(key));
            }
        }
        intent.putExtra("scale", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        intent.putExtra("return-data", true);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        if (fragment != null)
            fragment.startActivityForResult(intent, RequestCode_Crop);
        else if (context != null)
            context.startActivityForResult(intent, RequestCode_Crop);
    }

    private void copyUriToFile(Context ctx, Uri srcUri, File dstFile) {
        try (InputStream in = ctx.getContentResolver().openInputStream(srcUri);
             OutputStream out = new FileOutputStream(dstFile)) {
            if (in == null) return;
            byte[] buf = new byte[8192];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } catch (Exception e) {
            WsLog.e("copyUriToFile failed: " + e.getMessage());
        }
    }

    public Uri getResultUri(Intent data) {
        Uri ret = null;
        String savedFileString = null;

        if (null != tempFile && !"".equals(tempFile)) {
            // save된 file이 있다면
            File temp = new File(tempFile);
            if (temp.exists() && temp.length() > 0) {
                savedFileString = tempFile;
            }
            tempFile = null;
        }

        if (null == savedFileString && null != data) {
            // data로 uri 가 반환된다면
            Uri uriData = data.getData();
            if (null != uriData) {
                Activity act = getActiveActivity();
                if (act != null) {
                    String realPath = WsUtils.getRealPathFromURI(uriData, act);
                    if (realPath != null && new File(realPath).length() > 0) {
                        savedFileString = realPath;
                    }
                }
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

        if (null == savedFileString && null != data && null != (extras = data.getExtras()) && null != (photo = extras.getParcelable("data"))) {
            if (photo.getWidth() > 0 && photo.getHeight() > 0) {
                File temp = generateTempFilePath();
                WsBitmapTools.saveBitmap(photo, temp.getAbsolutePath());

                savedFileString = temp.getAbsolutePath();
            }
        }

        if (savedFileString != null) {
            File file = new File(savedFileString);
            if (file.exists() && file.length() > 0)
                ret = Uri.fromFile(file);
        }

        return ret;
    }

    @SuppressWarnings("deprecation")
    private Uri getSamsungPhoneUri() {
        Uri imageCaptureUri = null;
        Activity act = getActiveActivity();
        if (act == null) return null;

        try {
            Log.i("TAG", "inside Samsung Phones");
            String[] projection = {
                    MediaStore.Images.Thumbnails._ID, // The columns we want
                    MediaStore.Images.Thumbnails.IMAGE_ID,
                    MediaStore.Images.Thumbnails.KIND,
                    MediaStore.Images.Thumbnails.DATA};
            String selection = MediaStore.Images.Thumbnails.KIND + "=" + MediaStore.Images.Thumbnails.MINI_KIND;

            String sort = MediaStore.Images.Thumbnails._ID + " DESC";

            Cursor myCursor = act.managedQuery(
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
            myCursor = act.managedQuery(
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
        Activity act = getActiveActivity();
        File toFile = new File(tempFile);
        File fromFile = null;
        FileInputStream inputStream = null;
        FileOutputStream outputStream = null;

        Uri uriImages = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        int id = -1;

        String[] IMAGE_PROJECTION = {
                MediaStore.Images.ImageColumns.DATA,
                MediaStore.Images.ImageColumns._ID,
        };
        try {
            Cursor cursorImages = act.getContentResolver().query(uriImages, IMAGE_PROJECTION, null, null, null);
            if (cursorImages != null && cursorImages.moveToLast()) {
                fromFile = new File(cursorImages.getString(0));
                id = cursorImages.getInt(1);
                cursorImages.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        toFile = new File(act.getExternalFilesDir(Environment.DIRECTORY_PICTURES), System.currentTimeMillis() + ".jpg");

        try {
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
        if (fromFile != null) {
            fromFile.delete();
        }
        act.getContentResolver().delete(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                MediaStore.Images.ImageColumns._ID + "=" + id,
                null
        );
        return Uri.fromFile(toFile);
    }

    public File generateTempFilePath() {
        Activity act = getActiveActivity();
        File parentDir = act.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
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
        Activity act = getActiveActivity();
        final ArrayList<CropOption> cropOptions = new ArrayList<CropOption>();
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setType("image/*");
        List<ResolveInfo> list = act.getPackageManager().queryIntentActivities(intent, 0);

        int size = list.size();

        if (size == 0) {
            //crop 수행할 api가 없다.
            return false;
        } else {
            intent.setData(getSafeUriForFile(new File(tempFile)));

            intent.putExtra("scale", true);
            intent.putExtra("return-data", true);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

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
        if (data == null || data.getData() == null) return null;
        if (context instanceof Activity) {
            return WsUtils.getRealPathFromURI(data.getData(), (Activity) context);
        }
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
            File outputDir = getActiveActivity().getCacheDir();
            tempFile = File.createTempFile("temp", ".png", outputDir);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return tempFile;
    }
}
