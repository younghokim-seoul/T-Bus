package com.windnsoft.tbus.General;

import android.app.Dialog;
import android.content.Context;
import android.view.ViewGroup.LayoutParams;
import android.widget.ProgressBar;

import com.windnsoft.tbus.R;


public class CreateProgressDialog extends Dialog {
//    private Context context;
//    private View view;
//    private LayoutParams layoutParams;

    public static CreateProgressDialog show(Context context) {
        return show(context, null, null);
    }

    public static CreateProgressDialog show(Context context, CharSequence title, CharSequence message) {
        return show(context, title, message, false);
    }

    public static CreateProgressDialog show(Context context, CharSequence title, CharSequence message, boolean indeterminate) {
        return show(context, title, message, indeterminate, false, null);
    }

    public static CreateProgressDialog show(Context context, CharSequence title, CharSequence message, boolean indeterminate, boolean cancelable) {
        return show(context, title, message, indeterminate, cancelable, null);
    }

    public static CreateProgressDialog show(Context context, CharSequence title, CharSequence message, boolean indeterminate, boolean cancelable, OnCancelListener cancelListener) {
        CreateProgressDialog dialog = new CreateProgressDialog(context);

        dialog.setTitle(title);
        dialog.setCancelable(cancelable);
        dialog.setOnCancelListener(cancelListener);
            /* The next line will add the ProgressBar to the dialog. */

        dialog.addContentView(new ProgressBar(context), new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        dialog.showSuper();

        return dialog;
    }

    public CreateProgressDialog(Context context) {
        super(context, R.style.NewDialog);
//        this.context = context;
//        this.view =
//        this.layoutParams =
        addContentView(new ProgressBar(context), new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
    }

    public void show() {
        super.show();
    }

    public void showSuper() {
        super.show();
    }
}
