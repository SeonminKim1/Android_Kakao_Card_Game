package kr.ac.hansung.example.cardproject;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;

import java.util.ArrayList;
import java.util.Calendar;

public class MainView extends AppCompatActivity {
    static DatabaseReference mDatabase;

    // 랭킹 시스템 변수들 ----------------------------------------------------------------------------
    ArrayList<User> userList = new ArrayList<User>();
    ListView listView = null;
    RankAdapter adapter;

    // 출석체크 앱 사용 변수들 -----------------------------------------------------------------------
    AlertDialog absentDialog;
    static int day, month, year;
    Button btn[] = new Button[31];
    //----------------------------------
    String UserId;
    String UserNickname;
    String Nickname;
    int Score = 0;
    int Mint = 0;
    static TextView tv_mint;

    int login;
    static int updateCount=0, updateCount2=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ----------------------
        LayoutInflater inflater;
        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout linear = (LinearLayout) inflater.inflate(R.layout.activity_main, null);
        setContentView(linear);

        // 닉네임 받음.
        Intent UserIntent = getIntent();
        UserId = UserIntent.getExtras().getString("ID");
        Log.d("닉네임",UserId);
        UserNickname = UserIntent.getExtras().getString("NickName");
        Log.d("닉네임",UserNickname);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        listView = (ListView) findViewById(R.id.rank_listview);
        tv_mint = findViewById(R.id.tv_mint);

        login = 0;
        mDatabase.child("userlist").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(login==0){
                    login++;
                    updateCount = Integer.parseInt(dataSnapshot.child("updateCount").getValue().toString());
                    updateCount2 = Integer.parseInt(dataSnapshot.child("updateCount2").getValue().toString());
                    int logCnt = Integer.parseInt(dataSnapshot.child("loginCount").getValue().toString());
                    mDatabase.child("userlist").child("loginCount").setValue(Integer.toString(logCnt+1));
                    if(dataSnapshot.child(UserId).getValue()!=null) {
                        mDatabase.child("userlist").child(UserId).child("NickName").setValue(UserNickname);
                        mDatabase.child("userlist").child(UserId).child("Score").setValue(Integer.parseInt(dataSnapshot.child(UserId).child("Score").getValue().toString()));
                        mDatabase.child("userlist").child(UserId).child("Mint").setValue(Integer.parseInt(dataSnapshot.child(UserId).child("Mint").getValue().toString()));
                        mDatabase.child("userlist").child(UserId).child("DayDay").setValue(Integer.parseInt(dataSnapshot.child(UserId).child("DayDay").getValue().toString()));
                    }
                    else{
                        mDatabase.child("userlist").child(UserId).child("NickName").setValue(UserNickname);
                        mDatabase.child("userlist").child(UserId).child("Score").setValue(1000);
                        mDatabase.child("userlist").child(UserId).child("Mint").setValue(0);
                        mDatabase.child("userlist").child(UserId).child("DayDay").setValue(0);
                    }

                    String str = dataSnapshot.getValue().toString();
                    str=str.replace('{',',');
                    str=str.replace('}',' ');
                    String str2[] = str.split(",");
                    String str3[] = new String[(str2.length/5)*3];
                    int count=0;
                    for(int i=2; i<str2.length-1; i++) {
                        str2[i] = str2[i].trim();
                        if(i%5==3 || i%5==4 || i%5==1){
                            str3[count]=str2[i];
                            count++;
                        }
                    }
                    String str4[] = new String[str3.length];
                    for(int i=0; i<str3.length; i++) {
                        str3[i] = str3[i].replace("Score=","");
                        str3[i] = str3[i].replace("Mint=","");
                        str4[i] = str3[i].replace("NickName=","");
                    }
                    for(int i=0; i<str4.length/3; i++) {
                        for(int j=0; j<3; j++){
                            if(j==0) Score = Integer.parseInt(str4[j+i*3]);
                            else if(j==1) Mint = Integer.parseInt(str4[j+i*3]);
                            else if(j==2) Nickname = str4[j+i*3];
                        }
                        userList.add(new User(Nickname, Score, Mint));
                    }
                    Score = Integer.parseInt((dataSnapshot.child(UserId).child("Score").getValue()).toString());
                    Mint = Integer.parseInt((dataSnapshot.child(UserId).child("Mint").getValue()).toString());
                    ArrayList<User> rankList = new ArrayList<User>();
                    while(userList.size()!=0) {
                        int high=0;
                        for(int j=0;j<userList.size();j++) {
                            if (userList.get(j).getScore() > userList.get(high).getScore())
                                high = j;
                        }
                        rankList.add(new User(userList.get(high).getID(), userList.get(high).getScore(), userList.get(high).getMint()));
                        userList.remove(high);
                    }
                    adapter = new RankAdapter(MainView.this, rankList); // userList 전달해서 RankAdapter에서 반영시킴.
                    listView.setAdapter(adapter);
                    tv_mint.setText(Integer.toString(Mint));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }

    protected void onResume(){
        super.onResume();
        login=0;
    }

    // 보물찾기 기능 !!
    public void Go_SearchTreasure(View view) {
        Intent intent = new Intent(MainView.this, Function_SearchTreasure.class);
        intent.putExtra("mint",Mint);
        intent.putExtra("UserId",UserId);
        startActivity(intent);
    }

