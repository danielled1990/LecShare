package com.busywww.myliveevent.lecshareClasses;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.widget.EditText;

import com.busywww.myliveevent.LecShareDB.UserInfoSingelton;
import com.busywww.myliveevent.R;
import com.busywww.myliveevent.LecShareDB.User;

import java.sql.SQLException;

/**
 * Created by coral on 21/08/2016.
 */
public class BackgroundTask extends AsyncTask<String,Void,String> {
    String register_url = "http://10.0.0.2/LecShare/register.php";
    String login_url = "http://10.0.0.2/LecShare/login.php";
    //String logout_url = "http://192.168.192.157/LecShare/logout.php";
    Context context;
    Activity activity;
    ProgressDialog progressDialog;
    AlertDialog.Builder builder;
    boolean login = false;
    boolean register = false;
    public BackgroundTask(Context context)
    {
        this.context = context;
        activity = (Activity)context;
    }

    @Override
    protected void onPreExecute() {
        builder = new AlertDialog.Builder(context);
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please wait");
        progressDialog.setMessage("Connecting to server...");
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    @Override
    protected String doInBackground(String... params) {
        String method = params[0];
      //  boolean flag = true;

        if(method.equals("register"))
        {
            register = true;

           // try {
           //     URL url = new URL(register_url);
           //     HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
           //     httpURLConnection.setRequestMethod("POST");
           //     httpURLConnection.setDoOutput(true);
           //     httpURLConnection.setDoInput(true);
           //     OutputStream outputStream = httpURLConnection.getOutputStream();
           //     BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream,"UTF-8"));

                String name = params[1];
                String email = params[2];
                String password = params[3];
                String school = params[4];

            UserInfoSingelton.getInstance().setUserSchool(school);
            UserInfoSingelton.getInstance().setUserName(name);
            UserInfoSingelton.getInstance().setUserPassword(password);
            UserInfoSingelton.getInstance().setUserEmail(email);
            User.SetUserInfoToDB(name,email,password,school);

              /*  String data = URLEncoder.encode("name","UTF-8")+"="+URLEncoder.encode(name,"UTF-8")+"&"+
                        URLEncoder.encode("email","UTF-8")+"="+URLEncoder.encode(email,"UTF-8")+"&"+
                        URLEncoder.encode("password","UTF-8")+"="+URLEncoder.encode(password,"UTF-8")+"&"+
                        URLEncoder.encode("school","UTF-8")+"="+URLEncoder.encode(school,"UTF-8");
                bufferedWriter.write(data);
                bufferedWriter.flush();
                bufferedWriter.close();
                outputStream.close();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                String line ="";
                while((line=bufferedReader.readLine())!=null)
                {
                    stringBuilder.append(line+"\n");
                }
                httpURLConnection.disconnect();
                Thread.sleep(5000);
                return stringBuilder.toString().trim();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
        }
        else if(method.equals("login")) {
            //try {
            //    URL url = new URL(login_url);
            //    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            //    httpURLConnection.setRequestMethod("POST");
            //    httpURLConnection.setDoOutput(true);
            //    httpURLConnection.setDoInput(true);
            //    OutputStream outputStream = httpURLConnection.getOutputStream();
            //    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream,"UTF-8"));
            String email = params[1];
            String password = params[2];
            try {
                login = createConnection(email,password);
                if(login)
                {
                    User.GetUserInfo(email,password);
                    return "success";

                }

            } catch (SQLException e) {
                e.printStackTrace();
            }



        }
        return null;
    }
    public boolean createConnection(String email,String password) throws SQLException {
        boolean flag =User.validateCredentials(email,password);

        return flag;
      //  String mail =EmailField

    }



    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(String json) {


             if(login)
            {
//                String code = "Login true";
//                String message = "Hello";
//                Intent intent = new Intent(activity,MainActivity.class);
//                intent.putExtra("message",message);
//                activity.startActivity(intent);
            }
            else if(!login && !register)
            {
                String code = "Login false";
                String message = "login failed";
                showDialog("Login Failed!", message, code);
            }

        if(register)
        {

            Intent intent = new Intent(activity,UserCourses.class);
            activity.startActivity(intent);
        }


    }

    public void showDialog(String title, String message, String code)
    {

        builder.setTitle(title);
        if (code.equals("reg_true") || code.equals("reg_false"))
        {
            builder.setMessage(message);
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    activity.finish();
                }
            });
        }
        else if(code.equals("login_false"))
        {
            builder.setMessage(message);
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    EditText email, password;
                    email = (EditText) activity.findViewById(R.id.EmailField);
                    password = (EditText) activity.findViewById(R.id.PasswordField);
                    email.setText("");
                    password.setText("");
                    dialog.dismiss();

                }
            });
        }
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
