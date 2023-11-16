package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AddStudentActivity extends AppCompatActivity {
    EditText edtName, edtPhone, edtEmail;
    Button btnSave;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_student);

        edtName = findViewById(R.id.editTextName);
        edtPhone = findViewById(R.id.editTextEmail);
        edtEmail = findViewById(R.id.editTextPhone);
        btnSave = findViewById(R.id.btnSave);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addStudent(
                        edtName.getText().toString(),
                        edtPhone.getText().toString(),
                        edtEmail.getText().toString()
                );
            }
        });
    }

    private void addStudent(String name, String email, String phone){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(getString(R.string.base_url) + "add-student.php")
                .post(
                        new FormBody.Builder()
                                .add("name", name)
                                .add("email", email)
                                .add("phone", phone)
                                .build()
                ).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.d("Add student", "onFailure: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    String responseData = response.body().string();
                    JSONObject object = new JSONObject(responseData);

                    setResult(RESULT_OK);
                    finish();
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}