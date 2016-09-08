package com.busywww.myliveevent.lecshareClasses;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.busywww.myliveevent.LecShareDB.Course;
import com.busywww.myliveevent.LecShareDB.CourseUpdate;
import com.busywww.myliveevent.LecShareDB.UploadCourseToSql;
import com.busywww.myliveevent.LecShareDB.UserInfoSingelton;
import com.busywww.myliveevent.R;
import com.busywww.myliveevent.util.CustomOnItemSelectedListener;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Created by Alona on 8/29/2016.
 */
public class UserCourses extends Activity {

    private static final int SET_COURSES = 1;
    private static final int GET_COURSES = 0;
    private static final int SET_COURSER_USER = 2;

    UserInfoSingelton userData = UserInfoSingelton.getInstance();

    ArrayList<Course> mAllCourses;
    ArrayList<String> coursesNames;
    ArrayList<String> semesters;

    Button AddButtn;
    Button saveAndSendButtn;
    Spinner courseSpinner, semesterSpinner;

    private String semester;
    private String course;

    TextView name;
    TextView sem;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_courses);

        coursesNames = new ArrayList<>();
        try {
            UploadCourseToSql getCourses = new UploadCourseToSql(GET_COURSES, UserInfoSingelton.getInstance().getUserSchool());

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
       // CourseUpdate.Courses = UserInfoSingelton.getInstance().getCourses();

        if(CourseUpdate.Courses.size() != 0)
        {
            coursesNames = addItemsOnCoursesSpinner(CourseUpdate.Courses);
            addItemsOnSemesterSpinner(CourseUpdate.Courses);
        }

        addListenerOnButtons();
        addListenerOnSpinnerCourseSelection();
        addListenerOnSpinnerSemesterSelection();

    }


    public static ArrayList<String> addItemsOnCoursesSpinner(ArrayList<Course> courses) {
      ArrayList<String>  coursesNames = new ArrayList<>();

        for(int i=0;i<courses.size();i++)
        {
            coursesNames.add(courses.get(i).getCourseName());
        }

        return  coursesNames;
    }

    private void addItemsOnSemesterSpinner(ArrayList<Course> courses) {
        semesters = new ArrayList<>();
        semesters.add("A");
        semesters.add("B");
        semesters.add("C");
    }

    private void addListenerOnButtons() {

        AddButtn = (Button) findViewById(R.id.buttonAddCorces);
        courseSpinner = (Spinner) findViewById(R.id.course_spinner);
        semesterSpinner = (Spinner) findViewById(R.id.semester_spinner);

        AddButtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                course = String.valueOf(courseSpinner.getSelectedItem());
                int pos =courseSpinner.getSelectedItemPosition();
                userData.AddUserCourse(CourseUpdate.Courses.get(pos));


                semester = String.valueOf(semesterSpinner.getSelectedItem());
                addRow();
            }
        });


        saveAndSendButtn = (Button) findViewById(R.id.save_start);
        saveAndSendButtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //DB courses to student table update
                UploadCourseToSql getCourses = new UploadCourseToSql(SET_COURSER_USER,userData.getUserSchool());
                getCourses.execute();
                String username = UserInfoSingelton.getInstance().getUserName();
                Intent intent = new Intent(UserCourses.this, MainActivity.class);
                intent.putExtra("message", String.format("Hello %s", username));
                finish();
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

            }
        });

    }

    private void addListenerOnSpinnerCourseSelection() {
        courseSpinner = (Spinner) findViewById(R.id.course_spinner);
        ArrayAdapter<String> adapter_course = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, coursesNames);
        adapter_course.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        courseSpinner.setAdapter(adapter_course);
        courseSpinner.setOnItemSelectedListener(new CustomOnItemSelectedListener());

    }

    private void addListenerOnSpinnerSemesterSelection() {
        semesterSpinner = (Spinner) findViewById(R.id.semester_spinner);
        ArrayAdapter<String> adapter_semester = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, semesters);
        adapter_semester.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        semesterSpinner.setAdapter(adapter_semester);
        courseSpinner.setOnItemSelectedListener(new CustomOnItemSelectedListener());

    }


    private void addRow() {

        TableLayout tbl = (TableLayout) findViewById(R.id.table_layout);


        TableRow row = new TableRow(this);
        row.setBackgroundColor(Color.WHITE);
       // row.setPadding("5dp");


        name = new TextView(this);
        name.setBackgroundColor(Color.WHITE);
        name.setLayoutParams(
                new TableRow.LayoutParams(
                        TableRow.LayoutParams.WRAP_CONTENT,
                        TableRow.LayoutParams.MATCH_PARENT,1.0f));
        name.setText(course);

        sem = new TextView(this);
        sem.setBackgroundColor(Color.WHITE);
        sem.setLayoutParams(
                new TableRow.LayoutParams(
                        TableRow.LayoutParams.WRAP_CONTENT,
                        TableRow.LayoutParams.MATCH_PARENT,1.0f));

        sem.setText(semester);

        row.addView(name);
        row.addView(sem);
        tbl.addView(row,new TableLayout.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));

    }

}






