package com.windnsoft.tbus;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.windnsoft.tbus.Common.GlobalVars;
import com.windnsoft.tbus.General.WsUtils;

public class CoActivity extends Activity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.company_activity);

        findViewById(R.id.topCloseBtn).setOnClickListener(this);
        findViewById(R.id.bottomNewsBtn).setOnClickListener(this);
        findViewById(R.id.bottomCompanyBtn).setOnClickListener(this);
        findViewById(R.id.bottomInfoBtn).setOnClickListener(this);
        findViewById(R.id.bottomMemberBtn).setOnClickListener(this);
        findViewById(R.id.bottomTalkieBtn).setOnClickListener(this);

        findViewById(R.id.coRestaurantBtn).setOnClickListener(this);
        findViewById(R.id.coTourismBtn).setOnClickListener(this);
        findViewById(R.id.coHotelBtn).setOnClickListener(this);
        findViewById(R.id.coLekaBtn).setOnClickListener(this);
        findViewById(R.id.coShopBtn).setOnClickListener(this);
        findViewById(R.id.coDriveBtn).setOnClickListener(this);
        findViewById(R.id.coShipBtn).setOnClickListener(this);
        findViewById(R.id.coEtcBtn).setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.coRestaurantBtn:
                openList(getResources().getStringArray(R.array.sector)[0]);
                break;
            case R.id.coHotelBtn:
                openList(getResources().getStringArray(R.array.sector)[1]);
                break;
            case R.id.coShopBtn:
                openList(getResources().getStringArray(R.array.sector)[2]);
                break;
            case R.id.coShipBtn:
                openList(getResources().getStringArray(R.array.sector)[3]);
                break;
            case R.id.coTourismBtn:
                openList(getResources().getStringArray(R.array.sector)[4]);
                break;
            case R.id.coLekaBtn:
                openList(getResources().getStringArray(R.array.sector)[5]);
                break;
            case R.id.coDriveBtn:
                openList(getResources().getStringArray(R.array.sector)[6]);
                break;
            case R.id.coEtcBtn:
                openList(getResources().getStringArray(R.array.sector)[7]);
                break;

            case R.id.topCloseBtn:
                finish();
                break;
            case R.id.bottomNewsBtn:
                Intent intent = new Intent(CoActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.bottomCompanyBtn:
                break;
            case R.id.bottomInfoBtn:
                startActivity(new Intent(CoActivity.this, InfoActivity.class));
                finish();
                break;
            case R.id.bottomMemberBtn:
                startActivity(new Intent(CoActivity.this, MemberActivity.class));
                finish();
                break;
            case R.id.bottomTalkieBtn:
                WsUtils.runExternalAppOrGooglePlay(this, GlobalVars.WalieTaliePackageName);
                break;
        }
    }

    void openList(String sector){
        Intent intent = new Intent(CoActivity.this, CoListActivity.class);
        intent.putExtra("sector", sector);
        startActivity(intent);
    }

}
