package com.portmaster.controller;

import com.portmaster.model.PortInfo;
import com.portmaster.util.PortScanner;
import com.portmaster.util.ProcessKiller;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * @Author: linyi
 * @Date: 2025/8/2
 * @ClassName: MainController
 * @Version: 1.0
 * @Description: 主界面控制器
 */
public class MainController implements Initializable {
    
    @FXML
    private TableView<PortInfo> portTableView;
    
    @FXML
    private TableColumn<PortInfo, String> protocolColumn;
    
    @FXML
    private TableColumn<PortInfo, String> localAddressColumn;
    
    @FXML
    private TableColumn<PortInfo, Number> portColumn;
    
    @FXML
    private TableColumn<PortInfo, String> foreignAddressColumn;
    
    @FXML
    private TableColumn<PortInfo, String> stateColumn;
    
    @FXML
    private TableColumn<PortInfo, Number> pidColumn;
    
    @FXML
    private TableColumn<PortInfo, String> processNameColumn;
    
    @FXML
    private TextField searchTextField;
    
    @FXML
    private Button searchButton;
    
    @FXML
    private Button refreshButton;

    @FXML
    private Button autoRefreshButton;

    @FXML
    private Button killButton;
    
    @FXML
    private Label statusLabel;
    
    private ObservableList<PortInfo> portData = FXCollections.observableArrayList();
    private List<PortInfo> allPortData;
    private ScheduledExecutorService scheduler;
    private volatile boolean isScanning = false;
    private volatile boolean autoRefreshEnabled = true;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 初始化表格列
        setupTableColumns();

        // 加载端口数据
        loadPortData();

        // 设置按钮事件
        setupButtonEvents();

