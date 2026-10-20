package com.windnsoft.tbus.Fragment;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.windnsoft.tbus.Entities.UserEntity;
import com.windnsoft.tbus.General.DialogDismissListener;
import com.windnsoft.tbus.General.WsLog;
import com.windnsoft.tbus.General.WsUtils;
import com.windnsoft.tbus.R;

import java.util.List;

import static com.windnsoft.tbus.Common.GlobalVars.GetPictureFullPath;

/**
 * Created by Windn(Windnsoft@gmail.com) on 2017.
 */

public class CompanyDetailFragment extends DialogFragment implements View.OnClickListener {
    public final static String TAG = "CompanyDetailFragment";

    UserEntity entity;

    DialogDismissListener onDismissListener;

    public CompanyDetailFragment() {
    }

    public static CompanyDetailFragment newInstance(UserEntity item) {
        CompanyDetailFragment fragment = new CompanyDetailFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        fragment.setEntity(item);
        return fragment;
    }

    public void setEntity(UserEntity item) {
        entity = item;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (!args.isEmpty()) {
//            this.bid = args.getInt("bid");
//            board = BoardIDs.BoardMap.get(bid);
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        }

        try {
            WsLog.e(entity.locale + "// " + entity.addr);
            Geocoder geocoder = new Geocoder(getActivity());
            final List<Address> addresses = geocoder.getFromLocationName(entity.locale + " " + entity.addr, 1);

            if (addresses != null && addresses.size() > 0) {
                MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.cvMapView);
                mapFragment.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap googleMap) {
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude()));
                        googleMap.addMarker(markerOptions);

                        googleMap.moveCamera(CameraUpdateFactory.newLatLng(markerOptions.getPosition()));
                        googleMap.animateCamera(CameraUpdateFactory.zoomTo(14));
                    }
                });
            } else {
                Toast.makeText(getActivity(), "주소를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(getActivity(), "구글맵에 연결할 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
//        loadReplyList(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.company_detail, container, false);
        view.findViewById(R.id.bvBackBtn).setOnClickListener(this);
        view.findViewById(R.id.cdCallBtn).setOnClickListener(this);

        ((TextView) view.findViewById(R.id.bvTitle)).setText(entity.company_name);
        ((TextView) view.findViewById(R.id.bvWriterNameTxt)).setText(entity.company_name);
        ((TextView) view.findViewById(R.id.bvWriterDateTxt)).setText(entity.time);

        ((TextView) view.findViewById(R.id.cvProductTxt)).setText(entity.product);
        ((TextView) view.findViewById(R.id.cvAddrTxt)).setText(entity.locale+" "+entity.addr);
        ((EditText) view.findViewById(R.id.cvManagerNameTxt)).setText(entity.manager_name);
        ((EditText) view.findViewById(R.id.cvManagerTelTxt)).setText(entity.manager_tel);
        ((EditText) view.findViewById(R.id.cvContactTxt)).setText(entity.contact);
        ((EditText) view.findViewById(R.id.cvUsageTxt)).setText(entity.usage);
        ((EditText) view.findViewById(R.id.cvMenuTxt)).setText(entity.menu);
        ((EditText) view.findViewById(R.id.cvCapacityTxt)).setText("" + entity.capacity);
        ((EditText) view.findViewById(R.id.cvTimeTxt)).setText(entity.time);
        ((EditText) view.findViewById(R.id.cvClosedTxt)).setText(entity.closed);
        ((EditText) view.findViewById(R.id.cvParkingTxt)).setText(entity.parking);
        ((EditText) view.findViewById(R.id.cvBookingTxt)).setText(entity.booking ? "가능" : "불가능");
        ((EditText) view.findViewById(R.id.cvDetailTxt)).setText(entity.detail);
        ImageView ivWriter = (ImageView) view.findViewById(R.id.bvWriterImg);
        if (entity.profile != null && !entity.profile.isEmpty()) {
            Glide.with(getActivity()).load(GetPictureFullPath(entity.profile)).into(ivWriter);
        }

        if (entity.photoList != null && entity.photoList.size() > 0) {
            LinearLayout photoLO = (LinearLayout) view.findViewById(R.id.cvPhotosLo);
            Point screenSize = WsUtils.getDeviceScreenSize(getActivity());
            final int imgHeight = screenSize.y / 5*2;
            final int imgWidth = screenSize.x;
            for (String img : entity.photoList) {
                final ImageView iv = new ImageView(getActivity());
                photoLO.addView(iv);
                final LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) iv.getLayoutParams();
                params.width = imgWidth;
                params.height = imgHeight;
                iv.setLayoutParams(params);

                String imgPath = GetPictureFullPath(img);
                Glide.with(getActivity()).load(imgPath).centerCrop().into(iv);
            }
        }


        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bvBackBtn:
                dismiss();
                break;
            case R.id.cdCallBtn:
                if (entity != null)
                    WsUtils.phoneCall(getActivity(), entity.tel);
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
    }

    @Override
    public void onPause() {
        super.onPause();
        WsUtils.hideKeyboard(getActivity(), getView());

    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        Fragment fragment = getFragmentManager().findFragmentById(R.id.cvMapView);
        if(fragment != null)
            getFragmentManager().beginTransaction().remove(fragment).commit();
        if (onDismissListener != null)
            onDismissListener.onDismiss(dialog);

    }

    public void setDismissListener(DialogDismissListener listener) {
        onDismissListener = listener;
    }


}
