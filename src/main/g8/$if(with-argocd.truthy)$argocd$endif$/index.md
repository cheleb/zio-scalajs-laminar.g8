# Argocd

To deploy the application using ArgoCD, you need to install ArgoCD in your Kubernetes cluster. You can follow the instructions in the [ArgoCD documentation](https://argoproj.github.io/argo-cd/getting_started/).

Or you can follow dedicated instructions for the [ArgoCD](argocd.md) in this project.

After installing ArgoCD, you can deploy the application by running the following steps.

## Key Pair

The ArgoCD server requires a key pair to authenticate to the Git repository, that will hold your project helm/kustomize file.

* your project can be private ..
* ArgoCD image will push the updated helm/kustomize file to the repository, when a new image is pushed to the registry.

Hence, you should generate a new key pair and add the public key to the GitHub repository and expose the private key to the ArgoCD server.

* The private key should be added to the ArgoCD server.
* The public key should be added to the GitHub repository.

For some reason, the public key used this way cannot be reused, hence it might be a good idea to generate a new key pair for the ArgoCD server and the GitHub repository.

```bash
ssh-keygen -t ed25519 -C "argocd$projectId$" -f ~/.ssh/argocd_$projectId$_ed25519
```

Then you need to publish the [GitOps folder to the GitHub repository](./gitops.md).

When done you can add the repository to the ArgoCD server.


## Declare the GitHub repository in ArgoCD

1. Login to the ArgoCD server:
```bash
argocd login argocd-server.argocd.svc.cluster.local
```
2. Add the repository credential to ArgoCD:
```bash
argocd repocreds add git@github.com:$githubUser$/$projectId$-gitops.git --ssh-private-key-path ~/.ssh/argocd_$projectId$_ed25519
```

3. Add the repository to ArgoCD:
```bash
argocd repo add git@github.com:$githubUser$/$projectId$-gitops.git
```

Now you should see the repository in the ArgoCD UI. You can deploy the application by clicking on the `Sync` button.

Or in the CLI:
```bash
argocd repo list
```



## Deploy the application

The folder [Application](Application/) contains the Kubernetes resources for the application.

* secrets.yaml: Contains the private key for the GitHub repository.
* $projectId$.yaml: Contains the Kubernetes resources for the application.

The secrets.yaml file contains the private key for the GitHub repository. You only need to add the private key to the Kubernetes secret, if you use the `write back` method provided by ArgoCD Image Updater.

ArgoCD Image Updater is a tool that automatically updates the container images in the Kubernetes resources. It can update the container images in the Kubernetes resources by using the `write back` method. The `write back` method requires the private key to push the changes to the GitHub repository.

You can deploy the application by running the following commands:

1. Add private key to the Kubernetes secret

Copy `secrets-template.yaml` to `secrets.yaml` and add the private key to the `privateKey` field.

Apply the secret to the Kubernetes cluster:

```bash
kubectl apply -f secrets.yaml
```

2. Optional: Init namespace && Password

```bash
kubectl create namespace $k8sNamespace$
kubectl -n $k8sNamespace$  create secret generic postgresql-secrets --from-literal=POSTGRES_PASSWORD=*************
```

3. Deploy the application

```bash
kubectl apply -f $projectId$.yaml
```

Note that the application will be deployed in the namespace `$k8sNamespace$`.

3. Optional: Create secret for the postgres password

If you did not did it the the step 2, you can create the secret for the postgres password by running the following command:
```bash
kubectl -n $k8sNamespace$  create secret generic postgresql-secrets --from-literal=POSTGRES_PASSWORD=*************
```
Until the secret is created, the application will not be able to start.