FROM tfennelly/java6_dev

# See https://github.com/tfennelly/dockerfiles/blob/master/java6_dev

MAINTAINER Tom Fennelly <tom.fennelly@gmail.com>

# clone the smooks repo
RUN git clone https://github.com/smooks/smooks.git /home/smooks

WORKDIR /home/smooks
