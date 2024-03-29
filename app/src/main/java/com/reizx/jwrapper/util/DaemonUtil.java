package com.reizx.jwrapper.util;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DaemonUtil {
    /**
     * 监控服务
     */
    private static ScheduledThreadPoolExecutor monitorService = new ScheduledThreadPoolExecutor(1);

    public static void daemon() {
        // 启动服务（每隔一段时间监控输出一下内存信息）
        startDaemon();

        // 添加钩子，实现优雅停服（主要验证钩子的作用）
        Runtime.getRuntime().addShutdownHook(new Thread("DAEMON-STOP") {
            @Override
            public void run() {
                System.out.println("接收到退出的讯号，开始打扫战场，释放资源，完成优雅停服");
                stopDaemon();
            }
        });
    }


    /**
     * 启动监控服务，监控一下内存信息
     */
    public static void startDaemon() {
        System.out.println(String.format("启动监控服务 %s", Thread.currentThread().getId()));
        monitorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                //设置为空
                System.out.println(String.format("最大内存: %dm  已分配内存: %dm  已分配内存中的剩余空间: %dm  最大可用内存: %dm",
                        Runtime.getRuntime().maxMemory() / 1024 / 1024,
                        Runtime.getRuntime().totalMemory() / 1024 / 1024,
                        Runtime.getRuntime().freeMemory() / 1024 / 1024,
                        (Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory() +
                                Runtime.getRuntime().freeMemory()) / 1024 / 1024));
            }
        }, 2, 2, TimeUnit.SECONDS);
    }


    /**
     * 释放资源（代码来源于 flume 源码）
     * 主要用于关闭线程池（看不懂的同学莫纠结，当做黑盒去对待）
     */
    public static void stopDaemon() {
        System.out.println(String.format("开始关闭线程池 %s", Thread.currentThread().getId()));
        if (monitorService != null) {
            monitorService.shutdown();
            try {
                monitorService.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                System.err.println("Interrupted while waiting for monitor service to stop");
            }
            if (!monitorService.isTerminated()) {
                monitorService.shutdownNow();
                try {
                    while (!monitorService.isTerminated()) {
                        monitorService.awaitTermination(10, TimeUnit.SECONDS);
                    }
                } catch (InterruptedException e) {
                    System.err.println("Interrupted while waiting for monitor service to stop");
                }
            }
        }
        System.out.println(String.format("线程池关闭完成 %s", Thread.currentThread().getId()));
    }
}
