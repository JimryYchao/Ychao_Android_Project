package com.Ychao.PerformanceAnalysis.Sampler;

import android.util.Log;

import com.Ychao.PerformanceAnalysis.Utils.DoubleUtil;
import com.Ychao.PerformanceAnalysis.Utils.ShellCmdUtils;

import java.io.IOException;
import java.io.InputStream;

public class GPUSampler {

    private static GPUSampler instance;
    private static final Object locker = new Object();

    public static GPUSampler getInstance() {
        if (instance == null) {
            synchronized (locker) {
                if (instance == null) {
                    instance = new GPUSampler();
                }
            }
        }
        return instance;
    }


    private static final String TAG = "[GPU Sampler]";

    private final String GpuFreq_Cmd = "/system/bin/cat"; // adb shell cat
    private final String MaxGpuFreq_FilePath = "/sys/class/kgsl/kgsl-3d0/devfreq/max_freq";
    private final String MinGpuFreq_FilePath = "/sys/class/kgsl/kgsl-3d0/devfreq/min_freq";
    private final String Gpu_Busy_Percentage = "/sys/class/kgsl/kgsl-3d0/gpu_busy_percentage";
    private final String Gpu_Model = "/sys/class/kgsl/kgsl-3d0/gpu_model";
    private final String GpuBusy = "/sys/class/kgsl/kgsl-3d0/gpubusy";


    public String getGpuModel() {

        String result = "";
        ProcessBuilder cmd;
        try {
            String[] args = {GpuFreq_Cmd, Gpu_Model};
            cmd = new ProcessBuilder(args);
            Process process = cmd.start();
            InputStream in = process.getInputStream();
            byte[] re = new byte[24];
            while (in.read(re) != -1) {
                result = result + new String(re);
            }
            in.close();
        } catch (IOException ex) {
            Log.d(TAG, "getGpuModel: Failed");
            ex.printStackTrace();
            result = "NaN";
        }
        return result.trim();
    }

    public String getCurGpuUsage() {
        String result = "";
        ProcessBuilder cmd;
        try {
            String[] args = {GpuFreq_Cmd, Gpu_Busy_Percentage};
            cmd = new ProcessBuilder(args);
            Process process = cmd.start();
            InputStream in = process.getInputStream();
            byte[] re = new byte[24];
            while (in.read(re) != -1) {
                result = result + new String(re);
            }
            in.close();
        } catch (IOException ex) {
            Log.d(TAG, "getCurGpuUsage: Failed");
            ex.printStackTrace();
            result = "N/A";
        }
        return result.trim();
    }

    public String getMinGpuFreq() {
        String result = "";
        ProcessBuilder cmd;
        try {
            String[] args = {GpuFreq_Cmd, MinGpuFreq_FilePath};
            cmd = new ProcessBuilder(args);
            Process process = cmd.start();
            InputStream in = process.getInputStream();
            byte[] re = new byte[24];
            while (in.read(re) != -1) {
                result = result + new String(re);
            }
            in.close();
            result = String.valueOf(DoubleUtil.div(Long.parseLong(result.trim()), 1000000, 2)) + "MHz";
        } catch (IOException ex) {
            Log.d(TAG, "getMinGpuFreq: Failed");
            ex.printStackTrace();
            result = "N/A";
        }
        return result;
    }

    public String getMaxGpuFreq() {
        String result = "";
        ProcessBuilder cmd;
        try {
            String[] args = {GpuFreq_Cmd, MaxGpuFreq_FilePath};
            cmd = new ProcessBuilder(args);
            Process process = cmd.start();
            InputStream in = process.getInputStream();
            byte[] re = new byte[24];
            while (in.read(re) != -1) {
                result = result + new String(re);
            }
            in.close();
            result = String.valueOf(DoubleUtil.div(Long.parseLong(result.trim()), 1000000, 2)) + "MHz";
        } catch (IOException ex) {
            Log.d(TAG, "getMaxGpuFreq: Failed");
            ex.printStackTrace();
            result = "N/A";
        }

        return result;
    }

}
