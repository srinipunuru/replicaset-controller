# Flink Kubernetes Controller

### Build this project

```shell script
 mvn package spring-boot:repackage
```

### Apply CRD and Start the controller

```shell script
# Apply the FlinkCluster CRD
kubectl apply -f crd\operator.yaml

# Start the controller
java -jar ./target/flink-controller-1.0.jar
```

### Create and Delete FlinkCluster objects

When you create or delete the objects, You should see corresponding events being logged in the controller that is running.
```shell script
# create object
kubectl apply -f test-yaml/TestCluster.yaml

#delete object
kubectl delete -f kubectl apply -f test-yaml/TestCluster.yaml
```

### Create and push custom controller Docker Image

```shell script
cd docker-build
./build-image.sh 
export CR_PAT=a58bc801b78791c95df837e3401e1394d7c6ee43
echo $CR_PAT | docker login ghcr.io -u USERNAME --password-stdin 
docker tag flink-controller:latest ghcr.io/srinipunuru/flink-controller:latest
docker push ghcr.io/srinipunuru/flink-controller:latest
``` 

### Build Spring Boot Docker Image

This one uses spring boot maven plugin to create the docker image.

```bash
mvn spring-boot:build-image
```