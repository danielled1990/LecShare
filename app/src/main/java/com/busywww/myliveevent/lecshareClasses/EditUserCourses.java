package com.busywww.myliveevent.lecshareClasses;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;

import com.busywww.myliveevent.LecShareDB.Course;
import com.busywww.myliveevent.LecShareDB.CourseUpdate;
import com.busywww.myliveevent.LecShareDB.UploadCourseToSql;
import com.busywww.myliveevent.LecShareDB.UserInfoSingelton;
import com.busywww.myliveevent.R;
import com.busywww.myliveevent.util.Constants;
import com.busywww.myliveevent.util.CustomOnItemSelectedListener;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Created by Alona on 9/4/2016.
 */
public class EditUserCourses extends Activity {

    Spinner deleteCoursesSpinner;
    Spinner addCoursesSpinner;
    ImageButton returnButton , saveButton;
    Button add_button, delete_button;
    boolean toDelete =false;
    boolean toUpdate = false;

  //  ArrayList<Course> courses;
    ArrayList<String> coursesNames;
    ArrayList<String> userCoursesNames;
    UserInfoSingelton userData ;

    private String course;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_courses);
        coursesNames = new ArrayList<>();
        userCoursesNames = new ArrayList<>();
        userData = UserInfoSingelton.getInstance();

        try {
            UploadCourseToSql getCourses = new UploadCourseToSql(Constants.GET_COURSES, UserInfoSingelton.getInstance().getUserSchool());

            CourseUpdate.Courses = getCourses.execute().get();
        }

        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        catch(ExecutionException e)
        {
            e.printStackTrace();

        }

        coursesNames = UserCourses.addItemsOnCoursesSpinner(CourseUpdate.Courses);
        userCoursesNames = UserCourses.addItemsOnCoursesSpinner(userData.getUserCourses());

        addListenerOnSpinnerCourseSelection();
        addListenerOnSpinnerCourseDeleteSelection();
        addListenerOnButtons();


    }

    private void addListenerOnButtons()
    {

        add_button = (Button) findViewById(R.id.buttonAdd);
        add_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                toUpdate = true;
                course = String.valueOf(addCoursesSpinner.getSelectedItem());
                int pos = addCoursesSpinner.getSelectedItemPosition();
                userData.AddCourseToUpdate(CourseUpdate.Courses.get(pos));


            }
        });

        delete_button=(Button) findViewById(R.id.buttonDelete);
        delete_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                toDelete = true;
                course = String.valueOf(deleteCoursesSpinner.getSelectedItem());
                int pos = deleteCoursesSpinner.getSelectedItemPosition();
                userData.AddCourseToDelete(CourseUpdate.Courses.get(pos));


            }
        });


        saveButton = (ImageButton) findViewById(R.id.imageButtonSave);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(toDelete && (userData.getCoursesToDelete().size() > 0))
                {
                    UploadCourseToSql uploadTask = new UploadCourseToSql(Constants.DELETE_USER_COURSES,userData.getUserSchool());
                    uploadTask.execute();
                    toDelete = false;

                }
                if(toUpdate && (userData.getCoursesToUpdate().size()>0))
                {
                    UploadCourseToSql uploadTask = new UploadCourseToSql(Constants.UPDATE_COURSES,userData.getUserSchool());
                    uploadTask.execute();
                    toUpdate = false;
                }


            }
        });


        returnButton = (ImageButton) findViewById(R.id.imageButtonBack);
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String message = "Hello " + UserInfoSingelton.getInstance().getUserName() ;
                Intent intent = new Intent(EditUserCourses.this,MainActivity.class);
                intent.putExtra("message",message);
                EditUserCourses.this.startActivity(intent);

            }
        });



    }

    private void addListenerOnSpinnerCourseSelection() {

        addCoursesSpinner = (Spinner) findViewById(R.id.spinnerAdd);
        ArrayAdapter<String> adapter_course = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,coursesNames );
        adapter_course.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        addCoursesSpinner.setAdapter(adapter_course);
        addCoursesSpinner.setOnItemSelectedListener(new CustomOnItemSelectedListener());

    }

    private void addListenerOnSpinnerCourseDeleteSelection() {
        deleteCoursesSpinner = (Spinner) findViewById(R.id.spinnerDelete);
        ArrayAdapter<String> adapter_semester = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item,userCoursesNames );
        adapter_semester.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        deleteCoursesSpinner.setAdapter(adapter_semester);
        deleteCoursesSpinner.setOnItemSelectedListener(new CustomOnItemSelectedListener());

    }









}
