package com.example.yunoi.cleaningmaster;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static android.content.Context.ALARM_SERVICE;


public class TodolistFragment
        extends Fragment implements LoadAlarmsReceiver.OnAlarmsLoadedListener {

    View view;
    private SQLiteDatabase db;
    private ArrayList<TodolistVo> list = new ArrayList<TodolistVo>();
    private ArrayList<AlarmVO> alarmList = new ArrayList<>();
    private TodolistAdapter todolistAdapter;
    private LinearLayoutManager linearLayoutManager;
    public static ConstraintLayout todo_constraintLayout;
    public static int score = 0;
    public static String groupText; //구역이름
    public static String taskText; //구역이름
    private EditText alerEdt; // alert
    private String task; // 청소내용
    private static final String TAG = "TodolistFragment";

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({EDIT_ALARM, ADD_ALARM, UNKNOWN})
    @interface Mode {
    }

    public static final int EDIT_ALARM = 1;
    public static final int ADD_ALARM = 2;
    public static final int UNKNOWN = 0;

    public static final String ALARM_EXTRA = "alarm_extra";
    public static final String MODE_EXTRA = "mode_extra";
    private Context context;
    private LoadAlarmsReceiver mReceiver;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mReceiver = new LoadAlarmsReceiver(this);
        Log.i(getClass().getSimpleName(), "onCreate ...");

    }

    @Override
    public void onResume() {
        super.onResume();
//        selectCleaningArea(groupText);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.todolist_fragment, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.todo_listView);
        todo_constraintLayout = view.findViewById(R.id.todo_constraintLayout);


        //액션바 설정
        ActionBar actionBar = ((MainActivity) getActivity()).getSupportActionBar();
        // Custom Actionbar를 사용하기 위해 CustomEnabled을 true 시키고 필요 없는 것은 false 시킨다
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(false);            //액션바 아이콘을 업 네비게이션 형태로 표시합니다.
        actionBar.setDisplayShowTitleEnabled(false);        //액션바에 표시되는 제목의 표시유무를 설정합니다.
        actionBar.setDisplayShowHomeEnabled(false);//홈 아이콘을 숨김처리합니다.

        View actionbarlayout = inflater.inflate(R.layout.todolist_actionbar_layout, null);
        actionBar.setCustomView(actionbarlayout);
        //액션바 양쪽 공백 없애기
        Toolbar parent = (Toolbar) actionbarlayout.getParent();
        parent.setContentInsetsAbsolute(0, 0);
        ActionBar.LayoutParams params = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
        actionBar.setCustomView(actionbarlayout, params);

        //end of 액션바 공백 없애기

        //액션바 버튼
        ImageButton imageButton = actionbarlayout.findViewById(R.id.actionbar_todoBtnBack);
        TextView actionbar_todoText = actionbarlayout.findViewById(R.id.actionbar_todoText);
        ImageButton actionbar_todoBtnAddlist = actionbarlayout.findViewById(R.id.actionbar_todoBtnAddlist);

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Fragment fragment = new MainFragment(); //돌아가는 fragment
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.coordinatorLayout, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });


        if (getArguments() != null) {
            groupText = getArguments().getString("groupText");
            taskText = getArguments().getString("taskText");
            actionbar_todoText.setText(groupText);
        }


        //리싸이클러뷰 설정
        linearLayoutManager = new LinearLayoutManager(view.getContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        alarmList = DBHelper.getInstance(context).areaSort(groupText);

        todolistAdapter = new TodolistAdapter(context, R.layout.todo_list_holder_layout, alarmList);
        recyclerView.setAdapter(todolistAdapter);
        todolistAdapter.notifyDataSetChanged();
//        selectCleaningArea(groupText);//구역마다 저장한 할일들 가져오기
        score = selectScore(view.getContext());


        //list 추가 + todolist 알람 설정

        //현재 년,월,일
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        String year = new SimpleDateFormat("yyyy").format(date);
        String month = new SimpleDateFormat("MM").format(date);
        String day = new SimpleDateFormat("dd").format(date);

        final int currentYear = Integer.parseInt(year);
        final int currentMonth = Integer.parseInt(month);
        final int currentDay = Integer.parseInt(day);

        actionbar_todoBtnAddlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                final View alertDialogView = View.inflate(v.getContext(), R.layout.dialog_add_todolist, null);
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext(), R.style.MyDialogTheme);
                builder.setView(alertDialogView);
                builder.setPositiveButton("저장", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        alerEdt = alertDialogView.findViewById(R.id.alert_todolist_alerEdt);
                        task = alerEdt.getText().toString();
                        if (task.equals("")) {
                            Toast.makeText(v.getContext(), "할 일을 적어주세요!", Toast.LENGTH_SHORT).show();
                        } else {
                            insertCleaningArea(new TodolistVo(currentYear, currentMonth, currentDay, groupText, task, 0, 0));
                            Toast.makeText(v.getContext(), "저장되었습니다!", Toast.LENGTH_SHORT).show();
                        }
                        list.add(new TodolistVo(task));
                        final long id = DBHelper.getInstance(context).addAlarm();
                        LoadAlarmsService.launchLoadAlarmsService(context);
                        final AlarmVO alarm1 = new AlarmVO(id);
                        alarm1.setArea(groupText);
                        alarm1.setLabel(task);
                        int message = DBHelper.getInstance(context).updateAlarm(alarm1);
                        alarmList.add(alarm1);
                        Log.d(TAG, "onCreateView. Dialog positive. test value : " + alarm1);
                        Log.d(TAG,"if message == 1 {success} : "+message);
                    }
                });
                builder.setNegativeButton("취소", null);
                builder.show();
                todolistAdapter.notifyDataSetChanged();

            }
        });

        todolistAdapter.setAlarmClickListener(new TodolistAdapter.alarmClickListener() {
            @Override
            public void onAlarmClick(View v, int position) {

                final AlarmVO alarm = alarmList.get(position);
                Log.d(TAG, "SetAlarmClickListener.alarm position: " + position);
                Log.d(TAG, "SetAlarmClickListener.alarm list: " + alarm.toString());

                final Intent launchEditAlarmIntent =
                        AddEditAlarmActivity.buildAddEditAlarmActivityIntent(
                                context, AddEditAlarmActivity.EDIT_ALARM
                        );

                launchEditAlarmIntent.putExtra(AddEditAlarmActivity.ALARM_EXTRA, alarm);
                startActivity(launchEditAlarmIntent);
            }
        });
        return view;
    }//end of onCreatView


    //cleaningTBL 구역 저장하기(insert) (현재 년도, 월, 일, 구역, 할일,taskCount 나머지는 2개 checkCount,score 0 으로)
    public void insertCleaningArea(TodolistVo todolistVo) {
        db = DBHelper.getInstance(getActivity().getApplicationContext()).getWritableDatabase();
        int year = todolistVo.getYear();
        int month = todolistVo.getMonth();
        int day = todolistVo.getDay();
        String todolist_text = todolistVo.getTodolist_text();
        String groupName = todolistVo.getGroupName();
        int checkcount = todolistVo.getCheckcount();
        int state = todolistVo.getAlarmState();
        db.execSQL("INSERT INTO cleaningTBL (year, month, day, area, task, checkCount, alarmState)" +
                " VALUES (" + year + ", " + month + ", " + day + ", '" + groupName + "', '" + todolist_text + "', " + checkcount + ", " + state + ");");
        list.add(new TodolistVo(todolist_text));
        todolistAdapter.notifyDataSetChanged();
        Log.d(TAG, "DB 저장됨");
    }

    //저장된 DB 내용 가져오기 (할일,체크박스 true,false)
    public void selectCleaningArea(String name) {
        db = DBHelper.getInstance(getActivity().getApplicationContext()).getWritableDatabase();
        Cursor cursor;
        cursor = db.rawQuery("SELECT task,checkCount FROM cleaningTBL WHERE area=" + "'" + name + "';", null);
        list.clear();
        while (cursor.moveToNext()) {

            list.add(new TodolistVo(cursor.getString(0), cursor.getInt(1)));
            Log.d(TAG, "DB에서 select함 내용 : " + cursor.getString(0) + " / check 유,무 : " + cursor.getInt(1));

        }
        todolistAdapter.notifyDataSetChanged();
        cursor.close();
        Log.d(TAG, "DB에서 select함");
    }

    //저장된 스코어 가져오기
    public int selectScore(Context context) {
        db = DBHelper.getInstance(context.getApplicationContext()).getWritableDatabase();
        Cursor cursor1;
        cursor1 = db.rawQuery("SELECT * FROM profileTBL", null);
        String nickname = null;
        while (cursor1.moveToNext()) {

            nickname = cursor1.getString(0);
        }
        Log.d(TAG, "닉네임 확인 : " + nickname);

        Cursor cursor;
        cursor = db.rawQuery("SELECT Score FROM profileTBL WHERE NickName=" + "'" + nickname + "';", null);
        int score = 0;
        while (cursor.moveToNext()) {
            score = cursor.getInt(0);

        }
        cursor.close();
        Log.d(TAG, "가져온 score : " + score);
        return score;
    }

    private void toastDisplay(String s) {
        Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStart() {
        super.onStart();
        final IntentFilter filter = new IntentFilter(LoadAlarmsService.ACTION_COMPLETE);
        LocalBroadcastManager.getInstance(context).registerReceiver(mReceiver, filter);
        LoadAlarmsService.launchLoadAlarmsService(context);
        Log.d(TAG, "onStart");
    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(context).unregisterReceiver(mReceiver);
        Log.d(TAG, "onStop");
    }

    @Override
    public void onAlarmsLoaded(ArrayList<AlarmVO> alarms) {
        for (AlarmVO list : alarms) {
            Log.d(TAG, "onAlarmsLoaded. list: " + list.toString());
        }

        alarmList = DBHelper.getInstance(context).areaSort(groupText);
        todolistAdapter.setAlarms(alarmList);
        Log.d(TAG, "onAlarmsLoaded");
    }
}





