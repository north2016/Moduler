package com.easy.moduler.lib;

/**
 * Created by baixiaokang on 18/3/6.
 */

public class Constants {


    //==================URL========================//
    //组件的包名前缀
    public static final String MODULE_PACKAGE_PRE = "com.easy.moduler.module_";
    //服务器的包名
    public static final String SERVICE_PACKAGE_NAME = "com.easy.moduler.module_service";

    //==================MessagerConstants============//
    public static final String TAG = "Message";
    public static final String MESSAGE_DATA = "message_data";
    public static final String REGISTER_ID = "registerId";
    public static final String REGISTER_RES = "registerRes";//注册结果  0 失败 1 成功
    public static final String NOTICE_MSG = "notice_message";

    public static final int REGISTER_SEC = 1;
    public static final int REGISTER_FAIL = 0;


    //==========模块以及模块下的事件============//
    /**
     * 模块定义说明：
     * <p>
     * 模块暂定为0x1  ->0xf   例如：0xa
     * <p>
     * 模块下的事件  暂定为：模块名＋事件id   例如：0xa001
     */

    public static final int MODULE_S = 0x0;//服务器标识

    public static final int MODULE_PRINT_LOG = 0x0001;//log打印事件


    public static final int MODULE_A = 0xa;
    public static final int MODULE_A_EVENT001 = 0xa001;


    public static final int MODULE_B = 0xb;
    public static final int MODULE_B_EVENT001 = 0xb001;

    //==================模块间的服务定义============//
    /**
     * 服务定义规则：
     * 1、服务的请求ID必须是负值(正值表示事件)
     * 2、服务的请求ID必须是奇数，偶数表示该服务的返回事件，
     * 即：   requestID－1 ＝ returnID
     * 例如  -0xa001表示服务请求  -0xa002表示-0xa001的服务返回
     */
    public static final int SERVICE_A_UID = -0xa001;

}
