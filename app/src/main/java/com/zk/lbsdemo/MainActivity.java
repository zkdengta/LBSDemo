package com.zk.lbsdemo;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;
import com.yanzhenjie.sofia.Sofia;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.baidu_map_view)
    MapView mMapView;
    @BindView(R.id.tv_road_condition)
    TextView mTvRoadCondition;
    @BindView(R.id.tv_2D_maps)
    TextView mTv2DMaps;
    @BindView(R.id.tv_satellite_maps)
    TextView mTvSatelliteMaps;
    @BindView(R.id.tv_heat_maps)
    TextView mTvHeatMaps;

    private BaiduMap mBaiduMap;
    private LocationClient mLocationClient;
    private MyLocationConfiguration.LocationMode mCurrentMode;

    private boolean isFirstLocate = true;
    private boolean isClickRoadCondition = true;
    private boolean isClickHeat = true;

    private Drawable mRoadConditionGray = null;
    private Drawable mRoadConditionColours = null;
    private Drawable m2dGray = null;
    private Drawable m2dBlack = null;
    private Drawable mSatelliteGray = null;
    private Drawable mmSatelliteBlack = null;
    private Drawable mHeatGray = null;
    private Drawable mHeatBlack = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //修改状态栏背景为透明
        Sofia.with(this)
                .invasionStatusBar()
                .statusBarDarkFont()
                .statusBarBackgroundAlpha(0);

        ButterKnife.bind(this);

        //动态申请访问权限
        AndPermission.with(this)
                .permission(Permission.READ_PHONE_STATE, Permission.ACCESS_COARSE_LOCATION,
                        Permission.WRITE_EXTERNAL_STORAGE, Permission.ACCESS_FINE_LOCATION, Permission.CAMERA)
                .start();

        initDrawable();
        initMapView();
    }

    /**
     * 初始化图片资源
     */
    private void initDrawable() {
        mRoadConditionGray = getResources().getDrawable(R.drawable.ic_road_condition_black_24dp, null);// 找到资源图片
        // 这一步必须要做，否则不会显示。
        mRoadConditionGray.setBounds(0, 0, mRoadConditionGray.getMinimumWidth(), mRoadConditionGray.getMinimumHeight());
        mRoadConditionColours = getResources().getDrawable(R.drawable.ic_road_condition_colours_24dp, null);// 找到资源图片
        // 这一步必须要做，否则不会显示。
        mRoadConditionColours.setBounds(0, 0, mRoadConditionColours.getMinimumWidth(), mRoadConditionColours.getMinimumHeight());;

        m2dGray = getResources().getDrawable(R.drawable.ic_2d_gray_24dp, null);// 找到资源图片
        // 这一步必须要做，否则不会显示。
        m2dGray.setBounds(0, 0, m2dGray.getMinimumWidth(), m2dGray.getMinimumHeight());;
        m2dBlack = getResources().getDrawable(R.drawable.ic_2d_black_24dp, null);// 找到资源图片
        // 这一步必须要做，否则不会显示。
        m2dBlack.setBounds(0, 0, m2dBlack.getMinimumWidth(), m2dBlack.getMinimumHeight());;

        mSatelliteGray = getResources().getDrawable(R.drawable.ic_satellite_map_gray_24dp, null);// 找到资源图片
        // 这一步必须要做，否则不会显示。
        mSatelliteGray.setBounds(0, 0, mSatelliteGray.getMinimumWidth(), mSatelliteGray.getMinimumHeight());;
        mmSatelliteBlack = getResources().getDrawable(R.drawable.ic_satellite_map_black_24dp, null);// 找到资源图片
        // 这一步必须要做，否则不会显示。
        mmSatelliteBlack.setBounds(0, 0, mmSatelliteBlack.getMinimumWidth(), mmSatelliteBlack.getMinimumHeight());;

        mHeatGray = getResources().getDrawable(R.drawable.ic_heat_map_gray_24dp, null);// 找到资源图片
        // 这一步必须要做，否则不会显示。
        mHeatGray.setBounds(0, 0, mHeatGray.getMinimumWidth(), mHeatGray.getMinimumHeight());;
        mHeatBlack = getResources().getDrawable(R.drawable.ic_heat_map_black_24dp, null);// 找到资源图片
        // 这一步必须要做，否则不会显示。
        mHeatBlack.setBounds(0, 0, mHeatBlack.getMinimumWidth(), mHeatBlack.getMinimumHeight());;
    }

    /**
     * 初始化，配置MapView
     */
    private void initMapView() {

        //地图总控制器，获取实例
        mBaiduMap = mMapView.getMap();

        //开启地图的定位图层
        mBaiduMap.setMyLocationEnabled(true);

        //初始化定位模式
        mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;

        //定位初始化
        mLocationClient = new LocationClient(this);

        //MyLocationConfiguration类来构造包括定位的属性，定位模式、是否开启方向、设置自定义定位图标、精度圈填充颜色以及精度圈边框颜色5个属性。
        mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(
                mCurrentMode, true, null));

        //通过LocationClientOption设置LocationClient相关参数
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);

        //设置locationClientOption
        mLocationClient.setLocOption(option);

        //注册LocationListener监听器
        MyLocationListener myLocationListener = new MyLocationListener();
        mLocationClient.registerLocationListener(myLocationListener);
        //开启地图定位图层
        mLocationClient.start();
    }

    @OnClick({R.id.tv_road_condition, R.id.tv_2D_maps, R.id.tv_satellite_maps, R.id.tv_heat_maps})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_road_condition:
                if (isClickRoadCondition){
                    //开启交通图
                    mBaiduMap.setTrafficEnabled(true);
                    mTvRoadCondition.setCompoundDrawables(null,mRoadConditionColours,null,null);
                }else {
                    //关闭交通图
                    mBaiduMap.setTrafficEnabled(false);
                    mTvRoadCondition.setCompoundDrawables(null,mRoadConditionGray,null,null);
                }
                isClickRoadCondition = !isClickRoadCondition;
                break;
            case R.id.tv_2D_maps:
                //普通地图 ,mBaiduMap是地图控制器对象
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
                mTvSatelliteMaps.setCompoundDrawables(null,mSatelliteGray,null,null);
                mTv2DMaps.setCompoundDrawables(null,m2dBlack,null,null);
                break;
            case R.id.tv_satellite_maps:
                //卫星地图
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
                mTvSatelliteMaps.setCompoundDrawables(null,mmSatelliteBlack,null,null);
                mTv2DMaps.setCompoundDrawables(null,m2dGray,null,null);
                break;
            case R.id.tv_heat_maps:
                if (isClickHeat){
                    //开启交通图
                    mBaiduMap.setBaiduHeatMapEnabled(true);
                    mTvHeatMaps.setCompoundDrawables(null,mHeatBlack,null,null);
                }else {
                    //关闭交通图
                    mBaiduMap.setBaiduHeatMapEnabled(false);
                    mTvHeatMaps.setCompoundDrawables(null,mHeatGray,null,null);
                }
                isClickHeat = !isClickHeat;
                break;
        }
    }

    /**
     * 继承抽象类BDAbstractListener并重写其onReceieveLocation方法来获取定位数据，并将其传给MapView。
     */
    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            //mapView 销毁后不在处理新接收的位置
            if (location == null || mMapView == null) {
                return;
            }
            if (isFirstLocate) {
                LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
                MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
                mBaiduMap.animateMapStatus(update);
                update = MapStatusUpdateFactory.zoomTo(16.0f);
                mBaiduMap.animateMapStatus(update);
                isFirstLocate = false;
            }
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(location.getDirection()).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);
        }
    }

    @Override
    protected void onResume() {
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mLocationClient.stop();
        mBaiduMap.setMyLocationEnabled(false);
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
        super.onDestroy();
    }
}
