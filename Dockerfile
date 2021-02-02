FROM debian

EXPOSE 3000

RUN apt update
RUN apt -y upgrade
RUN apt -y install npm make

RUN mkdir -p /home/app

COPY . /home/app

WORKDIR /home/app/PfoertnerServer

RUN make setup

CMD ["make", "run"]
