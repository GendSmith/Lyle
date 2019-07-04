package com.example.mxz.lyle;



        import android.content.DialogInterface;
        import android.content.Intent;
        import android.support.v7.app.AlertDialog;
        import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
        import android.view.*;
        import android.widget.EditText;
        import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    private UserServer userServer = new UserServer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if(!userServer.isConnected()) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            AlertDialog alertDialog = dialogBuilder.setTitle("警告")
                    .setMessage("无法连接到服务器，请检查网络设置！")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }).create();
            alertDialog.setCancelable(false);
            alertDialog.show();
        }

    }


    public void loginButton(View v) {
        EditText editText = (EditText)findViewById(R.id.editText);
        String username = editText.getText().toString();
        if(username.length() < 4 || username.length() > 32) {
            Toast.makeText(this, "用户名长度不合法！", Toast.LENGTH_SHORT).show();
            editText.requestFocus();
            return;
        }
        EditText editText2 = (EditText)findViewById(R.id.editText2);
        String password = editText2.getText().toString();
        if(password.length() < 4 || password.length() > 32) {
            Toast.makeText(this, "密码长度不合法！", Toast.LENGTH_SHORT).show();
            editText2.requestFocus();
            return;
        }
        int id = userServer.getIdByUsername(username);
        if(id == -1) {
            Toast.makeText(this, "用户名不存在，您可能想要注册？", Toast.LENGTH_SHORT).show();
            editText.requestFocus();
            return;
        }
        String truePassword = null;
        try { truePassword = userServer.getUserById(id).getString("password"); }catch (Exception e) { e.printStackTrace(); }
        if(truePassword == null || !password.equals(truePassword)) {
            Toast.makeText(this, "密码错误！", Toast.LENGTH_SHORT).show();
            editText2.requestFocus();
            return;
        }
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra("id", id);
        startActivity(intent);
    }

    public void registerButton(View v) {
        EditText editText = (EditText)findViewById(R.id.editText);
        String username = editText.getText().toString();
        if(username.length() < 4 || username.length() > 32) {
            Toast.makeText(this, "用户名长度不合法！", Toast.LENGTH_SHORT).show();
            editText.requestFocus();
            return;
        }
        EditText editText2 = (EditText)findViewById(R.id.editText2);
        String password = editText2.getText().toString();
        if(password.length() < 4 || password.length() > 32) {
            Toast.makeText(this, "密码长度不合法！", Toast.LENGTH_SHORT).show();
            editText2.requestFocus();
            return;
        }
        if(userServer.getIdByUsername(username) != -1) {
            Toast.makeText(this, "用户名已存在，请换一个", Toast.LENGTH_SHORT).show();
            editText.requestFocus();
            return;
        }
        userServer.addUser(username, password);
        int tempID = userServer.getIdByUsername(username);
        Toast.makeText(this, "注册成功！你是我们的第" + String.valueOf(tempID) + "个用户！", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra("id", tempID);
        startActivity(intent);
    }
}

