package com.portmaster.model;


/**
 * @Author: linyi
 * @Date: 2025/8/2
 * @ClassName: PortInfo
 * @Version: 1.0
 * @Description: 端口信息类
 */
public class PortInfo {
    private String protocol;
    private String localAddress;
    private int port;
    private String foreignAddress;
    private String state;
    private int pid;
    private String processName;

    public PortInfo() {
    }

    public PortInfo(String protocol, String localAddress, int port, String foreignAddress, String state, int pid, String processName) {
        this.protocol = protocol;
        this.localAddress = localAddress;
        this.port = port;
        this.foreignAddress = foreignAddress;
        this.state = state;
        this.pid = pid;
        this.processName = processName;
    }

    // Getters and Setters
    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getLocalAddress() {
        return localAddress;
    }

    public void setLocalAddress(String localAddress) {
        this.localAddress = localAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getForeignAddress() {
        return foreignAddress;
    }

    public void setForeignAddress(String foreignAddress) {
        this.foreignAddress = foreignAddress;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    @Override
    public String toString() {
        return "PortInfo{" +
                "protocol='" + protocol + '\'' +
                ", localAddress='" + localAddress + '\'' +
                ", port=" + port +
                ", foreignAddress='" + foreignAddress + '\'' +
                ", state='" + state + '\'' +
                ", pid=" + pid +
                ", processName='" + processName + '\'' +
                '}';
    }
}