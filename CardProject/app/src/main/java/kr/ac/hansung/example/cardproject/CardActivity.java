package kr.ac.hansung.example.cardproject;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Random;


public class CardActivity extends AppCompatActivity {

    public static final int STATE_SHOW = 0;
    public static final int STATE_PLAY = 1;

    public static final int STATE_SEL1 = 2;
    public static final int STATE_SEL2 = 3;
    public static final int STATE_ITEM = 4;

    public int state;

    Card MCard[][];
    //CardThread thread;
    CardThread thread;
    CardThread2 thread2;

    public Card select1, select2;
    public int sel1x, sel1y, sel2x, sel2y;

    public int player, myturn;
    int first, setplayer, setRoom, showend, clickcount;

    int item ,myRankScore;
    static String Room;

    public int matchCount = 0;
    Random rand;

    public TextView score_p1, score_p2, Mint_tv;
    View view;
    public ImageButton[][] mButton;

    public Button itemButton;

    public static DatabaseReference mDatabase;


    int[] d = new int[]{R.drawable.card_a, R.drawable.card_b, R.drawable.card_c, R.drawable.card_d
            , R.drawable.card_e, R.drawable.card_f, R.drawable.card_g, R.drawable.card_h,
            R.drawable.card_i, R.drawable.card_j, R.drawable.card_k, R.drawable.card_l};

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.score);

        mDatabase = FirebaseDatabase.getInstance().getReference(); // FireBaseDatabase 와 연결.

        itemButton = findViewById(R.id.itemBt);
        Mint_tv = findViewById(R.id.mint_text);

        mButton = new ImageButton[4][6];

        mButton[0][0] = (ImageButton) findViewById(R.id.imageButtonc1);
        mButton[1][0] = (ImageButton) findViewById(R.id.imageButtonc2);
        mButton[2][0] = (ImageButton) findViewById(R.id.imageButtonc3);
        mButton[3][0] = (ImageButton) findViewById(R.id.imageButtonc4);
        mButton[0][1] = (ImageButton) findViewById(R.id.imageButtonc5);
        mButton[1][1] = (ImageButton) findViewById(R.id.imageButtonc6);
        mButton[2][1] = (ImageButton) findViewById(R.id.imageButtonc7);
        mButton[3][1] = (ImageButton) findViewById(R.id.imageButtonc8);
        mButton[0][2] = (ImageButton) findViewById(R.id.imageButtonc9);
        mButton[1][2] = (ImageButton) findViewById(R.id.imageButtonc10);
        mButton[2][2] = (ImageButton) findViewById(R.id.imageButtonc11);
        mButton[3][2] = (ImageButton) findViewById(R.id.imageButtonc12);
        mButton[0][3] = (ImageButton) findViewById(R.id.imageButtonc13);
        mButton[1][3] = (ImageButton) findViewById(R.id.imageButtonc14);
        mButton[2][3] = (ImageButton) findViewById(R.id.imageButtonc15);
        mButton[3][3] = (ImageButton) findViewById(R.id.imageButtonc16);
        mButton[0][4] = (ImageButton) findViewById(R.id.imageButtonc17);
        mButton[1][4] = (ImageButton) findViewById(R.id.imageButtonc18);
        mButton[2][4] = (ImageButton) findViewById(R.id.imageButtonc19);
        mButton[3][4] = (ImageButton) findViewById(R.id.imageButtonc20);
        mButton[0][5] = (ImageButton) findViewById(R.id.imageButtonc21);
        mButton[1][5] = (ImageButton) findViewById(R.id.imageButtonc22);
        mButton[2][5] = (ImageButton) findViewById(R.id.imageButtonc23);
        mButton[3][5] = (ImageButton) findViewById(R.id.imageButtonc24);

        score_p1 = (TextView) findViewById(R.id.score_p1);
        score_p2 = (TextView) findViewById(R.id.score_p2);

        for (int y = 0; y < 6; y++) {
            for (int x = 0; x < 4; x++) {
                mButton[x][y].setBackgroundResource(R.drawable.back);
            }
        }

        MCard = new Card[4][6];

        rand = new Random();

        for (int y = 0; y < 6; y++) {
            for (int x = 0; x < 4; x++) {
                MCard[x][y] = new Card(Card.IMAGE_A);
            }
        }

        myturn = -1; //짝맞추기 실패할때마다 하나씩 증가하여 고정된 player변수와 비교하여 차례 결정
        first = 0; //게임시작시 파이어베이스에서 값을 받아올때 제한하기 위함
        player = -1; //게임시작시 파이어베이스에서 0과 1로 순환하며 할당받아 고정 myturn변수와 비교하여 차례를 비교
        setplayer = -1; //게임시작시 파이어베이스에서 player에 값을 한번만 할당받기 위함
        setRoom = -1; //게임시작시 파이어베이스에서 Room에 값을 한번만 할당받기 위함
        showend = -1; //게임시작시 카드를3초동안 보여줄때 클릭을 할수 없도록 하기위함
        clickcount = 0; //자신의 차례에 카드를 몇번 뒤집었나 비교하기 위함
        item = 0; //아이템 사용여부를 알기위함

        mDatabase.child("gameRoom").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (setRoom == -1) {
                    setRoom++;
                    Room = dataSnapshot.child("Num").getValue().toString();
                    int count = 0;
                    for (int y = 0; y < 6; y++) {
                        for (int x = 0; x < 4; x++) {
                            if (dataSnapshot.child(Room).child("CardState").child("card " + count).getValue() == null) {
                                mDatabase.child("gameRoom").child(Room).child("CardState").child("card " + count).setValue(0);
                            } else {
                                mDatabase.child("gameRoom").child(Room).child("CardState").child("card " + count)
                                        .setValue(Integer.parseInt(dataSnapshot.child(Room).child("CardState").child("card " + count).getValue().toString()));
                            }
                            count++;
                        }
                    }
                    mDatabase.child("gameRoom").child(Room).child("sel1").setValue("0");
                    mDatabase.child("gameRoom").child(Room).child("sel2").setValue("0");
                    mDatabase.child("gameRoom").child(Room).child("userturn").setValue("0");
                    mDatabase.child("gameRoom").child(Room).child("CardState").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) { // 값이 변경되면 자동호출됨.
                            if (player == 1) {
                                int count = 0; // child() 구별해줄려고, card 0 ,card 1 ,card 2
                                for (int y = 0; y < 6; y++) {
                                    for (int x = 0; x < 4; x++) {
                                        MCard[x][y].color = dataSnapshot.child("card " + count).getValue(Integer.class); // card 0, card 1, card 2, card 3 ...에 순차적으로 접근
                                        count++; //
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) { // 데이터베이스서 못불러왔을시
                            Log.w("Main", "Failed to read value", databaseError.toException());
                        }
                    });

                    mDatabase.child("gameRoom").child(Room).child("userturn").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) { // 값이 변경되면 자동호출됨.
                            myturn++;
                            Log.d("userturn", myturn + "");
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) { // 데이터베이스서 못불러왔을시
                            Log.w("userturn", myturn + "", databaseError.toException());
                        }
                    });

                    mDatabase.child("gameRoom").child(Room).child("sel1").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) { // 값이 변경되면 자동호출됨.
                            if (first == 1 && Integer.parseInt(dataSnapshot.getValue().toString()) != 100) {
                                if (myturn % 2 != player) {
                                    String snum = dataSnapshot.getValue().toString();
                                    int inum = Integer.parseInt(snum);
                                    int count = 0;
                                    for (int y = 0; y < 6; y++) {
                                        for (int x = 0; x < 4; x++) {
                                            if (inum == count) getClick(x, y, (View) mButton[x][y]);
                                            count++;
                                        }
                                    }
                                    Log.d("sel1", "" + inum);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) { // 데이터베이스서 못불러왔을시
                            Log.w("sel1", "", databaseError.toException());
                        }
                    });

                    mDatabase.child("gameRoom").child(Room).child("sel2").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) { // 값이 변경되면 자동호출됨.
                            if (first == 1 && Integer.parseInt(dataSnapshot.getValue().toString()) != 100) {
                                if (myturn % 2 != player) {
                                    String snum = dataSnapshot.getValue().toString();
                                    int inum = Integer.parseInt(snum);
                                    int count = 0;
                                    for (int y = 0; y < 6; y++) {
                                        for (int x = 0; x < 4; x++) {
                                            if (inum == count) getClick(x, y, (View) mButton[x][y]);
                                            count++;
                                        }
                                    }
                                    Log.d("sel2", "" + inum);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) { // 데이터베이스서 못불러왔을시
                            Log.w("sel2", "", databaseError.toException());
                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        mDatabase.child("userlist").child(getIntent().getExtras().getString("ID")).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Mint_tv.setText(dataSnapshot.child("Mint").getValue().toString());
                myRankScore = Integer.parseInt(dataSnapshot.child("Score").getValue().toString());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        mDatabase.child("player").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) { // 값이 변경되면 자동호출됨.
                if(setplayer==-1) {
                    int getplayer = Integer.parseInt(dataSnapshot.getValue().toString());
                    player = getplayer;
                    getplayer++;
                    setplayer++;
                    if(player==0)ShuffledCards();
                    mDatabase.child("player").setValue(Integer.toString(getplayer%2));
                }
                else if(Integer.parseInt(dataSnapshot.getValue().toString())==0){
                    playStart();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) { // 데이터베이스서 못불러왔을시
            }
        });


        state = STATE_SHOW;
    }

    public void onItemButton(View v){
        if((myturn%2)==player&&state==STATE_PLAY) {
            if(Integer.parseInt(Mint_tv.getText().toString())>=50) {
                Toast.makeText(this,"한장 미리보기! -50민트",Toast.LENGTH_SHORT).show();
                Mint_tv.setText(Integer.toString(Integer.parseInt(Mint_tv.getText().toString())-50));
                mDatabase.child("userlist").child(getIntent().getExtras().getString("ID")).child("Mint").setValue(Integer.toString(Integer.parseInt(Mint_tv.getText().toString())));
                item = 1;
            }
        }
        else{
            Toast.makeText(this,"지금은 사용할 수 없습니다.",Toast.LENGTH_SHORT).show();
        }
    }

    public void playStart(){
        thread = new CardThread(this);
        thread.start();
        Log.d("player",""+player);
        first=1;
    }

    public void ShuffledCards() {
        for (int y = 0; y < 6; y++) {
            for (int x = 0; x < 4; x++) {
                MCard[x][y].color = (x * 3 + y * 5) % 12;
            }
        }
        for (int r = 0; r < 50; r++) {
            int x = rand.nextInt(4), y = rand.nextInt(6);
            int temp = MCard[0][0].color;
            MCard[0][0].color = MCard[x][y].color;
            MCard[x][y].color = temp;
        }
        int count = 0;
        for (int y = 0; y < 6; y++) {
            for (int x = 0; x < 4; x++) {
                mDatabase.child("gameRoom").child(Room).child("CardState").child("card " + count).setValue(MCard[x][y].color);
                count++;
            }
        }
    }

    Handler mHandler = new Handler();
    public void endGame() {
        int myScore = Integer.parseInt(score_p1.getText().toString());
        int otherScore = Integer.parseInt(score_p2.getText().toString());
        mDatabase.child("userlist").child(getIntent().getExtras().getString("ID")).child("Score")
                .setValue(myRankScore + (myScore - otherScore));
        matchCount = 0; // 24개 되면 끝나는거
        // 보물 찾기 성공 다이얼로그 만들기 !!
        final AlertDialog[] finish_Dialog = new AlertDialog[1];
        final AlertDialog.Builder builder = new AlertDialog.Builder(CardActivity.this);
        if (myScore > otherScore) {
            builder.setTitle("게임 승리 !");
            builder.setMessage("내 점수: " + myScore + "\n" + "상대점수: " + otherScore + "\n" + Math.abs(myScore - otherScore) + "차이로 나 승리!!");
            builder.setIcon(R.drawable.game_win);
        } else {
            builder.setTitle("게임 패배 !");
            builder.setMessage("내 점수: " + myScore + "\n" + "상대점수: " + otherScore + "\n" + Math.abs(myScore - otherScore) + "차이로 상대 승리!!");
            builder.setIcon(R.drawable.game_lose);
        }

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                builder.setNegativeButton("닫기", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(player==0)
                            mDatabase.child("userlist").child("updateCount").setValue(MainView.updateCount+1);
                        else
                            mDatabase.child("userlist").child("updateCount2").setValue(MainView.updateCount2+1);
                        finish();
                    }
                });
                finish_Dialog[0] = builder.create();
                finish_Dialog[0].show();
            }
        });
    }

    public void clickButton(int x, int y, View v) {
        if(showend==1&&item==0) {
            if (MCard[x][y].state == Card.CARD_CLOSE && (myturn % 2) == player) {
                clickcount++;
                switch (state) {
                    case STATE_PLAY:
                        select1 = MCard[x][y];
                        sel1x = x;
                        sel1y = y;
                        select1.state = Card.CARD_OPEN;
                        v.setBackgroundResource(d[MCard[x][y].color]);
                        state = STATE_SEL1;
                        mDatabase.child("gameRoom").child(Room).child("sel1").setValue("100");
                        mDatabase.child("gameRoom").child(Room).child("sel1").setValue((x + y * 4) + "");
                        break;
                    case STATE_SEL1:
                        select2 = MCard[x][y];
                        sel2x = x;
                        sel2y = y;
                        select2.state = Card.CARD_OPEN;
                        v.setBackgroundResource(d[MCard[x][y].color]);
                        state = STATE_SEL2;
                        mDatabase.child("gameRoom").child(Room).child("sel2").setValue("100");
                        mDatabase.child("gameRoom").child(Room).child("sel2").setValue((x + y * 4) + "");
                        break;
                }
                thread = new CardThread(this);
                thread.start();
            }
        }
        else if(state!=STATE_ITEM&&item==1){
            sel2x = x;
            sel2y = y;
            v.setBackgroundResource(d[MCard[x][y].color]);
            state = STATE_ITEM;
            thread = new CardThread(this);
            thread.start();
            item=0;
        }
    }

    public void getClick(int x, int y, View v) {
        clickcount++;
        if (MCard[x][y].state == Card.CARD_CLOSE) {
            switch (state) {
                case STATE_PLAY:
                    select1 = MCard[x][y];
                    sel1x = x;
                    sel1y = y;
                    select1.state = Card.CARD_OPEN;
                    v.setBackgroundResource(d[MCard[x][y].color]);
                    state = STATE_SEL1;
                    break;
                case STATE_SEL1:
                    select2 = MCard[x][y];
                    sel2x = x;
                    sel2y = y;
                    select2.state = Card.CARD_OPEN;
                    v.setBackgroundResource(d[MCard[x][y].color]);
                    state = STATE_SEL2;
                    break;
            }
            thread2 = new CardThread2(this);
            thread2.start();
        }
    }

    public void onClickButton1(View v) {
        clickButton(0, 0, v);
    }

    public void onClickButton2(View v) {
        clickButton(1, 0, v);
    }

    public void onClickButton3(View v) {
        clickButton(2, 0, v);
    }

    public void onClickButton4(View v) {
        clickButton(3, 0, v);
    }

    public void onClickButton5(View v) {
        clickButton(0, 1, v);
    }

    public void onClickButton6(View v) {
        clickButton(1, 1, v);
    }

    public void onClickButton7(View v) {
        clickButton(2, 1, v);
    }

    public void onClickButton8(View v) {
        clickButton(3, 1, v);
    }

    public void onClickButton9(View v) {
        clickButton(0, 2, v);
    }

    public void onClickButton10(View v) {
        clickButton(1, 2, v);
    }

    public void onClickButton11(View v) {
        clickButton(2, 2, v);
    }

    public void onClickButton12(View v) {
        clickButton(3, 2, v);
    }

    public void onClickButton13(View v) {
        clickButton(0, 3, v);
    }

    public void onClickButton14(View v) {
        clickButton(1, 3, v);
    }

    public void onClickButton15(View v) {
        clickButton(2, 3, v);
    }

    public void onClickButton16(View v) {
        clickButton(3, 3, v);
    }

    public void onClickButton17(View v) {
        clickButton(0, 4, v);
    }

    public void onClickButton18(View v) {
        clickButton(1, 4, v);
    }

    public void onClickButton19(View v) {
        clickButton(2, 4, v);
    }

    public void onClickButton20(View v) {
        clickButton(3, 4, v);
    }

    public void onClickButton21(View v) {
        clickButton(0, 5, v);
    }

    public void onClickButton22(View v) {
        clickButton(1, 5, v);
    }

    public void onClickButton23(View v) {
        clickButton(2, 5, v);
    }

    public void onClickButton24(View v) {
        clickButton(3, 5, v);
    }
}