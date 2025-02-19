package com.dspread.demoui.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dspread.demoui.R;
import com.dspread.demoui.utils.JsonHandler;
import com.dspread.demoui.utils.TitleUpdateListener;
import com.dspread.demoui.utils.NetCheckHelper;
import com.dspread.demoui.utils.TRACE;
import com.dspread.demoui.utils.UpdateAppHelper;
import com.dspread.demoui.widget.CustomDialog;
import com.dspread.demoui.beans.VersionEnty;
import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.model.Progress;
import com.lzy.okgo.request.base.Request;
import com.xuexiang.xutil.app.PathUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 *
 * @author user
 */
public class AboutFragment extends Fragment implements View.OnClickListener {
    private TitleUpdateListener myListener;
    private RelativeLayout btnVersionUpdate;
    private TextView tvVersionCode;

    private String absolutePath;
    private ProgressBar mProgressBar;
    private ImageView ivRedDot;
    private boolean isCheckUpgrade = false;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        myListener = (TitleUpdateListener) getActivity();
        myListener.setFragmentTitle(getString(R.string.about));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, container, false);
        btnVersionUpdate = view.findViewById(R.id.version_update);
        mProgressBar = view.findViewById(R.id.pb_loading);
        tvVersionCode = view.findViewById(R.id.tv_version_code);
        ivRedDot = view.findViewById(R.id.iv_red_dot);
        ivRedDot.setVisibility(View.GONE);
        btnVersionUpdate.setOnClickListener(this);

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String packageVersionName = UpdateAppHelper.getPackageVersionName(getContext(), "com.dspread.demoui");

        tvVersionCode.setText("v" + packageVersionName);

        checkVersionUpdate();

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.version_update:
                //Version updates
                isCheckUpgrade = true;
                checkVersionUpdate();

                break;
            default:
                break;

        }
    }

    private void checkVersionUpdate() {
        try {
            boolean b = NetCheckHelper.checkNetworkAvailable(getActivity());
            if (b) {
                checkNewVersion();
                TRACE.d("network connection");
            } else {
                Toast.makeText(getActivity(), "no network connection", Toast.LENGTH_SHORT).show();
                TRACE.d("no network connection");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkNewVersion() throws IOException {
        String commitUrl = "https://gitlab.com/api/v4/projects/4128550/jobs/artifacts/master/raw/pos_android_studio_demo/pos_android_studio_app/build/outputs/apk/release/commit.json?job=assembleRelease";
        commitUrl = "https://github.com/DspreadOrg/android/releases/download/Release/version_info.json";
        downloadFileCourse(getActivity(), commitUrl, PathUtils.getAppExtCachePath(), "commit.json");
    }


    public void downloadFileCourse(final Context context, String fileUrl, String destFileDir, String destFileName) {
        try {
            //String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ2YWxpZHRpbWUiOjAsInVzZXJpZCI6IjJlNmI0YTdmYzQ5NTRmMzNiZjI2ZjhmMjViNGFmNjIwIiwiZGV2aWNlaW5mbyI6ImVjZjA2OTcyMTgxODZhODIifQ.XJsDI1lzKd2_I7aABf-90mXiWgRU5mzDq3pThn2rKj8";
            OkGo.<File>get(fileUrl).tag(context)
                    .execute(new FileCallback(destFileDir, destFileName) {
                        @Override
                        public void onSuccess(com.lzy.okgo.model.Response<File> response) {
                            mProgressBar.setVisibility(View.INVISIBLE);
                            absolutePath = response.body().getAbsolutePath();
                            Log.e("download_Success-path", absolutePath + "");
                            try {
                                String s = readerMethod(new File(absolutePath));
                                Gson gson = new Gson();
                                s= JsonHandler.parseJsonWithQuotes(s);
                                Log.e("download_Success-JSON;", s);
                                VersionEnty versionEnty = gson.fromJson(s, VersionEnty.class);
                                String versionCode = (String) versionEnty.getVersionCode();
                                //String replace = versionCode.trim().replace(" ", "");
                               // int length = replace.length();
                                //String substring = replace.substring(11, length);
                                int versionCodeInt = Integer.parseInt(versionCode);
                                Object versionName = versionEnty.getVersionName();
                                String modifyContent = (String) versionEnty.getModifyContent();
                                if (modifyContent.length() > 300) {
                                    modifyContent = modifyContent.substring(0, 300) + "......";
                                }
                                Log.e("download_Success-JSON;", s + "" + "versionCode:" + versionCode);
                                String downloadUrl = versionEnty.getDownloadUrl();
                                Log.e("download_Success-JSON", "downloadUrl:" + downloadUrl);
                                int packageVersionCode = UpdateAppHelper.getPackageVersionCode(getContext(), "com.dspread.demoui");
                                if (packageVersionCode < versionCodeInt) {
                                    ivRedDot.setVisibility(View.VISIBLE);
                                    if (isCheckUpgrade) {
                                        dialog(downloadUrl, versionName.toString(), modifyContent.toString());
                                        isCheckUpgrade = false;
                                    }

                                } else {
                                    ivRedDot.setVisibility(View.GONE);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }


                        @Override
                        public void onStart(Request<File, ? extends Request> request) {
                            super.onStart(request);
                            mProgressBar.setVisibility(View.VISIBLE);


                        }

                        @Override
                        public void onFinish() {
                            super.onFinish();
                            mProgressBar.setVisibility(View.INVISIBLE);

                        }

                        @Override
                        public void onError(com.lzy.okgo.model.Response<File> response) {
                            super.onError(response);
                            mProgressBar.setVisibility(View.INVISIBLE);

                        }

                        @Override
                        public void downloadProgress(Progress progress) {
                            super.downloadProgress(progress);
                        }
                    });

        } catch (Exception e) {
            Log.e("downLoad fail;", e.toString() + "");
        }
    }
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
//        super.onSaveInstanceState(outState);
    }

    private static String readerMethod(File file) throws IOException {
        FileReader fileReader = new FileReader(file);
        Reader reader = new InputStreamReader(new FileInputStream(file), "Utf-8");
        int ch = 0;
        StringBuffer sb = new StringBuffer();
        while ((ch = reader.read()) != -1) {
            sb.append((char) ch);
        }
        fileReader.close();
        reader.close();
        return sb.toString();
    }


    private void dialog(String downUrl, String versionName, String modifyContent) {
        CustomDialog.Builder builder = new CustomDialog.Builder(getActivity());
        builder.setTitle("Found New Version");
        builder.setMessage(
                "upgrade version：" + versionName + "？" + "\n" +
                        "\n"
                        + modifyContent
        );
        builder.setPositiveButton("", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Upgrade",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        // String downloadUrl = "https://gitlab.com/api/v4/projects/4128550/jobs/artifacts/develop_new_demo/raw/pos_android_studio_demo/pos_android_studio_app/build/outputs/apk/release/pos_android_studio_app-release.apk?job=assembleRelease";
                        UpdateAppHelper.useApkDownLoadFunction(getContext(), downUrl);
                        if (ivRedDot != null) {
                            ivRedDot.setVisibility(View.GONE);
                        }
                    }
                });
        builder.setCloseButton(new CustomDialog.OnCloseClickListener() {
            @Override
            public void setCloseOnClick() {

                if (customDialog != null) {
                    customDialog.dismiss();
                }
            }
        });

        customDialog = builder.create(R.layout.dialog_update_layout);
        customDialog.setCanceledOnTouchOutside(false);
        customDialog.show();
    }

    private CustomDialog customDialog;


    public static String getAppVersionName(Context context) {

        String versionName = "";

        try {

            PackageManager pm = context.getPackageManager();

            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);

            versionName = pi.versionName;

            if (versionName == null || versionName.length() <= 0) {

                return "";

            }

        } catch (Exception e) {

            Log.e("VersionInfo", "Exception", e);

        }

        return versionName;

    }


}