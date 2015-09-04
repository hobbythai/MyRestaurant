package com.hobbythai.myrestaurant;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {

    //explicit
    private UserTABLE objUserTABLE;
    private FoodTABLE objFoodTABLE;
    private EditText userEditText, passwordEditText;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //bind widget
        bindWidget();

        //create connect database
        createAndConnectedDatabase();

        //tester add value
        //testerAddValue();

        //delete all data
        deleteAllData();

        //synchronize JSON to SQLite
        synJASONtoSQLite();


    }//on create

    private void bindWidget() {
        userEditText = (EditText) findViewById(R.id.editText);
        passwordEditText = (EditText) findViewById(R.id.editText2);
    }


    public void clickLogin(View view) {

        String strUser = userEditText.getText().toString().trim();
        String strPassword = passwordEditText.getText().toString().trim();

        //chk zero
        if (strUser.equals("") || strPassword.equals("")) {

            //have space
            errorDialog("Have Space","Please Fill All Blank");

        } else {

            //no space
            try {

                String[] strMyResult = objUserTABLE.searchUser(strUser);
                if (strPassword.equals(strMyResult[2])) {
                    welcomeDialog(strMyResult[3]);
                } else {
                    errorDialog("Password False", "Pls Try Again");
                }

            } catch (Exception e) {
                errorDialog("ไม่มี User", "ไม่มี "+strUser +"ในฐานข้อมูล");
            }

        }//if

    }//click login

    private void welcomeDialog(String strName) {
        AlertDialog.Builder objBuilder = new AlertDialog.Builder(this);
        objBuilder.setIcon(R.drawable.danger);
        objBuilder.setTitle("Welcome");
        objBuilder.setMessage("Welcome" + strName + "\n" + "To Our Shop");
        objBuilder.setCancelable(false);
        objBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

    }

    private void errorDialog(String strTitle, String strMessage) {

        AlertDialog.Builder objBuilder = new AlertDialog.Builder(this);
        objBuilder.setIcon(R.drawable.danger);
        objBuilder.setTitle(strTitle);
        objBuilder.setMessage(strMessage);
        objBuilder.setCancelable(false);
        objBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        objBuilder.show();

    }//error dialog


    private void synJASONtoSQLite() {

        //change policy
        StrictMode.ThreadPolicy objThreadPolicy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(objThreadPolicy);

        int intTime = 0;
        while (intTime<=1) {

            InputStream objInputStream = null;
            String strJASON = null;
            String strUserURL = "http://www.swiftcodingthai.com/3sep/php_get_data_master.php";
            String strFoodURL = "http://www.swiftcodingthai.com/3sep/php_get_data_food.php";
            HttpPost objHttpPost = null;

            //1.create input stream
            try {

                HttpClient objHttpClient = new DefaultHttpClient();
                if (intTime != 1) {
                    objHttpPost = new HttpPost(strUserURL);
                } else {
                    objHttpPost = new HttpPost(strFoodURL);
                }//if

                HttpResponse objHttpResponse = objHttpClient.execute(objHttpPost);
                HttpEntity objHttpEntity = objHttpResponse.getEntity();
                objInputStream = objHttpEntity.getContent();

            } catch (Exception e) {
                Log.d("Rest", "Input ==> " + e.toString());
            }//try

            //2.create JSON
            try {

                BufferedReader objBufferedReader = new BufferedReader(new InputStreamReader(objInputStream, "UTF-8"));
                StringBuilder objStringBuilder = new StringBuilder();
                String strLine = null;
                while ((strLine = objBufferedReader.readLine()) != null) {
                    objStringBuilder.append(strLine);
                }//while
                objInputStream.close();
                strJASON = objStringBuilder.toString();

            } catch (Exception e) {
                Log.d("Rest", "JSON ==> " + e.toString());
            }

            //3.update to SQLite
            try {

                final JSONArray objJsonArray = new JSONArray(strJASON);
                for (int i = 0; i < objJsonArray.length(); i++) {
                    JSONObject jsonObject = objJsonArray.getJSONObject(i);
                    if (intTime != 1) {
                        String strUser = jsonObject.getString("User");
                        String strPassword = jsonObject.getString("Password");
                        String strName = jsonObject.getString("Name");
                        objUserTABLE.addNewUser(strUser, strPassword, strName);
                    } else {
                        String strFood = jsonObject.getString("Food");
                        String strSource = jsonObject.getString("Source");
                        String strPrice = jsonObject.getString("Price");
                        objFoodTABLE.addFood(strFood, strSource, strPrice);
                    }
                }

            } catch (Exception e) {
                Log.d("Rest", "Update ==> "+e.toString());
            }

            intTime += 1;
        }//while
    }

    private void deleteAllData() {
        SQLiteDatabase objSqLiteDatabase = openOrCreateDatabase("Restaurant.db", MODE_PRIVATE, null);
        objSqLiteDatabase.delete("userTABLE", null, null);
        objSqLiteDatabase.delete("foodTABLE", null, null);
    }

    private void testerAddValue() {
        objUserTABLE.addNewUser("testUser", "12345", "kullachat");
        objFoodTABLE.addFood("กระเพรา", "testSouce", "1234");
    }

    private void createAndConnectedDatabase() {
        objUserTABLE = new UserTABLE(this);
        objFoodTABLE = new FoodTABLE(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}//main
