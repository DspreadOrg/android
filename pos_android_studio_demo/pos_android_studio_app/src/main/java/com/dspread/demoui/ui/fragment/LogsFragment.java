package com.dspread.demoui.ui.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dspread.demoui.R;
import com.dspread.demoui.beans.Constants;
import com.dspread.demoui.ui.dialog.Mydialog;
import com.dspread.demoui.utils.DingTalkTest;
import com.dspread.demoui.utils.LogFileConfig;
import com.dspread.demoui.utils.TRACE;
import com.dspread.demoui.utils.TitleUpdateListener;

/**
 * [一句话描述该类的功能]
 *
 * @author : [DH]
 * @createTime : [2023/9/12 16:24]
 * @updateRemark : [说明本次修改内容]
 */
public class LogsFragment extends Fragment {
    private TitleUpdateListener myListener;
    private TextView tv_log;
    private static LogFileConfig logFileConfig;
    private ProgressBar progress_loading;


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        myListener = (TitleUpdateListener) getActivity();
        myListener.sendValue(getString(R.string.show_log));
        TRACE.setContext(getContext());
    }

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_logs, container, false);
        tv_log = view.findViewById(R.id.tv_log);
        progress_loading = view.findViewById(R.id.progress_loading);
        logFileConfig = LogFileConfig.getInstance(getContext());
        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        String log = logFileConfig.readLog();
        tv_log.setMovementMethod(ScrollingMovementMethod.getInstance());
        if(log != null && !"".equals(log)){
            tv_log.setText(log);
        }else {
            tv_log.setText("Empty logs");
        }

    }
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
//        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
//        super.onCreateOptionsMenu(menu, inflater);
//        inflater.inflate(R.menu.menu_logs_upload,menu);
        inflater.inflate(R.menu.main, menu);
        menu.add(0, 1, 0, getString(R.string.upload_logs));
        menu.add(0, 2, 0, "Clear logs");
        super.onCreateOptionsMenu(menu, inflater);
    }
    private final int ErrorCode = 1001;
   private Handler handler = new Handler(Looper.myLooper()){
       @Override
       public void handleMessage(@NonNull Message msg) {
           super.handleMessage(msg);
           switch (msg.what){
               case ErrorCode:
                   Mydialog.ErrorDialog(getActivity(), getString(R.string.network_failed), new Mydialog.OnMyClickListener() {
                       @Override
                       public void onCancel() {
                           Mydialog.ErrorDialog.dismiss();
                       }

                       @Override
                       public void onConfirm() {
                           Mydialog.ErrorDialog.dismiss();
                       }
                   });
                   break;
               default:
                   break;
           }
       }
   };
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == 1){
            EditText editText = new EditText(getContext());
            editText.setHint(getString(R.string.input_email));
            AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
            dialog.setTitle(getString(R.string.input_email));
            dialog.setView(editText);
            dialog.setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    String email = editText.getText().toString();
                    if("Empty logs".equals(tv_log.getText().toString())){
                        Toast.makeText(getContext(),"Empty logs",Toast.LENGTH_SHORT).show();
                    }else {
                        log = email+": "+tv_log.getText().toString();
                        new Thread(runnable).start();
                    }
                }
            });
            dialog.show();

        }else if(item.getItemId() == 2){
            if (logFileConfig.logFileWR!=null) {
                logFileConfig.deleteDir(logFileConfig.logFileWR);
                tv_log.setText("Empty logs");
            }
        }
        return super.onOptionsItemSelected(item);
    }
    private String log;
    Runnable runnable = () -> {
        try {
            boolean isAtAll = false;

//            List mobileList = Lists.newArrayList();
            String content = "issues: "+log;

            String reqStr = DingTalkTest.buildReqStr(content, isAtAll);

            String result =DingTalkTest.postJson(Constants.dingdingUrl, reqStr);

            System.out.println("result == " + result);
            if (result==null){
                Message msg = new Message();
                 msg.what = ErrorCode;
                 handler.sendMessage(msg);
            }
        }catch (Exception e){
            Log.e("Exception","e:"+e.toString());

            e.printStackTrace();

        }
    };

}
