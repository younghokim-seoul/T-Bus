package com.windnsoft.tbus;

import android.Manifest;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;
import com.windnsoft.tbus.CallSms.CallSmsListFragment;
import com.windnsoft.tbus.Common.GlobalVars;
import com.windnsoft.tbus.Entities.BoardIDs;
import com.windnsoft.tbus.Entities.MountainListType;
import com.windnsoft.tbus.Fragment.BoardListView;
import com.windnsoft.tbus.Fragment.BoardWriteFragment;
import com.windnsoft.tbus.Fragment.MountainListView;
import com.windnsoft.tbus.Fragment.MountainWriteFragment;
import com.windnsoft.tbus.General.DialogDismissListener;
import com.windnsoft.tbus.General.WsLog;
import com.windnsoft.tbus.General.WsUtils;

import java.util.Date;
import java.util.List;

import static com.windnsoft.tbus.Common.GlobalVars.GetAdFullPath;

public class InfoListActivity extends Activity implements View.OnClickListener {
    static BoardIDs.BoardEntity BoardID;
    BoardListView boardListView;
    MountainListView mountainListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int bid = BoardIDs.Free;
        Bundle args = getIntent().getExtras();
        if (args != null && !args.isEmpty() && args.containsKey("bid")) {
            bid = args.getInt("bid");
        }
        BoardID = BoardIDs.BoardMap.get(bid);
        if (bid == BoardIDs.Mountain) {
            setContentView(R.layout.mountain_list_activity);
            findViewById(R.id.mlBookAllBtn).setOnClickListener(this);
            findViewById(R.id.mlBookDisabledBtn).setOnClickListener(this);
            findViewById(R.id.mlBookEnabledBtn).setOnClickListener(this);

        } else {
            setContentView(R.layout.info_list_activity);
        }

        findViewById(R.id.topCloseBtn).setOnClickListener(this);
        findViewById(R.id.bottomNewsBtn).setOnClickListener(this);
        findViewById(R.id.bottomCompanyBtn).setOnClickListener(this);
        findViewById(R.id.bottomInfoBtn).setOnClickListener(this);
        findViewById(R.id.bottomMemberBtn).setOnClickListener(this);
        findViewById(R.id.bottomTalkieBtn).setOnClickListener(this);
        findViewById(R.id.boardWriteBtn).setOnClickListener(this);
        findViewById(R.id.boardInviteBtn).setOnClickListener(this);



        ((TextView) findViewById(R.id.ilTitleTxt)).setText(BoardID.text);

        loadList(bid);
        loadAd();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.boardInviteBtn:
                openCallSmsFragment();
                break;

            case R.id.boardWriteBtn:
                openWrite(BoardID.id);
                break;

