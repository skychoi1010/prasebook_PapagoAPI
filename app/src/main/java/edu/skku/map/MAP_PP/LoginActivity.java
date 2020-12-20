package edu.skku.map.MAP_PP;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    AppCompatEditText email;
    AppCompatEditText pwd;
    UserData userdataStorage;
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        AppCompatTextView login = findViewById(R.id.login);
        AppCompatTextView createAccount = findViewById(R.id.createAccount);
        AppCompatTextView skip = findViewById(R.id.skip);
        email = findViewById(R.id.email_login);
        pwd = findViewById(R.id.pwd_login);
        mAuth = FirebaseAuth.getInstance();
        if (user != null) {
            // User is signed in
            final Intent intent = getIntent();
            if(intent.getFlags() == Intent.FLAG_ACTIVITY_NO_HISTORY){
                intent.removeFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                email.setText(intent.getStringExtra("email"));
                pwd.setText(intent.getStringExtra("pwd"));
                login.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String email_str = email.getText().toString().trim();
                        String pwd_str = pwd.getText().toString().trim();

                        Toast.makeText(LoginActivity.this, "Authentication failed",
                                Toast.LENGTH_SHORT).show();
                        mAuth.signInWithEmailAndPassword(email_str, pwd_str)
                                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        final String username;
                                        if (!task.isSuccessful()) {
                                            Toast.makeText(LoginActivity.this, "Authentication failed",
                                                    Toast.LENGTH_SHORT).show();
                                        } else {
                                            username = mAuth.getCurrentUser().getDisplayName();
                                            setUsername(username);
                                            Intent intent2 = new Intent(LoginActivity.this, MainActivity.class);
                                            startActivity(intent2);
                                        }
                                    }
                                });
                    }
                });
            }
            else {
                Intent i = new Intent(LoginActivity.this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
            }
        } else {
            // User is signed out
        }

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email_str = email.getText().toString().trim();
                String pwd_str = pwd.getText().toString().trim();

                mAuth.signInWithEmailAndPassword(email_str, pwd_str)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                final String username;
                                if (!task.isSuccessful()) {
                                    Toast.makeText(LoginActivity.this, "Authentication failed",
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    username = mAuth.getCurrentUser().getDisplayName();
                                    setUsername(username);
                                    Intent intent2 = new Intent(LoginActivity.this, MainActivity.class);
                                    startActivity(intent2);
                                }
                            }
                        });
            }
        });
        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(getApplicationContext(), CreateAccountActivity.class);
                startActivity(intent1);
            }
        });
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInAnonymously();
            }
        });
    }

    public void setUsername(String username) {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        //Toast.makeText(handbook_start.this, "username"+ username, Toast.LENGTH_SHORT).show();
        if (user != null) {
            // User is signed in
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(username)
                    .build();

            user.updateProfile(profileUpdates)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                //successful
                            }
                        }
                    });
        } else {
            Toast.makeText(LoginActivity.this, "no user signed in", Toast.LENGTH_SHORT).show();
        }
    }

    private void signInAnonymously(){
        mAuth.signInAnonymously().addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    setUsername("Anonymous");
                    FirebaseUser user = mAuth.getInstance().getCurrentUser();
                    String userId = user.getUid();
                    //Example you need save a Store in
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference userdata = database.getReference("UserData");
                    userdataStorage = new UserData(null, "Anonymous", null);
                    userdata.child(userId).setValue(userdataStorage);
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(LoginActivity.this, "login error", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public class UserData {
        public String email;
        public String name;
        public String password;

        public UserData(){};

        public UserData(String email, String name, String password) {
            this.email = email;
            this.name = name;
            this.password = password;
        }
    }
}
