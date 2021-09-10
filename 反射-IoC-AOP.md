

# 反射-IoC-AOP



> ## 补充

动态语言：运行时可改变自身结构的语言；（javascript/PHP/Python）

静态语言：运行时不可改变自身结构的语言；

Java：准动态语言，利用反射机制使其获得动态语言的特性。



1. 一个类在内存中只有一个class对象；
2. class对象只能由系统建立；
3. 一个类被加载后，类的整个结构都会被封装在Class对象中；
4. 一个Class对象对应一个加载到JVM中的.class文件；



## 反射

：reflection，在运行时分析类及执行类中方法。

：通过反射获取任意一个类的内部信息（类名、构造器、字段...），并直接操作对象的内部属性及方法。

```
常规方式：引入包类名称——通过new实例化——取得实例化对象；
反射方式：实例化对象——getClass()方法——得到完整的包类名称
```



### class类的创建方式

```java
//通过对象实例获得
Class c1 = targetObject.getClass();

//通过Class.forname获得
Class c2 = Class.forname("path.path");

//通过具体类获得
Class c3 = targetClass.class;

//通过基本内置类型包装类的Type属性
Class c4 = Integer.TYPE;

//通过类加载器xxxClassLoader.loadClass()传入类路径获取
Class c5 = ClassLoader.loadClass("cn.javaguide.targetObject");
```



**动态代理的实现依赖反射**

JDK动态代理只能代理接口，不能代理类。

- 如果目标对象的实现类实现了接口，Spring AOP将会采用JDK动态代理来生产AOP代理类；
- 如果目标对象的实现类没有实现接口，Spring AOP将会采用CGLIB来生成AOP代理类。

```java
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



### 反射e.g:

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

- ​	控制：对象创建（实例化）的权利

- ​	反转：控制权的转移（交给外部环境）


：无需new对象，而是通过**IoC容器**来实例化对象，Ioc容器实际是个Map，存放各种对象；



### DI：IoC的一种实现/一种精确说法

：Dependency Injection

●**谁依赖于谁**：应用程序依赖于IoC容器；

●**为什么需要依赖**：应用程序需要IoC容器来提供对象需要的外部资源；

●**谁注入谁**：IoC容器注入应用程序某个对象——应用程序依赖的对象；

●**注入了什么**：注入某个对象所需要的外部资源——对象、资源、常量数据等。



### Spring bean

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



### 注解

：反射机制读取注解

#### 元注解

@Target  //使用在哪些地方

@Retention。//运行在哪些地方有效

@Document。//将注解生成在JAVAdoc中

@Inherited  //子类可以继承父类注解



#### 自定义注解

@Interface  //继承java.lang.annotation.Annotation

```java
@interface MyAnnotation{
  //注解的参数：参数类型+参数名+（）
  String name();
  
  int age() default 0;
  int id() default -1;
}
```



### [代理模式](/代理模式.md)



## AOP

背景：封装的过程也增加了代码的重复性；

解决：动态地讲代码切入到类的指定方法中。

：Aspect oriented programming 面向切面变成，是OOP（面向对象编程）的一种延续。

**1. 横切逻辑代码**：在多个纵向（顺序）流程中出现相同子流程的代码

**2. 横切逻辑代码使用场景**：事务控制、权限校验、日志

![VilSd7a9vZhPx6E](https://i.loli.net/2021/09/02/VilSd7a9vZhPx6E.png)



### Spring AOP

：基于动态代理

：已集成AspectJ



![](https://camo.githubusercontent.com/3c56fc05c00d6ecba86e493389f597c8cc2478aa1ede2867bedbb57d74d65b41/68747470733a2f2f696d616765732e7869616f7a6875616e6c616e2e636f6d2f70686f746f2f323031392f39323664666335343962303664323830613337333937663966643439626639642e6a7067)

