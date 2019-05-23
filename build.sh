#!/bin/bash

docker build -t jms-client:1.0 .
if [ "$(kubectl get --ignore-not-found Deployment jmsclient | wc -l)" != 0 ]; then
  kubectl delete -f jmsclient.yaml
fi
kubectl apply -f jmsclient.yaml
