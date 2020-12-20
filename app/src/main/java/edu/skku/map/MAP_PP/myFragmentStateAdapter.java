package edu.skku.map.MAP_PP;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

public class myFragmentStateAdapter extends FragmentStateAdapter {

    String username_str;

    public myFragmentStateAdapter(@NonNull FragmentActivity fragmentActivity, String username) {
        super(fragmentActivity);
        username_str = username;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Bundle bundle = new Bundle();
        bundle.putString("param1", username_str);

        switch(position){
            case 0:
                Fragment fragment = new HomeFragment();
                fragment.setArguments(bundle);
                return fragment;
            case 1:
                Fragment fragment2 = new NoteFragment();
                fragment2.setArguments(bundle);
                return fragment2;
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}