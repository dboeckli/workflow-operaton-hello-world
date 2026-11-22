cd target/helm/repo

$file = Get-ChildItem -Filter workflow-operaton-hello-world-chart-*.tgz | Select-Object -First 1
$APPLICATION_NAME = Get-ChildItem -Directory | Where-Object { $_.LastWriteTime -ge $file.LastWriteTime } | Select-Object -ExpandProperty Name

helm uninstall $APPLICATION_NAME --namespace workflow-operaton-hello-world

kubectl delete pod -n workflow-operaton-hello-world --field-selector=status.phase==Succeeded
kubectl delete pod -n workflow-operaton-hello-world --field-selector=status.phase==Failed
