package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EditStudentActivity extends AppCompatActivity {
    EditText edtName, edtPhone, edtEmail;
    Button btnSave;
    String id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_student);

        edtName = findViewById(R.id.editTextName);
        edtPhone = findViewById(R.id.editTextEmail);
        edtEmail = findViewById(R.id.editTextPhone);
        btnSave = findViewById(R.id.btnSave);

        Bundle data = getIntent().getExtras();
        edtEmail.setText(data.getString("email"));
        edtName.setText(data.getString("name"));
        edtPhone.setText(data.getString("phone"));
        id = data.getString("id");

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateStudent(
                        edtEmail.getText().toString(),
                        edtPhone.getText().toString(),
                        edtName.getText().toString()
                );
            }
        });
    }

    private void updateStudent(String email, String phone, String name) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(getString(R.string.base_url) + "update-student.php")
                .post(new FormBody.Builder()
                        .add("id", id)
                        .add("phone", phone)
                        .add("name", name)
                        .add("email", email)
                        .build()
                ).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.d("Update student", "onFailure: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                setResult(RESULT_OK);
                finish();
            }
        });
    }
}