        // 启动定时刷新任务
        startAutoRefresh();
    }
    
    /**
     * 设置表格列
     */
    private void setupTableColumns() {
        protocolColumn.setCellValueFactory(new PropertyValueFactory<>("protocol"));
        localAddressColumn.setCellValueFactory(new PropertyValueFactory<>("localAddress"));
        portColumn.setCellValueFactory(new PropertyValueFactory<>("port"));
        foreignAddressColumn.setCellValueFactory(new PropertyValueFactory<>("foreignAddress"));
        stateColumn.setCellValueFactory(new PropertyValueFactory<>("state"));
        pidColumn.setCellValueFactory(new PropertyValueFactory<>("pid"));
        processNameColumn.setCellValueFactory(new PropertyValueFactory<>("processName"));
        
        portTableView.setItems(portData);
    }
    
    /**
     * 加载端口数据（首次加载）
     */
    private void loadPortData() {
        loadPortData(false);
    }

    /**
     * 加载端口数据
     * @param isAutoRefresh 是否为自动刷新
     */
    private void loadPortData(boolean isAutoRefresh) {
        if (isScanning) {
            return; // 如果正在扫描，则不重复扫描
        }

        isScanning = true;

        if (!isAutoRefresh) {
            // 手动刷新时清空数据并显示扫描状态
            statusLabel.setText("正在扫描端口...");
            portData.clear();
            allPortData = new ArrayList<>();
        }

        new Thread(() -> {
            final int[] count = {0};

            try {
                if (isAutoRefresh) {
                    // 自动刷新时，先获取所有数据再更新
                    final List<PortInfo> newPortData = PortScanner.scanPorts();

                    Platform.runLater(() -> {
                        updatePortDataSmoothly(newPortData);
                        String autoRefreshStatus = autoRefreshEnabled ? " (自动刷新中...)" : " (自动刷新已停用)";
                        statusLabel.setText("端口信息已更新，共 " + newPortData.size() + " 个端口" + autoRefreshStatus);
                        isScanning = false;
                    });
                } else {
                    // 手动刷新时，使用回调函数实现边扫描边显示
                    PortScanner.scanPortsWithCallback(portInfo -> {
                        Platform.runLater(() -> {
                            portData.add(portInfo);
                            allPortData.add(portInfo);
                            count[0]++;
                            statusLabel.setText("正在扫描端口... 已找到 " + count[0] + " 个端口");
                        });
                    });

                    Platform.runLater(() -> {
                        if (allPortData.isEmpty()) {
                            statusLabel.setText("扫描完成，未找到端口信息");
                        } else {
                            String autoRefreshStatus = autoRefreshEnabled ? " (自动刷新中...)" : " (自动刷新已停用)";
                            statusLabel.setText("端口扫描完成，共找到 " + allPortData.size() + " 个端口" + autoRefreshStatus);
                        }
                        isScanning = false;
                    });
                }
            } catch (Exception e) {
                Platform.runLater(() -> {
                    statusLabel.setText("扫描失败: " + e.getMessage());
                    isScanning = false;
                });
            }
        }).start();
    }

    /**
     * 平滑更新端口数据，保持用户选择和滚动位置
     */
    private void updatePortDataSmoothly(List<PortInfo> newData) {
        // 保存当前选中的项
        PortInfo selectedItem = portTableView.getSelectionModel().getSelectedItem();
        int selectedIndex = portTableView.getSelectionModel().getSelectedIndex();

        // 保存滚动位置
        double scrollPosition = 0;
        if (portTableView.getSkin() != null) {
            // 获取当前滚动位置（这里简化处理）
        }

        // 更新数据
        allPortData = new ArrayList<>(newData);

        // 如果当前没有搜索过滤，则显示所有数据
        String searchText = searchTextField.getText().trim();
        if (searchText.isEmpty()) {
            portData.clear();
            portData.addAll(newData);
        } else {
            // 如果有搜索条件，重新应用搜索
            searchPort();
            return; // 搜索方法会处理选择恢复
        }

        // 尝试恢复选择
        if (selectedItem != null) {
            // 查找相同的端口项（基于协议、地址、端口号匹配）
            for (int i = 0; i < portData.size(); i++) {
                PortInfo item = portData.get(i);
                if (item.getProtocol().equals(selectedItem.getProtocol()) &&
                    item.getLocalAddress().equals(selectedItem.getLocalAddress()) &&
                    item.getPort() == selectedItem.getPort()) {
                    portTableView.getSelectionModel().select(i);
                    break;
                }
            }
        }
    }

    /**
     * 启动自动刷新任务
     */
    private void startAutoRefresh() {
        if (scheduler == null || scheduler.isShutdown()) {
            scheduler = Executors.newScheduledThreadPool(1);
        }

        // 5秒自动刷新一次端口信息
        scheduler.scheduleAtFixedRate(() -> {
            if (!isScanning && autoRefreshEnabled) {
                Platform.runLater(() -> loadPortData(true)); // 传入true表示自动刷新
            }
        }, 5, 5, TimeUnit.SECONDS);

        autoRefreshEnabled = true;
        Platform.runLater(() -> autoRefreshButton.setText("停止自动刷新"));
    }

    /**
     * 停止自动刷新任务
     */
    private void stopAutoRefresh() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 切换自动刷新状态
     */
    private void toggleAutoRefresh() {
        autoRefreshEnabled = !autoRefreshEnabled;

        if (autoRefreshEnabled) {
            autoRefreshButton.setText("停止自动刷新");
            statusLabel.setText("自动刷新已启用，每10秒无感刷新一次");
        } else {
            autoRefreshButton.setText("启动自动刷新");
            statusLabel.setText("自动刷新已停用");
        }
    }
    
    /**
     * 设置按钮事件
     */
    private void setupButtonEvents() {
        // 搜索按钮事件
        searchButton.setOnAction(event -> searchPort());

        // 刷新按钮事件
        refreshButton.setOnAction(event -> loadPortData());

        // 自动刷新开关按钮事件
        autoRefreshButton.setOnAction(event -> toggleAutoRefresh());

        // 终止进程按钮事件
        killButton.setOnAction(event -> killSelectedProcesses());
    }
    
    /**
     * 搜索端口
     */
    private void searchPort() {
        String searchText = searchTextField.getText().trim();
        
        if (searchText.isEmpty()) {
            // 如果搜索框为空，显示所有端口
            portData.clear();
            portData.addAll(allPortData);
            statusLabel.setText("显示所有端口，共 " + allPortData.size() + " 个");
            return;
        }
        
        try {
            int port = Integer.parseInt(searchText);
            
            if (port < 0 || port > 65535) {
                statusLabel.setText("端口号应在 0-65535 范围内");
                return;
            }
            
            List<PortInfo> searchResult = PortScanner.searchByPort(allPortData, port);
            portData.clear();
            portData.addAll(searchResult);
            statusLabel.setText("搜索完成，找到 " + searchResult.size() + " 个匹配项");
        } catch (NumberFormatException e) {
            statusLabel.setText("请输入有效的端口号");
        }
    }
    
    /**
     * 终止选中的进程
     */
    private void killSelectedProcesses() {
        PortInfo selectedPort = portTableView.getSelectionModel().getSelectedItem();
        
        if (selectedPort == null) {
            showAlert(Alert.AlertType.WARNING, "警告", "请选择要终止的端口");
            return;
        }
        
        // 确认对话框
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("确认终止进程");
        confirmAlert.setHeaderText("确定要终止进程 \"" + selectedPort.getProcessName() + "\" (PID: " + selectedPort.getPid() + ") 吗？");
        confirmAlert.setContentText("终止进程可能导致相关应用程序异常关闭，请谨慎操作。");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // 在后台线程中执行终止操作
                new Thread(() -> {
                    String result = ProcessKiller.killProcess(selectedPort.getPid());
                    
                    // 在JavaFX应用线程中更新UI
                    javafx.application.Platform.runLater(() -> {
                        statusLabel.setText(result);
                        
                        // 刷新端口列表
                        if (result.startsWith("成功终止进程")) {
                            loadPortData();
                        }
                    });
                }).start();
            }
        });
    }
    
    /**
     * 显示警告对话框
     */
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * 处理窗口关闭事件
     */
    public void setExitHandler(Stage stage) {
        stage.setOnCloseRequest(event -> {
            event.consume(); // 阻止默认的关闭行为

            // 弹出确认对话框
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("确认退出");
            alert.setHeaderText("确定要退出端口管理工具吗？");
            alert.setContentText("点击确定退出程序，点击取消返回。");

            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    // 停止定时任务
                    stopAutoRefresh();
                    System.exit(0);
                }
            });
        });
    }
}