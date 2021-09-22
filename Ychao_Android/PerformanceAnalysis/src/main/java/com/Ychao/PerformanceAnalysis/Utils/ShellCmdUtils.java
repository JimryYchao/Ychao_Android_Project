package com.Ychao.PerformanceAnalysis.Utils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @return String: null, value, ""
 */


public class ShellCmdUtils {

    private final static String TAG = "[Shell CMD]";

    public static String exec_CMD(String CMD, int startLine, int lines) {
        Process PS = exec_Cmd(CMD);
        StringBuilder SB = new StringBuilder();
        try {
            InputStreamReader ISR = new InputStreamReader(PS.getInputStream());
            BufferedReader BR = new BufferedReader(ISR);

            String line;

            for (int j = 0; j < startLine - 1; j++) {
                if (BR.readLine() == null) {
                    return null;
                }
            }

            for (int i = 0; i < lines; i++) {
                if ((line = BR.readLine()) != null) {
                    SB.append(line);
                } else {
                    break;
                }
            }

            FileUtils.closeReader(BR);
            FileUtils.closeReader(ISR);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (PS != null) {
                PS.destroy();
            }
        }
        return SB.toString();
    }

    public static String exec_CMD(String CMD, int lines) {
        Process PS = exec_Cmd(CMD);
        StringBuilder SB = new StringBuilder();
        try {
            InputStreamReader ISR = new InputStreamReader(PS.getInputStream());
            BufferedReader BR = new BufferedReader(ISR);

            String line;

            for (int i = 0; i < lines; i++) {
                if ((line = BR.readLine()) != null) {
                    SB.append(line);
                } else {
                    break;
                }
            }

            FileUtils.closeReader(BR);
            FileUtils.closeReader(ISR);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (PS != null) {
                PS.destroy();
            }
        }
        return SB.toString();
    }

    public static String exec_CMD(String CMD) {
        Process PS = exec_Cmd(CMD);
        StringBuilder SB = new StringBuilder();
        try {
            InputStreamReader ISR = new InputStreamReader(PS.getInputStream());
            BufferedReader BR = new BufferedReader(ISR);


            String line;
            while ((line = BR.readLine()) != null) {
                SB.append(line);
            }

            FileUtils.closeReader(BR);
            FileUtils.closeReader(ISR);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (PS != null) {
                PS.destroy();
            }
        }
        return SB.toString();
    }

    public static String exec_CMD(String[] CMD) {
        Process PS = exec_Cmd(CMD);
        StringBuilder SB = new StringBuilder();
        try {
            InputStreamReader ISR = new InputStreamReader(PS.getInputStream());
            BufferedReader BR = new BufferedReader(ISR);


            String line;
            while ((line = BR.readLine()) != null) {
                SB.append(line);
            }

            FileUtils.closeReader(BR);
            FileUtils.closeReader(ISR);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (PS != null) {
                PS.destroy();
            }
        }
        return SB.toString();
    }

    public static String exec_CMD(String[] CMD, int lines) {
        Process PS = exec_Cmd(CMD);
        StringBuilder SB = new StringBuilder();
        try {
            InputStreamReader ISR = new InputStreamReader(PS.getInputStream());
            BufferedReader BR = new BufferedReader(ISR);

            String line;

            for (int i = 0; i < lines; i++) {
                if ((line = BR.readLine()) != null) {
                    SB.append(line );
                } else {
                    break;
                }
            }

            FileUtils.closeReader(BR);
            FileUtils.closeReader(ISR);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (PS != null) {
                PS.destroy();
            }
        }
        return SB.toString();
    }

    public static String exec_CMD(String[] CMD, int startLine, int lines) {
        Process PS = exec_Cmd(CMD);
        StringBuilder SB = new StringBuilder();
        try {
            InputStreamReader ISR = new InputStreamReader(PS.getInputStream());
            BufferedReader BR = new BufferedReader(ISR);

            String line;

            for (int j = 0; j < startLine - 1; j++) {
                if (BR.readLine() == null) {
                    return null;
                }
            }

            for (int i = 0; i < lines; i++) {
                if ((line = BR.readLine()) != null) {
                    SB.append(line);
                } else {
                    break;
                }
            }

            FileUtils.closeReader(BR);
            FileUtils.closeReader(ISR);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (PS != null) {
                PS.destroy();
            }
        }
        return SB.toString();
    }

    public static String exec_SHC(String CMD) {
        Process PS = exec_ShC(CMD);
        StringBuilder SB = new StringBuilder();
        try {
            InputStreamReader ISR = new InputStreamReader(PS.getInputStream());
            BufferedReader BR = new BufferedReader(ISR);


            String line;
            while ((line = BR.readLine()) != null) {
                SB.append(line );
            }

            FileUtils.closeReader(BR);
            FileUtils.closeReader(ISR);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (PS != null) {
                PS.destroy();
            }
        }
        return SB.toString();
    }

    public static String exec_SHC(String CMD, int lines) {
        Process PS = exec_ShC(CMD);
        StringBuilder SB = new StringBuilder();
        try {
            InputStreamReader ISR = new InputStreamReader(PS.getInputStream());
            BufferedReader BR = new BufferedReader(ISR);

            String line;

            for (int i = 0; i < lines; i++) {
                if ((line = BR.readLine()) != null) {
                    SB.append(line);
                } else {
                    break;
                }
            }

            FileUtils.closeReader(BR);
            FileUtils.closeReader(ISR);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (PS != null) {
                PS.destroy();
            }
        }
        return SB.toString();
    }

    public static String exec_SHC(String CMD, int startLine, int lines) {
        Process PS = exec_ShC(CMD);
        StringBuilder SB = new StringBuilder();
        try {
            InputStreamReader ISR = new InputStreamReader(PS.getInputStream());
            BufferedReader BR = new BufferedReader(ISR);

            String line;

            for (int j = 0; j < startLine - 1; j++) {
                if (BR.readLine() == null) {
                    return null;
                }
            }

            for (int i = 0; i < lines; i++) {
                if ((line = BR.readLine()) != null) {
                    SB.append(line);
                } else {
                    break;
                }
            }

            FileUtils.closeReader(BR);
            FileUtils.closeReader(ISR);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (PS != null) {
                PS.destroy();
            }
        }
        return SB.toString();
    }




    private static Process exec_ShC(String cmd) {
        Process process = null;
        ProcessBuilder PB = null;
        String[] args = {"sh", "-c", cmd};
        try {
            PB = new ProcessBuilder(args);
            process = PB.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return process;
    }

    private static Process exec_Cmd(String[] cmd) {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return process;
    }

    private static Process exec_Cmd(String cmd) {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            Log.e(TAG, "exec_Cmd: The command format is incorrect.");
            e.printStackTrace();
        }
        return process;
    }


}
