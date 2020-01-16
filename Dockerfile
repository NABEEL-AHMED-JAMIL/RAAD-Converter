FROM maven:3.6.1-jdk-8
## Application-confuration
ADD /target/RAAD-Converter-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","app.jar"]
EXPOSE 9098
##==================Liber-Office=============
RUN apt-get update && apt-get -y install \
    apt-transport-https locales-all libpng16-16 libxinerama1 libgl1-mesa-glx libfontconfig1 libfreetype6 libxrender1 \
    libxcb-shm0 libxcb-render0 adduser cpio findutils \
# This line added tempory
    procps \
    && apt-get -y install libreoffice --no-install-recommends \
    && rm -rf /var/lib/apt/lists/*
    
# Set the locale
RUN sed -i -e 's/# en_US.UTF-8 UTF-8/en_US.UTF-8 UTF-8/' /etc/locale.gen && \
    locale-gen
ENV LANG en_US.UTF-8
ENV LANGUAGE en_US:en
ENV LC_ALL en_US.UTF-8
