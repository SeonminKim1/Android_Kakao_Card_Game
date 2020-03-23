package kr.ac.hansung.example.cardproject;


public class User {
    String ID;
    int RankScore;
    int Mint;

    public User(String id, int rankscore, int mint) {
        this.ID = id;
        this.RankScore = rankscore;
        this.Mint = mint;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public int getScore() { return RankScore; }

    public void setScore(int score) {
        RankScore = score;
    }

    public int getMint() {
        return Mint;
    }

    public void setMint(int mint) {
        Mint = mint;
    }

}
