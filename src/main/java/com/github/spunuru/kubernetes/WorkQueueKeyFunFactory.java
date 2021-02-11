package com.github.spunuru.kubernetes;

import com.github.spunuru.kubernetes.models.V1beta1FlinkCluster;
import io.kubernetes.client.extended.controller.reconciler.Request;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import java.util.function.Function;

public interface WorkQueueKeyFunFactory {
	class FlinkClusterCustomObjectWorkQueueKeyFunc implements Function<V1beta1FlinkCluster, Request> {
		@Override
		public Request apply(V1beta1FlinkCluster obj) {
			V1ObjectMeta objectMeta = obj.getMetadata();
			return new Request(objectMeta.getNamespace(), objectMeta.getName());
		}
	}
}
