# 反射/AOP/DI

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

### 反射e.g:（？）

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



## AOP

## DI



