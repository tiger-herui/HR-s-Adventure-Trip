# 反射/IoC/AOP



## 反射

：在运行时分析类及执行类中方法。

：通过反射获取任意一个类的所有属性和方法，并调用。



**动态代理的实现依赖反射**

```java
//通过JDK实现动态代理，使用反射类Method调用指定方法。

public class DebugInvocationHandler implements InvocationHandler {
  	// 代理类中的真实对象
    private final Object target;

    public DebugInvocationHandler(Object target) {
        this.target = target;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws InvocationTargetException, IllegalAccessException {
        Object result = method.invoke(target, args);
        return result;
    }
}
```

### 

### 反射e.g:（？）

```java
//获取TargetObject类的Class对象并且创建TargetObject类实例
Class<?> tagetClass = Class.forName("cn.javaguide.TargetObject");
TargetObject targetObject = (TargetObject) tagetClass.newInstance();

//获取所有类中所有定义的方法
Method[] methods = tagetClass.getDeclaredMethods();

// 获取指定方法并调用
Method publicMethod = tagetClass.getDeclaredMethod("publicMethod", String.class);

publicMethod.invoke(targetObject, "JavaGuide"); //???这里是啥意思

//获取指定参数并对参数进行修改
Field field = tagetClass.getDeclaredField("value");

//为了对类中的参数进行修改我们取消安全检查
field.setAccessible(true);
field.set(targetObject, "JavaGuide");

//调用 private 方法
Method privateMethod = tagetClass.getDeclaredMethod("privateMethod");

//为了调用private方法我们取消安全检查
privateMethod.setAccessible(true);
privateMethod.invoke(targetObject);
```



## IoC

：Inversion  of control 控制反转，一种关于对象创建和管理的**设计思想**；

​	控制：对象创建（实例化）的权利

​	反转：控制权的转移（交给外部环境）

：无需new对象，而是通过**IoC容器**来实例化对象。



### DI：IoC最常见的一种实现

：Dependency Injection





1. ### Spring bean

bean：被IoC容器管理的对象

IoC容器通过**配置元数据（XML文件、注解、java配置类）**来管理对象：

![LygCfU29ihSclVO](https://i.loli.net/2021/09/02/LygCfU29ihSclVO.png)



### @Component VS @Bean

1. 都是注册bean到Spring容器中；

2. `@Component` 注解**作用于类**（表明一个类是组件类），而`@Bean`注解**作用于方法**；

3. `@Component`通常是通过**类路径扫描**来自动侦测、**自动装配**到 Spring 容器中；`@Bean` 注解通常是我们在标有该注解的方法中定义产生 bean；

   > 可使用 `@ComponentScan` 定义要扫描的路径

4. `@Bean` 的自定义性更强，当引用第三方库中的类装配到 `Spring`容器时，只能通过 `@Bean`来实现（不能改变别人的源代码吧）。

#### @Component

- @Service：标注业务层组件;
- @Controller：标注控制层组件；
- @Repository：标注数据访问组件，即DAO组件；
- @Component：泛指组件，当组件不好归类的时候，可以使用这个注解进行标注，标识为一个Bean。



## AOP

：Aspect oriented programming 面向切面变成，是OOP（面向对象编程）的一种延续。

**1. 横切逻辑代码**：在多个纵向（顺序）流程中出现相同子流程的代码

**2. 横切逻辑代码使用场景**：事务控制、权限校验、日志

![VilSd7a9vZhPx6E](https://i.loli.net/2021/09/02/VilSd7a9vZhPx6E.png)



### Spring AOP

：基于动态代理

：已集成AspectJ

![](https://camo.githubusercontent.com/3c56fc05c00d6ecba86e493389f597c8cc2478aa1ede2867bedbb57d74d65b41/68747470733a2f2f696d616765732e7869616f7a6875616e6c616e2e636f6d2f70686f746f2f323031392f39323664666335343962303664323830613337333937663966643439626639642e6a7067)
