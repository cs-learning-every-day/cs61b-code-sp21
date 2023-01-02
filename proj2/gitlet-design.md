# Gitlet Design Document
## Res
- [Write yourself a Git!](https://wyag.thb.lt/#org88aa70e)
- [Git from the inside out](https://codewords.recurse.com/issues/two/git-from-the-inside-out)
- [这才是真正的Git——Git内部原理揭秘！](https://zhuanlan.zhihu.com/p/96631135)
- [CS61B学习笔记——proj2 gitlet的实现](https://blog.csdn.net/weixin_43405649/article/details/124270510)
- [CS61B Gitlet入坑指南](https://zhuanlan.zhihu.com/p/533852291)
- [Gitlet js](http://gitlet.maryrosecook.com/docs/gitlet.html)
- [Understanding Git — Index](https://konrad126.medium.com/understanding-git-index-4821a0765cf)
- [Make your monorepo feel small with Git’s sparse index](https://github.blog/2021-11-10-make-your-monorepo-feel-small-with-gits-sparse-index/#:~:text=The%20Git%20index%20is%20a,to%20be%20%E2%80%9Cstaged%20changes%E2%80%9D.)

## 存储布局 
.gitlet
    / objects  -- 将文件sha-1 id 取前两位当文件夹, 后38位当文件名
        / commits -- 提交记录
        / blobs -- 文件快照
            / 01
                / 00addc5befcfd6ae8cc18473297a5336242d92
    / refs
        / heads -- 分支信息
            / master  存放当前commit id
        / remotes
        / tags
    / stageAdded - staged for addition
    / stageRemoval - staged for removal
    / HEAD -- current branch name


## 手动测试
```
cd tesla-test
javac ../gitlet/*.java && cp ../gitlet/*.class ./gitlet/ 
java gitlet.Main init
java gitlet.Main add file
...
```

## 代码测试
运行前一定要先在gitlet源文件目录下运行javac *.java编译文件才可以。
```
python tester.py samples/test01-init.in
输出结果
python tester.py --verbose samples/test01-init.in
```

## 指令实现

#### init
创建默认master分支, HEAD内容为master, master内容为最后一次提交，这就是默认提交一次init commit, 写入空的index区

#### add
为文件创建blob, 添加到index区中,

## 调试指南
先编译javac *.java

python3 tester.py samples/test01-init.in

详细信息

python3 tester.py --verbose samples/test01-init.in
