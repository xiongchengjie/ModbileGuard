package cn.edu.gdmec.android.mobileguard.m1home;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import cn.edu.gdmec.android.mobileguard.R;
import cn.edu.gdmec.android.mobileguard.m1home.adapter.HomeAdaper;
import cn.edu.gdmec.android.mobileguard.m2theftguard.LostFindActivity;
import cn.edu.gdmec.android.mobileguard.m2theftguard.dialog.InterPasswordDialog;
import cn.edu.gdmec.android.mobileguard.m2theftguard.dialog.SetupPasswordDialog;
import cn.edu.gdmec.android.mobileguard.m2theftguard.utils.MD5Utils;

public class HomeActivity extends AppCompatActivity {
    private GridView gv_home;
    private long mExitTime;
//    存储手机密码的sp
    private SharedPreferences msharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        getSupportActionBar().hide();//去除标题栏
        msharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
        gv_home = (GridView) findViewById(R.id.gv_home);
        gv_home.setAdapter(new HomeAdaper(HomeActivity.this));
        gv_home.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0://点击手机防盗
                        if(isSetUpPassword()){
                            //弹出输入密码对话框
                            showInterPswdDialog();
                        }else {
                            //弹出设置密码对话框
                            showSetUpPswdDialog();
                        }
                        break;

                }
            }
        });
    }
//    intent=信使  启动新的活动并且传递信息
    public void startActivity(Class<?> cls){
        Intent intent = new Intent(HomeActivity.this,cls);
        startActivity(intent);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - mExitTime) < 2000) {
                System.exit(0);
            } else {
                mExitTime = System.currentTimeMillis();
                Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
            }
            return true;//拦截回调函数的处理  阻止系统对回退键的默认处理

        }
        return super.onKeyDown(keyCode, event);
    }

//弹出设置密码对话框   本方法需要完成“手机防盗模块”之后才能启用
    private void showSetUpPswdDialog(){
        final SetupPasswordDialog setupPasswordDialog = new SetupPasswordDialog(HomeActivity.this);
        setupPasswordDialog
                .setCallBack(new SetupPasswordDialog.MyCallBack() {
                    @Override
                    public void ok() {
                        String firstPswd = setupPasswordDialog.mFirstPWDET
                                .getText().toString().trim();
                        String affirmPwsd = setupPasswordDialog.mAffirmET
                                .getText().toString().trim();
                        if(!TextUtils.isEmpty(firstPswd) && !TextUtils.isEmpty(affirmPwsd)){
                            if(firstPswd.equals(affirmPwsd)){
                                savePswd(affirmPwsd);
                                setupPasswordDialog.dismiss();
                                showInterPswdDialog();
                            //*************
                            }else{
                                Toast.makeText(HomeActivity.this,"两次密码不一致!",Toast.LENGTH_LONG).show();
                            }
                        }else{
                            Toast.makeText(HomeActivity.this,"密码不能为空！", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void cancel() {
                        setupPasswordDialog.dismiss();
                    }
                });
        setupPasswordDialog.setCancelable(true);
        setupPasswordDialog.show();
    }

    private void showInterPswdDialog(){
    final String password = getPassword();
    final InterPasswordDialog mInPswdDialog = new InterPasswordDialog(
            HomeActivity.this);
        mInPswdDialog.setMyCallBack(new InterPasswordDialog.MyCallBack() {
            @Override
            public void confirm() {
                if(TextUtils.isEmpty(mInPswdDialog.getPassword())){
                    Toast.makeText(HomeActivity.this,"密码不能为空！",Toast.LENGTH_SHORT).show();
                }else if(password.equals(MD5Utils.encode(mInPswdDialog
                        .getPassword()))){
                    mInPswdDialog.dismiss();
//                    Toast.makeText(HomeActivity.this,"可以进入手机防盗模块",Toast.LENGTH_LONG).show();
                    startActivity(LostFindActivity.class);
                }else {
                    //对话框消失
                    mInPswdDialog.dismiss();
                    Toast.makeText(HomeActivity.this,"密码有误，请重新输入！",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void cancle() {
                mInPswdDialog.dismiss();
            }
        });
        mInPswdDialog.setCancelable(true);
        //让对话框显示
        mInPswdDialog.show();
    }

//保存密码
    private void savePswd(String affirmPswd) {
        SharedPreferences.Editor edit = msharedPreferences.edit();
        edit.putString("PhoneAntiTheftPWD",MD5Utils.encode(affirmPswd));
        edit.commit();
    }

//获取密码的方法...
    private String getPassword(){
        String password = msharedPreferences.getString("PhoneAntiTheftPWD",null);
        if(TextUtils.isEmpty(password)){
            return "";
        }
        return password;
    }

    private boolean isSetUpPassword(){
        String password = msharedPreferences.getString("PhoneAntiTheftPWD",null);
        if(TextUtils.isEmpty(password)){
            return false;
        }
        return true;
    }
}