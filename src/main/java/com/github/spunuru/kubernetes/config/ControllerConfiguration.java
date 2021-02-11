package com.github.spunuru.kubernetes.config;

import com.github.spunuru.kubernetes.FlinkClusterReconciler;
import com.github.spunuru.kubernetes.models.V1beta1FlinkCluster;
import com.github.spunuru.kubernetes.models.V1beta1FlinkClusterList;
import java.io.IOException;
import java.util.concurrent.Executors;

import io.kubernetes.client.extended.controller.Controller;
import io.kubernetes.client.extended.controller.ControllerManager;
import io.kubernetes.client.informer.SharedInformerFactory;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.openapi.models.V1ReplicaSet;
import io.kubernetes.client.openapi.models.V1ReplicaSetList;
import io.kubernetes.client.spring.extended.controller.annotation.GroupVersionResource;
import io.kubernetes.client.spring.extended.controller.annotation.KubernetesInformer;
import io.kubernetes.client.spring.extended.controller.annotation.KubernetesInformers;
import io.kubernetes.client.spring.extended.controller.factory.KubernetesControllerFactory;
import io.kubernetes.client.util.ClientBuilder;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ControllerConfiguration {

    @Bean
    public ApiClient kubernetesSharedClient() throws IOException {
        return ClientBuilder.defaultClient(); // The apiClient used by informer-factory.
    }

    @Bean
    public SharedInformerFactory sharedInformerFactory() {
        // Registering informer-factory so that the processor {@link io.kubernetes.client.spring.extended.controller.KubernetesInformerFactoryProcessor}
        // automatically provisions the annotated informers.
        return new ControllerSharedInformerFactory();
    }

    @Bean
    public FlinkClusterReconciler flinkClusterReconciler() {
        return new FlinkClusterReconciler();
    }

    @Bean("flinkcluster-controller")
    public KubernetesControllerFactory replicaSetController(SharedInformerFactory sharedInformerFactory, FlinkClusterReconciler rs) {
        return new KubernetesControllerFactory(sharedInformerFactory, rs);
    }

    @Bean
    public CommandLineRunner starter(SharedInformerFactory sharedInformerFactory, @Qualifier("flinkcluster-controller") Controller flinkClusterController) {
        return args -> {
            // Optionally wrap the controller-manager with {@link io.kubernetes.client.extended.leaderelection.LeaderElector}
            // so that the controller works in HA setup.
            // https://github.com/kubernetes-client/java/blob/master/examples/src/main/java/io/kubernetes/client/examples/LeaderElectionExample.java
            ControllerManager controllerManager = new ControllerManager(
                    sharedInformerFactory,
                    flinkClusterController
            );
            // Starts the controller-manager in background.
            Executors.newSingleThreadExecutor().submit(controllerManager);
        };
    }

    @KubernetesInformers({
            @KubernetesInformer( // Adding a flinkcluster-informer to the factory for list-watching replicaset resources
                    apiTypeClass = V1beta1FlinkCluster.class,
                    apiListTypeClass = V1beta1FlinkClusterList.class,
                    groupVersionResource =
                    @GroupVersionResource(
                            apiGroup = "kubernetes.spunuru.github.com",
                            apiVersion = "v1beta1",
                            resourcePlural = "flinkclusters")),
    })
    class ControllerSharedInformerFactory extends SharedInformerFactory {
    }
}
