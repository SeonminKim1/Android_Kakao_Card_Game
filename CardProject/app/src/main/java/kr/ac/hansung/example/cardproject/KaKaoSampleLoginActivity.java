package kr.ac.hansung.example.cardproject;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kakao.auth.ErrorCode;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.Session;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.MeResponseCallback;
import com.kakao.usermgmt.response.model.UserProfile;
import com.kakao.util.exception.KakaoException;
import com.kakao.util.helper.log.Logger;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

// https://developers.kakao.com/docs/android/user-management#로그인
public class KaKaoSampleLoginActivity extends AppCompatActivity {
    private static final String TAG = "KAKAO";
    SessionCallback callback;
    private long number;
    private String Nickname=null;

    
    /**
     * onCreate 로그인 Activity가 생성될 때 로그인 버튼을 찾아오고, 세션의 상태가 변경될 때 불리는 세션 콜백을 추가 해줌.
     * onActivityResult 로그인 activity를 이용하여 sdk에서 필요로 하는 activity를 띄우기 때문
     * onDestory 세션의 상태가 변경될 때 불리는 세션 콜백을 삭제함.
     * @param savedInstanceState 기존 session 정보가 저장된 객체
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.kakao_login);
        //setContentView(R.layout.kakaomap_activity);
        //getHashKey();

        callback = new SessionCallback();
        Session.getCurrentSession().addCallback(callback);
        requestMe();
    }

    // HashKey얻는 것 . KaKao developer의 앱의 HashKey와 똑같아야 됨.
    private void getHashKey(){
        try {                                                        // 패키지이름을 입력해줍니다.
            PackageInfo info = getPackageManager().getPackageInfo("kr.ac.hansung.example.cardproject", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d(TAG,"key_hash="+ Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // 앱이 파괴됬을 때
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Session.getCurrentSession().removeCallback(callback);
    }

    private class SessionCallback implements ISessionCallback {
        @Override
        public void onSessionOpened() {
            UserManagement.requestMe(new MeResponseCallback() {
                // 단계1) 로그인 버튼 누른 후 실패시
                @Override
                public void onFailure(ErrorResult errorResult){
                    String message = "failed to get user info. msg = "+ errorResult;

                    ErrorCode result = ErrorCode.valueOf(errorResult.getErrorCode());
                    if(result==ErrorCode.CLIENT_ERROR_CODE){

                        // 에러로 인한 실패
                        // finish();
                    }else{
                        //redirectMainActivity();
                    }
                }

                // 단계1) 로그인 버튼 눌렀을 경우 비정상 종료시
                @Override
                public void onSessionClosed(ErrorResult errorResult) {
                }

                // 단계1) 로그인 버튼 눌렀는데 아이디가 없을때
                @Override
                public void onNotSignedUp() {
                    Toast.makeText(KaKaoSampleLoginActivity.this,"카카오톡 아이디 없음 !",Toast.LENGTH_SHORT).show();
                }

                // 단계1) 로그인 버튼 눌렀는데 성공했을 때
                @Override
                public void onSuccess(UserProfile userProfile) {
                    // 로그인에 성공하면 로그인한, 사용자의 일련번호, 닉네임, 이미지 url등을 리턴합니다.
                    number = userProfile.getId();
                    Nickname = userProfile.getNickname();
                    Log.d("UserProfile", userProfile.toString());
                    Log.d("UserProfile", userProfile.getId() + "");
                    redirectSignupActivity();
                }
            });
        } // 로그인 접속 성공

        // 단계1) 로그인버튼 눌렀는데 아이디 비교하기도 전에 터졌을 때
        @Override
        public void onSessionOpenFailed(KakaoException exception) {
            if(exception != null) {
                Logger.e(exception);
            }
        }
    }

    // 단계2) 유저의 정보를 받아오는 함수
    public void requestMe(){
        UserManagement.requestMe(new MeResponseCallback() {
            // 단계2) 유저 정보 받아오기 실패했을 때
            @Override
            public void onFailure(ErrorResult errorResult){
                Log.d(TAG, "error message= 유저정보 받아오기 실패" + errorResult);
                // super.onFailure(errorResult);
            }

            // 단계2) 유저 정보 받아오는 창이 종료됬을 때
            @Override
            public void onSessionClosed(ErrorResult errorResult) {
                Log.d(TAG, "error message = 유저정보 받아오는 창 종료" + errorResult);
            }

            // 단계2) 유저 정보 받는데 카카오톡 회원이 아닐때
            @Override
            public void onNotSignedUp() {
                // 카카오톡 회원이 아닐시
                Log.d(TAG,"카카오톡 회원이 아닙니다");
            }

            // 단계2) 유저 정보 받아오는거 잘 성공했을 떄 !
            @Override
            public void onSuccess(UserProfile result) {
                Nickname = result.getNickname();
                number = result.getId();

                Log.d("UserProfile2", result.getNickname()+"\n");
                Log.d("UserProfile2", result.getId()+"");
            }
        });
    }

    // 단계3) 로그인 접속 성공후에 다음 페이지로
    protected void redirectSignupActivity() {
        final Intent intent = new Intent(this, MainView.class);
        intent.putExtra("ID",Long.toString(number));
        intent.putExtra("NickName",Nickname);
        startActivity(intent);

        Toast.makeText(KaKaoSampleLoginActivity.this, Nickname+ "님 반갑습니다! ",Toast.LENGTH_SHORT).show();
        finish();
    }
}


