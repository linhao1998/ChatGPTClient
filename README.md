# ChatGPTClient

开源的ChatGPT Android客户端App，提供更好的移动端ChatGPT使用体验。

>项目持续完善中，欢迎使用反馈。

## 示例图片
<p align="left">
   <img src=".\images\0.jpg" style="width:200px;" /><img src=".\images\1.jpg" style="width:200px;" /><img src=".\images\2.jpg" style="width:200px;" /><img src=".\images\3.gif" style="width:200px;" />
</p>

## 特性

- 支持本地数据存储
- 支持Markdown格式
- 支持单步对话和连续对话切换
- 支持横屏和夜间模式
- 支持字体大小设置

## 下载

#### Apk下载链接： [Apk下载链接](https://github.com/linhao1998/ChatGPTClient/releases/download/1.0.0/app-release.apk)

## 使用说明

1. 进入网站https://platform.openai.com/account/api-keys，界面如下：

<img src=".\images\apikey.png" style="width:80%; float:left" />

2. 创建一个OpenAI API key，复制该key；

3. 进入App设置页面，在你的API key中填入上次复制的key，填写正确后即可正常使用。

*注意：需要有ChatGPT账号，App使用需要能够科学上网。*

## 技术栈和开源库

- JetPack

  - LiveData：可观察的数据持有类，在数据发生变化时通知组件更新UI。

  - ViewModel：管理与界面相关的数据，可帮助处理旋转等配置更改时发生的生命周期问题。

  - Room：提供SQLite数据库的抽象层，方便进行数据库操作。

  - Flow：用于异步数据流的处理。

- Markwon：用于Markdown解析的库。

- Prism4j：用于代码高亮的库。

- openai-kotlin：Kotlin的OpenAI API库。

- material-components-android：适用于 Android 的模块化和可定制的 Material Design UI 组件。

- Toasty：用于显示自定义Toast消息的库。

## 架构

项目使用MVVM架构模式实现。

<img src=".\images\chatgpt_architecture.png" />

## License

```
Copyright (C) ChatGPTClient Open Source Project

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```