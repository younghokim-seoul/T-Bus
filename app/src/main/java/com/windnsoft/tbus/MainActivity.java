package com.windnsoft.tbus;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.utils.DiskCacheUtils;
import com.bumptech.glide.Glide;
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
import com.windnsoft.tbus.General.WsBitmapTools;
import com.windnsoft.tbus.General.WsLog;
import com.windnsoft.tbus.General.WsUtils;

import java.io.File;
import java.util.Date;

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
        ImageLoader imageLoader = ImageLoader.getInstance();
        Date date = new Date();
        String imgPath = GetAdFullPath("1.jpg?"+date.getDay());
        WsLog.e(imgPath);
        final File image = DiskCacheUtils.findInCache(imgPath, imageLoader.getDiskCache());
        final ImageView adImg = (ImageView)findViewById(R.id.advImg);
        final Point screenSize = WsUtils.getDeviceScreenSize(MainActivity.this);
        final int imgWidth = screenSize.x;

        final LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) adImg.getLayoutParams();

        if (image != null && image.exists()) {
            Point bitmapSize = WsBitmapTools.getBitmapSize(image.getPath());
            final int imgHeight = imgWidth * bitmapSize.y / bitmapSize.x;
            params.width = imgWidth;
            params.height = imgHeight;
            adImg.setLayoutParams(params);
            Glide.with(MainActivity.this).load(image).centerCrop().into(adImg);

        } else {
            imageLoader.loadImage(imgPath, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String s, View view) {
//                            holder.profile.setImageBitmap(null);
                }

                @Override
                public void onLoadingFailed(String s, View view, FailReason failReason) {

                }

                @Override
                public void onLoadingComplete(String s, View view, final Bitmap bitmap) {

                    final int imgHeight = imgWidth * bitmap.getHeight()/ bitmap.getWidth();
                    params.width = imgWidth;
                    params.height = imgHeight;
                    adImg.setLayoutParams(params);

                    Glide.with(MainActivity.this).load(s).centerCrop().into(adImg);

                }

                @Override
                public void onLoadingCancelled(String s, View view) {

                }
            });
        }
    }

    private void openCallSmsFragment() {
        Fragment f = getFragmentManager().findFragmentByTag(CallSmsListFragment.TAG);
        if (f instanceof CallSmsListFragment) {
            getFragmentManager().popBackStackImmediate(f.getClass().getName(), 0);
        } else {

            CallSmsListFragment fragment = CallSmsListFragment.newInstance();
            fragment.setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Dialog);
            fragment.show(getFragmentManager(), CallSmsListFragment.TAG);
        }
    }
}
