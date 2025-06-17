# Simple Microservice

This is a simple microservice project built with Spring Boot and MySQL. The project demonstrates a basic setup of a microservice with CRUD operations and a RESTful API.

## Features

- Spring Boot 2.1.15.RELEASE
- Postgres Database
- RESTful API
- Redis
- Mongodb
- Docker and Docker Compose
- kubernetes

## Getting Started

### Prerequisites

Before you begin, ensure you have met the following requirements:

- Docker
- Docker Compose
- Kubernates

## Usage

### API Endpoints
- POST /userservice/login
- POST /userservice/logout 
- GET /productservice/all
- GET /v1/news


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


