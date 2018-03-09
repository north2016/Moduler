关于组件化，相信很多人耳熟能详，网上的组件化框架也如雨后春笋，最近做了一些 [组件化调研](https://www.jianshu.com/p/0d45f2a894ba)，开始着手探索适合自己项目的一条组件化之路，在此分享一下，欢迎指正交流。

###组件化之核心技术点

    在阅读了大部分组件化相关的文章和框架之后，大致能总结出以下几点：
        1、组件的路由的注册和中央路由的采集
        2、组件间的通信(跨进程)和打包之后的模块间通信(同进程)，以及其兼容(跨/同进程)
        3、组件向外提供服务，以及组建间的服务的同异步互调，以及其兼容(跨/同进程)


我们的目标以及达到的成果：
   
    1、最小代价组件化，最简单的配置、最灵活的切换
    2、组件路由自动化注册，中央路由自动化采集
    3、服务自动注册，兼容同异步，兼容跨/同进程
    4、OkBus实现的通信机制，兼容同异步，兼容跨/同进程，与传统使用方式完全   一样，无感知无差别
    5、跨组件调用时自动唤醒，单个组件调试时无需手动打开目标组件，即使目标开启后被杀掉进程，同样可以唤醒加通信一步到位
    6、代码量少的可怜，实在一句多余的代码都不想写的懒人体验





###一、组建自动注册和中央路由自动收集APT+SPI

   ####第一步，路由Map自动化注册交给(APT)annotationProcessor

   具体细节无需多言，注解标识目标url＋注解处理器集中处理生成代码,借助javapoet和auto-service，实现自动注册路由到组件Map。


注意、这一步，生成的代码类都会被标注上@AutoService，成为路由注册服务的提供者

   ####第二步，中央路由采用SPI自动收集

  Java提供的SPI全名就是Service Provider Interface,下面是一段官方的解释,，其实就是为某个接口寻找服务的机制，有点类似IOC的思想，将装配的控制权移交给ServiceLoader。
    
SPI在平时我们用到的会比较少，但是在Android模块开发中就会比较有用，不同的模块可以基于接口编程，每个模块有不同的实现service provider,然后通过SPI机制自动注册到一个配置文件中，就可以实现在程序运行时扫描加载同一接口的不同service provider。这样模块之间不会基于实现类硬编码，可插拔。


注上@AutoService的接口实现类，会在META-INF下自动生成接口的服务实现列表
![META-INF下自动生成接口的服务实现列表](http://upload-images.jianshu.io/upload_images/751860-f56efdf290ae5856.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


在最终打包的Application自动采集子组件的路由器:
```  
        ServiceLoader<IRouterRulesCreator> loader =  ServiceLoader.load(IRouterRulesCreator.class);
        for (IRouterRulesCreator rules : loader) Router.addRouterRule(rules);

```  

在独立运行的组件Application也是如此。

   ####第三步，Messager扩展OkBus实现跨/同进程的无差别操作

在未组件化之前，使用OkBus的APP架构图为：

![未组件化之前OkBus的APP架构图](http://upload-images.jianshu.io/upload_images/751860-099667ed60035ce9.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


在同一进程中，使用OKBus在不同模块间传递Message数据。


组件化之后的架构图为：


![Messager扩展OkBus实现跨进程](http://upload-images.jianshu.io/upload_images/751860-e28bf7ef7d91fbbe.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)



特点：

    1、组件化和非组件化对OkBus来说使用方式完全一样，无感知无差别，旧代码基本不用改
    2、Messenger 相对于ContentProvider、Socket、AIDL。操作最简单，它是对AIDL的Message传递做了封装，Message可以作为任何序列数据的载体
    3、只有一个服务器，再多组件整体架构也不冗乱
    4、自动判断单组件运行和多组件打包状态，
    5、模块自动化注册，数据自动经过服务器转发，可以到达APP内的组件的任何一环



实现原理：
     服务器保存客户端注册的信使，收到消息时，遍历转发
``` 
 //1.根据模块ID保存所有的客户端信使 
private ConcurrentHashMap<Integer, Messenger> mClientMessengers = new ConcurrentHashMap<>();

``` 
``` 
 //2.收到消息时转发给其他模块的处理器，来源模块除外 
                Enumeration keys = mClientMessengers.keys();
                while (keys.hasMoreElements()) {
                    int moduleId = (int) keys.nextElement();
                    Messenger mMessenger = mClientMessengers.get(moduleId);
                    if (moduleId != msg.arg1) {//不是目标来源模块，进行分发
                        Message _msg = Message.obtain(msg);
                        try {
                            mMessenger.send(_msg);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
``` 






   ####第四步，OkBus实现同异步服务互调

服务互调就是OkBus的两个消息，触发服务一个消息，返回结果一个消息


异步互调就是两个消息，对应两个回调

``` 
   /**
     * 服务规则：
     * 1、服务的请求ID必须是负值(正值表示事件)
     * 2、服务的请求ID必须是奇数，偶数表示该服务的返回事件，
     * 即：   requestID－1 ＝ returnID
     * 例如  -0xa001表示服务请求  -0xa002表示-0xa001的服务返回
     */

``` 

``` 

    /**
     * 注册服务
     *
     * @param serviceId 服务id
     * @param callback  服务调用的回调
     * @param <T>       服务返回的数据范型
     */
    public <T> void registerService(final int serviceId, final CallBack<T> callback) {
        okBus.unRegister(serviceId);//服务提供者只能有一个
        okBus.register(serviceId, new Event() {
            @Override
            public void call(Message msg) {
                //TODO 优化到子线程
                OkBus.getInstance().onEvent(serviceId - 1, callback.onCall(msg));
            }
        });
    }



/**
     * 异步调用服务
     *
     * @param serviceId 服务id
     * @param callback  回调
     */
    public void fetchService(final int serviceId, final Event callback) {
        if (serviceId > 0 || serviceId % 2 == 0) {
            assert false : "请求ID必须是负奇值!";
            return;
        }
        //1、先注册回调
        okBus.register(serviceId - 1, new Event() {
            @Override
            public void call(Message msg) {
                callback.call(msg);
                okBus.unRegister(serviceId - 1);//服务是单次调用，触发后即取消注册
            }
        }, Bus.BG);
        //2、通知目标模块
        okBus.onEvent(serviceId);
    }
``` 


两个即时的消息一来一回，就完成了服务的互调，
服务因为是实时调用，因此调用完之后立马注销回调即可。


同步调用则是在异步调用的基础上加了锁：

``` 
/**
     * 同步调用服务
     *
     * @param serviceId 服务ID
     * @param timeout   超时时间
     * @return
     */
    public synchronized <T> T fetchService(final int serviceId, int timeout) {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<T> resultRef = new AtomicReference<>();
        service.execute(new Runnable() {
            @Override
            public void run() {
                fetchService(serviceId, new Event() {
                    @Override
                    public void call(Message msg) {
                        try {
                            resultRef.set((T) msg.obj);
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            latch.countDown();
                        }
                    }
                });
            }
        });
        try {
            latch.await(timeout, TimeUnit.SECONDS); //最多等待timeout秒
        } catch (Exception e) { //等待中断
            e.printStackTrace();
        }
        return resultRef.get();
    }
``` 


由于主线程加锁来实现的同步，所以要根据不同组件的ANR触发上限传入timeout

同时，所有数据接收的处理必须放到子线程，否则就是死锁：

``` 
 private class WorkThread extends Thread {
        Handler mHandler;

        @Override
        public void run() {
            Looper.prepare();
            mHandler = new ServiceHandler();
            mMessenger = new Messenger(mHandler);
            Looper.loop();
        }

        public void quit() {
            mHandler.getLooper().quit();
        }
    }
``` 

注意：子线程Handler需要自己Looper.prepare





   ####第五步，组件和服务的自动化注册

    原理也是SPI，1、声明一个组件：
``` 
@AutoService(IModule.class)
public class Module extends BaseModule {
    @Override
    public void afterConnected() {

    }

    @Override
    public int getModuleIdId() {
        return Constants.MODULE_B;
    }
}
``` 


2、SPI自动注册
``` 
        //自动注册组件服务
        ServiceLoader<IModule> modules = ServiceLoader.load(IModule.class);
        for (IModule module : modules) module.init();
``` 
   
####第六步，自动唤醒，按需加载


单个组件调试时，自动唤醒服务器，需要调用某个组件服务时，自动唤醒目标组件，服务器和目标组件打不打开，有没有被杀死，都能正常唤醒继续通信

实现方案：
   1、任意组件打开时，自动唤醒服务器


   BaseAppModuleApp里面：

``` 
      Intent intent = new Intent(MessengerService.class.getCanonicalName());// 5.0+ need explicit intent
      intent.setPackage(Constants.SERVICE_PACKAGE_NAME); // the package name of Remote Service
      boolean mIsBound = bindService(intent, mBaseModule.mConnection, BIND_AUTO_CREATE);
        
``` 


2、调用目标组件的服务时，自动唤醒目标组件
``` 

        Intent ait = new Intent(NoticeService.class.getCanonicalName()); //唤醒目标进程的服务Action名
        ait.setPackage(Constants.MODULE_PACKAGE_PRE + module_name);   //唤醒目标进程的包名
        BaseAppModuleApp.getBaseApplication().bindService(ait, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                if (service != null) {
                    LogUtils.logOnUI(Constants.TAG, "已经自动唤醒" + module_name);
                    Messenger moduleNameMessenger = new Messenger(service);
                    Message _msg = Message.obtain();
                    Bundle _data = new Bundle();
                    _data.putBoolean(Constants.NOTICE_MSG, true);
                    _msg.setData(_data);
                    _msg.replyTo = okBus.mServiceMessenger;//把服务器的信使给目标组件的信使，让他俩自己联系，这里仅仅是通知
                    try {
                        moduleNameMessenger.send(_msg);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //唤醒成功，继续发送异步请求，通知目标模块
                    okBus.onEvent(serviceId);
                } else {
                    LogUtils.logOnUI(Constants.TAG, module_name + "进程,本来就是醒的");
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                LogUtils.logOnUI(Constants.TAG, "自动唤醒目标进程失败 module_name:" + module_name);
            }
        }, BIND_AUTO_CREATE);
    }
``` 
####其它

   1、主app壳的处理：
``` 
dependencies {
    if (isDebug.toBoolean()) {//调试阶段，只保证基本逻辑不报错
        implementation project(":lib")
    } else {//打包阶段，才真正的引入业务逻辑模块
        implementation project(":module_a")
        implementation project(":module_b")
        implementation project(":module_service")
    }
}
``` 

     开发阶段，模块不稳定，直接引用，避免各种麻烦。

     稍微稳定之后，可以直接使用一个编译好的aar包，减少编译工作量提升编译速度。

     对于完全稳定，基本不会改的模块，直接引用仓库上的内容，在gradle中声明依赖就行了。

2、组件只有当做单独APP运行时才有自己的application
``` 
sourceSets {
        main {
            if (isDebug.toBoolean()) {
                manifest.srcFile 'src/main/debug/AndroidManifest.xml'//这里面才有application
            } else {
                manifest.srcFile 'src/main/AndroidManifest.xml'
            }
        }
    }
``` 

3、全局组件开关 gradle.properties设置isDebug，gradle自动切换
``` 
if (isDebug.toBoolean()) {
    apply plugin: 'com.android.application'
} else {
    apply plugin: 'com.android.library'
}
``` 