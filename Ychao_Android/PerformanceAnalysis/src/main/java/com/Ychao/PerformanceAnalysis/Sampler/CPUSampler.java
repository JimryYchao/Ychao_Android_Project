package com.Ychao.PerformanceAnalysis.Sampler;

import android.os.Process;
import android.util.Log;

import com.Ychao.PerformanceAnalysis.Utils.DoubleUtil;
import com.Ychao.PerformanceAnalysis.Utils.FileUtils;
import com.Ychao.PerformanceAnalysis.Utils.ShellCmdUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;

public class CPUSampler {

    private static CPUSampler instance;
    private final static Object locker = new Object();

    public static CPUSampler getInstance() {

        if (instance == null) {
            synchronized (locker) {
                if (instance == null) {
                    instance = new CPUSampler();
                }
            }
        }
        return instance;
    }


    private static final String TAG = "[CPU Sampler]";

    private final String CpuFreq_Cmd = "/system/bin/cat"; //adb shell cat
    private final String Cpufreq_Path = "/sys/devices/system/cpu/cpu";
    private final String MaxCpuFreq_FilePath = "/cpufreq/cpuinfo_max_freq";
    private final String MinCpuFreq_FilePath = "/cpufreq/cpuinfo_min_freq";
    private final String CurCpuFreq_FilePath = "/cpufreq/scaling_cur_freq";
    private final String cpuInfo_FilePath = "/proc/cpuinfo";


    public String getCpuModel() {
        try {
            FileReader FR = new FileReader(cpuInfo_FilePath);
            BufferedReader BR = new BufferedReader(FR);

            String text = BR.readLine();
            String[] array = text.split(":\\s+", 2);
            for (int i = 0; i < array.length; i++) {
            }

            FileUtils.closeReader(BR);
            FileUtils.closeReader(FR);

            return array[1];

        } catch (IOException e) {
            Log.e(TAG, "getCpuModel: Failed");
            e.printStackTrace();
        }
        return null;
    }

    public long getCpuProcessorMaxFreq(int processorIndex) {

        String[] CMDs = {CpuFreq_Cmd, Cpufreq_Path + processorIndex + MaxCpuFreq_FilePath};

        String result = ShellCmdUtils.exec_CMD(CMDs);

        if (result != null && result != "") {
            return Long.parseLong(result.trim());
        } else {
            Log.e(TAG, "getCpuProcessorMaxFreq: Failed");
            return -1;
        }
    }

    public long getCpuProcessorMinFreq(int processorIndex) {
        String[] CMDs = {CpuFreq_Cmd, Cpufreq_Path + processorIndex + MinCpuFreq_FilePath};

        String result = ShellCmdUtils.exec_CMD(CMDs);

        if (result != null && result != "") {
            return Long.parseLong(result.trim());
        } else {
            Log.e(TAG, "getCpuProcessorMinFreq: Failed");
            return -1;
        }
    }

