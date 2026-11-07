package com.windnsoft.tbus.General;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Point;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.windnsoft.tbus.Common.GlobalVars;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Created by Windn(Windnsoft@gmail.com) on 2016.
 */

public class WsUtils {
    public static int getRelativeLeft(View myView) {
        if (myView.getParent() == myView.getRootView())
            return myView.getLeft();
        else
            return myView.getLeft() + getRelativeLeft((View) myView.getParent());
    }

    public static int getRelativeTop(View myView) {
        if (myView.getParent() == myView.getRootView())
            return myView.getTop();
        else
            return myView.getTop() + getRelativeTop((View) myView.getParent());
    }

    public static int getStatusBarHeight(Resources resources) {
        int result = 0;
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static String getRealPathFromURI(Uri uri, Activity context) {
        if (uri == null || context == null) return null;
        if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        try (InputStream in = context.getContentResolver().openInputStream(uri)) {
            if (in != null) {
                File tempFile = new File(context.getCacheDir(), "upload_" + System.currentTimeMillis() + ".jpg");
                try (OutputStream out = new FileOutputStream(tempFile)) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                }
                if (tempFile.exists() && tempFile.length() > 0) {
                    return tempFile.getAbsolutePath();
                }
            }
        } catch (Exception e) {
            WsLog.e("Failed to copy URI to cache file: " + e.getMessage());
        }

        String[] projection = {MediaStore.Images.Media.DATA};
        try (Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                return cursor.getString(column_index);
            }
        } catch (Exception e) {
            WsLog.e("Failed to query MediaStore DATA: " + e.getMessage());
        }
        return null;
    }

    public static void showKeyboard(Activity context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
//        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        imm.showSoftInput(view, 0);
    }

    public static void hideKeyboard(Activity context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
//        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static boolean isKeayboardShown(View activityRootView, Context context) {
        boolean result = false;
        int heightDiff = activityRootView.getRootView().getHeight() - activityRootView.getHeight();
        if (heightDiff > dpToPx(context, 200)) {
            result = true;
        }
        return result;
    }

    public static float dpToPx(Context context, float valueInDp) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInDp, metrics);
    }

    public static boolean isExistExternalApp(Context context, String packageName) {
        if (context == null || packageName == null) {
            return false;
        }
        try {
            context.getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException ignored) {
        } catch (Exception ignored) {
        }

        PackageManager pkgMgr = context.getPackageManager();
        try {
            Intent launchIntent = pkgMgr.getLaunchIntentForPackage(packageName);
            if (launchIntent != null) {
                return true;
            }
        } catch (Exception ignored) {
        }

        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        try {
            List<ResolveInfo> mApps = pkgMgr.queryIntentActivities(mainIntent, 0);
            for (int i = 0; i < mApps.size(); i++) {
                if (mApps.get(i).activityInfo.packageName.startsWith(packageName)) {
                    return true;
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    public static void runExternalAppOrMarket(Context context, String packageName, String fallbackUrl) {
        if (context == null) return;
        try {
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                return;
            }
        } catch (Exception ignored) {
        }

        try {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUrl));
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        } catch (Exception e) {
            Toast.makeText(context, "스토어로 이동할 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    public static void runWalkieTalkieApp(Context context) {
        runExternalAppOrMarket(context, GlobalVars.WalieTaliePackageName, GlobalVars.WalieTalieOneStoreUrl);
    }

    public static void runExternalAppOrGooglePlay(Context context, String packageName) {
        runExternalAppOrMarket(context, packageName, "market://details?id=" + packageName);
    }

    public static void phoneCall(Context context, String number) {
        if (number == null || number.trim().isEmpty()) {
            Toast.makeText(context, "등록된 전화번호가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", number.trim(), null));
        startCommunicationActivity(context, intent, "전화 앱을 실행할 수 없습니다.");
    }

    public static boolean composeSms(Context context, String number, String message) {
        if (number == null || number.trim().isEmpty()) {
            Toast.makeText(context, "등록된 전화번호가 없습니다.", Toast.LENGTH_SHORT).show();
            return false;
        }
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("smsto", number.trim(), null));
        if (message != null) {
            intent.putExtra("sms_body", message);
        }
        return startCommunicationActivity(context, intent, "문자 앱을 실행할 수 없습니다.");
    }

    private static boolean startCommunicationActivity(Context context, Intent intent, String errorMessage) {
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        try {
            context.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException | SecurityException e) {
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public static Point getDeviceScreenSize(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return new Point(dm.widthPixels, dm.heightPixels);
    }

    public static String formatBoardDate(String rawTime) {
        if (rawTime == null || rawTime.trim().isEmpty()) {
            return "";
        }
        String trimmed = rawTime.trim();
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d{4})[-/.년\\s]+(\\d{1,2})[-/.월\\s]+(\\d{1,2})[일\\sT]+(\\d{1,2})[:시\\s]+(\\d{1,2})");
        java.util.regex.Matcher matcher = pattern.matcher(trimmed);
        if (matcher.find()) {
            try {
                int yyyy = Integer.parseInt(matcher.group(1));
                int MM = Integer.parseInt(matcher.group(2));
                int dd = Integer.parseInt(matcher.group(3));
                int HH = Integer.parseInt(matcher.group(4));
                int mm = Integer.parseInt(matcher.group(5));
                return String.format(java.util.Locale.KOREA, "%04d년 %02d월 %02d일 %02d시 %02d분", yyyy, MM, dd, HH, mm);
            } catch (Exception ignored) {
            }
        }
        String[] patterns = new String[] {
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd HH:mm",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm"
        };
        for (String p : patterns) {
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(p, java.util.Locale.KOREA);
                java.util.Date date = sdf.parse(trimmed);
                if (date != null) {
                    java.text.SimpleDateFormat outSdf = new java.text.SimpleDateFormat("yyyy년 MM월 dd일 HH시 mm분", java.util.Locale.KOREA);
                    return outSdf.format(date);
                }
            } catch (Exception ignored) {
            }
        }
        return trimmed;
    }
}
