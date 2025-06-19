# 🧩 Simple Microservice Project

A simple microservices-based project built using Spring Boot 3.2.5, PostgreSQL, Redis, MongoDB, and containerized with Docker. This project demonstrates basic CRUD operations and RESTful APIs, complete with Kubernetes orchestration and local Docker registry support.

---

## 🚀 Features

- ✅ Spring Boot 3.2.5
- 🐘 PostgreSQL Database
- 📦 Redis for caching
- 🍃 MongoDB for document-based storage
- 📡 RESTful APIs
- 🐳 Docker & Docker Compose support
- ☸️ Kubernetes & Helm for deployment
- 📥 Local Docker Registry

---

## 🛠️ Getting Started

### 📋 Prerequisites

Make sure the following tools are installed:

- [Docker](https://www.docker.com/)
- [Docker Compose](https://docs.docker.com/compose/)
- [Rancher Desktop](https://rancherdesktop.io/) or compatible Kubernetes environment
- [Helm](https://helm.sh/) (for managing Kubernetes deployments)

---

## ⚙️ Installation

### 1. Use Docker Compose to start PostgreSQL, MongoDB, Redis, and the local registry

```bash
docker compose up -d
```

### 2. Use Makefile for build all image and push to local registry
```bash
make all USERNAME=localhost:5000
make pus USERNAME=localhost:5000
```

### 3. Install and setup ingress in local cluster
```bash
helm install ingress-nginx ingress-nginx/ingress-nginx \
  --namespace ingress-nginx --create-namespace \
  --set controller.kind=Deployment \
  --set controller.admissionWebhooks.enabled=false \
  --set controller.service.type=NodePort \
  --set controller.service.nodePorts.http=30080 \
  --set controller.service.nodePorts.https=30443 \
  --set controller.hostNetwork=false \
  --set defaultBackend.enabled=true \
  --set controller.minReadySeconds=5 \
  --set controller.progressDeadlineSeconds=60
```
### 4. Run kubernates
```bash
kubectl create namespace local-dev
kubectl apply -f "name file yaml in folder kuberbates"
```



