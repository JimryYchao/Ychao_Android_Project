package com.Ychao.PerformanceAnalysis;

import android.content.Context;
import android.os.Process;

import com.Ychao.PerformanceAnalysis.Sampler.CPUSampler;
import com.Ychao.PerformanceAnalysis.Sampler.GPUSampler;
import com.Ychao.PerformanceAnalysis.Sampler.MemorySampler;
import com.Ychao.PerformanceAnalysis.Threads.CachedThread;
import com.Ychao.PerformanceAnalysis.Utils.DoubleUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.PreferenceChangeEvent;

public class SamplerSystem {

    private static SamplerSystem instance;
    private Context context;

    public static SamplerSystem getInstance(Context context) {
        if (instance == null) {
            instance = new SamplerSystem();
            instance.context = context;
        }
        return instance;
    }


    final CachedThread CachedThread = new CachedThread();

    public void getSample(SampleReceiver sampleReceiver, int SampleType) {
        CachedThread.exec(new Runnable() {
            @Override
            public void run() {

                String val = getSample(SampleType);
                sampleReceiver.OnSampleReceived(val);
            }
        });

    }

    public void getSamples(SampleReceiver samplesReceiver, int[] SampleTypes) {

        String[] calls = new String[SampleTypes.length];
        CachedThread.exec(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < SampleTypes.length; i++) {
                    calls[i] = getSample(SampleTypes[i]);
                }
                samplesReceiver.OnSamplesReceived(calls);
            }
        });
    }

    //region SampleType

    private String getSample(int SampleType) {

        switch (SampleType) {
            case 1000:
                return getTotalCpuRate();
            case 1001:
                return getProcessCpuRate();
            case 1002:
                return getCpuModel();
            case 1003:
                return getTopListByCpuOrder();

            case 2000:
                return getTotalMemRate();
            case 2001:
                return getProcessMemRate();
            case 2002:
                return getTotalMem();
            case 2003:
                return getProcessTotalPSS();
            case 2004:
                return getTopListByMemOrder();

            case 2010:
                return getSampleMemInfo();
            case 2011:
                return getDetailMemInfo();
            case 2012:
                return getAvailableMem();

            case 2020:
                return getPrivateDirty();
            case 2021:
                return getProcessPSSInfo();
            case 2022:
                return getNativeHeap();
            case 2023:
                return getDalvikHeap();

            case 3000:
                return getTotalGpuRate();
            case 3001:
                return getGpuModel();
            case 3002:
                return getMinGpuFreq();
            case 3003:
                return getMaxGpuFreq();
            default:
                return null;
        }
    }

    //TODO CPU
    private final int ToTalCpuRate = 1000;
    private final int ProcessCpuRate = 1001;
    private final int CpuModel = 1002;
    private final int TopListByCpuOrder = 1003;

    //TODO MEM
    private final int TotalMemRate = 2000;
    private final int ProcessMemRate = 2001;
    private final int TotalMemSize = 2002; // MB
    private final int ProcessTotalPSS = 2003; // MB
    private final int TopListByMemOrder = 2004;

    private final int SampleMemInfo = 2010;
    private final int DetailMemInfo = 2011;
    private final int AvailableMem = 2012; // MB

    private final int PrivateDirty = 2020; // KB
    private final int ProcessPSSInfo = 2021; // KB
    private final int NativeHeap = 2022; // KB
    private final int DalvikHeap = 2023; //KB


    //TODO GPU
    private final int TotalGpuRate = 3000;
    private final int GpuModel = 3001;
    private final int GpuMinFreq = 3002;
    private final int GpuMaxFreq = 3003;

    //endregion


    //region getSample Function

    //TODO CPU SAMPLES
    public String getCpuModel() {
        return CPUSampler.getInstance().getCpuModel();
    }

    public String getTotalCpuRate() {
        return String.valueOf(CPUSampler.getInstance().getTotalCpuUsage());
    }

    public String getProcessCpuRate() {
        return String.valueOf(CPUSampler.getInstance().getProcessCpuUsageByTop(Process.myPid()));
    }

    public String getTopListByCpuOrder() {
        return CPUSampler.getInstance().getTopListByCpuOrder(10);
    }

    //TODO MEM SAMPLES

    private double TotalMem = -1; //MB

    public String getTotalMemRate() {
        double AvailSize = DoubleUtil.div(MemorySampler.getInstance().getAvailableMemSize(context), 1024 ^ 2, 2);
        return String.valueOf(DoubleUtil.div(100 * (TotalMem - AvailSize), TotalMem, 2)); //MB/MB
    }

    public String getProcessMemRate() {
        return MemorySampler.getInstance().getProcessMemUsageByTop(Process.myPid());// 存在 null 值
    }

    public String getTotalMem() {
        if (TotalMem < 0)
            TotalMem = DoubleUtil.div(MemorySampler.getInstance().getTotalMemSize(), 1024, 2);//MB
        return String.valueOf(TotalMem);
    } // MB

    public String getProcessTotalPSS() {
        return String.valueOf(DoubleUtil.div(MemorySampler.getInstance().getProcessTotalPSS(context, Process.myPid()), 1024, 2));
    } // MB

    public String getSampleMemInfo() {
        long[] mems = MemorySampler.getInstance().getSampleMemInfo();

        String memInfo = "Total Mem: " + DoubleUtil.div(mems[0], 1024, 2) + " MB";
        memInfo += "\nFree Mem: " + DoubleUtil.div(mems[1], 1024, 2) + " MB";
        memInfo += "\nBuffer Mem: " + DoubleUtil.div(mems[2], 1024, 2) + " MB";
        memInfo += "\nCached Mem: " + DoubleUtil.div(mems[3], 1024, 2) + " MB";

        return memInfo;
    } // MB

    public String getDetailMemInfo() {
        return MemorySampler.getInstance().getDetailMemInfo();
    } // KB

    public String getAvailableMem() {
        return String.valueOf(DoubleUtil.div(MemorySampler.getInstance().getAvailableMemSize(context), 1024 ^ 2, 2));
    } // MB

    public String getPrivateDirty() {
        long[] privateMems = MemorySampler.getInstance().getPrivateDirty(context, Process.myPid());

        String memInfo = "Native Private Dirty: " + privateMems[0] + " KB";
        memInfo += "\nDalvik Private Dirty: " + privateMems[1] + " KB";
        memInfo += "\nTotal Private Dirty: " + privateMems[2] + " KB";

        return memInfo;
    } // KB

    public String getProcessPSSInfo() {
        long[] Pss = MemorySampler.getInstance().getPSSInfo(context, Process.myPid());

        String memInfo = "Native PSS: " + Pss[0] + " KB";
        memInfo += "\nDalvik PSS: " + Pss[1] + " KB";
        memInfo += "\nTotal PSS: " + Pss[2] + " KB";

        return memInfo;
    } // KB

    public String getNativeHeap() {
        long[] Heaps = MemorySampler.getInstance().getNativeHeap();

        String memInfo = "Native Heap Size : " + Heaps[0] + " KB";
        memInfo += "\nNative Heap Allocated Size: " + Heaps[1] + " KB";
        memInfo += "\nNative Heap Free Size: " + Heaps[2] + " KB";

        return memInfo;
    } // KB

    public String getDalvikHeap() {
        long[] Heaps = MemorySampler.getInstance().getNativeHeap();


        String memInfo = "Dalvik Heap Size : " + Heaps[0] + " KB";
        memInfo += "\nDalvik Heap Allocated Size: " + Heaps[1] + " KB";

        return memInfo;
    } // KB

    public String getTopListByMemOrder() {
        return MemorySampler.getInstance().getTopListByCpuOrder(10);
    }

    //TODO GPU SAMPLES
    public String getTotalGpuRate() {
        return GPUSampler.getInstance().getCurGpuUsage();
    }

    public String getMinGpuFreq() {
        return GPUSampler.getInstance().getMinGpuFreq();
    }

    public String getMaxGpuFreq() {
        return GPUSampler.getInstance().getMaxGpuFreq();
    }

    public String getGpuModel() {
        return GPUSampler.getInstance().getGpuModel();
    }

    //endregion
}
