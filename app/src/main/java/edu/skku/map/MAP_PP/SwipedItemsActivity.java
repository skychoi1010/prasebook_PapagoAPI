package edu.skku.map.MAP_PP;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;
import java.util.List;

public class SwipedItemsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    DrawerLayout drawerLayout;
    String username_str;
    MenuItem home;
    MenuItem pastPhrases;
    MenuItem logout;
    AppCompatTextView user_name;
    TextView noPost;
    Switch aSwitch;
    LinearLayoutManager mLayoutManager;
    private SwipedItemsActivity.RecyclerViewListAdapter2 ra;
    private ArrayList<SwipedListViewItem> list = new ArrayList<>();
    private RecyclerView recyclerView2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.swiped_items);
        noPost = findViewById(R.id.noPosts2);
        Toolbar tb = (Toolbar) findViewById(R.id.toolbar2);
        setSupportActionBar(tb);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.drawer);
        View header = ((NavigationView)findViewById(R.id.drawer)).getHeaderView(0);

        user_name = header.findViewById(R.id.user_name);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            // No session user
            user_name.setText("Anonymous, please log in to save data");
        }
        else if (user.isAnonymous()){
            user_name.setText("Anonymous");
        }
        else{
            username_str = user.getDisplayName();
            user_name.setText(username_str);
        }

        Menu menu = navigationView.getMenu();

        // find MenuItem you want to change
        home = menu.findItem(R.id.navigationHome);
        pastPhrases = menu.findItem(R.id.navigationPastPhrases);
        logout = menu.findItem(R.id.navigationLogout);

        navigationView.setNavigationItemSelectedListener(this);
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, tb, R.string.app_name, R.string.app_name);
        drawerToggle.syncState();

        ra = new SwipedItemsActivity.RecyclerViewListAdapter2(getApplicationContext(), post());
        recyclerView2 = (RecyclerView)findViewById(R.id.recyclerView2);
        mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
        recyclerView2.setLayoutManager(mLayoutManager);
        recyclerView2.setHasFixedSize(true);
        recyclerView2.setAdapter(ra);
        aSwitch = findViewById(R.id.switch2);
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
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawerLayout.closeDrawer(GravityCompat.START);

        switch (item.getItemId()){
            case R.id.navigationHome:
                Intent intent = new Intent(SwipedItemsActivity.this, MainActivity.class);
                startActivity(intent);
                break;

            case R.id.navigationPastPhrases:
                break;

            case R.id.navigationLogout:
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    // User is signed in
                    FirebaseAuth.getInstance().signOut();
                    Intent i = new Intent(SwipedItemsActivity.this, LoginActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                } else {
                    // User is signed out
                    Toast.makeText(SwipedItemsActivity.this, "You are already logged out", Toast.LENGTH_SHORT).show();
                }
                break;
        }


        return false;
    }

    class RecyclerViewListAdapter2 extends RecyclerView.Adapter<SwipedItemsActivity.RecyclerViewListAdapter2.Holder> {

        private Context context;
        private List<SwipedListViewItem> list;

        public RecyclerViewListAdapter2(Context context, List<SwipedListViewItem> list) {
            this.context = context;
            this.list = list;
        }

        @Override
        public SwipedItemsActivity.RecyclerViewListAdapter2.Holder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.swiped_item_row, parent, false);
            SwipedItemsActivity.RecyclerViewListAdapter2.Holder holder = new SwipedItemsActivity.RecyclerViewListAdapter2.Holder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(final SwipedItemsActivity.RecyclerViewListAdapter2.Holder holder, final int position) {
            SwipedListViewItem items = list.get(position);
            if(list.get(position) != null) {
                holder.input.setText(items.getInput());
                holder.output.setText(items.getOutput());
                holder.date.setText(items.getDate());
                holder.swiped_date.setText("Finished at : " + items.getSwipedDate());
            }
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        public class Holder extends RecyclerView.ViewHolder{
            public TextView input;
            public TextView output;
            public TextView date;
            public TextView swiped_date;

            public Holder(View view){
                super(view);
                input = (TextView) view.findViewById(R.id.textView_input);
                output = (TextView) view.findViewById(R.id.textView_output);
                date = (TextView) view.findViewById(R.id.textView_date);
                swiped_date = (TextView) view.findViewById(R.id.textView_date2);
            }
        }
    }

    public List<SwipedListViewItem> post() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseDatabase mdatabase = FirebaseDatabase.getInstance();
        if (user == null) {
            // No session user
            Toast.makeText(getApplicationContext(), "Please log in to save data", Toast.LENGTH_SHORT).show();
        }
        String userId = user.getUid();
        String path = "Phrasebooks/" + userId + "/Swiped/";
        mdatabase.getReference(path)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        if(!dataSnapshot.exists()){
                            noPost.setVisibility(View.VISIBLE);
                        }
                        else{
                            noPost.setVisibility(View.INVISIBLE);
                            SwipedListViewItem listData = dataSnapshot.getValue(SwipedListViewItem.class);
                            list.add(listData);
                            ra.notifyDataSetChanged();
                            recyclerView2.scrollToPosition(ra.getItemCount() - 1);
                        }
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                        ra.notifyDataSetChanged();
                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        return list;
    }

}
