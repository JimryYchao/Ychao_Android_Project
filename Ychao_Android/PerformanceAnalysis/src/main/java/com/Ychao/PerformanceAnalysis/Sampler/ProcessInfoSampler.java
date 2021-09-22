package com.Ychao.PerformanceAnalysis.Sampler;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Process;
import android.util.Log;

import com.Ychao.PerformanceAnalysis.Utils.ShellCmdUtils;

import java.util.List;

public class ProcessInfoSampler {

    private static final String TAG = "[Process Sampler]";

    public static int getPid() {
        try {
            int pid = Process.myPid();
            //Log.d(TAG, "Current App Process PID: " + pid);
            return pid;
        } catch (Exception e) {
            Log.d(TAG, "getPid: Not get App Process PID.");
            e.printStackTrace();
        }
        return -1;
    }
    private final static String TopCmd_PidCpuInfo = "top -n 1 | grep ";

    public static String getProcessTopInfo(int pid) {
        String cmd = TopCmd_PidCpuInfo + pid;

        String cmd_CallStr = ShellCmdUtils.exec_SHC(cmd, 1);
        if (cmd_CallStr != null & cmd_CallStr != "") {

            return cmd_CallStr;

        } else {
            Log.d(TAG, "getProcessTopInfo: not get TopInfo by this PID: " + pid);
            return null;
        }
    }

    public static String getPackageName(Context context) {
        String processName = "";
        ActivityManager Am = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        int pid = getPid();

        List<ActivityManager.RunningAppProcessInfo> list = Am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : list) {
            if (runningAppProcessInfo.pid == pid) {
                processName = runningAppProcessInfo.processName;
            } else {
                Log.d(TAG, "Not found the Name of current Process.");
                return "";
            }
        }
        Log.d(TAG, "getPackageName: Current Process-Name is " + processName);
        return processName;
    }
}
