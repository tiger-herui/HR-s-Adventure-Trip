# Java

## 内存

### 堆

：存放new的对象和数组；

：可以被所有线程共享，不会存放别的对象引用；

### 栈

：存放基本变量类型

：引用对象的变量

### 方法区(特殊的堆)

：可以被所有的线程共享

：包含所有class和static变量



## 类的加载过程

1. Load：类的加载，将class文件读入内存，并将这些静态数据转换为方法区运行时的数据结构，然后创建一个java.lang.Class对象；

2. Link：类的链接，将类的二进制数据合并到JVM的运行环境中；

3. Initializa：类的初始化

   

## 定时任务

```java
@EnableScheduling
@Scheduled(fixedDelay = 1000, initialDelay = 1000)
@Scheduled(fixedDelay = "${fixedDelay.in.milliseconds}")
@Scheduled(fixedRateString = "${fixedRate.in.milliseconds}")
@Scheduled(cron = "${cron.expression}")
```

### corn表达式

1. *：表示匹配该域的任意值。假如在Minutes域使用*, 即表示每分钟都会触发事件。

2. ?：只能用在DayofMonth和DayofWeek两个域。它也匹配域的任意值，但实际不会。因为DayofMonth和DayofWeek会相互影响。例如想在每月的20日触发调度，不管20日到底是星期几，则只能使用如下写法： 13 13 15 20 * ?, 其中最后一位只能用？，而不能使用*，如果使用*表示不管星期几都会触发，实际上并不是这样。　　

3. -：表示范围。例如在Minutes域使用5-20，表示从5分到20分钟每分钟触发一次 

4. /：表示起始时间开始触发，然后每隔固定时间触发一次。例如在Minutes域使用5/20,则意味着5分钟触发一次，而25，45等分别触发一次. 

5. ,：表示列出枚举值。例如：在Minutes域使用5,20，则意味着在5和20分每分钟触发一次。 

6. L：表示最后，只能出现在DayofWeek和DayofMonth域。如果在DayofWeek域使用5L,意味着在最后的一个星期四触发。 

7. W：表示有效工作日(周一到周五),只能出现在DayofMonth域，系统将在离指定日期的最近的有效工作日触发事件。例如：在 DayofMonth使用5W，如果5日是星期六，则将在最近的工作日：星期五，即4日触发。如果5日是星期天，则在6日(周一)触发；如果5日在星期一到星期五中的一天，则就在5日触发。另外一点，W的最近寻找不会跨过月份 。

8. LW：这两个字符可以连用，表示在某个月最后一个工作日，即最后一个星期五。 

9. #：用于确定每个月第几个星期几，只能出现在DayofMonth域。例如在4#2，表示某月的第二个星期三。

### 异步多线程

```java
@EnableAsync

//It must be applied to public methods only.
//It can be proxied
//Self-invocation — calling the async method from within the same class — won't work.
@Async

//AsyncResult class that implements Future, and we can use this to track the result of asynchronous method execution.
AsyncResult<String>("hello world")
```

### Setting delay or rate dynamically

```java
@Configuration
@EnableScheduling
public class DynamicSchedulingConfig implements SchedulingConfigurer {

    @Autowired
    private TickService tickService;

    @Bean
    public Executor taskExecutor() {
        return Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(taskExecutor());
        taskRegistrar.addTriggerTask(
          new Runnable() {
              @Override
              public void run() {
                  tickService.tick();
              }
          },
          new Trigger() {
              @Override
              public Date nextExecutionTime(TriggerContext context) {
                  Optional<Date> lastCompletionTime =
                    Optional.ofNullable(context.lastCompletionTime());
                  Instant nextExecutionTime =
                    lastCompletionTime.orElseGet(Date::new).toInstant()
                      .plusMillis(tickService.getDelay());
                  return Date.from(nextExecutionTime);
              }
          }
        );
    }

}
```





### 定时任务框架

#### 小顶堆

：完全二叉树；

：每一个节点的值都大于其父节点；

：每一个节点都是一个job(定时任务)

：节点值对应delay(执行时间)

：使用**数组**存储 ——>取模快速定位父节点



插入：插入的元素和父节点的值相比较，小于则交互位置（逐层上浮）

删除堆顶元素：将尾部元素放入堆顶，然后下沉（和子节点较小的交换）



#### 时间调度算法



## 线程池

：池化技术——提高资源利用率；

### Executor框架

#### Runnable/Callable：任务

执行任务需要实现的 **`Runnable` 接口** 或 **`Callable`接口**。

**`Runnable` 接口**或 **`Callable` 接口** 实现类都可以被 **`ThreadPoolExecutor`** 或 **`ScheduledThreadPoolExecutor`** 执行。



#### Executor：任务执行

​	任务执行机制的核心接口 **`Executor`** ，以及继承自 `Executor` 接口的 **`ExecutorService` 接口。**

**`	ThreadPoolExecutor`** 和 **`ScheduledThreadPoolExecutor`** 这两个关键类实现了 **ExecutorService 接口**。



![hMy4PgpVZDT61XK](https://i.loli.net/2021/09/16/hMy4PgpVZDT61XK.jpg)

#### Future：异步计算结果

`Future` 接口以及 `Future` 接口的实现类 **`FutureTask`** 类都可以代表异步计算的结果。

把 **`Runnable`接口** 或 **`Callable` 接口** 的实现类提交给 **`ThreadPoolExecutor`** 或 **`ScheduledThreadPoolExecutor`** 执行，调用 `submit()` 方法时会返回一个 **`FutureTask`** 对象



![iVfQhuNvamBKdyA](https://i.loli.net/2021/09/16/iVfQhuNvamBKdyA.png)

## 重试

```java
@EnableRetry

@Retryable
```

- maxAttempts :最大重试次数，默认为3；
- value：抛出指定异常才会重试;
- include：和value一样，默认为空;
- exclude：指定不处理的异常;
- backoff：重试等待策略，默认使用@Backoff，value默认为1000L；



## 事件处理