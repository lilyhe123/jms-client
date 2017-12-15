## Prerequisites
1. You need to have a Kubernetes cluster up and running with kubectl installed.
2. You need to get wlthint3client.jar from an installed WebLogic directory $WL_HOME/server/lib and put it in the folder jms-client/container-scripts/lib.

## How to Build and Run
### build the jms-client image
`$ docker build -t jms-client .`
### deploy the jms client pod
`$ kubectl create -f jmsclient.yml`
### delete the jms client pod
`$ kubectl delete -f jmsclient.yml`
