package com.zk.lbsdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

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

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.baidu_map_view)
    MapView mMapView;

    private BaiduMap mBaiduMap;
    private LocationClient mLocationClient;
    private MyLocationConfiguration.LocationMode mCurrentMode;

    private boolean isFirstLocate = true;

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

        initMapView();
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

    /**
     * 继承抽象类BDAbstractListener并重写其onReceieveLocation方法来获取定位数据，并将其传给MapView。
     */
    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            //mapView 销毁后不在处理新接收的位置
            if (location == null || mMapView == null){
                return;
            }
            if (isFirstLocate){
                LatLng ll = new LatLng(location.getLatitude(),location.getLongitude());
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
