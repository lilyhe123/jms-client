

docker build -t jms-client:1.0 .
kubectl delete -f jmsclient.yaml
kubectl apply -f jmsclient.yaml
