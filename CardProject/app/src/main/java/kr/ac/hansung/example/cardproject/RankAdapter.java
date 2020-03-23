package kr.ac.hansung.example.cardproject;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by seong on 2018-11-15.
 */

public class RankAdapter extends ArrayAdapter<User> {

    LayoutInflater minflater = LayoutInflater.from(getContext());
    ArrayList<User> mlist;

    TextView Myid;
    TextView Myrank;
    TextView Rankscore;

    public RankAdapter(Context context, ArrayList<User> list) {
        super(context, R.layout.userlist, list);
        mlist = list;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        View ranklayout = minflater.inflate(R.layout.userlist, parent, false);

        Myrank = ranklayout.findViewById(R.id.myrank_textview);
        Myid = ranklayout.findViewById(R.id.myid_textview);
        Rankscore = ranklayout.findViewById(R.id.rankscore_textview);


        Myrank.setText((i+1)+"");
        Myid.setText(mlist.get(i).getID());
        Rankscore.setText(Integer.toString(mlist.get(i).getScore()));

        return ranklayout;
    }

}
