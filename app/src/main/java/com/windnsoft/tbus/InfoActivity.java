package com.windnsoft.tbus;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.windnsoft.tbus.Common.GlobalVars;
import com.windnsoft.tbus.Entities.BoardIDs;
import com.windnsoft.tbus.General.WsUtils;

public class InfoActivity extends Activity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info_activity);

        findViewById(R.id.topCloseBtn).setOnClickListener(this);
        findViewById(R.id.bottomNewsBtn).setOnClickListener(this);
        findViewById(R.id.bottomCompanyBtn).setOnClickListener(this);
        findViewById(R.id.bottomInfoBtn).setOnClickListener(this);
        findViewById(R.id.bottomMemberBtn).setOnClickListener(this);
        findViewById(R.id.bottomTalkieBtn).setOnClickListener(this);

        findViewById(R.id.infDealBtn).setOnClickListener(this);
        findViewById(R.id.infDispatchBtn).setOnClickListener(this);
        findViewById(R.id.infFestivalBtn).setOnClickListener(this);
        findViewById(R.id.infIntermittentBtn).setOnClickListener(this);
        findViewById(R.id.infMountainBtn).setOnClickListener(this);
        findViewById(R.id.infNewsBtn).setOnClickListener(this);
        findViewById(R.id.infTrafficBtn).setOnClickListener(this);


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.infDealBtn:
                intentBoard(BoardIDs.Deals);
                break;
            case R.id.infDispatchBtn:
                intentBoard(BoardIDs.Dispense);
                break;
            case R.id.infFestivalBtn:
                intentBoard(BoardIDs.Festival);
                break;
            case R.id.infIntermittentBtn:
                intentBoard(BoardIDs.Interdiction);
                break;
            case R.id.infMountainBtn:
                intentBoard(BoardIDs.Mountain);
                break;
            case R.id.infNewsBtn:
                intentBoard(BoardIDs.News);
                break;
            case R.id.infTrafficBtn:
                intentBoard(BoardIDs.Traffic);
                break;

            case R.id.topCloseBtn:
                finish();
                break;
            case R.id.bottomNewsBtn:
                Intent intent = new Intent(InfoActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.bottomCompanyBtn:
                startActivity(new Intent(InfoActivity.this, CoActivity.class));
                finish();
                break;
            case R.id.bottomInfoBtn:
                break;
            case R.id.bottomMemberBtn:
                startActivity(new Intent(InfoActivity.this, MemberActivity.class));
                finish();
                break;
            case R.id.bottomTalkieBtn:
                WsUtils.runWalkieTalkieApp(this);
                break;
        }
    }

    public void intentBoard(int bid) {
        Intent intent = new Intent(InfoActivity.this, InfoListActivity.class);
        intent.putExtra("bid", bid);
        startActivity(intent);
//        finish();
    }

}
