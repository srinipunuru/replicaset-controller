package com.github.spunuru.kubernetes;

import com.github.spunuru.kubernetes.models.V1beta1FlinkCluster;
import io.kubernetes.client.extended.controller.reconciler.Reconciler;
import io.kubernetes.client.extended.controller.reconciler.Request;
import io.kubernetes.client.extended.controller.reconciler.Result;
import io.kubernetes.client.informer.SharedInformer;
import io.kubernetes.client.informer.cache.Lister;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.spring.extended.controller.annotation.AddWatchEventFilter;
import io.kubernetes.client.spring.extended.controller.annotation.DeleteWatchEventFilter;
import io.kubernetes.client.spring.extended.controller.annotation.KubernetesReconciler;
import io.kubernetes.client.spring.extended.controller.annotation.KubernetesReconcilerReadyFunc;
import io.kubernetes.client.spring.extended.controller.annotation.KubernetesReconcilerWatch;
import io.kubernetes.client.spring.extended.controller.annotation.KubernetesReconcilerWatches;
import io.kubernetes.client.spring.extended.controller.annotation.UpdateWatchEventFilter;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@KubernetesReconciler(
        watches =
        @KubernetesReconcilerWatches({
                @KubernetesReconcilerWatch(
                        workQueueKeyFunc = WorkQueueKeyFunFactory.FlinkClusterCustomObjectWorkQueueKeyFunc.class,
                        apiTypeClass = V1beta1FlinkCluster.class,// the reconciler needs to subscribe FlinkCluster events.
                        resyncPeriodMillis = 60 * 1000L // resync every 60s
                )
        }))
public class FlinkClusterReconciler implements Reconciler {

    private final static Logger logger = LoggerFactory.getLogger(FlinkClusterReconciler.class);

    private Map<String,V1beta1FlinkCluster> deletedObjects = new HashMap<String,V1beta1FlinkCluster>();
    private Map<String,V1beta1FlinkCluster> addedObjects = new HashMap<String,V1beta1FlinkCluster>();
    private Map<String,V1beta1FlinkCluster> updatedObjects = new HashMap<String,V1beta1FlinkCluster>();

    @Autowired
    private Lister<V1beta1FlinkCluster> rsLister;

    @Autowired
    private SharedInformer<V1beta1FlinkCluster> rsInformer;

    @Autowired
    private ApiClient apiClient;


    // the reconciler won't be dealing with reconciler-requests until the ready-func passes.
    // the method must be public-access.
    @KubernetesReconcilerReadyFunc
    public boolean informerCacheReady() {
        return rsInformer.hasSynced();
    }

    @AddWatchEventFilter(apiTypeClass = V1beta1FlinkCluster.class)
    public boolean onAddFilter(V1beta1FlinkCluster cluster) {
        String name = cluster.getMetadata().getName();
        String namespace = cluster.getMetadata().getNamespace();
        addedObjects.put(name.concat(namespace), cluster);
        logger.info(String.format("Handling onAdd event for FlinkCluster custom resource %s.%s", name, namespace));
        return true;
    }

    @UpdateWatchEventFilter(apiTypeClass = V1beta1FlinkCluster.class)
    public boolean onUpdateFilter(V1beta1FlinkCluster cluster, V1beta1FlinkCluster newCluster) {
        return false;
    }

    @DeleteWatchEventFilter(apiTypeClass = V1beta1FlinkCluster.class)
    public boolean onDeleteFilter(V1beta1FlinkCluster cluster, boolean deletedFinalStateUnknown) {
        String name = cluster.getMetadata().getName();
        String namespace = cluster.getMetadata().getNamespace();
        deletedObjects.put(name.concat(namespace), cluster);
        logger.info(String.format("Handling onDelete event for FlinkCluster custom resource %s.%s", name, namespace));
        return true;
    }

    public Result reconcile(Request request) {
        logger.info(String.format("Triggered reconciliation for %s.%s", request.getName(), request.getNamespace()));
        String name = request.getNamespace().concat(request.getName());
        if(deletedObjects.containsKey(name)) {
            deleteCluster(deletedObjects.get(name));
            deletedObjects.remove(name);
        } else if (addedObjects.containsKey(name)) {
            addCluster(addedObjects.get(name));
            addedObjects.remove(name);
        }

        return new Result(false);
    }

    private void addCluster(V1beta1FlinkCluster flinkCluster) {
        logger.info(String.format("Adding Flink cluster %s", flinkCluster.toString()));
    }

    private void deleteCluster(V1beta1FlinkCluster flinkCluster) {
        logger.info(String.format("Deleting Flink cluster %s", flinkCluster.toString()));
    }


}
