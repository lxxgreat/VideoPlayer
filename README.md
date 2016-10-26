# VideoPlayer
功能点如下:
  
1. 视频播放功能  
(1) 支持android所支持的视频格式  

2. 文件下载到本地 (采用github开源库, 已验证)  
(1) 断点续传  
(2) Disk cache  
(3) 多线程异步下载  

3. 下载到本地后, 加密保存, 解密播放  
(1) 使用AES128加密  
(2) 为提高加解密效率, 只对文件头前16字节加解密  

4. 当文件大小大于20M时, 考虑android对app内存要求, 直接播放网络视频

5. 在MI2,MI3,MI4,MI5, N5上已验证

6. 服务器地址及文件大小如下:  
(1) 地址如: http://114.55.231.90:1987/static/public/MP4/test1.mp4  
(2) 大小  
-rw-r--r-- 1 shane wheel 35073567 10月 22 21:21 test1.mp4  
-rw-r--r-- 1 shane wheel 20704310 10月 22 23:20 test2.mp4  
-rw-r--r-- 1 shane wheel  7136220 10月 25 22:43 test3.mp4  
  
7. TODO LIST  







