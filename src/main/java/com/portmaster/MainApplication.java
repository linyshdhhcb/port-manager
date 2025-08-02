package com.portmaster;

import com.portmaster.controller.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;


/**
 * @Author: linyi
 * @Date: 2025/8/2
 * @ClassName: MainApplication
 * @Version: 1.0
 * @Description: 主程序入口
 */
public class MainApplication extends Application {
    
    private static final String APP_TITLE = "端口管理工具";
    
    @Override
    public void start(Stage primaryStage) throws IOException {
        // 加载FXML文件
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/portmaster/main.fxml"));
        Parent root = loader.load();
        
        // 获取控制器实例
        MainController controller = loader.getController();
        
        // 设置窗口标题
        primaryStage.setTitle(APP_TITLE);
        
        // 设置图标
        try {
            primaryStage.getIcons().add(new Image(Objects.requireNonNull(
                    getClass().getResourceAsStream("/com/portmaster/icon.png"))));
        } catch (Exception e) {
            // 如果图标不存在，则忽略
        }
        
        // 创建场景并设置到舞台
        Scene scene = new Scene(root);
        
        // 添加CSS样式表
        try {
            scene.getStylesheets().add(Objects.requireNonNull(
                    getClass().getResource("/css/main.css")).toExternalForm());
        } catch (Exception e) {
            // 如果样式表不存在，则忽略
        }
        
        primaryStage.setScene(scene);
        
        // 设置窗口大小
        primaryStage.setWidth(1000);
        primaryStage.setHeight(600);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(500);
        
        // 设置退出处理
        controller.setExitHandler(primaryStage);
        
        // 显示窗口
        primaryStage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}