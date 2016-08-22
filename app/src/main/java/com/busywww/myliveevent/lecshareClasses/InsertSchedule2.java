package com.busywww.myliveevent.lecshareClasses;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.busywww.myliveevent.R;

/**
 * Created by coral on 21/08/2016.
 */
public class InsertSchedule2 extends FragmentActivity {
    Spinner courseSpinner ,fromHourSpinner, toHourSpinner, daySpinner;
    ArrayAdapter<CharSequence> adapter_course , adapter_fromHour, adapter_toHour, adapter_day;

    Button button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule2_layout);

        courseSpinner = (Spinner)findViewById(R.id.course_spinner);
        adapter_course = ArrayAdapter.createFromResource(this,R.array.courses,android.R.layout.simple_spinner_item);
        adapter_course.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        courseSpinner.setAdapter(adapter_course);

        fromHourSpinner = (Spinner)findViewById(R.id.from_hour_spinner);
        adapter_fromHour = ArrayAdapter.createFromResource(this, R.array.hour,android.R.layout.simple_spinner_item);
        adapter_fromHour.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromHourSpinner.setAdapter(adapter_fromHour);

        toHourSpinner = (Spinner)findViewById(R.id.to_hour_spinner);
        adapter_toHour = ArrayAdapter.createFromResource(this,R.array.hour,android.R.layout.simple_spinner_item);
        adapter_toHour.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        toHourSpinner.setAdapter(adapter_toHour);

        daySpinner = (Spinner)findViewById(R.id.day_spinner);
        adapter_day = ArrayAdapter.createFromResource(this,R.array.days,android.R.layout.simple_spinner_item);
        adapter_day.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        daySpinner.setAdapter(adapter_day);

        button = (Button) findViewById(R.id.save_start);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(InsertSchedule2.this,MainActivity.class);
                finish();
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
    }
}
