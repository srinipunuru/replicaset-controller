#!/bin/bash
mvn clean package spring-boot:repackage && cd docker-build

JAR_SOURCE=".././target/flink-controller*.jar"
JAR_TARGET="flink-operator.jar"

/bin/rm -f $JAR_TARGET
/bin/cp $JAR_SOURCE $JAR_TARGET

NAME=flink-controller
TAG=latest

SOURCE_IMG=$NAME:$TAG
docker build -t $SOURCE_IMG .

SHA256=$(docker inspect --format='{{index .Id}}' $SOURCE_IMG)
IFS=':' read -ra TOKENS <<< "$SHA256"

echo "IFS:$IFS"

#export CR_PAT=a58bc801b78791c95df837e3401e1394d7c6ee43
#echo $CR_PAT | docker login ghcr.io -u USERNAME --password-stdin
#docker tag $NAME:$TAG ghcr.io/srinipunuru/$NAME:$TAG
#docker push ghcr.io/srinipunuru/$NAME:$TAG

#TARGET_IMG=123456789012.dkr.ecr.us-east-1.amazonaws.com/$NAME:$TAG
#aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin 123456789012.dkr.ecr.us-east-1.amazonaws.com
#docker tag $SOURCE_IMG $TARGET_IMG
#docker push $TARGET_IMG