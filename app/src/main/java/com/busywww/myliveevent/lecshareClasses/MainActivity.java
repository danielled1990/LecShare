package com.busywww.myliveevent.lecshareClasses;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.busywww.myliveevent.AppSplash;
import com.busywww.myliveevent.LecShareDB.Course;
import com.busywww.myliveevent.LecShareDB.UserInfoSingelton;
import com.busywww.myliveevent.R;
import com.busywww.myliveevent.util.CustomOnItemSelectedListener;
import com.busywww.myliveevent.util.LessonSingelton;

import java.util.ArrayList;

/**
 * Created by coral on 21/08/2016.
 */
public class MainActivity extends AppCompatActivity {
    ArrayList<String> coursesNames;
    public static Course ChosenCourse;

    DrawerLayout drawerLayout;
    Toolbar toolbar;
    ActionBarDrawerToggle actionBarDrawerToggle;
    Spinner courseSpinner;
   // ArrayAdapter<CharSequence> adapterCourse;
    TextView textViewWelcome;
    NavigationView navigationView;
    FragmentTransaction fragmentTransaction;
    AlertDialog.Builder builder;
    Button button_join_live;
    Button button_watch_record;
    ImageButton button_start_new;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        //  requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.setDrawerListener(actionBarDrawerToggle);
        //actionBarDrawerToggle.syncState();

        fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.main_container, new HomeFragment());
        fragmentTransaction.commit();
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.logout_id:
                        builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("Logging out...");
                        builder.setMessage("Are you sure you want to logout?.");
                        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                BackgroundTask backgroundTask = new BackgroundTask(MainActivity.this);
                                backgroundTask.execute("logout");
                            }
                        });
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();

                }

                return false;
            }
        });


         addItemsOnCoursesSpinner(UserInfoSingelton.getInstance().getUserCourses());
         addListenerOnSpinnerCourseSelection();
         button_watch_record = (Button) findViewById(R.id.button_watch);
         button_watch_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setChosenCourse();
               // startActivity(new Intent(MainActivity.this,AppSplash.class));
            }
        });
//        button_start_new = (ImageButton) findViewById(R.id.add_button);
//        button_start_new.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                setChosenCourse();
//                //startActivity(new Intent(MainActivity.this,AppSplash.class));
//            }
//        });
        textViewWelcome = (TextView) findViewById(R.id.welcome_user);
        String message = getIntent().getStringExtra("message");
        textViewWelcome.setText(message);
        button_join_live = (Button) findViewById(R.id.button_join);
        button_join_live.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setChosenCourse();
                startActivity(new Intent(MainActivity.this,AppSplash.class));
            }
        });
    }

    private void setChosenCourse()
    {
        int pos =courseSpinner.getSelectedItemPosition();
        ChosenCourse = UserInfoSingelton.getInstance().getUserCourses().get(pos);
        LessonSingelton.getInstanceSingelton().setLessonCourse(ChosenCourse);
    }

    @Override
    protected void onPostCreate( Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        actionBarDrawerToggle.syncState();
    }

    public void showAlertWatchRecord(View view) {

        setChosenCourse();//**************
        AlertDialog.Builder myAlert = new AlertDialog.Builder(this);
        myAlert.setTitle("Live Streaming Now!");
        myAlert.setIcon(R.drawable.stream_black);
        myAlert.setMessage("Live streaming is happening now. Would you like to join?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(MainActivity.this,LoginActivity.class));
            }
        });
        myAlert.setNegativeButton("No", new DialogInterface.OnClickListener() { //נפתח שיעור חדש
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(MainActivity.this,AppSplash.class));
            }
        });
        myAlert.show();
    }
    public void goStream(View view) {
        Intent intent = new Intent(MainActivity.this,AppSplash.class);
        startActivity(intent);
    }

    private void addListenerOnSpinnerCourseSelection() {
        courseSpinner = (Spinner) findViewById(R.id.choose_course_spinner);
        ArrayAdapter<String> adapter_course = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, coursesNames);
        adapter_course.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        courseSpinner.setAdapter(adapter_course);
        courseSpinner.setOnItemSelectedListener(new CustomOnItemSelectedListener());

    }

    private void addItemsOnCoursesSpinner(ArrayList<Course> courses) {
        coursesNames = new ArrayList<>();

        for(int i=0;i<courses.size();i++)
        {
            coursesNames.add(courses.get(i).getCourseName());
        }


    }

}