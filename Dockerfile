FROM java:8
#作者
MAINTAINER caisijun@merotek.cn
#声明一个挂载点，容器内此路径会对应宿主机的某个文件夹
VOLUME /tmp
#复制上下文目录下的target/au9999-crawling-data-1.0.jar 到容器里
COPY target/yugong-b-1.0.3.jar yugong.jar

#bash方式执行，使au9999-crawling-data-1.0.jar可访问
#RUN新建立一层，在其上执行这些命令，执行结束后， commit 这一层的修改，构成新的镜像。
RUN bash -c "touch /yugong.jar"

#声明运行时容器提供服务端口，这只是一个声明，在运行时并不会因为这个声明应用就会开启这个端口的服务
EXPOSE 8080

#指定容器启动程序及参数   <ENTRYPOINT> "<CMD>"
ENTRYPOINT ["java","-jar","yugong.jar"]
