#Building Docker Image for Patent Master
FROM amazonlinux:latest

# Install OpenJDK-8
RUN yum install -y java-1.8.0-openjdk;

# Setup JAVA_HOME -- useful for docker commandline
ENV JAVA_HOME /usr/lib/jvm/jre-1.8.0-openjdk-1.8.0.*.x86_64/
RUN export JAVA_HOME

#Install office
RUN amazon-linux-extras install libreoffice
RUN yum install -y procps

ADD target/RAAD-Converter-0.0.1-SNAPSHOT.jar app.jar
## copy local fount for remove utf-8 issue
COPY Fonts /usr/share/fonts

EXPOSE 9097
ENTRYPOINT ["java", "-Dfile.encoding=UTF-8", "-jar", "app.jar"]


#Building Docker Image for Patent Master
#FROM amazonlinux:1

# Add primary jar
#ADD target/RAAD-Converter-0.0.1-SNAPSHOT.jar app.jar
#ADD Fonts /System/Library/Fonts

# Install OpenJDK-8
#RUN yum install -y java-1.8.0-openjdk;
# Setup JAVA_HOME -- useful for docker commandline
#ENV JAVA_HOME /usr/lib/jvm/jre-1.8.0-openjdk-1.8.0.*.x86_64/
#RUN export JAVA_HOME

#RUN mkdir /var/raad

##==================Liber-Office=============
#RUN apk update && apk add -y install \
#    apt-transport-https locales-all libpng16-16 libxinerama1 libgl1-mesa-glx libfontconfig1 libfreetype6 libxrender1 \
#    libxcb-shm0 libxcb-render0 adduser cpio findutils \
#    procps \
#    && apk add -y install libreoffice --no-install-recommends \
#    && rm -rf /var/lib/apt/lists/*

# Default listener port
#EXPOSE 9091

# startup
#ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","app.jar"]


#FROM maven:3.6.1-jdk-8
#ADD target/RAAD-Converter-0.0.1-SNAPSHOT.jar app.jar
## copy local fount for remove utf-8 issue
#COPY Fonts /usr/share/fonts
#ENTRYPOINT ["java", "-Dfile.encoding=UTF-8", "-jar", "app.jar"]
##==================Liber-Office=============
#RUN apt-get update && apt-get -y install \
#    apt-transport-https locales-all libpng16-16 libxinerama1 libgl1-mesa-glx libfontconfig1 libfreetype6 libxrender1 \
#    libxcb-shm0 libxcb-render0 adduser cpio findutils \
#    procps \
#    && apt-get -y install libreoffice --no-install-recommends \
#    && rm -rf /var/lib/apt/lists/*
## 889 for websecoket
#EXPOSE 9096 889

#FROM maven:3.6.1-jdk-8
### Application-confuration
#ADD /target/RAAD-Converter-0.0.1-SNAPSHOT.jar app.jar
#RUN sh -c 'touch RAAD-Converter-0.0.1-SNAPSHOT.jar'
#VOLUME /tmp
#RUN apt-get update && apt-get -y install \
#  libreoffice-common \
#  unoconv \
#  hyphen-af hyphen-en-us \
#  fonts-dejavu fonts-dejavu-core fonts-dejavu-extra \
#  fonts-droid-fallback fonts-dustin fonts-f500 fonts-fanwood fonts-freefont-ttf fonts-liberation \
#  fonts-lmodern fonts-lyx fonts-sil-gentium fonts-texgyre fonts-tlwg-purisa fonts-opensymbol && \
#  rm -rf /var/lib/apt/lists/*
#ENTRYPOINT ["java", "-Dfile.encoding=UTF-8", "-jar", "app.jar"]

#
##RUN wget http://downloadarchive.documentfoundation.org/libreoffice/old/6.3.3.1/deb/x86_64/LibreOffice_6.3.3.1_Linux_x86-64_deb.tar.gz -O libo.tar.gz
##RUN apt update \
##  && apt install -y libxinerama1 libfontconfig1 libdbus-glib-1-2 libcairo2 libcups2 libglu1-mesa libsm6 unzip \
##  && tar -zxvf libo.tar.gz
##
##RUN dpkg -i *.deb
### refresh repos otherwise installations later may fail
##RUN apt-get update
### tdf#117557 - Add CJK Fonts to LibreOffice Online Docker Image
##RUN apt-get -y install fonts-wqy-zenhei fonts-wqy-microhei fonts-droid-fallback fonts-noto-cjk
####==================Liber-Office=============
##RUN apt-get update && apt-get -y install \
##    apt-transport-https locales-all libpng16-16 libxinerama1 libgl1-mesa-glx libfontconfig1 libfreetype6 libxrender1 \
##    libxcb-shm0 libxcb-render0 adduser cpio findutils \
##    procps \
##    && apt-get -y install libreoffice --no-install-recommends \
##    && rm -rf /var/lib/apt/lists/*