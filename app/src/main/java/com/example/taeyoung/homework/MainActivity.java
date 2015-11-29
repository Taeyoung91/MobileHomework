package com.example.taeyoung.homework;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Calendar;

public class MainActivity extends Activity {
    View dialogView;

    DatePicker datePicker;
    TextView dateTextView;
    EditText editDiary;
    Button saveBtn;

    String dateText;
    String fileName;
    File diaryFile;

    Calendar cal = Calendar.getInstance();
    int cYear = cal.get(Calendar.YEAR);
    int cMonth = cal.get(Calendar.MONTH);
    int cDay = cal.get(Calendar.DAY_OF_MONTH);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dateTextView = (TextView)findViewById(R.id.dateTextView);// 날짜 부분
        saveBtn = (Button)findViewById(R.id.saveBtn); // 저장 버튼
        editDiary = (EditText)findViewById(R.id.editDiary); // 일기 내용 부분
        editDiary.setTextSize(20);

        dateText = cYear + "년 " + (cMonth + 1) + "월 " + cDay + "일"; // 일기장의
        fileName = cYear + "_" + (cMonth + 1) + "_" + cDay + ".txt";  // 초기화면을
        dateTextView.setText(dateText);    // 오늘날짜와 오늘 일기 내용으로
        editDiary.setText(readDiary(fileName));  // 초기화. readDiary 호출로 오늘 날짜에 일기 파일이 존재하면 읽어온다.

        dateTextView.setOnClickListener(diaryListener); // TextView을 클릭하면 DatePicker 가 나타나 날짜 선택이 가능하게 한다.

        saveBtn.setOnClickListener(saveListener); //저장 버튼을 누르면 현재 TextView에 보여지는 날짜에 해당하는 파일에 EditText의 일기 내용을 저장하는
                                                    //savaListener을 설정하였다.
    }

    /* 일기장의 날짜 부분 TextView에 달리는 Listener */
    View.OnClickListener diaryListener = new View.OnClickListener(){
        public void onClick(View view){
            dialogView = View.inflate(MainActivity.this, R.layout.datepicker, null);
            datePicker = (DatePicker)dialogView.findViewById(R.id.datePicker);



            datePicker.init(cYear, cMonth, cDay, new DatePicker.OnDateChangedListener() {
                public void onDateChanged(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                    fileName = Integer.toString(year) + "_"
                            + Integer.toString(monthOfYear + 1) + "_"
                            + Integer.toString(dayOfMonth) + ".txt";
                    dateText =  Integer.toString(year) + "년 "
                            + Integer.toString(monthOfYear + 1) + "월 "
                            + Integer.toString(dayOfMonth) + "일";
                }
            });
            datePicker.updateDate(cYear, cMonth, cDay);

            AlertDialog.Builder dlg = new AlertDialog.Builder(MainActivity.this);
            dlg.setTitle("날짜 선택");
            dlg.setIcon(R.mipmap.ic_launcher);
            dlg.setView(dialogView);
            dlg.setPositiveButton("확인",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String str = readDiary(fileName);
                            dateTextView.setText(dateText);
                            editDiary.setText(str);

                        }
                    });
            dlg.setNegativeButton("취소",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(getApplicationContext(), "날짜 선택 취소", Toast.LENGTH_SHORT).show();
                        }
                    });
            dlg.show();
        }
    };

    /* 일기장의 '저장'버튼에 달리는 Listener */
    View.OnClickListener saveListener = new View.OnClickListener(){
        public void onClick(View view){
            try {
                File file = new File(getExternalFilesDir("mydiary"),fileName);

                PrintWriter pw = new PrintWriter(file);
                String str = editDiary.getText().toString();
                pw.println(str);
                pw.close();
                Toast.makeText(getApplicationContext(), fileName + "이 저장됨", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
            }

        }
    };


    /* 일기 파일의 내용을 읽은 후 리턴하는 Method */
    public String readDiary(String fName){

        String diaryStr = null;

        try {
            File file = new File(getExternalFilesDir("mydiary"),fName);
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);

            String strConcat = ""; //누적할 변수선언
            String str="";
            while((str=br.readLine())!=null){
                strConcat += str +"\n";
            }
            br.close();
            diaryStr = (new String(strConcat)).trim();

        } catch (Exception e) {
            editDiary.setHint("일기 없음");
        }

        return diaryStr;
    }

    /* 메뉴 부분 */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater mInflater = getMenuInflater();
        mInflater.inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.reload:
                String str = readDiary(fileName);
                dateTextView.setText(dateText);
                editDiary.setText(str);
                Toast.makeText(getApplicationContext(),
                        fileName+ "을 다시 불러옴", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.delete:
                diaryFile = new File(getExternalFilesDir("mydiary"),fileName);
                if(!diaryFile.exists()) // 파일이 존재하지않으면
                    Toast.makeText(getApplicationContext(), "해당 날짜 일기 없음", Toast.LENGTH_SHORT).show();
                else  //존재한다면
                    questionDelete(); // 삭제 메서드 호출
                return true;

            case R.id.bigSize:
                editDiary.setTextSize(35);
                return true;

            case R.id.midSize:
                editDiary.setTextSize(20);
                return true;

            case R.id.smallSize:
                editDiary.setTextSize(10);
                return true;
        }

        return false;
    }

    /* 삭제 Dialog 메서드 */
    public void questionDelete(){

        AlertDialog.Builder dlg = new AlertDialog.Builder(MainActivity.this);
        dlg.setTitle("경고");
        dlg.setIcon(R.mipmap.ic_launcher);
        dlg.setMessage(dateText + " 일기를 삭제하시겠습니까?");
        dlg.setPositiveButton("확인",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (diaryFile.delete()) {
                            editDiary.setText("");
                            editDiary.setHint("일기 없음");
                            Toast.makeText(getApplicationContext(), "삭제 완료", Toast.LENGTH_SHORT).show();
                        } else
                            Toast.makeText(getApplicationContext(), "삭제 실패", Toast.LENGTH_SHORT).show();
                    }
                });
        dlg.setNegativeButton("취소",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(), "삭제 취소", Toast.LENGTH_SHORT).show();
                    }
                });
        dlg.show();
    }

}
