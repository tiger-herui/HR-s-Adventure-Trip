​	Synchronized是Java中解决并发问题的一种最常用的方法，也是最简单的一种方法。

​	Synchronized的作用主要有三个：

1. 确保线程互斥的访问同步代码

2. 保证共享变量的修改能够及时可见

3. 有效解决重排序问题

   

![48o7fSxcO2zZsWJ](https://i.loli.net/2021/09/26/48o7fSxcO2zZsWJ.png)

![LWFP5RINlhD34HY](https://i.loli.net/2021/09/26/LWFP5RINlhD34HY.png)

## 对象头

![U6MOB7eudymfkF4](https://i.loli.net/2021/09/29/U6MOB7eudymfkF4.jpg)



### Monitor

![5VnCSdhfy1XzIMc](https://i.loli.net/2021/09/27/5VnCSdhfy1XzIMc.jpg)

​	当一个线程尝试获得锁时，如果该锁已经被占用，则会将该线程封装成一个ObjectWaiter对象插入到cxq的队列尾部，然后暂停当前线程。当持有锁的线程释放锁前，会将cxq中的所有元素移动到EntryList中去，并唤醒EntryList的队首线程。

​	如果一个线程在同步块中调用了`Object#wait`方法，会将该线程对应的ObjectWaiter从EntryList移除并加入到WaitSet中，然后释放锁。当wait的线程被notify之后，会将对应的ObjectWaiter从WaitSet移动到EntryList中。



monitor监视器源码是C++写的，在虚拟机的ObjectMonitor.hpp文件中。

```java
ObjectMonitor() {
    _header       = NULL;
    _count        = 0;
    _waiters      = 0,
    _recursions   = 0;  // 线程重入次数
    _object       = NULL;  // 存储Monitor对象
    _owner        = NULL;  // 持有当前线程的owner
    _WaitSet      = NULL;  // wait状态的线程列表
    _WaitSetLock  = 0 ;
    _Responsible  = NULL ;
    _succ         = NULL ;
    _cxq          = NULL ;  // 单向列表
    FreeNext      = NULL ;
    _EntryList    = NULL ;  // 处于等待锁状态block状态的线程列表
    _SpinFreq     = 0 ;
    _SpinClock    = 0 ;
    OwnerIsThread = 0 ;
    _previous_owner_tid = 0;
  }
```



### Monitorenter

每个对象有一个监视器锁（monitor）。当monitor被占用时就会处于锁定状态，线程执行`monitorenter`指令时尝试获取monitor的所有权，过程如下：

1. 如果monitor的进入数为0，则该线程进入monitor，然后将进入数设置为1，该线程即为monitor的所有者

2. 如果线程已经占有该monitor，只是重新进入，则进入monitor的进入数加1

3. 如果其他线程已经占用了monitor，则该线程进入阻塞状态，直到monitor的进入数为0，再重新尝试获取monitor的所有权

### Monitorexit

执行`monitorexit`的线程必须是object所对应的monitor的所有者。

指令执行时，monitor的进入数减1，如果减1后进入数为0，那线程退出monitor，不再是这个monitor的所有者。其他被这个monitor阻塞的线程可以尝试去获取这个 monitor 的所有权。 

有两个`monitorexit`指令的原因是：为了保证抛异常的情况下也能释放锁，所以`javac`为同步代码块添加了一个隐式的try-finally，在finally中会调用`monitorexit`命令释放锁。



## 修饰普通方法（this对象）

### 同步方法和非同步方法（不需要读锁）可以同时调用

![DPeGjtIS6bmqFlf](https://i.loli.net/2021/09/27/DPeGjtIS6bmqFlf.jpg)



### 可重入

![ALGaP8B4wE3tK52](https://i.loli.net/2021/09/27/ALGaP8B4wE3tK52.png)



### 调用父类同步方法

![DIXRiHvpmdVw426](https://i.loli.net/2021/09/27/DIXRiHvpmdVw426.png)



### ACC_SYNCHRONIZED

​	方法的同步并没有通过指令monitorenter和monitorexit来完成（理论上其实也可以通过这两条指令来实现），不过相对于普通方法，其常量池中多了**ACC_SYNCHRONIZED**标示符。JVM就是根据该标示符来实现方法的同步的：

​	当方法调用时，调用指令将会检查方法的 ACC_SYNCHRONIZED 访问标志是否被设置，如果设置了，执行线程将先获取monitor，获取成功之后才能执行方法体，方法执行完后再释放monitor。

​	在方法执行期间，其他任何线程都无法再获得同一个monitor对象。 其实本质上没有区别，只是方法的同步是一种隐式的方式来实现，无需通过字节码来完成。同步方法的时候，一旦执行到这个方法，就会先判断是否有标志位，然后，ACC_SYNCHRONIZED会去隐式调用刚才的两个指令：monitorenter和monitorexit。所以归根究底，还是monitor对象的争夺。



## 修饰静态方法

对静态方法本质上是属于类的方法（所以即使方法属于不同的对象也

### 静态方法和非静态方法不互斥

![7UE6R9aVtnkKi5d](https://i.loli.net/2021/09/27/7UE6R9aVtnkKi5d.png)

## 修饰代码块



## 用户态和内核态的转换

> Linux系统的体系结构分为用户空间（应用程序的活动空间）和内核。所有的程序都在用户空间运行，进入用户运行状态（用户态），也有操作可能涉及内核运行，比如I/O。
>
> ObjectMonitor源码含有Atomic::cmpxchg_ptr，Atomic::inc_ptr等内核函数，对应的线程就是park()和upark()，这个操作就设计用户态和内核态的转换。

![Xqnkm4uglzbV8Ys](https://i.loli.net/2021/09/29/Xqnkm4uglzbV8Ys.jpg)

1. 用户态把一些数据放到寄存器，或者创建对应的堆栈，表明需要操作系统提供的服务。
2. 用户态执行系统调用（系统调用是操作系统的最小功能单位）。
3. CPU切换到内核态，跳到对应的内存指定的位置执行指令。
4. 系统调用处理器去读取我们先前放到内存的数据参数，执行程序的请求。
5. 调用完成，操作系统重置CPU为用户态返回结果，并执行下个指令。



## 锁升级

<img src="https://i.loli.net/2021/09/27/W2d7okRpBsMOHYZ.jpg" alt="W2d7okRpBsMOHYZ" style="zoom: 200%;" />





## Synchronized vs Lock

- synchronized是关键字，是JVM层面的底层啥都帮我们做了，而Lock是一个接口，是JDK层面的有丰富的API。
- synchronized会自动释放锁，而Lock必须手动释放锁。
- synchronized是不可中断的，Lock可以中断也可以不中断。
- 通过Lock可以知道线程有没有拿到锁，而synchronized不能。
- synchronized能锁住方法和代码块，而Lock只能锁住代码块。
- Lock可以使用读锁提高多线程读效率。
- synchronized是非公平锁，ReentrantLock可以控制是否是公平锁。