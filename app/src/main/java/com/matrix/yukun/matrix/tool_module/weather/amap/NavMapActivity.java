package com.matrix.yukun.matrix.tool_module.weather.amap;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;
import com.matrix.yukun.matrix.BaseActivity;
import com.matrix.yukun.matrix.MyApp;
import com.matrix.yukun.matrix.R;
import com.matrix.yukun.matrix.main_module.utils.SPUtils;
import com.matrix.yukun.matrix.tool_module.weather.activity.NavMapAdapter;
import com.matrix.yukun.matrix.util.log.LogUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class NavMapActivity extends BaseActivity implements LocationSource, AMapLocationListener, Inputtips.InputtipsListener {

    @BindView(R.id.iv_back)
    ImageView ivBack;
    @BindView(R.id.ll_enter)
    LinearLayout llEnter;
    @BindView(R.id.in_change)
    ImageView inChange;
    @BindView(R.id.rl_title)
    RelativeLayout rlTitle;
    @BindView(R.id.tab_layout)
    TabLayout tabLayout;
    @BindView(R.id.map_view)
    MapView mMapView;
    @BindView(R.id.lv_search)
    ListView lvSearch;
    @BindView(R.id.in_start)
    ImageView inStart;
    @BindView(R.id.et_start)
    EditText etStart;
    @BindView(R.id.iv_start_del)
    ImageView ivStartDel;
    @BindView(R.id.iv_end)
    ImageView ivEnd;
    @BindView(R.id.et_end)
    EditText etEnd;
    @BindView(R.id.iv_end_del)
    ImageView ivEndDel;
    private AMap mAMap;
    private UiSettings mUiSettings;
    private MyLocationStyle myLocationStyle;
    private AMapLocationClient mlocationClient;
    private AMapLocationClientOption mLocationOption;
    private OnLocationChangedListener mListener;
    private boolean followMove = true;
    private AMapInit mAMapInit;
    private LatLng mCurrentLatLng;
    private List<String> mMapNavs;
    private String mCityCode;
    private boolean isStart;
    private NavMapAdapter mNavMapAdapter;
    private List<Tip> mTips=new ArrayList<>();

    public static void start(Context context) {
        Intent intent = new Intent(context, NavMapActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void createMap(Bundle savedInstanceState) {
        mMapView.onCreate(savedInstanceState);
    }

    @Override
    public int getLayout() {
        return R.layout.activity_nav_map;
    }

    @Override
    public void initView() {
        mAMap = mMapView.getMap();
        mAMapInit = AMapInit.instance();
        mAMapInit.init(this, mAMap);
        mNavMapAdapter = new NavMapAdapter(this);
        lvSearch.setAdapter(mNavMapAdapter);
    }

    @Override
    public void initDate() {
        init();
        mCityCode = SPUtils.getInstance().getString("CityCode");
        ;
        mMapNavs = (Arrays.asList(getResources().getStringArray(R.array.map_nav)));
        mCurrentLatLng = MyApp.getLatLng();
        mAMapInit.changeMapCenter(mCurrentLatLng);
        initViewData();
    }

    private void initViewData() {
        for (int i = 0; i < mMapNavs.size(); i++) {
            tabLayout.addTab(tabLayout.newTab().setText(mMapNavs.get(i)));
        }
    }

    @Override
    public void initListener() {
        tabLayout.getTabAt(0).select();
        etStart.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!TextUtils.isEmpty(s.toString())){
                    isStart=true;
                    searchResule(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        etEnd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!TextUtils.isEmpty(s.toString())){
                    isStart=false;
                    searchResule(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        lvSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                lvSearch.setVisibility(View.GONE);
            }
        });
    }

    private void searchResule(String text) {
        InputtipsQuery inputquery = new InputtipsQuery(text, mCityCode);
        inputquery.setCityLimit(true);//限制在当前城市
        Inputtips inputTips = new Inputtips(this, inputquery);
        inputTips.setInputtipsListener(this);
        inputTips.requestInputtipsAsyn();
    }


    @OnClick({R.id.iv_back, R.id.in_change,R.id.iv_start_del, R.id.iv_end_del})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.in_change:
                break;
            case R.id.iv_start_del:
                finish();
                break;
            case R.id.iv_end_del:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        release();
    }

    private void release() {
        if (mMapView != null) {
            mMapView.onDestroy();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView.onPause();
    }

    private void init() {
        mUiSettings = mAMap.getUiSettings();
        initSetting();
        initLocat();
    }

    private void initLocat() {
        myLocationStyle = new MyLocationStyle();//初始化定位蓝点样式类myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。
        myLocationStyle.interval(2000); //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
        myLocationStyle.showMyLocation(true);
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);
        mAMap.moveCamera(CameraUpdateFactory.zoomTo(12));
        mAMap.showIndoorMap(true);
        mAMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
        mAMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
        mAMap.setOnMyLocationChangeListener(new AMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                mCurrentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                if (followMove) {
                    mAMap.animateCamera(CameraUpdateFactory.newLatLng(mCurrentLatLng));
                }
            }
        });

        mAMap.setOnMapTouchListener(new AMap.OnMapTouchListener() {
            @Override
            public void onTouch(MotionEvent motionEvent) {
                followMove = false;
            }
        });
    }

    private void initSetting() {
        mUiSettings.setZoomControlsEnabled(true);//是否显示缩放
        mAMap.setLocationSource(this);//通过aMap对象设置定位数据源的监听
        mAMap.setMyLocationEnabled(true);// 可触发定位并显示当前位置
        mUiSettings.setScaleControlsEnabled(true); //地图比例
        mUiSettings.setCompassEnabled(false); //指南针
        mUiSettings.setMyLocationButtonEnabled(true);//设置默认定位按钮是否显示，非必需设置
    }


    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mListener = onLocationChangedListener;
        if (mlocationClient == null) {
            //初始化定位
            mlocationClient = new AMapLocationClient(this);
            //初始化定位参数
            mLocationOption = new AMapLocationClientOption();
            //设置定位回调监听
            mlocationClient.setLocationListener(this);
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //设置定位参数
            mlocationClient.setLocationOption(mLocationOption);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mlocationClient.startLocation();//启动定位
        }
    }

    @Override
    public void deactivate() {
        if (mlocationClient != null) {
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
            mlocationClient = null;
        }
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (mListener != null && aMapLocation != null) {
            if (aMapLocation != null && aMapLocation.getErrorCode() == 0) {
                mListener.onLocationChanged(aMapLocation);// 显示系统小蓝点
            } else {
                String errText = "定位失败," + aMapLocation.getErrorCode() + ": " + aMapLocation.getErrorInfo();
                LogUtil.i("================", errText);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onGetInputtips(List<Tip> list, int i) {
        if(list.size()>0){
            lvSearch.setVisibility(View.VISIBLE);
            mTips.clear();
            mTips.addAll(list);
            mNavMapAdapter.setData(mTips);
        }
    }
}
