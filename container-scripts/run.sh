#!/bin/bash

if [ "$#" != 3 ]; then
  echo "$0 JMSSender|JMSReceiver domainName namespace"
  exit 1
fi

domainName=$2
namespace=$3
clusterDNS=${domainName}-cluster-cluster-1.${namespace}.svc.cluster.local


if [ $1 = 'JMSSender' ] || [ $1 = 'JMSReceiver' ]; then
  nohup java samples.$1 -url t3://${clusterDNS}:8001 -user weblogic -pass welcome1 -cf cf1 -dest dq1 -count 5000 -interval 500 >result 2>&1 &
else
  echo "$0 JMSSender|JMSReceiver domainName namespace"
  exit 1
fi
