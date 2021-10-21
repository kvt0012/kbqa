# git clone https://github.com/haophancs/gAnswer
# docker run -it --name ganswer --volume ./gAnswer/:/gAnswer --network="host" ubuntu:20.04

apt update \
    && apt autoclean \
    && apt install -y tmux vim wget git unrar unzip \
    && apt install -y python3 python3-pip \
    && apt install -y openjdk-8-jdk openjdk-8-jre \
    && apt upgrade -y

pip install gdown

mkdir -p ./data \
    && cd ./data \
    && gdown https://drive.google.com/u/0/uc?id=1hmqaftrTo0_qQNRApCuxFXaBx7SosNVy \
    && unrar x ./DBpedia2016.rar ./ \
    && rm ./DBpedia2016.rar \
    && cd ..

gdown https://drive.google.com/u/0/uc?id=1tEsi4pBOBHd2gmwVgIOgt-ypJZQH9G3S \
    && unzip ganswer_lib.zip \
    && mv ganswer_lib lib \
    && rm ganswer_lib.zip

chmod +rx ./start_http.sh
