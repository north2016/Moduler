package com.easy.moduler.lib.okbus;

/**
 * Created by baixiaokang on 18/3/6.
 */

public interface IModule {
    /**
     * 模块初始化，只有组建时才调用，用于开启子线程轮训消息
     */
    void init();

    /**
     * 模块ID
     *
     * @return 模块ID
     */
    int getModuleId();

    /**
     * 模块注册并连接成功后，可以做以下事情：
     * <p>
     * 1、注册监听事件
     * 2、发送事件
     * 3、注册服务
     * 4、调用服务
     */
    void afterConnected();
}
