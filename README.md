# Currency
一个美观的汇率转换软件，使用 kotlin 编写，通过 anko 框架编写 UI 并实现漂亮的主题动态切换效果。

<img src="screenshots/Screenshot.png" width = "300" alt="screenshot" align=center />

## kotlin

JetBrains 维护的 JVM 语言。和 Java6 相比，kotlin 表达更为简洁，提供 Lambda 表达式、类型推断、null 安全等特性。

```
fun main(args: Array<String>) {
    println("hello world")
}
```

## anko

anko 是 kotlin 提供的 ui 框架，通过 anko 我们可以在代码中定义布局，相比于 xml 具有更加强大的表现能力，，也可以显著减少 activity 的代码量。本项目中 ui 的主要部分使用 anko 框架编写，实现了很好的主题切换效果。

![gif](screenshots/anim.gif)

## 汇率数据

汇率数据通过雅虎财经提供的 Api 获得，网络请求部分使用 retrofit 框架。

## About me

email: yiyuanliu1997#gmail.com
