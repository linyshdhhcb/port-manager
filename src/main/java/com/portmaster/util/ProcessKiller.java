package com.portmaster.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


/**
 * @Author: linyi
 * @Date: 2025/8/2
 * @ClassName: ProcessKiller
 * @Version: 1.0
 * @Description: 进程终止工具类
 */
public class ProcessKiller {
    
    /**
     * 终止指定PID的进程
     * @param pid 进程ID
     * @return 终止结果消息
     */
    public static String killProcess(int pid) {
        try {
            // 执行taskkill命令终止进程
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("taskkill", "/PID", String.valueOf(pid), "/F");
            Process process = processBuilder.start();
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            
            int exitCode = process.waitFor();
            reader.close();
            
            if (exitCode == 0) {
                return "成功终止进程 PID: " + pid;
            } else {
                // 读取错误输出
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                StringBuilder errorOutput = new StringBuilder();
                String errorLine;
                
                while ((errorLine = errorReader.readLine()) != null) {
                    errorOutput.append(errorLine).append("\n");
                }
                errorReader.close();
                
                return "终止进程失败: " + errorOutput.toString();
            }
        } catch (IOException | InterruptedException e) {
            return "终止进程时发生错误: " + e.getMessage();
        }
    }
}