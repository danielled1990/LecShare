package com.busywww.myliveevent.lecshareClasses;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.busywww.myliveevent.LecShare;
import com.busywww.myliveevent.LecShareDB.UploadUserToSql;
import com.busywww.myliveevent.LecShareDB.User;
import com.busywww.myliveevent.LecShareDB.UserInfoSingelton;
import com.busywww.myliveevent.R;
import com.mysql.jdbc.PreparedStatement;

/**
 * Created by coral on 21/08/2016.
 */
public class Registeration extends Activity {

    Spinner spinner;
    ArrayAdapter<CharSequence> adapter;
    EditText name,email, pass, confPass;
    Button registerBtn;
    AlertDialog.Builder builder;
    String school;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.registeration_layout);
        name = (EditText)findViewById(R.id.name) ;
        email = (EditText)findViewById(R.id.email) ;
        pass = (EditText)findViewById(R.id.password) ;
        confPass = (EditText)findViewById(R.id.confirmPass) ;
        spinner = (Spinner)findViewById(R.id.school_spinner);
        adapter = ArrayAdapter.createFromResource(this,R.array.school_names,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                school = spinner.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        registerBtn = (Button) findViewById(R.id.register);
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(name.getText().toString().equals("") || email.getText().toString().equals("")|| pass.getText().toString().equals(""))
                {
                    builder = new AlertDialog.Builder(Registeration.this);
                    builder.setTitle("Something went wrong");
                    builder.setMessage("Please fill all the fields.");
                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
                else if(!(pass.getText().toString().equals(confPass.getText().toString())))
                {
                    builder = new AlertDialog.Builder(Registeration.this);
                    builder.setTitle("Something went wrong");
                    builder.setMessage("Password and Confirm password don't match.");
                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            pass.setText("");
                            confPass.setText("");
                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
                else //data provided by user is ok!
                {

                     BackgroundTask backgroundTask = new BackgroundTask(Registeration.this);
                     backgroundTask.execute("register",name.getText().toString(),email.getText().toString(),pass.getText().toString(), school);
                }
            }
        });

    }

//saves userInfo in singelton and to db

    public void insertSchedule(View view) {
        Intent intent = new Intent(Registeration.this,InsertSchedule.class);
        startActivity(intent);
    }
}
