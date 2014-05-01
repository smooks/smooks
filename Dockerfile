FROM ubuntu

MAINTAINER Tom Fennelly <tom.fennelly@gmail.com>

# avoid debconf and initrd
ENV DEBIAN_FRONTEND noninteractive
ENV INITRD No

# install Java
RUN apt-get update
RUN apt-get install -y software-properties-common python-software-properties
RUN add-apt-repository -y ppa:webupd8team/java
RUN apt-get update
RUN echo debconf shared/accepted-oracle-license-v1-1 select true | debconf-set-selections
RUN echo debconf shared/accepted-oracle-license-v1-1 seen true | debconf-set-selections
RUN apt-get install -y oracle-java6-installer

# install maven
RUN apt-get install -y maven

# install git
RUN apt-get install -y git

# clone the smooks repo
RUN git clone https://github.com/smooks/smooks.git /home/smooks

WORKDIR /home/smooks