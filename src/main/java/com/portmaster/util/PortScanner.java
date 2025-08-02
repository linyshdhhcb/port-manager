package com.portmaster.util;

import com.portmaster.model.PortInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @Author: linyi
 * @Date: 2025/8/2
 * @ClassName: PortScanner
 * @Version: 1.0
 * @Description: 端口扫描工具类
 */
public class PortScanner {
    
    /**
     * 扫描系统中所有被占用的端口
     * @return 端口信息列表
     */
    public static List<PortInfo> scanPorts() {
        List<PortInfo> portList = new ArrayList<>();

        try {
            // 执行netstat命令获取端口信息
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("netstat", "-ano");
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "GBK"));
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                // 跳过空行和标题行
                if (line.isEmpty() || line.contains("协议") || line.contains("活动连接")) {
                    continue;
                }

                // 解析端口信息行
                String[] parts = line.split("\\s+");
                if (parts.length >= 5) {
                    try {
                        String protocol = parts[0];
                        String localAddressFull = parts[1];
                        String foreignAddress = parts[2];
                        String state = parts[3];
                        int pid = Integer.parseInt(parts[4]);

                        // 解析本地地址和端口
                        String localAddress;
                        int port;

                        // 处理IPv6地址格式 [::]:port 或 IPv4地址格式 ip:port
                        if (localAddressFull.startsWith("[") && localAddressFull.contains("]:")) {
                            // IPv6格式: [::]:port
                            int lastColonIndex = localAddressFull.lastIndexOf(":");
                            localAddress = localAddressFull.substring(0, lastColonIndex);
                            port = Integer.parseInt(localAddressFull.substring(lastColonIndex + 1));
                        } else {
                            // IPv4格式: ip:port
                            int lastColonIndex = localAddressFull.lastIndexOf(":");
                            if (lastColonIndex > 0) {
                                localAddress = localAddressFull.substring(0, lastColonIndex);
                                port = Integer.parseInt(localAddressFull.substring(lastColonIndex + 1));
                            } else {
                                continue; // 跳过无法解析的行
                            }
                        }

                        // 暂时不获取进程名称，提高速度
                        String processName = "PID:" + pid;

                        PortInfo portInfo = new PortInfo(protocol, localAddress, port, foreignAddress, state, pid, processName);
                        portList.add(portInfo);

                    } catch (NumberFormatException e) {
                        System.err.println("解析端口信息失败: " + line + " - " + e.getMessage());
                        continue; // 跳过无法解析的行
                    }
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("netstat命令执行失败，退出码：" + exitCode);
            }
            reader.close();
        } catch (IOException | InterruptedException e) {
            // 记录错误信息
            System.err.println("端口扫描失败: " + e.getMessage());
            e.printStackTrace(); // 添加堆栈跟踪信息
            return portList; // 返回空列表
        }

        // 打印扫描到的端口信息（调试用）
        System.out.println(LocalDateTime.now() +"：自动扫描到的端口信息: " + portList.size());

        return portList;
    }

    /**
     * 实时扫描端口并通过回调函数逐个返回结果
     * @param callback 每找到一个端口时的回调函数
     */
    public static void scanPortsWithCallback(Consumer<PortInfo> callback) {
        try {
            // 执行netstat命令获取端口信息
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("netstat", "-ano");
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "GBK"));
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                // 跳过空行和标题行
                if (line.isEmpty() || line.contains("协议") || line.contains("活动连接")) {
                    continue;
                }

                // 解析端口信息行
                String[] parts = line.split("\\s+");
                if (parts.length >= 5) {
                    try {
                        String protocol = parts[0];
                        String localAddressFull = parts[1];
                        String foreignAddress = parts[2];
                        String state = parts[3];
                        int pid = Integer.parseInt(parts[4]);

                        // 解析本地地址和端口
                        String localAddress;
                        int port;

                        // 处理IPv6地址格式 [::]:port 或 IPv4地址格式 ip:port
                        if (localAddressFull.startsWith("[") && localAddressFull.contains("]:")) {
                            // IPv6格式: [::]:port
                            int lastColonIndex = localAddressFull.lastIndexOf(":");
                            localAddress = localAddressFull.substring(0, lastColonIndex);
                            port = Integer.parseInt(localAddressFull.substring(lastColonIndex + 1));
                        } else {
                            // IPv4格式: ip:port
                            int lastColonIndex = localAddressFull.lastIndexOf(":");
                            if (lastColonIndex > 0) {
                                localAddress = localAddressFull.substring(0, lastColonIndex);
                                port = Integer.parseInt(localAddressFull.substring(lastColonIndex + 1));
                            } else {
                                continue; // 跳过无法解析的行
                            }
                        }

                        // 暂时不获取进程名称，提高速度
                        String processName = "PID:" + pid;

                        PortInfo portInfo = new PortInfo(protocol, localAddress, port, foreignAddress, state, pid, processName);

                        // 立即通过回调函数返回结果
                        callback.accept(portInfo);

                    } catch (NumberFormatException e) {
                        System.err.println("解析端口信息失败: " + line + " - " + e.getMessage());
                        continue; // 跳过无法解析的行
                    }
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("netstat命令执行失败，退出码：" + exitCode);
            }
            reader.close();
        } catch (IOException | InterruptedException e) {
            // 记录错误信息
            System.err.println("端口扫描失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 根据进程ID获取进程名称
     * @param pid 进程ID
     * @return 进程名称
     */
    private static String getProcessNameByPid(int pid) {
        if (pid <= 0) {
            return "N/A";
        }

        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("tasklist", "/fi", "PID eq " + pid, "/fo", "csv", "/nh");
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "GBK"));
            String line;

            // 读取进程信息行 (CSV格式，无标题)
            if ((line = reader.readLine()) != null) {
                // CSV格式: "进程名","PID","会话名","会话#","内存使用"
                String[] parts = line.split(",");
                if (parts.length > 0) {
                    // 移除引号
                    String processName = parts[0].replace("\"", "");
                    return processName;
                }
            }

            process.waitFor();
            reader.close();
        } catch (IOException | InterruptedException e) {
            // 如果获取失败，返回PID
            return "PID:" + pid;
        }

        return "PID:" + pid;
    }
    
    /**
     * 根据端口号搜索端口信息
     * @param portList 端口信息列表
     * @param port 端口号
     * @return 匹配的端口信息列表
     */
    public static List<PortInfo> searchByPort(List<PortInfo> portList, int port) {
        List<PortInfo> result = new ArrayList<>();
        for (PortInfo portInfo : portList) {
            if (portInfo.getPort() == port) {
                result.add(portInfo);
            }
        }
        return result;
    }
}