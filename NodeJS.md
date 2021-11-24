# Node.js

：Node.js 就是运行在服务端的 JavaScript。

：一种javascript的运行环境，能够使得javascript脱离浏览器运行。

：Node.js是一个服务器端的、非阻断式I/O的、事件驱动的JavaScript运行环境。

**非阻塞**：把非阻塞的服务器想象成一个loop循环，这个loop会一直跑下去。一个新请求来了，这个loop就接了这个请求，把这个请求传给其他的进程（比如传给一个搞数据库查询的进程），然后响应一个回调（callback）。完事了这loop就接着跑，接其他的请求。

**事件驱动**：服务器只在用户那边有事发生的时候才响应（比如你社交网站上的好友发了新消息）



## 关键字

module.exports VS export

- module.exports原本就是一个空对象
- exprots是这个对象的一个引用变量
- 平时我们require进来的就是module.exports对象



## 解构

## 变量

：无论是什么类型的变量，在 Node.js 中都是以一个 `var` 来解决的

`null`

`undefined`

`NaN`

### 基础类型

number：直接赋值

string：直接赋值

boolean：直接赋值

array：引用赋值

```js
var dog = new Array();
var dog = new Array("1", "2", "3");
var dog = [
    "嘘~",
    "蛋花汤",
    "在睡觉"
];
```



## 函数

：在 JavaScript中，一个函数可以作为另一个函数的参数。

### 内置函数

 `parseInt` ： 将一个字符串解析成 `int` 类型的变量

```js
var a = "1";
var b = 2;
console.log(a + b);   //12
console.log(parseInt(a) + b);  //3
```

`typeof`：判断一个变量的类型

### 循环

`for ... i`：

`for ... in`：用来遍历 **JSON对象**、**数组**、**对象**的键名的，而不提供键值的遍历

```
for(var key in foo) {
    console.log(key + ": " + foo[key]);
}

//hello: world
//node: js
//blahblah: bar
```

### 成员函数声明

<类名>.prototype.<函数名> = <函数>

```js
function foo() {
  this.hello = "world";
}

foo.prototype.setHello = setHello;

//只有在加了 this 的时候才是调用类的成员变量，否则只是函数内的一个局部变量而已。
```

### 匿名函数

```js
foo.prototype.setHello = function(hello) {
    this.hello = hello;
}
```

## 回调

​	例如，我们可以一边读取文件，一边执行其他命令，在文件读取完成后，我们将文件内容作为回调函数的参数返回。这样在执行代码时就没有阻塞或等待文件 I/O 操作。

## Promise

## async

## await

