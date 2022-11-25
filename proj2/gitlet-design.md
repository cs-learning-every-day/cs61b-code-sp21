# Gitlet Design Document
## Res
- [Write yourself a Git!](https://wyag.thb.lt/#org88aa70e)
- [Git from the inside out](https://codewords.recurse.com/issues/two/git-from-the-inside-out)
- [这才是真正的Git——Git内部原理揭秘！](https://zhuanlan.zhihu.com/p/96631135)
- [CS61B学习笔记——proj2 gitlet的实现](https://blog.csdn.net/weixin_43405649/article/details/124270510)
- [Gitlet](https://yukang-lian.github.io/2022/06/25/Gitlet/)
- [Gitlet js](http://gitlet.maryrosecook.com/docs/gitlet.html)

## 存储布局 
.getlet
    / objects  -- 将文件sha-1 id 取前两位当文件夹, 后38位当文件名
        / commits -- 提交记录
        / blobs -- 文件
            / 01
                / 00addc5befcfd6ae8cc18473297a5336242d92
    / refs
        / heads -- 分支信息
            / master  存放当前commit id
        / remotes
        / tags
    / add-staged -- staged for addition
    / removal-staged -- staged for removal
    / HEAD -- current branch name


## 调试指南
先编译javac *.java

python3 tester.py samples/test01-init.in

详细信息

python3 tester.py --verbose samples/test01-init.in
