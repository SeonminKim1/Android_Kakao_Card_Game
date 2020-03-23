package kr.ac.hansung.example.cardproject;

/**
 * Created by seong on 2018-11-15.
 */

public class CardThread2 extends Thread{
    CardActivity view;

    public CardThread2(CardActivity view){
        this.view = view;
    }

    public void run(){
        switch (view.state){
            case CardActivity.STATE_SHOW:
                view.state = CardActivity.STATE_PLAY;
                run_show();
                break;
            case CardActivity.STATE_SEL2:
                run_sel2();
                break;
        }
    }
    public void run_show(){
        view.matchCount = 0;
        for(int y=0;y<6;y++){
            for(int x=0;x<4;x++){
                view.MCard[x][y].state = Card.CARD_SHOW;
                view.mButton[x][y].setBackgroundResource(view.d[view.MCard[x][y].color]);
            }
        }
        try{
            Thread.sleep(3000);
        }catch (InterruptedException e){
        }
        for(int y=0;y<6;y++){
            for(int x=0;x<4;x++){
                view.MCard[x][y].state = Card.CARD_CLOSE;
                view.mButton[x][y].setBackgroundResource(R.drawable.back);
            }
        }

        //view.start = (int)(System.currentTimeMillis()/1000);
    }
    public void run_sel2(){
        if(view.select1.color == view.select2.color){
            view.select1.state = Card.CARD_MATCHED;
            view.mButton[view.sel1x][view.sel1y].setBackgroundResource(view.d[view.MCard[view.sel1x][view.sel1y].color]);
            view.select2.state = Card.CARD_MATCHED;
            view.mButton[view.sel2x][view.sel2y].setBackgroundResource(view.d[view.MCard[view.sel2x][view.sel2y].color]);
            view.matchCount = view.matchCount +2;
            if((view.myturn%2)==view.player){
                if(view.clickcount<3) {
                    String score1 = view.score_p1.getText().toString();
                    int s1 = Integer.parseInt(score1) + 50;
                    view.score_p1.setText(String.valueOf(s1));
                }
                else if(view.clickcount<5) {
                    String score1 = view.score_p1.getText().toString();
                    int s1 = Integer.parseInt(score1) + 100;
                    view.score_p1.setText(String.valueOf(s1));
                }
                else {
                    String score1 = view.score_p1.getText().toString();
                    int s1 = Integer.parseInt(score1) + 150;
                    view.score_p1.setText(String.valueOf(s1));
                }
            }
            else {
                if(view.clickcount<3) {
                    String score2 = view.score_p2.getText().toString();
                    int s2 = Integer.parseInt(score2) + 50;
                    view.score_p2.setText(String.valueOf(s2));
                }
                else if(view.clickcount<5) {
                    String score2 = view.score_p2.getText().toString();
                    int s2 = Integer.parseInt(score2) + 100;
                    view.score_p2.setText(String.valueOf(s2));
                }
                else {
                    String score2 = view.score_p2.getText().toString();
                    int s2 = Integer.parseInt(score2) + 150;
                    view.score_p2.setText(String.valueOf(s2));
                }
            }
        }
        else{
            view.clickcount=0;
            try{
                Thread.sleep(500);
            }catch (Exception e){
            }
            view.select1.state=Card.CARD_CLOSE;
            view.mButton[view.sel1x][view.sel1y].setBackgroundResource(R.drawable.back);
            view.select2.state=Card.CARD_CLOSE;
            view.mButton[view.sel2x][view.sel2y].setBackgroundResource(R.drawable.back);
        }
        view.state = (view.matchCount<24)?(CardActivity.STATE_PLAY):(CardActivity.STATE_SHOW);
        if(view.matchCount==24)view.endGame();
        //view.end = (int)(System.currentTimeMillis()/1000);
    }
}