package com.windnsoft.tbus;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.windnsoft.tbus.Common.GlobalVars;
import com.windnsoft.tbus.Fragment.AdBannerSliderView;
import com.windnsoft.tbus.Fragment.CompanyListView;
import com.windnsoft.tbus.General.WsLog;
import com.windnsoft.tbus.General.WsUtils;

import java.util.Date;

import static com.windnsoft.tbus.Common.GlobalVars.GetAdFullPath;
import static com.windnsoft.tbus.R.layout.spinner;

public class CoListActivity extends Activity implements View.OnClickListener {

    CompanyListView listView;

    String sector="";
    long locale1;
    long locale2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.company_list_activity);

        findViewById(R.id.topCloseBtn).setOnClickListener(this);
        findViewById(R.id.bottomNewsBtn).setOnClickListener(this);
        findViewById(R.id.bottomCompanyBtn).setOnClickListener(this);
        findViewById(R.id.bottomInfoBtn).setOnClickListener(this);
        findViewById(R.id.bottomMemberBtn).setOnClickListener(this);
        findViewById(R.id.bottomTalkieBtn).setOnClickListener(this);

        Bundle args = getIntent().getExtras();
        if (args != null && !args.isEmpty() && args.containsKey("sector")) {
            sector = args.getString("sector");
        }
        listView = (CompanyListView) findViewById(R.id.coUserListView);

        final Spinner localeSpinner1 = (Spinner) findViewById(R.id.coLocale1Sp);
        final Spinner localeSpinner2 = (Spinner) findViewById(R.id.coLocale2Sp);
        localeSpinner1.setAdapter(ArrayAdapter.createFromResource(CoListActivity.this, R.array.locale, spinner));
        localeSpinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                locale1 = id;

                int r = R.array.localeSeoul;
                switch (position) {
                    case 0:
                        r = R.array.localeSeoul;
                        break;
                    case 1:
                        r = R.array.localeBusan;
                        break;
                    case 2:
                        r = R.array.localeDaegu;
                        break;
                    case 3:
                        r = R.array.localeIncheon;
                        break;
                    case 4:
                        r = R.array.localeGwangju;
                        break;
                    case 5:
                        r = R.array.localeDaejeon;
                        break;
                    case 6:
                        r = R.array.localeUlsan;
                        break;
                    case 7:
                        r = R.array.localeGyeonggi;
                        break;
                    case 8:
                        r = R.array.localeGangwon;
                        break;
                    case 9:
                        r = R.array.localeChungcheongbuk;
                        break;
                    case 10:
                        r = R.array.localeChungcheongnam;
                        break;
                    case 11:
                        r = R.array.localeJeollabuk;
                        break;
                    case 12:
                        r = R.array.localeJeollanam;
                        break;
                    case 13:
                        r = R.array.localeGyeongsangbuk;
                        break;
                    case 14:
                        r = R.array.localeGyeongsangnam;
                        break;
                    case 15:
                        r = R.array.localeJeju;
                        break;
                }
                localeSpinner2.setAdapter(ArrayAdapter.createFromResource(CoListActivity.this, r, R.layout.spinner));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        localeSpinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                locale2 = l;
                String selectedLocale1 = localeSpinner1.getSelectedItem() != null ? localeSpinner1.getSelectedItem().toString().trim() : "";
                String selectedLocale2 = localeSpinner2.getSelectedItem() != null ? localeSpinner2.getSelectedItem().toString().trim() : "";

                String locale;
                String displayLocale;
                if (selectedLocale2.isEmpty() || selectedLocale2.contains("전체") || i == 0) {
                    locale = selectedLocale1;
                    displayLocale = selectedLocale1;
                } else {
                    locale = selectedLocale1 + " " + selectedLocale2;
                    displayLocale = locale;
                }
                listView.loadList(sector, locale, displayLocale);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        loadAd();

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.topCloseBtn:
                finish();
                break;
            case R.id.bottomNewsBtn:
                startActivity(new Intent(CoListActivity.this, MainActivity.class));
                finish();
                break;
            case R.id.bottomCompanyBtn:
                Intent intent = new Intent(CoListActivity.this, CoActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.bottomInfoBtn:
                startActivity(new Intent(CoListActivity.this, InfoActivity.class));
                finish();
                break;
            case R.id.bottomMemberBtn:
                startActivity(new Intent(CoListActivity.this, MemberActivity.class));
                finish();
                break;
            case R.id.bottomTalkieBtn:
                WsUtils.runWalkieTalkieApp(this);
                break;
        }
    }

    void loadAd(){
        final AdBannerSliderView adImg = (AdBannerSliderView) findViewById(R.id.advImg);
        if (adImg != null) {
            adImg.loadAds(2);
        }
    }
}
