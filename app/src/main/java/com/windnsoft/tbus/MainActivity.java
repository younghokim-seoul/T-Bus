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
import com.windnsoft.tbus.Common.SharedPreferencesCache;
import com.windnsoft.tbus.Entities.BoardIDs;
import com.windnsoft.tbus.Entities.UserTypes;
import com.windnsoft.tbus.Fragment.BoardListView;
import com.windnsoft.tbus.Fragment.BoardWriteFragment;
import com.windnsoft.tbus.Fragment.BusEstimateListView;
import com.windnsoft.tbus.Fragment.BusEstimateWriteFragment;
import com.windnsoft.tbus.General.DialogDismissListener;
import com.windnsoft.tbus.General.WsLog;
import com.windnsoft.tbus.General.WsUtils;

import java.util.Date;
import java.util.List;

import static com.windnsoft.tbus.Common.GlobalVars.GetAdFullPath;

public class MainActivity extends Activity implements View.OnClickListener {
    static BoardIDs.BoardEntity BoardID;
    BoardListView boardListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int bid = BoardIDs.Free;
        Bundle args = getIntent().getExtras();
        if (args != null && !args.isEmpty() && args.containsKey("bid")) {
            bid = args.getInt("bid");
        }
        BoardID = BoardIDs.BoardMap.get(bid);

        if (bid == BoardIDs.Estimate) {
            setContentView(R.layout.bus_estimate_activity);

        } else {
            setContentView(R.layout.board_activity);
        }

        findViewById(R.id.boardBusTab).setOnClickListener(this);
        findViewById(R.id.boardEstimateTab).setOnClickListener(this);
        findViewById(R.id.boardFreeTab).setOnClickListener(this);
        findViewById(R.id.boardMyTab).setOnClickListener(this);
        findViewById(R.id.boardTractionTab).setOnClickListener(this);
        findViewById(R.id.boardWriteBtn).setOnClickListener(this);
        findViewById(R.id.boardInviteBtn).setOnClickListener(this);
        findViewById(R.id.topCloseBtn).setOnClickListener(this);
        findViewById(R.id.bottomNewsBtn).setOnClickListener(this);
        findViewById(R.id.bottomCompanyBtn).setOnClickListener(this);
        findViewById(R.id.bottomInfoBtn).setOnClickListener(this);
        findViewById(R.id.bottomTalkieBtn).setOnClickListener(this);


        View root = findViewById(R.id.activity_main);
        TextView tab = (TextView) findViewById(R.id.boardFreeTab);
        switch (bid) {
            case BoardIDs.Free:
                break;
            case BoardIDs.Bus:
                tab = (TextView) findViewById(R.id.boardBusTab);
                if (SharedPreferencesCache.getCurrentUser().type != UserTypes.Bus) {
                    findViewById(R.id.boardWriteBtn).setVisibility(View.GONE);
                }
                root.setBackgroundColor(Color.rgb(0xff, 0xf0, 0xf0));
                break;
            case BoardIDs.Estimate:
                tab = (TextView) findViewById(R.id.boardEstimateTab);
                root.setBackgroundColor(Color.rgb(0xf0, 0xff, 0xf0));
                break;
            case BoardIDs.Traction:
                tab = (TextView) findViewById(R.id.boardTractionTab);
                root.setBackgroundColor(Color.rgb(0xff, 0xff, 0xf0));
                break;
            case BoardIDs.My:
                tab = (TextView) findViewById(R.id.boardMyTab);
                findViewById(R.id.boardWriteBtn).setVisibility(View.GONE);
                root.setBackgroundColor(Color.rgb(0xff, 0xf0, 0xff));
                break;
        }
        tab.setBackground(getResources().getDrawable(R.drawable.prg_border_blue_dark));
        tab.setTextColor(Color.WHITE);

        loadAd();
        loadList(bid);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.boardBusTab:
                if(SharedPreferencesCache.getCurrentUser().type != UserTypes.Bus){
                    Toast.makeText(MainActivity.this, "관광버스 회원 전용", Toast.LENGTH_SHORT).show();
                    break;
                }
                intentBoard(BoardIDs.Bus);
                break;
            case R.id.boardEstimateTab:
                intentBoard(BoardIDs.Estimate);
                break;
            case R.id.boardFreeTab:
                intentBoard(BoardIDs.Free);
                break;
            case R.id.boardMyTab:
                intentBoard(BoardIDs.My);
                break;
            case R.id.boardTractionTab:
                intentBoard(BoardIDs.Traction);
                break;
            case R.id.boardWriteBtn:
                openWrite(BoardID.id);
                break;
            case R.id.boardInviteBtn:
                openCallSmsFragment();
                break;
            case R.id.topCloseBtn:
                finish();
                break;
            case R.id.bottomNewsBtn:
                break;
            case R.id.bottomCompanyBtn:
                Intent intent = new Intent(MainActivity.this, CoActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.bottomInfoBtn:
                startActivity(new Intent(MainActivity.this, InfoActivity.class));
                finish();
                break;
            case R.id.bottomTalkieBtn:
                WsUtils.runExternalAppOrGooglePlay(this, GlobalVars.WalieTaliePackageName);
                break;
        }
    }

    public void openWrite(final int boardID) {
        if (boardID == BoardIDs.Estimate) {
            BusEstimateWriteFragment fragment = BusEstimateWriteFragment.newInstance();
            fragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
            fragment.setDismissListener(new DialogDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    loadList(boardID);
                }
            });
            fragment.show(getFragmentManager(), BusEstimateWriteFragment.TAG);
            return;
        }

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

    public void intentBoard(int bid) {
        Intent intent = getIntent();
        intent.putExtra("bid", bid);
        startActivity(intent);
        finish();
    }

    public void loadList(int bid) {
        switch (bid) {
            case BoardIDs.Free:
            case BoardIDs.Bus:
            case BoardIDs.Traction:
            case BoardIDs.My:
                if (boardListView == null) {
                    boardListView = (BoardListView) findViewById(R.id.boardListView);
                    boardListView.setBoardId(BoardID.id);
                }
                boardListView.loadList();
                break;
            case BoardIDs.Estimate:
                  BusEstimateListView busEstimateListView = (BusEstimateListView) findViewById(R.id.busEstimateView);
                if(busEstimateListView!=null) {
                    busEstimateListView.setBoardId(BoardID.id);
                    busEstimateListView.loadList();
                }
                break;
        }

    }

    void loadAd(){
        Date date = new Date();
        String imgPath = GetAdFullPath("1.jpg?"+date.getDay());
        WsLog.e(imgPath);
        final ImageView adImg = (ImageView)findViewById(R.id.advImg);
        final Point screenSize = WsUtils.getDeviceScreenSize(MainActivity.this);
        final int imgWidth = screenSize.x;

        final LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) adImg.getLayoutParams();

        Glide.with(MainActivity.this)
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
                        Toast.makeText(MainActivity.this, "연락처 권한이 거부되어 초대 목록을 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setDeniedMessage("초대할 연락처 목록을 불러오기 위해 주소록 권한이 필요합니다.\n\n[설정] > [권한]에서 권한을 허용해주세요.")
                .setPermissions(Manifest.permission.READ_CONTACTS)
                .check();
    }
}