    public long getCpuProcessorCurFreq(int processorIndex) {
        String result = "-1";
        try {
            FileReader FR = new FileReader(Cpufreq_Path + processorIndex + CurCpuFreq_FilePath);
            BufferedReader BR = new BufferedReader(FR);
            String text = BR.readLine();
            result = text.trim();

            FileUtils.closeReader(BR);
            FileUtils.closeReader(FR);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "getCpuProcessorCurFreq: FilePath is Wrong");
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, "getCpuProcessorCurFreq: Failed");
            e.printStackTrace();
        }
        return Long.parseLong(result);
    }

    public String getTopListByCpuOrder(int amount) {
        String cmd = "top -n 1 -m " + amount + " -s 9";
        String result = ShellCmdUtils.exec_CMD(cmd, 5, amount);

        if (result != null && result != "")

            return result;
        else {
            Log.e(TAG, "getTopListByCpuOrder: Failed");
            return "NaN";
        }
    }

    public double getProcessCpuUsageByTop(int pid) {

        String CallStr = ProcessInfoSampler.getProcessTopInfo(pid);

        if (CallStr == null || CallStr == "") {
            return -1;
        }
        String[] pidTopInfo = CallStr.split("\\s+");

        if (pid == Integer.parseInt(pidTopInfo[0])) {

            double rate = Double.parseDouble(pidTopInfo[8]);
            return rate;
        } else {
            Log.d(TAG, "getProcessCoreUsage: not get PidCpu Usage by this Pid: " + pid);
            return -1;
        }
    }

    public double getProcessCpuUsage(int pid) {
        double usage = 0.0;
        String[] result1 = null;
        String[] result2 = null;
        double pCpu = 0.0;
        double aCpu = 0.0;
        double o_pCpu = 0.0;
        double o_aCpu = 0.0;
        if (pid >= 0) {
            result1 = getProcessCpuAction(pid);
            if (null != result1) {
                pCpu = Double.parseDouble(result1[1])
                        + Double.parseDouble(result1[2]);
            }
            result2 = getCpuAction();
            if (null != result2) {
                aCpu = 0.0;
                for (int i = 2; i < result2.length; i++) {
                    aCpu += Double.parseDouble(result2[i]);
                }
            }
            try {
                Thread.sleep(360);
            } catch (Exception e) {
                e.printStackTrace();
            }
            result1 = getProcessCpuAction(pid);
            if (null != result1) {
                o_pCpu = Double.parseDouble(result1[1])
                        + Double.parseDouble(result1[2]);
            }
            result2 = getCpuAction();
            if (null != result2) {
                aCpu = 0.0;
                for (int i = 2; i < result2.length; i++) {
                    aCpu += Double.parseDouble(result2[i]);
                }
            }
            if ((aCpu - o_aCpu) != 0) {
                usage = DoubleUtil.div(((pCpu - o_pCpu) * 100.00),
                        (aCpu - o_aCpu), 2);
                if (usage < 0) {
                    usage = 0;
                } else if (usage > 100) {
                    usage = 100;
                }
            }
        }
        return usage;
    }

    public double getCurProcessCpuUsage() {
        if (this.pid < 0) {
            this.pid = getPid();
            p_jif = 0.0;
            pCpu = 0.0;
            aCpu = 0.0;
            o_pCpu = 0.0;
            o_aCpu = 0.0;
        }

        double usage = 0.0;
        String[] result1 = null;
        String[] result2 = null;
        if (pid >= 0) {

            result1 = getProcessCpuAction(pid);
            if (null != result1) {
                pCpu = Double.parseDouble(result1[1])
                        + Double.parseDouble(result1[2]);
            }
            result2 = getCpuAction();
            if (null != result2) {
                aCpu = 0.0;
                for (int i = 2; i < result2.length; i++) {

                    aCpu += Double.parseDouble(result2[i]);
                }
            }
            usage = 0.0;
            if ((aCpu - o_aCpu) != 0) {
                usage = DoubleUtil.div(((pCpu - o_pCpu) * 100.00),
                        (aCpu - o_aCpu), 2);
                if (usage < 0) {
                    usage = 0;
                } else if (usage > 100) {
                    usage = 100;
                }
            }
            o_pCpu = pCpu;
            o_aCpu = aCpu;
        }
        p_jif = pCpu;
        return usage;

    }

    public double getTotalCpuUsage() {
        try {
            RandomAccessFile reader = new RandomAccessFile("/proc/stat", "r");
            String load = reader.readLine();
            String[] toks = load.split(" ");
            double idle1 = Double.parseDouble(toks[5]);
            double cpu1 = Double.parseDouble(toks[2])
                    + Double.parseDouble(toks[3]) + Double.parseDouble(toks[4])
                    + Double.parseDouble(toks[6]) + Double.parseDouble(toks[8])
                    + Double.parseDouble(toks[7]);
            // 2:user 3:nice 4:system 6:iowait 7:irq 8:softirq
            try {
                Thread.sleep(360);
            } catch (Exception e) {
                e.printStackTrace();
            }
            reader.seek(0);
            load = reader.readLine();
            reader.close();
            toks = load.split(" ");
            double idle2 = Double.parseDouble(toks[5]);
            double cpu2 = Double.parseDouble(toks[2])
                    + Double.parseDouble(toks[3]) + Double.parseDouble(toks[4])
                    + Double.parseDouble(toks[6]) + Double.parseDouble(toks[8])
                    + Double.parseDouble(toks[7]);
            double value = DoubleUtil.div((100.00 * ((cpu2 - cpu1))),
                    (cpu2 + idle2) - (cpu1 + idle1), 2);

            return value;
        } catch (IOException ex) {
            Log.d(TAG, "getTotalCpuUsage: Failed");
            ex.printStackTrace();
        }
        Log.e(TAG, "getTotalCpuUsage: Failed");
        return -1;
    }


    private double p_jif = 0.0;
    private double pCpu = 0.0;
    private double aCpu = 0.0;
    private double o_pCpu = 0.0;
    private double o_aCpu = 0.0;
    private int pid = -1;

    private String[] getCpuAction() {
        String cpuPath = "/proc/stat";
        String cpu = "";
        String[] result = new String[7];

        File f = new File(cpuPath);
        if (!f.exists() || !f.canRead()) {
            Log.d(TAG, "getCpuAction: /proc/stat not catch");
            return result;
        }

        FileReader fr = null;
        BufferedReader localBufferedReader = null;

        try {
            fr = new FileReader(f);
            localBufferedReader = new BufferedReader(fr, 8192);
            cpu = localBufferedReader.readLine();
            if (null != cpu) {
                result = cpu.split(" ");

            }
        } catch (FileNotFoundException e) {
            Log.d(TAG, "getCpuAction: Failed");
            e.printStackTrace();
        } catch (IOException e) {
            Log.d(TAG, "getCpuAction: Failed");
            e.printStackTrace();
        } finally {
            FileUtils.closeReader(localBufferedReader);
            FileUtils.closeReader(fr);
            return result;
        }
    }

    private String[] getProcessCpuAction(int pid) {
        String cpuPath = "/proc/" + pid + "/stat";
        String cpu = "";
        String[] result = new String[3];

        File f = new File(cpuPath);
        if (!f.exists() || !f.canRead()) {
            /*
             * 进程信息可能无法读取，
             * 同时发现此类进程的PSS信息也是无法获取的，用PS命令会发现此类进程的PPid是1，
             * 即/init，而其他进程的PPid是zygote,
             * 说明此类进程是直接new出来的，不是Android系统维护的
             */
            Log.d(TAG, "getProcessCpuAction: 此进程非Android系统维护进程");
            return result;
        }

        FileReader fr = null;
        BufferedReader localBufferedReader = null;

        try {
            fr = new FileReader(f);
            localBufferedReader = new BufferedReader(fr, 8192);
            cpu = localBufferedReader.readLine();
            if (null != cpu) {
                String[] cpuSplit = cpu.split(" ");
                result[0] = cpuSplit[1];
                result[1] = cpuSplit[13];
                result[2] = cpuSplit[14];
            }
        } catch (IOException e) {
            Log.d(TAG, "getProcessCpuAction: Failed");
            e.printStackTrace();
        } finally {
            FileUtils.closeReader(localBufferedReader);
            FileUtils.closeReader(fr);

            return result;
        }
    }

    private int getPid() {
        try {
            int pid = Process.myPid();
            //Log.d(TAG, "Current App Process PID: " + pid);
            return pid;
        } catch (Exception e) {
            Log.d(TAG, "Not get App Process PID.");
            e.printStackTrace();
        }
        return -1;
    }

}
