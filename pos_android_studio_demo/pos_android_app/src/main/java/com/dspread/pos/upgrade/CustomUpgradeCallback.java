package com.dspread.pos.upgrade;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dspread.pos.utils.TRACE;
import com.dspread.pos_android_app.R;
import com.tencent.upgrade.bean.UpgradeStrategy;
import com.tencent.upgrade.callback.UpgradeStrategyRequestCallback;
import com.tencent.upgrade.core.UpgradeManager;

import me.goldze.mvvmhabit.utils.SPUtils;

public class CustomUpgradeCallback implements UpgradeStrategyRequestCallback {

    private final Context mContext;
    private final Activity mActivity;
    private final Handler mMainHandler;

    private AlertDialog mUpgradeDialog;

    public CustomUpgradeCallback(Context context) {
        this.mContext = context;
        this.mActivity = context instanceof Activity ? (Activity) context : null;
        this.mMainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void onReceiveStrategy(UpgradeStrategy upgradeStrategy) {
        TRACE.i("Received upgrade strategy: " + upgradeStrategy);

        if (upgradeStrategy == null) {
            TRACE.w("Upgrade strategy is null");
            return;
        }

        mMainHandler.post(() -> showUpgradeDialog(upgradeStrategy));
    }

    @Override
    public void onFail(int errorCode, String errorMsg) {
        TRACE.e("Upgrade check failed: code=" + errorCode + ", message=" + errorMsg);
    }

    @Override
    public void onReceivedNoStrategy() {
        TRACE.i("No upgrade strategy received - app is up to date");
    }

    private void showUpgradeDialog(UpgradeStrategy strategy) {
        dismissUpgradeDialog();
        String versionName = strategy.getApkBasicInfo().getVersionName();
        String versionCode = String.valueOf(strategy.getApkBasicInfo().getVersionCode());
        String updateLog = strategy.getClientInfo().getDescription();
        String title = strategy.getClientInfo().getTitle();
        String downloadUrl = strategy.getApkBasicInfo().getDownloadUrl();
        boolean isForceUpdate = true;

        String message = buildUpdateMessage(versionName, versionCode, updateLog, isForceUpdate);

        View customView = LayoutInflater.from(mContext).inflate(R.layout.dialog_upgrade, null);
        TextView tvTitle = customView.findViewById(R.id.tvTitle);
        TextView tvMessage = customView.findViewById(R.id.tvMessage);
        ImageView btnClose = customView.findViewById(R.id.btnClose);
        Button btnUpdate = customView.findViewById(R.id.btnUpdate);
        Button btnLater = customView.findViewById(R.id.btnLater);

        tvTitle.setText(title);
        tvMessage.setText(message);

        if (!isForceUpdate) {
            btnLater.setVisibility(View.VISIBLE);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setView(customView)
                .setCancelable(!isForceUpdate);

        mUpgradeDialog = builder.create();
        mUpgradeDialog.show();

        // Mark dialog as shown
        SPUtils.getInstance().put("update_dialog_shown", true);

        btnClose.setOnClickListener(v -> {
            if (mUpgradeDialog != null && mUpgradeDialog.isShowing()) {
                mUpgradeDialog.dismiss();
            }
        });

        btnUpdate.setOnClickListener(v -> {
            if (mUpgradeDialog != null) {
                mUpgradeDialog.dismiss();
            }
            startDownload(downloadUrl, versionName);
        });

        btnLater.setOnClickListener(v -> {
            if (mUpgradeDialog != null && mUpgradeDialog.isShowing()) {
                mUpgradeDialog.dismiss();
            }
        });

    }

    private String buildUpdateMessage(String versionName, String versionCode, String updateLog, boolean isForceUpdate) {
        StringBuilder message = new StringBuilder();

        message.append(updateLog != null && !updateLog.isEmpty() ? updateLog : "Bug fixes and improvements");

        return message.toString();
    }

    private void startDownload(String downloadUrl, String versionName) {
        if (downloadUrl == null || downloadUrl.isEmpty()) {
            showErrorDialog("Download URL is empty");
            return;
        }

        try {
            UpgradeManager.getInstance().startDownload();
        } catch (Exception e) {
            handleDownloadError("Failed to start download: " + e.getMessage());
        }
    }

    private void handleDownloadError(String errorMsg) {
        TRACE.e("Download error: " + errorMsg);
        showErrorDialog("Download failed: " + errorMsg);
    }

    private void showErrorDialog(String message) {
        new AlertDialog.Builder(mContext)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }
    private void dismissUpgradeDialog() {
        if (mUpgradeDialog != null && mUpgradeDialog.isShowing()) {
            mUpgradeDialog.dismiss();
        }
    }
}
