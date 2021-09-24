package com.Ychao.PerformanceAnalysis.Sampler;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Debug;
import android.util.Log;

import com.Ychao.PerformanceAnalysis.Utils.FileUtils;
import com.Ychao.PerformanceAnalysis.Utils.ShellCmdUtils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

// Default: double -1
//          long

public class MemorySampler {

    private static MemorySampler instance;
    private static final Object locker = new Object();

    public static MemorySampler getInstance() {
        if (instance == null) {
            synchronized (locker) {
                if (instance == null) {
                    instance = new MemorySampler();
                }
            }
        }
        return instance;
    }


    private static final String TAG = "[Memory Sampler]";
    private final static String Meminfo_FilePath = "/proc/meminfo";

    public String getDetailMemInfo() {
        String[] CMDs = {"/system/bin/cat", Meminfo_FilePath};

        String result = ShellCmdUtils.exec_CMD(CMDs);

        if (result != null && result != "") {
            return result;
        } else {
            Log.e(TAG, "getCpuProcessorMaxFreq: Failed");
            return "NaN";
        }
    } // KB

    /**
     * Total,Free,Buffers,Cached (KB)
     */
    public long[] getSampleMemInfo() {
        long MemTotal = 0;
        long MemFree = 0;
        long Buffers = 0;
        long Cached = 0;

        try {
            FileReader FR = new FileReader(Meminfo_FilePath);
            BufferedReader BR = new BufferedReader(FR, 8192);
            //
            String _tempStr = "";
            StringBuilder SB = new StringBuilder();

            for (int i = 0; i < 6; i++) {
                if ((_tempStr = BR.readLine()) != null) {
                    SB.append(_tempStr);
                } else {
                    break;
                }
            }

            String text = SB.toString();

            if (text != null) {
                String[] array = text.split("\\s+");
                MemTotal = Integer.valueOf(array[1]).longValue();
                MemFree = Integer.valueOf(array[3]).longValue();
                Buffers = Integer.valueOf(array[7]).longValue();
                Cached = Integer.valueOf(array[9]).longValue();
            } else {
                Log.d(TAG, "getMemoryInfo: Read Meminfo Failed");
            }

            FileUtils.closeReader(BR);
            FileUtils.closeReader(FR);

        } catch (FileNotFoundException e) {
            Log.d(TAG, "getMemoryInfo: Failed");
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            Log.d(TAG, "getMemoryInfo: Failed");
            e.printStackTrace();
        } finally {
            return new long[]{MemTotal, MemFree, Buffers, Cached};
        }
    } // KB

    public long getAvailableMemSize(Context context) {
        ActivityManager.MemoryInfo outInfo = new ActivityManager.MemoryInfo();
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        am.getMemoryInfo(outInfo);
        long availMem = outInfo.availMem;
        return availMem;
    }

