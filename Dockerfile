#使用jdk8作为基础镜像
FROM openjdk:8
#指定作者
MAINTAINER songyuankun

EXPOSE 8082

ENV LANG C.UTF-8

ADD ./src/main/resources/config/application.yml /application.yml

#将复制指定的docker-demo-0.0.1-SNAPSHOT.jar为容器中的job.jar，相当于拷贝到容器中取了个别名
ADD ./build/libs/fanli-1.0-SNAPSHOT.jar /application.jar
#创建一个新的容器并在新的容器中运行命令
#RUN bash -c 'touch /application.jar'
#设置时区
#ENV TZ=PRC
#RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
#相当于在容器中用cmd命令执行jar包  指定外部配置文件
ENTRYPOINT ["java","-jar","-Duser.timezone=GMT+08","/application.jar"]