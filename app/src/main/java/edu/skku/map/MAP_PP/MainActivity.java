package edu.skku.map.MAP_PP;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    DrawerLayout drawerLayout;
    String username_str;
    MenuItem home;
    MenuItem pastPhrases;
    MenuItem logout;
    AppCompatTextView user_name;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar tb = (Toolbar) findViewById(R.id.toolbar2);
        setSupportActionBar(tb);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        ViewPager2 viewPager2 = findViewById(R.id.viewpager);
        viewPager2.setAdapter(new myFragmentStateAdapter(this, username_str));

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

        // MenuItem
        home = menu.findItem(R.id.navigationHome);
        pastPhrases = menu.findItem(R.id.navigationPastPhrases);
        logout = menu.findItem(R.id.navigationLogout);

        navigationView.setNavigationItemSelectedListener(this);
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, tb, R.string.app_name, R.string.app_name);
        drawerToggle.syncState();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawerLayout.closeDrawer(GravityCompat.START);

        switch (item.getItemId()){
            case R.id.navigationHome:
                break;

            case R.id.navigationPastPhrases:
                Intent intent = new Intent(MainActivity.this, SwipedItemsActivity.class);
                startActivity(intent);
                break;

            case R.id.navigationLogout:
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    // User is signed in
                    FirebaseAuth.getInstance().signOut();
                    Intent i = new Intent(MainActivity.this, LoginActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                } else {
                    // User is signed out
                    Toast.makeText(MainActivity.this, "You are already logged out", Toast.LENGTH_SHORT).show();
                }
                break;
        }
        return false;
    }

}