            case R.id.topCloseBtn:
                finish();
                break;
            case R.id.bottomNewsBtn:
                startActivity(new Intent(InfoListActivity.this, MainActivity.class));
                finish();
                break;
            case R.id.bottomCompanyBtn:
                Intent intent = new Intent(InfoListActivity.this, CoActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.bottomInfoBtn:
                startActivity(new Intent(InfoListActivity.this, InfoActivity.class));
                finish();
                break;
            case R.id.bottomMemberBtn:
                startActivity(new Intent(InfoListActivity.this, MemberActivity.class));
                finish();
                break;
            case R.id.bottomTalkieBtn:
                WsUtils.runWalkieTalkieApp(this);
                break;

            case R.id.mlBookAllBtn:
                loadMountainList(MountainListType.All);
                break;
            case R.id.mlBookDisabledBtn:
                loadMountainList(MountainListType.BookDisabled);
                break;
            case R.id.mlBookEnabledBtn:
                loadMountainList(MountainListType.BookEnabled);
                break;
        }
    }

    public void openWrite(final int boardID) {
        if (boardID == BoardIDs.Mountain) {
            MountainWriteFragment fragment = MountainWriteFragment.newInstance(boardID);
            fragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
            fragment.setDismissListener(new DialogDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    loadList(boardID);
                }
            });
            fragment.show(getFragmentManager(), MountainWriteFragment.TAG);

        } else {
            BoardWriteFragment fragment = BoardWriteFragment.newInstance(boardID);
            fragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
            fragment.setDismissListener(new DialogDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    loadList(boardID);
                }
            });
            fragment.show(getFragmentManager(), BoardWriteFragment.TAG);
        }
    }

    public void loadList(int bid) {
        switch (bid) {
            case BoardIDs.Deals:
            case BoardIDs.Dispense:
            case BoardIDs.Interdiction:
            case BoardIDs.Festival:
            case BoardIDs.News:
            case BoardIDs.Traction:
            case BoardIDs.Traffic:
                if (boardListView == null) {
                    boardListView = (BoardListView) findViewById(R.id.boardListView);
                    boardListView.setBoardId(BoardID.id);
                }
                boardListView.loadList();
                break;
            case BoardIDs.Mountain:
                if (mountainListView == null)
                    mountainListView = (MountainListView) findViewById(R.id.mountainListView);
                if (mountainListView != null) {
                    mountainListView.setBoardId(BoardID.id);
                    loadMountainList(MountainListType.All);
                }
                break;
        }
    }

    void loadMountainList(int type) {
        if (mountainListView == null) {
            mountainListView = (MountainListView) findViewById(R.id.mountainListView);
            mountainListView.setBoardId(BoardID.id);
        }
        mountainListView.loadList(type);
        TextView allBtn = (TextView) findViewById(R.id.mlBookAllBtn);
        TextView enableBtn = (TextView) findViewById(R.id.mlBookEnabledBtn);
        TextView disableBtn = (TextView) findViewById(R.id.mlBookDisabledBtn);
        switch (type) {
            case MountainListType.All:
                allBtn.setTextColor(Color.WHITE);
                enableBtn.setTextColor(Color.BLACK);
                disableBtn.setTextColor(Color.BLACK);
                break;
            case MountainListType.BookEnabled:
                allBtn.setTextColor(Color.BLACK);
                enableBtn.setTextColor(Color.WHITE);
                disableBtn.setTextColor(Color.BLACK);
                break;
            case MountainListType.BookDisabled:
                allBtn.setTextColor(Color.BLACK);
                enableBtn.setTextColor(Color.BLACK);
                disableBtn.setTextColor(Color.WHITE);
                break;
        }
    }

    void loadAd(){
        Date date = new Date();
        String imgPath = GetAdFullPath("3.jpg?"+date.getDay());
        WsLog.e(imgPath);
        final ImageView adImg = (ImageView)findViewById(R.id.advImg);
        final Point screenSize = WsUtils.getDeviceScreenSize(InfoListActivity.this);
        final int imgWidth = screenSize.x;

        final LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) adImg.getLayoutParams();

        Glide.with(InfoListActivity.this)
                .asBitmap()
                .load(imgPath)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap bitmap, @Nullable Transition<? super Bitmap> transition) {
                        final int imgHeight = imgWidth * bitmap.getHeight() / bitmap.getWidth();
                        params.width = imgWidth;
                        params.height = imgHeight;
                        adImg.setLayoutParams(params);
                        adImg.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        adImg.setImageBitmap(bitmap);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                    }
                });
    }

    private void openCallSmsFragment() {
        TedPermission.create()
                .setPermissionListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        Fragment f = getFragmentManager().findFragmentByTag(CallSmsListFragment.TAG);
                        if (f != null) {
                            getFragmentManager().beginTransaction().remove(f).commit();
                        }

                        CallSmsListFragment fragment = CallSmsListFragment.newInstance();
                        fragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
                        fragment.show(getFragmentManager(), CallSmsListFragment.TAG);
                    }

                    @Override
                    public void onPermissionDenied(List<String> deniedPermissions) {
                        Toast.makeText(InfoListActivity.this, "연락처 권한이 거부되어 초대 목록을 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setDeniedMessage("초대할 연락처 목록을 불러오기 위해 주소록 권한이 필요합니다.\n\n[설정] > [권한]에서 권한을 허용해주세요.")
                .setPermissions(Manifest.permission.READ_CONTACTS)
                .check();
    }
}
