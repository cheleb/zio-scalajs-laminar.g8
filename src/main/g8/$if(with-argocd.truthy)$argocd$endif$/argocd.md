# ArgoCD 

ArgoCD is a declarative, GitOps continuous delivery tool for Kubernetes.

```shell
kubectl create namespace argocd
kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml
```

## Accessing the ArgoCD UI

Need to wait for the argocd-server to be ready before accessing the UI.

* retrieve the initial admin password

```shell
kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d | pbcopy
```

* With Obrbstack argocd is exposed at http://argocd-server.argocd.svc.cluster.local without effort.

<!-- 
* Or port-forward the argocd-server service to your local machine http://localhost:8080
```shell
kubectl port-forward -n argocd services/argocd-server 8080:80
```
-->
 login to the UI using `admin` and the password retrieved above.


## ArgoCD Image Updater

Image updater is a tool that automatically updates the container images in a Kubernetes cluster.

It is an ArgoCD extension that can be installed in the same namespace as ArgoCD.


```shell
kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj-labs/argocd-image-updater/stable/manifests/install.yaml
```

This extension will stat our registry and update images in the cluster:
* when a new image is pushed to the registry
* given some metadata are present in the deployment descriptor.

### ConfigMaps

We need to edit the configmap to add our local registry, to let the image updater know where to look for images.

```shell
kubectl edit configmaps -n argocd argocd-image-updater-config
```

Add append the following to the data section, or at the end of file.

```yaml
data:
  log.level: debug
  registries.conf: |
    registries:
    - name: local
      api_url: http://registry.orb.local
      prefix: registry.orb.local
      insecure: true
      default: true
```

Notice the `prefix` and unsecure `true` fields, the prefix is the registry domain name, and unsecure is set to true because we are using a self-signed certificate.

This prefix is used to match the image name in the deployment descriptor.

### Secrets



Now let proceed to [application environment setup](app-setup.md).
