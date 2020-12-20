package edu.skku.map.MAP_PP;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.zip.Inflater;

public class NoteFragment extends Fragment {

    TextView noPost;
    Switch aSwitch;

    public NoteFragment() {}

    private RecyclerViewListAdapter ra;
    private ArrayList<ListViewItem> list = new ArrayList<>();
    private RecyclerView recyclerView;
    private LinearLayoutManager mLayoutManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().findViewById(R.id.PhraseBook_header).setVisibility(View.VISIBLE);
        getActivity().findViewById(R.id.papago_header).setVisibility(View.GONE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_note, container, false);
        noPost = view.findViewById(R.id.noPosts);
        ra = new RecyclerViewListAdapter(getActivity(), list);
        recyclerView = (RecyclerView)view.findViewById(R.id.recyclerView);
        mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(ra);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
        aSwitch = view.findViewById(R.id.switch1);
        if(aSwitch.isChecked()){
            mLayoutManager.setReverseLayout(false);
            mLayoutManager.setStackFromEnd(false);
        }
        else{
            mLayoutManager.setReverseLayout(true);
            mLayoutManager.setStackFromEnd(true);
        }
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    mLayoutManager.setReverseLayout(false);
                    mLayoutManager.setStackFromEnd(false);
                }
                else{
                    mLayoutManager.setReverseLayout(true);
                    mLayoutManager.setStackFromEnd(true);
                }
            }
        });
        post();
        return view;
    }

    private ItemTouchHelper.SimpleCallback itemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return true;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder holder, int swipeDir) {
            final int position = holder.getAdapterPosition();
            ra.notifyItemRemoved(position);
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            String userId = user.getUid();
            final DatabaseReference userdata = database.getReference("Phrasebooks").child(userId);
            final ListViewItem items = list.get(position);
            if(list != null) {
                final Query userdataQuery = userdata.child("Studying").orderByChild("date").equalTo(items.getDate());
                userdataQuery.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot datasnapshot1 : dataSnapshot.getChildren()){
                            if(datasnapshot1.child("date").getValue() == items.getDate()){
                                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA);
                                Date now = new Date();
                                formatter.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
                                String date = formatter.format(now);
                                listdata listData = new listdata(datasnapshot1.child("date").getValue().toString(), datasnapshot1.child("input").getValue().toString(), datasnapshot1.child("output").getValue().toString(), date);
                                userdata.child("Swiped").push().setValue(listData);
                                datasnapshot1.getRef().removeValue();
                                list.remove(position);
                                ra.notifyDataSetChanged();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }

        }
    };


    class RecyclerViewListAdapter extends RecyclerView.Adapter<RecyclerViewListAdapter.Holder> {

        private Context context;
        private List<ListViewItem> list;
        private RadioButton lastCheckedRB = null;

        RecyclerViewListAdapter(Context context, List<ListViewItem> list) {
            this.context = context;
            this.list = list;
        }

        @NonNull
        @Override
        public RecyclerViewListAdapter.Holder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_row, parent, false);
            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerViewListAdapter.Holder holder, final int position) {
            ListViewItem items = list.get(position);

            if(list.get(position) != null) {
                holder.input.setText(items.getInput());
                holder.output.setText(items.getOutput());
                holder.date.setText(items.getDate());
                if(items.getLevel().equals("hard")){
                    holder.hard.setChecked(true);
                    holder.soso.setChecked(false);
                    holder.easy.setChecked(false);
                }
                if(items.getLevel().equals("soso")){
                    holder.hard.setChecked(false);
                    holder.soso.setChecked(true);
                    holder.easy.setChecked(false);
                }
                if(items.getLevel().equals("easy")){
                    holder.hard.setChecked(false);
                    holder.soso.setChecked(false);
                    holder.easy.setChecked(true);
                }
            }

            View.OnClickListener rbClick = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final RadioButton checked_rb = (RadioButton) v;

                    if(lastCheckedRB != null){
                        lastCheckedRB.setChecked(false);
                    }

                    lastCheckedRB = checked_rb;

                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    String userId = user.getUid();
                    final DatabaseReference userdata = database.getReference("Phrasebooks").child(userId).child("Studying");
                    final ListViewItem items = list.get(position);
                    if(list != null) {
                        final Query userdataQuery = userdata.orderByChild("date").equalTo(items.getDate());
                        userdataQuery.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for(DataSnapshot datasnapshot1 : dataSnapshot.getChildren()){
                                    if(datasnapshot1.child("date").getValue() == items.getDate()){
                                        int hard_id, soso_id, easy_id;
                                        hard_id = holder.hard.getId();
                                        soso_id = holder.soso.getId();
                                        easy_id = holder.easy.getId();
                                        if (lastCheckedRB.getId() == hard_id) {
                                            datasnapshot1.child("level").getRef().setValue("hard");
                                            items.setLevel("hard");
                                            holder.hard.setChecked(true);
                                            holder.soso.setChecked(false);
                                            holder.easy.setChecked(false);
                                        }
                                        if (lastCheckedRB.getId() == soso_id) {
                                            datasnapshot1.child("level").getRef().setValue("soso");
                                            items.setLevel("soso");
                                            holder.hard.setChecked(false);
                                            holder.soso.setChecked(true);
                                            holder.easy.setChecked(false);
                                        }
                                        if (lastCheckedRB.getId() == easy_id) {
                                            datasnapshot1.child("level").getRef().setValue("easy");
                                            items.setLevel("easy");
                                            holder.hard.setChecked(false);
                                            holder.soso.setChecked(false);
                                            holder.easy.setChecked(true);
                                        }
                                        ra.notifyDataSetChanged();
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });
                    }
                }
            };

            holder.hard.setOnClickListener(rbClick);
            holder.soso.setOnClickListener(rbClick);
            holder.easy.setOnClickListener(rbClick);

        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class Holder extends RecyclerView.ViewHolder{
            TextView input;
            TextView output;
            TextView date;
            RadioGroup radioGroup;
            RadioButton hard;
            RadioButton soso;
            RadioButton easy;

            Holder(View view){
                super(view);
                input = (TextView) view.findViewById(R.id.textView_input);
                output = (TextView) view.findViewById(R.id.textView_output);
                date = (TextView) view.findViewById(R.id.textView_date);
                radioGroup = (RadioGroup)view.findViewById(R.id.radioGroup);
                hard = (RadioButton) view.findViewById(R.id.hard);
                soso = (RadioButton) view.findViewById(R.id.soso);
                easy = (RadioButton) view.findViewById(R.id.easy);
            }
        }
    }

    private void post() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseDatabase mdatabase = FirebaseDatabase.getInstance();
        if (user == null) {
            // No session user
            Toast.makeText(getContext(), "Please log in to save data", Toast.LENGTH_SHORT).show();
        }
        assert user != null;
        String userId = user.getUid();
        String path = "Phrasebooks/" + userId + "/Studying/";
        mdatabase.getReference(path)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                        if(!dataSnapshot.exists()){
                            noPost.setVisibility(View.VISIBLE);
                        }
                        else{
                            ListViewItem listViewItem = dataSnapshot.getValue(ListViewItem.class);
                            list.add(listViewItem);
                            ra.notifyDataSetChanged();
                            recyclerView.scrollToPosition(list.size() - 1);
                            noPost.setVisibility(View.INVISIBLE);
                        }
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {
                        ra.notifyDataSetChanged();
                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    public static class listdata {
        public String date;
        public String input;
        public String output;
        public String swipedDate;

        public void listdata() {};

        public listdata(String date, String input, String output, String swipedDate) {
            this.date = date;
            this.input = input;
            this.output = output;
            this.swipedDate = swipedDate;
        }
    }
}
