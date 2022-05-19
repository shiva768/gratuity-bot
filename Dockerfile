FROM ubuntu:latest

RUN apt update \
    && apt install software-properties-common -y \
    && apt-add-repository ppa:bitcoin/bitcoin -y \
    && apt update \
    && apt install bitcoind -y
RUN bitcoind & sleep 1 && kill $!
RUN echo "rpcuser=hoge\nrpcpassword=hogehoge123\nrpcport=51200\nrpcallowip=0.0.0.0/0" > ~/.bitcoin/bitcoin.conf

CMD /bin/bash