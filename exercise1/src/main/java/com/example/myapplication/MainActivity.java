package com.example.myapplication;

import androidx.activity.result.ActivityResultRegistry;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    ListView lvStudents;
    List<Student> students = new ArrayList<>();
    ArrayAdapter<Student> arrayAdapter;
    AlertDialog ad;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(!isInternetConnected()){
            showNoInternetDialog();
        }

        lvStudents = findViewById(R.id.lvStudents);

        arrayAdapter = new ArrayAdapter<Student>(this, android.R.layout.two_line_list_item, android.R.id.text1, students){
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View itemView = super.getView(position, convertView, parent);

                TextView text1, text2;
                text1 = itemView.findViewById(android.R.id.text1);
                text2 = itemView.findViewById(android.R.id.text2);

                text1.setText(students.get(position).getName());
                text2.setText(students.get(position).getEmail());

                itemView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
                    @Override
                    public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
                        contextMenu.add(position, 123, 1, "Edit");
                        contextMenu.add(position, 124, 2, "Delete");
                    }
                });

                return itemView;
            }
        };

        lvStudents.setAdapter(arrayAdapter);

        registerReceiver(new Receiver(), new IntentFilter("NETWORK_CHANGE"));
        registerReceiver(new NetworkChangeReceiver(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    private void getStudents(){
        Toast.makeText(this, "Get students", Toast.LENGTH_SHORT).show();
        String url = getString(R.string.base_url) + "get-students.php";
        Log.d("getStudents", url);

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.d("onFailure", e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    String responseData = response.body().string();
                    JSONObject object = new JSONObject(responseData);
                    JSONArray data = object.getJSONArray("data");
                    Log.d("onResponse", responseData);
                    for(int i = 0; i < data.length(); i++){
                        Student student = new Student();
                        JSONObject eachStudent = data.getJSONObject(i);
                        student.setEmail(eachStudent.getString("email"));
                        student.setId(eachStudent.getString("id"));
                        student.setName(eachStudent.getString("name"));
                        student.setPhone(eachStudent.getString("phone"));
                        students.add(student);
                        Log.d("onResponse", "run: " + student.toString());
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            arrayAdapter.notifyDataSetChanged();
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void deleteStudent(String id){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(getString(R.string.base_url) + "delete-student.php")
                .post(new FormBody.Builder()
                        .add("id", id)
                        .build()
                ).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.d("Delete student", "onFailure: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                students.clear();
                getStudents();
            }
        });
    }

    private boolean isInternetConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    private void showNoInternetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Không có kết nối internet");
        builder.setMessage("Vui lòng kiểm tra kết nối internet của bạn và thử lại.");
        builder.setPositiveButton("OK", (dialog, which) -> finish());
        builder.setCancelable(false);
        ad = builder.show();
    }

    private class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(ad != null)
                ad.dismiss();
            getStudents();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, 12312, 1, "Add student")
            .setIcon(getDrawable(R.drawable.ic_baseline_add_24))
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        int position = item.getGroupId();
        switch (item.getItemId()){
            case 123:
                String name = students.get(position).getName(),
                        email = students.get(position).getEmail(),
                        phone = students.get(position).getPhone(),
                        id = students.get(position).getId();
                Intent intent = new Intent(this, EditStudentActivity.class);
                intent.putExtra("id", id);
                intent.putExtra("name", name);
                intent.putExtra("email", email);
                intent.putExtra("phone", phone);

                startActivityForResult(intent, 6132);
                break;
            case 124:
                AlertDialog alertDialog = new AlertDialog.Builder(this)
                        .setMessage("Bạn có muốn xóa sinh viên này?")
                        .setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).setPositiveButton("Xóa", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                deleteStudent(students.get(position).getId());
                            }
                        }).show();
                break;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case 12312:
                Intent intent = new Intent(this, AddStudentActivity.class);
                startActivityForResult(intent, 123);
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 123 && resultCode == RESULT_OK){
            students.clear();
            getStudents();
        }else if(requestCode == 6132 && resultCode == RESULT_OK){
            students.clear();
            getStudents();
        }
    }
}