    // 출석체크 기능 !
    public void Go_Absent(View view) {
        mDatabase.child("userlist").child(UserId).child("Mint").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Mint = Integer.parseInt(dataSnapshot.getValue().toString());
                tv_mint.setText(Integer.toString(Mint));
                Log.d("출석체크 ","민트:"+Mint);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("DatabaseError","Failed to read 민트",databaseError.toException());
            }
        });

        mDatabase.child("userlist").child(UserId).child("DayDay").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                day=dataSnapshot.getValue(Integer.class);
                Log.d("출석체크 ","날짜:"+day);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("DatabaseError","Failed to read 날짜",databaseError.toException());
            }
        });
        // 1. 달력으로 받아옴. (year, month, day ) Toast 및 Firebase에 날짜 구분저장용.
        Calendar c = Calendar.getInstance();
        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH);
        day = c.get(Calendar.DATE);
        mDatabase.child("userlist").child(UserId).child("DayDay").setValue(day); // 초기데이터 저장

        // 2. 커스텀 다이얼로그 생성
        AlertDialog.Builder builder = new AlertDialog.Builder(this); // custom dialog를 위한 layout xml 초기화
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View absent_view = inflater.inflate(R.layout.absent_dialog, null);
        builder.setView(absent_view);

        // 3. 다이얼로그에 버튼 생성
        builder.setPositiveButton("닫기", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d("날짜는 "," "+day);
                mDatabase.child("userlist").child(UserId).child("DayDay").setValue(day);
            }
        });
        absentDialog = builder.create();
        absentDialog.show();
        // ---------------- 다이얼로그 생성 완료 --------------------

        // 4. 달력의 현재 날짜이미지 바꿔주기 위해 id 다 얻어옴.
        btn[0]=absentDialog.findViewById(R.id.imgview_day1); btn[1]=absentDialog.findViewById(R.id.imgview_day2); btn[2]=absentDialog.findViewById(R.id.imgview_day3); btn[3]=absentDialog.findViewById(R.id.imgview_day4);
        btn[4]=absentDialog.findViewById(R.id.imgview_day5); btn[5]=absentDialog.findViewById(R.id.imgview_day6); btn[6]=absentDialog.findViewById(R.id.imgview_day7); btn[7]=absentDialog.findViewById(R.id.imgview_day8);
        btn[8]=absentDialog.findViewById(R.id.imgview_day9); btn[9]=absentDialog.findViewById(R.id.imgview_day10); btn[10]=absentDialog.findViewById(R.id.imgview_day11); btn[11]=absentDialog.findViewById(R.id.imgview_day12);
        btn[12]=absentDialog.findViewById(R.id.imgview_day13); btn[13]=absentDialog.findViewById(R.id.imgview_day14); btn[14]=absentDialog.findViewById(R.id.imgview_day15); btn[15]=absentDialog.findViewById(R.id.imgview_day16);
        btn[16]=absentDialog.findViewById(R.id.imgview_day17); btn[17]=absentDialog.findViewById(R.id.imgview_day18); btn[18]=absentDialog.findViewById(R.id.imgview_day19); btn[19]=absentDialog.findViewById(R.id.imgview_day20);
        btn[20]=absentDialog.findViewById(R.id.imgview_day21); btn[21]=absentDialog.findViewById(R.id.imgview_day22); btn[22]=absentDialog.findViewById(R.id.imgview_day23); btn[23]=absentDialog.findViewById(R.id.imgview_day24);
        btn[24]=absentDialog.findViewById(R.id.imgview_day25); btn[25]=absentDialog.findViewById(R.id.imgview_day26); btn[26]=absentDialog.findViewById(R.id.imgview_day27); btn[27]=absentDialog.findViewById(R.id.imgview_day28);
        btn[28]=absentDialog.findViewById(R.id.imgview_day29); btn[29]=absentDialog.findViewById(R.id.imgview_day30); btn[30]=absentDialog.findViewById(R.id.imgview_day31);
        for(int i=0; i<day-1; i++){
            btn[i].setBackgroundResource(R.drawable.mint_absent); // btn배열은 0부터 시작하니까 1뺴서 넣기.
        }
        btn[day-1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn[day-1].setBackgroundResource(R.drawable.mint_absent);
                Toast.makeText(getApplicationContext(),year+"년 "+(month+1)+"월 "+day+"일 "+ "출석 되었습니다. \n100민트획득!!",Toast.LENGTH_SHORT).show();
                //Toast.makeText(getApplicationContext(),"100민트 획득 !!",Toast.LENGTH_SHORT).show();

                mDatabase.child("userlist").child(UserId).child("Mint").setValue(Mint+100);
            }
        });

    }



    // 카카오톡 로그아웃.
    public void onlogout(View view) {
        onClickLogout();
    }

    // 카카오톡 로그아웃.
    private void onClickLogout() {
        UserManagement.requestLogout(new LogoutResponseCallback() {
            @Override
            public void onCompleteLogout() {
                Intent intent = new Intent(MainView.this, KaKaoSampleLoginActivity.class);
                startActivity(intent);
                Toast.makeText(MainView.this, "로그아웃 성공 !! ", Toast.LENGTH_SHORT).show();
                finish();
                // 원하는 코드 ( 예를 들면 액티비티 이동)
            }
        });
    }

    public void onStartButton(View view) {
        Intent intent = new Intent(this, CardActivity.class);
        intent.putExtra("ID",UserId);
        startActivity(intent);
    }
}
