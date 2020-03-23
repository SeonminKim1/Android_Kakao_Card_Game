package kr.ac.hansung.example.cardproject;

import android.app.Activity;
import android.app.Application;

import com.kakao.auth.KakaoSDK;

public class GlobalApplication extends Application{
    private static volatile GlobalApplication obj =null; // volatile은 변덕스러운으로 '컴파일을 할 때 매번 이 변수를 검사해주세요' 라고 하는 것 최적화 하지 말아달라는 뜻
    private static volatile Activity currentActivity = null;

    @Override
    public void onCreate(){
        super.onCreate();
        obj=this;
        KakaoSDK.init(new KakaoSDKAdapter());
    }

    public static GlobalApplication getGlobalApplicationContext(){
        return obj;
    }

    public static Activity getCurrentActivity(){
        return currentActivity;
    }

    // Activity가 올라올때마다 Activity의 onCreate에서 호출해줘야 한다.
    public static void setCurrentActivity(Activity currentActivity){
        GlobalApplication.currentActivity=currentActivity;
    }
}
