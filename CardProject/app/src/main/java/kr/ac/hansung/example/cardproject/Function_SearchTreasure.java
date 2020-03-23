package kr.ac.hansung.example.cardproject;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Random;

public class Function_SearchTreasure extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "hihihi";
    private GoogleMap mMap;

    // 구글 API 연결은 내 위치에 대한 정보 얻기위해서 구글에서 제공하는 보다 쉬운 방법 - FusedLocationProviderClient
    private GoogleApiClient mGoogleApiClient; // GoogleApiClient.ConnectionCallbacks , OnConnectionFailedListener 달게 됨.
    private FusedLocationProviderClient mFusedLocationClient;
    private ToggleButton b_mapmode, b_showCurPos;
    static public int REQUEST_CODE_PERMISSIONS = 1000; // REQUESTPermission에 대한

    LatLng myLocation;
    Handler mHandler;

    DatabaseReference mDatabase;

    float a,b ; // 위도와 경도
    LatLng hansung = new LatLng(37.611025, 127.060215);
    double random_a, random_b;
    double alpha,beta;
    MarkerOptions mk;
    CircleOptions circle = null;
    TextView tv_mint;

    // MainView intent로 넘어오는 것들.
    int mint;
    String UserId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.searchtreasure);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        ////////////////////////////////// 권한 요청
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
        }
        ////////////////////////////////// 권한 요청 끝

        ////////////////////////////////// GoogleAPIClient로 놀기~
        // GoogleAPIClient의 인스턴스 생성
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this); // 현재 위치 정보를 얻을 수 있는 객체

        // 위성모드 or 일반모드
        b_mapmode = findViewById(R.id.b_mapmode);
        b_mapmode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                else
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            }
        });
        Intent fromMainViewIntent = getIntent();
        mint = fromMainViewIntent.getExtras().getInt("mint");
        UserId = fromMainViewIntent.getExtras().getString("UserId");

        myLocation = new LatLng(hansung.latitude, hansung.longitude);
        mHandler = new Handler();
    }

    // 권한 체크 다이얼로그에 대해 거부됬을때 승낙됬을 때
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1000:
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 권한이 승인될 경우
                } else {
                    // 권한이 거부될 경우
                    Toast.makeText(this, "권한 체크 거부 됨", Toast.LENGTH_LONG).show();
                    finish();
                }
                return;
        }
    }
    /////////////////////////////////////// 현재 위치로 카메라 움직이기 + 마커에 현재 위치 표시하기 시작

    /////////////////////////////////////// 사용자 위치 정보 허용 얻기 끝
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(17.0f));
        mMap.setMyLocationEnabled(true);

        // 마커 클릭한 순간의 이벤트 <-> OninfoWindowClickListener (마커 정보창을 클릭한 순간의 이벤트)
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if(marker.getPosition()==myLocation){
                    Toast.makeText(Function_SearchTreasure.this,"보물상자를 클릭하세요", Toast.LENGTH_SHORT).show();
                }
                else {

                    double d = getDistance(marker.getPosition(), myLocation); // 클릭한 보물마커와 현재 내 위치간의 차이
                    if (d <= 50) {
                        mMap.clear();
                        mMap.addMarker(new MarkerOptions().position(myLocation).title("현재위치"));

                        // 보물 찾기 성공 다이얼로그 만들기 !!
                        final AlertDialog[] treasureopen_Dialog = new AlertDialog[1];
                        final AlertDialog.Builder builder = new AlertDialog.Builder(Function_SearchTreasure.this);
                        builder.setTitle("보물 찾기 성공");
                        builder.setMessage("200 민트 획득하셨습니다");
                        builder.setIcon(R.drawable.treasure_open);
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                builder.setNegativeButton("닫기", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                });
                                treasureopen_Dialog[0] = builder.create();
                                treasureopen_Dialog[0].show();
                            }
                        });

                        MainView.tv_mint.setText((mint+200)+"");
                        mDatabase.child("userlist").child(UserId).child("Mint").setValue(mint+200);
                        //Toast.makeText(Function_SearchTreasure.this,"보물 찾기 성공 !!", Toast.LENGTH_SHORT).show();
                        //Toast.makeText(Function_SearchTreasure.this, "200 민트획득!! ", Toast.LENGTH_SHORT).show();
                    } else if (d > 50) {
                        Toast.makeText(Function_SearchTreasure.this, "아직 좀더 가야해요 !! 남은거리 " + (float) d, Toast.LENGTH_SHORT).show();
                    }
                }
                return false;
            }
        });

        // 표시컨트롤 or 제스처 동작 만지기
        UiSettings ui = mMap.getUiSettings();
        // 표시 컨트롤 종류 설정
        ui.setZoomControlsEnabled(true); // 줌 컨트롤러 보이게. 기본 값 false
        ui.setMyLocationButtonEnabled(true); // 현재 위치 표시 활성화된 경우 만 표시 기본값 true
        ui.setCompassEnabled(true); // 나침반 표시. 지도의 위쪽에 정북쪽일 때는 나타나지 않음. 기본 값 true

        // 제스처 동작의 사용 가능성 설정
        ui.setScrollGesturesEnabled(true); // 스크롤 컨트롤러 동작 사용.
        ui.setZoomGesturesEnabled(true); // 줌 컨트롤러 동작 사용여부
        ui.setRotateGesturesEnabled(true); // 회전 동작 가능여부
        ui.setTiltGesturesEnabled(true); // 땅을 바라보는 각도 조절
        ui.setAllGesturesEnabled(true); // 모든 제스처의 일괄 금지 / 허가
    }

    public void currentLocation(View view){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && // 여기까진 위치정보 권한 잘 설정되어있는지
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {  // 여기까진 위치정보 권한 잘 설정되어있는지
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, // 권한 얻기위한 대화상자 표시
                    REQUEST_CODE_PERMISSIONS);
            return;
        }

        // 내 위치 파악하기 !!!!
        mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                //
                // Toast.makeText(Function_SearchTreasure.this,"뭔데",Toast.LENGTH_SHORT).show();
                if (location != null) {
                    myLocation = new LatLng(location.getLatitude(), location.getLongitude()); // 현재위치 (latitude : 위도 / longitude : 경도)
                    Toast.makeText(Function_SearchTreasure.this,"현재위치는 "+location.getLatitude()+","+location.getLongitude(),Toast.LENGTH_SHORT).show();
                    Log.d("현재위치는 ",location.getLatitude()+","+location.getLongitude());
                    mMap.addMarker(new MarkerOptions()
                            .position(myLocation)
                            .title("현재위치!!"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(17.0f));
                }
            }
        });
    }
    // 보물 생성 버튼이 눌렸을 때 !!!!
    // 1.권한체크 -> 2.mFusedLocation을 이용한 myLocation 내 위치 생성.
    // 3. 내 위치로 카메라 이동 및 마커생성
    // 4. 주변에 보물 랜덤 생성
    public void SearchTreasure_btn_click(View view) {
        // 내 위치에 원그리기 !!!!

        if (circle == null) {
            circle = new CircleOptions()
                    .center(myLocation) // 원 생성시 중심
                    .radius(180) // 반지름
                    .strokeColor(Color.CYAN) // 테두리 색
                    .strokeWidth(1.0f) // 테두리 둘레 길이
                    .fillColor(Color.parseColor("#220000ff")); // 채우기 색
            mMap.addCircle(circle);
        }

        // 보물 랜덤 생성
        BitmapDrawable bitmap_draw = (BitmapDrawable)getResources().getDrawable(R.drawable.treasure);
        Bitmap bit = bitmap_draw.getBitmap();
        Bitmap smallMarker = Bitmap.createScaledBitmap(bit, 100, 100, false);
        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(smallMarker);
        int rand = new Random().nextInt(4); // 0~3사이
        Log.d("random값",rand+" ");

        // 알파 베타 = 내 위치 기준으로 4사분면 얼마큼씩 (+) 되서 생성될껀지
        switch(rand){
            case 0:
                alpha = ((double)(new Random().nextInt(1000)))/1000000; // 0.000100 ~ 0.000999
                beta = ((double)(new Random().nextInt(1000)))/1000000; // 0.000100 ~ 0.000999
                break;

            case 1:
                alpha = ((double)(new Random().nextInt(1000)))/1000000; // 0.000100 ~ 0.000999
                beta = -((double)(new Random().nextInt(1000)))/1000000; // -0.000100 ~ -0.000999
                break;

            case 2:
                alpha = -((double)(new Random().nextInt(1000)))/1000000; // - 0.000100 ~ -0.000999
                beta = -((double)(new Random().nextInt(1000)))/1000000; // - 0.000100 ~ -0.000999
                break;
            case 3:
                alpha = -((double)(new Random().nextInt(1000)))/1000000; // -0.000100 ~ -0.000999
                beta = ((double)(new Random().nextInt(1000)))/1000000;  // 0.000100 ~ 0.000999
                break;
        }
        Log.d("알파베타",alpha+","+beta);

        // 보물의 최종위치
        random_a = myLocation.latitude + alpha; random_b = myLocation.longitude + beta;

        mk = new MarkerOptions()
                .position(new LatLng(random_a,random_b)) // 마커의 위치
                .icon(bitmapDescriptor) // 이미지
                .title("보물");
        mMap.addMarker(mk);

        Log.d("보물위치",random_a+","+random_b);
    }

    // 두 마커간 거리구하기. (보물과 나 사이)
    public double getDistance(LatLng LatLng1, LatLng LatLng2) {
        double distance = 0;
        Location locationA = new Location("A");
        locationA.setLatitude(LatLng1.latitude);
        locationA.setLongitude(LatLng1.longitude);
        Location locationB = new Location("B");
        locationB.setLatitude(LatLng2.latitude);
        locationB.setLongitude(LatLng2.longitude);
        distance = locationA.distanceTo(locationB);

        return distance;
    }

    // 구글 API 연결에 따른 콜백메서드 1
    @Override
    public void onConnected(@Nullable Bundle bundle) { }

    // 구글 API 연결에 따른 콜백메서드 2
    @Override
    public void onConnectionSuspended(int i) { }

    // 구글 API 연결에 따른 콜백메서드 3
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) { }

    // 시작할때 API연결
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    // 앱 종료시 API 연결 해제
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }
}