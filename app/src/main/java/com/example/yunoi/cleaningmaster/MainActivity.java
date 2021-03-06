package com.example.yunoi.cleaningmaster;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;

import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.Toast;

import com.facebook.stetho.Stetho;

import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private CoordinatorLayout coordinatorLayout;
    private BottomNavigationView bottomMenu;
    private FragmentManager fragmentManager;
    private FragmentTransaction ft;
    private Fragment pedomterFrgment;
    private Fragment pedomterBar;
    private Fragment calendarFragment;
    private long backButtonTime=0;  // 뒤로가기 타이밍
    private SettingFragment settingFragment;
    private Fragment mainFragment;
    private Fragment profileFragment;
    private static final String TAG = "MainActivity";
    private InitActionReceiver InitActionReceiver;
    private IntentFilter intentFilter;

    //191212 am 11:20 도움말 이동에 관련된 SharedPreferences by 재훈
    SharedPreferences passTutorial;
    int tutorialState;

    TodayListFragment todayListFragment; //오늘할일 프레그먼트


    @Override
    protected void onResume() {
        super.onResume();
        intentFilter=new IntentFilter();
        intentFilter.addAction(Intent.ACTION_DATE_CHANGED);
        registerReceiver(InitActionReceiver,intentFilter);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Stetho.initializeWithDefaults(this);

        coordinatorLayout = findViewById(R.id.coordinatorLayout);
        bottomMenu = findViewById(R.id.bottomMenu);
        settingFragment = new SettingFragment();
        mainFragment = new MainFragment();
        pedomterFrgment=new PedomterFrgment();
        pedomterBar=new PedomterBar();
        todayListFragment=new TodayListFragment();
        profileFragment=new ProfileFragment();
        calendarFragment=new CalendarFragment();

        //채현 브로드캐스트 추가 데이터 초기화 부분!
        InitActionReceiver=new InitActionReceiver();

        //191212 pm 03:40 다시 보지 않기 설정 안할 시 자동으로 도움말로 이동 by 재훈
        passTutorial = getSharedPreferences("change",MODE_PRIVATE);
        tutorialState = passTutorial.getInt("First",0);
        if(tutorialState !=1){
            Intent intent = new Intent(MainActivity.this,TutorialGuideActivity.class);
            startActivity(intent);
        }

        // 노티피케이션 알림 눌렀을 때 오늘 청소 프래그먼트로 이동
        String menuFragment = getIntent().getStringExtra("todayFragment");

        fragmentManager = getSupportFragmentManager();
        ft = fragmentManager.beginTransaction();

        if (menuFragment != null) {
            if (menuFragment.equals("Notification")) {
                ft.replace(R.id.coordinatorLayout, todayListFragment);
                ft.commit();
            }
        } else {
            ft.replace(R.id.coordinatorLayout, mainFragment);
            ft.commit();
        }

        // bottomMenu를 변경했을 때 그것을 감지하여 해당된 프래그먼트를 세팅해주는 리스너
        bottomMenu.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.action_1:
                        setOnChangeFragment(0);
                        break;
                    case R.id.action_1_1:
                        setOnChangeFragment(1);
                        break;
                    case R.id.action_2:
                        setOnChangeFragment(2);
                        break;
                    case R.id.action_3:
                        setOnChangeFragment(3);
                        break;
                }
                return true;
            }
        });
//        setOnChangeFragment(0);


    }// end of onCreate

    private void setOnChangeFragment(int i) {
        // 화면 전환 위해서는 프래그먼트 매니저 필요
        fragmentManager = getSupportFragmentManager();
        // 프래그먼트 매니저의 권한을 받아서 화면을 바꾸는 역할의 트랜젝션 필요
        ft = fragmentManager.beginTransaction();
        switch(i){
            case 0:
                ft.replace(R.id.coordinatorLayout, mainFragment);
                ft.commit();
                break;
            case 1:
                ft.replace(R.id.coordinatorLayout, todayListFragment);
                ft.commit();
                break;
                //성민이꺼
            case 2:ft.replace(R.id.coordinatorLayout,pedomterFrgment);
                   ft.commit();

                break;
                //달력 항목 첫번째 생성
            case 3:
                ft.replace(R.id.coordinatorLayout,calendarFragment);
                ft.commit();

                break;

        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, 1, 0, "프로필");
        menu.add(0, 2, 0, "통계");
        menu.add(0, 3, 0, "설정");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 3:
                fragmentManager = getSupportFragmentManager();
                ft = fragmentManager.beginTransaction();
                Log.d("MainActivity", "설정");
                ft.replace(R.id.coordinatorLayout, settingFragment);
                ft.commit();
                return true;

            case 2:
                fragmentManager=getSupportFragmentManager();
                ft=fragmentManager.beginTransaction();
                Log.d("MainActivity","통계");
                ft.replace(R.id.coordinatorLayout,pedomterBar);
                ft.commit();
                return true;
            //191212 pm 05:40 상단메뉴 프로필 설정 -by 재훈
             case 1:
                fragmentManager=getSupportFragmentManager();
                ft=fragmentManager.beginTransaction();
                Log.d("MainActivity","프로필");
                ft.replace(R.id.coordinatorLayout,profileFragment);
                ft.commit();
                return true;

        }

        return false;
    }

    public void toastDisplay(String s) {
        Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        long currentTime = System.currentTimeMillis();
        long gapTime = currentTime - backButtonTime;

        if(gapTime>=0 && gapTime <= 2000){
            super.onBackPressed();
        }else{
            backButtonTime = currentTime;
            Toast.makeText(this, "뒤로 가기를 한번 더 누르면 어플이 종료됩니다.",Toast.LENGTH_SHORT).show();
        }


    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(InitActionReceiver);

    }
}
