package com.busywww.myliveevent.lecshareClasses;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.busywww.myliveevent.R;

/**
 * Created by coral on 21/08/2016.
 */
public class LoginActivity extends AppCompatActivity {
    Button signupBtn , loginBtn;
    AlertDialog.Builder builder;
    EditText email, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.login_layout);
        // Typeface typeface = Typeface.createFromAsset(getAssets(),"arlrdbd.ttf");
        // TextView welcomeText = (TextView) findViewById(R.id.welcome_user);
        // welcomeText.setTypeface(typeface);
        signupBtn=(Button) findViewById(R.id.register_button);
        signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,Registeration.class));
            }
        });

        email = (EditText) findViewById(R.id.EmailField);
        password = (EditText) findViewById(R.id.PasswordField);
        loginBtn=(Button) findViewById(R.id.login_btn);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( email.getText().toString().equals("") || password.getText().toString().equals("") ){
                    builder = new AlertDialog.Builder(LoginActivity.this);
                    builder.setTitle("Something went wrong");
                    builder.setMessage("Enter email and password");
                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
                else
                {
                    BackgroundTask backgroundTask = new BackgroundTask(LoginActivity.this);
                    backgroundTask.execute("login", email.getText().toString(), password.getText().toString());
                }
            }
        });
    }
}