    public long getTotalMemSize() {
        String readTemp = "";
        String memTotal = "";
        long memory = 0;
        try {
            FileReader fr = new FileReader(Meminfo_FilePath);
            BufferedReader localBufferedReader = new BufferedReader(fr, 8192);
            while ((readTemp = localBufferedReader.readLine()) != null) {
                if (readTemp.contains("MemTotal")) {
                    String[] total = readTemp.split(":");
                    memTotal = total[1].trim();
                }
            }
            localBufferedReader.close();
            String[] memKb = memTotal.split(" ");
            memTotal = memKb[0].trim();
            Log.d(TAG, "memTotal: " + memTotal);
            memory = Long.parseLong(memTotal);
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e.getMessage());
        }
        return memory;  //KB
    }

    /**
     *  native, dalvik, total (KB)
     */
    public long[] getPrivateDirty(Context context, int pid) {
        ActivityManager mAm = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        int[] pids = new int[1];
        pids[0] = pid;
        Debug.MemoryInfo[] memoryInfoArray = mAm.getProcessMemoryInfo(pids);
        Debug.MemoryInfo pidMemoryInfo = memoryInfoArray[0];
        long[] value = new long[3]; // Native Dalvik Total
        value[0] = pidMemoryInfo.nativePrivateDirty;
        value[1] = pidMemoryInfo.dalvikPrivateDirty;
        value[2] = pidMemoryInfo.getTotalPrivateDirty();

        return value;
    }

    /**
     * native, dalvik, total (KB)
     */
    public long[] getPSSInfo(Context context, int pid){
        long[] value = new long[3];// Native Dalvik Total
        if (pid >= 0) {
            int[] pids = {pid};


            ActivityManager mAm = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            Debug.MemoryInfo[] memoryInfoArray = mAm.getProcessMemoryInfo(pids);
            Debug.MemoryInfo pidMemoryInfo = memoryInfoArray[0];

            value[0] = pidMemoryInfo.nativePss;
            value[1] = pidMemoryInfo.dalvikPss;
            value[2] = pidMemoryInfo.getTotalPss();
        } else {
            value[0] = 0;
            value[1] = 0;
            value[2] = 0;
        }
        return value;
    }

    /**
     * 获取Runtime 本地堆分配, Native_heapSize, Native_heapAlloc, Native_headFree, 分析可能存在的内存泄露问题 (KB)
     */
    public long[] getNativeHeap(){
        int Native_HeapSize = 0;
        int Native_HeapAlloc = 1;
        int Native_HeapFree = 2;
        long[] value = new long[3];
        value[Native_HeapSize] = Debug.getNativeHeapSize() >> 10;
        value[Native_HeapAlloc] = Debug.getNativeHeapAllocatedSize() >> 10;
        value[Native_HeapFree] = value[Native_HeapSize] - value[Native_HeapFree];
        return value;
    }

    /**
     * 获取 Runtime 虚拟机堆分配数据, Dalvik_HeapSize, Dalvik_HeapAlloc (KB)
     */
    public long[] getDalvikHeap(){
        int Total_HeapSize = 0;
        int Total_HeapAlloc = 1;

        long[] value_total = new long[2];
        value_total[Total_HeapSize] = Runtime.getRuntime().totalMemory() >> 10;
        value_total[Total_HeapAlloc] = (Runtime.getRuntime().totalMemory() - Runtime
                .getRuntime().freeMemory()) >> 10;

        long[] value_native = getNativeHeap();

        int Dalvik_HeapSize = 0;
        int Dalvik_HeapAlloc = 1;
        long[] value_dalvik = new long[2];
        value_dalvik[Dalvik_HeapSize] = value_total[Total_HeapSize]
                - value_native[0];
        value_dalvik[Dalvik_HeapAlloc] = value_total[Total_HeapAlloc]
                - value_native[1];

        return value_dalvik;
    }

    public String getProcessMemUsageByTop(int pid){

        String CallStr = ProcessInfoSampler.getProcessTopInfo(pid);

        if (CallStr == null || CallStr == "") {
            return null;
        }
        String[] pidtopInfo = CallStr.split("\\s+");

        if (pid == Integer.parseInt(pidtopInfo[0])) {
            String rate = pidtopInfo[9];
            return rate;
        } else {
            Log.d(TAG, "getProcessCoreUsage: not get PidCpu Usage by this Pid: " + pid);
            return null;
        }
    }

    public String getTopListByCpuOrder(int amount) {
        String cmd = "top -n 1 -m " + amount + " -s 10";
        String result = ShellCmdUtils.exec_CMD(cmd, 5, amount);

        if (result != null && result != "")

            return result;
        else {
            Log.e(TAG, "getTopListByCpuOrder: Failed");
            return "NaN";
        }
    }

    public long getProcessTotalPSS(Context context, int pid){
        long[] value = getPSSInfo(context, pid);
        long totalPSS = value[2];

        return totalPSS; // KB
    }// KB

}
