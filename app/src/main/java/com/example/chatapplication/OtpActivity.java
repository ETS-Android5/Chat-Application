package com.example.chatapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.chatapplication.Adapters.UserObject;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class OtpActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    String codesent;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);
        mAuth= FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Authenticating...");
        progressDialog.setTitle("Verifying");
        String number =getIntent().getStringExtra("phonenumber");
        Log.d("phone","phone number="+number);
        sendVerificationCode(number);
    }
    public void back2(View view){
        Intent intent=new Intent(this,MainActivity.class);
        startActivity(intent);
    }
    public void next(View view){


        EditText editText= (EditText) findViewById(R.id.otp);
        String otp=editText.getText().toString().trim();

        if(otp.isEmpty()){
            editText.setError("Enter OTP");
            editText.requestFocus();
            return;
        }
        else if(otp.length()<6){
            editText.setError("OTP should be 6 Digits");
            editText.requestFocus();return;
        }
        else {
            verifyCode(otp);

        }

        Log.d("OTP","otp is "+otp);

    }
    private void sendVerificationCode(String number){
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                number,        // Phone number to verify
                10,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);

    }
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks
            =new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            String code=phoneAuthCredential.getSmsCode();
            if(code!=null){
                verifyCode(code);
            }
            //signInWithCredential(phoneAuthCredential);
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            Toast.makeText(OtpActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.d("error",e.getMessage());
        }

        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            codesent=s;
        }
    };
    private void verifyCode(String code){
        try {
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(codesent, code);
            signInWithCredential(credential);
            progressDialog.show();
        }
        catch (Exception e) {
            Log.i("exception",e.toString());
            Toast.makeText(OtpActivity.this,"Invalid credentials",Toast.LENGTH_LONG).show();
            progressDialog.dismiss();
        }
    }
    private void signInWithCredential(PhoneAuthCredential credential){
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            progressDialog.cancel();
                            Intent intent = new Intent(OtpActivity.this, MainActivity2.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }
                         else {
                            Toast.makeText(OtpActivity.this, "Invalid Otp", Toast.LENGTH_SHORT).show();
                            progressDialog.cancel();
                            // Sign in failed, display a left_message and update the UI
                            Log.w("signinfail", "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(OtpActivity.this, "Invalid Otp", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }
}