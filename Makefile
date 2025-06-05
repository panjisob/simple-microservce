# Daftar layanan mikroservis
SERVICES = auth-service newsfeed-service product-service user-service newsfeed-frontend

# Versi tag
TAG = 0.0.1

# Docker Hub username atau registry prefix (bisa di-set via `make USERNAME=...`)
USERNAME ?= defaultuser

# Target default
.PHONY: all $(SERVICES) push clean

all: $(SERVICES)

# Build image Docker untuk setiap service
$(SERVICES):
	docker build -t $(USERNAME)/$@:$(TAG) $@

# Push image ke registry
push:
	@for service in $(SERVICES); do \
		docker push $(USERNAME)/$$service:$(TAG); \
	done

# Clean image dari lokal
clean:
	@for service in $(SERVICES); do \
		docker rmi -f $(USERNAME)/$$service:$(TAG) || true; \
	done
