# Argocd

To deploy the application using ArgoCD, you need to install ArgoCD in your Kubernetes cluster. You can follow the instructions in the [ArgoCD documentation](https://argoproj.github.io/argo-cd/getting_started/).

After installing ArgoCD, you can deploy the application by running the following commands.

## Key Pair

The ArgoCD server requires a key pair to authenticate with the Git repository. You should generate a new key pair and add the public key to the GitHub repository and expose the private key to the ArgoCD server.

Hence it might be a good idea to generate a new key pair for the ArgoCD server...

```bash
ssh-keygen -t ed25519 -C "argocd@argocd"
```

## Declare the GitHub repository in ArgoCD

1. Login to the ArgoCD server:
```bash
argocd login argocd-server.argocd.svc.cluster.local
```
2. Add the repository credential to ArgoCD:
```bash
argocd repocreds add git@github.com:$githubUser$/$projectId$-gitops.git --ssh-private-key-path ~/.ssh/argocd_ed25519
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

## Publish the application GitOps to the GitHub

The [GitOps](GitOps/) folder contains the ArgoCD application configuration. You can publish this folder to a GitHub repository by running the following commands:

1. Create a new repository in GitHub.
```bash
cd GitOps
gh repo create $projectId$-gitops --private
git remote add origin git@github.com:$githubUser$/$projectId$-gitops.git
```
    
3. Push the GitOps folder to the repository.
```bash
git push -u origin master
```

2. Add public key to the repository settings.


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
2. Deploy the application

```bash
kubectl apply -f zio-laminar-demo-k8s.yaml
```