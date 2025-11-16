# Authentication Flow
This document explains the authentication flow with Keycloak, the API Gateway, and microservices. It highlights both **external access** for the React app and **internal access** for services running inside the Kubernetes cluster.

---

## Overview

- **React App** (External)
    - It accesses the Keycloak server via **Ingress** (`auth.dashboard.com`).
    - Uses the Keycloak public URLs for authentication (token requests, login redirects).

- **API Gateway & Microservices** (Internal)
    - They access Keycloak via **internal ClusterIP service** (`keycloak-service:80`).
    - Uses `KEYCLOAK_JWT_ISSUER_URI` and `KEYCLOAK_JWK_SET_URI` pointing to the internal service URL.
    - The gateway validates JWTs for all incoming requests to microservices.

- **DNS Resolution**
    - **External React App** uses public hostnames resolved via standard local DNS (`auth.dashboard.com`) in `/etc/host`
    - **Internal Services** (gateway, users, bookings) use **internal Kubernetes DNS**:
        - Updated via CoreDNS config in minikube for local development.
        - Maps `auth.dashboard.com` to `keycloak-service` IP internally.

---

## Internal vs External Flows

### 1. External Flow (React App → Keycloak Ingress)

Browser (React App)
        |
        v
+------------------+
| Keycloak Ingress |
| auth.dashboard.com|
+------------------+
        |
        v
Keycloak Service
        |
        v
Keycloak Pod


- External clients access `auth.dashboard.com` (via minikube tunnel or production ingress).
- Keycloak issues JWTs that include the issuer as `http://auth.dashboard.com/realms/bookme-realm`.

---

### 2. Internal Flow (Gateway / Microservices → Keycloak Service)

API Gateway / Microservice
        |
        v
+-------------------+
| Keycloak Service |
| keycloak-service |
+-------------------+
        |   
        v
Keycloak Pod



- Internal services do **not go through the ingress**.
- Use the ClusterIP service (`keycloak-service:80`) for token validation and JWKS fetching.
- Ensures internal communication works even if the ingress is restricted to external access only.

---

## Configuration Notes

- **Keycloak Service (ClusterIP)**:

```yaml
ports:
  - protocol: TCP
    port: 80        # Default HTTP port
    targetPort: 8080
```

### Internal URLs for Gateway:
```
KEYCLOAK_JWT_ISSUER_URI: "http://auth.dashboard.com/realms/bookme-realm"
KEYCLOAK_JWK_SET_URI: "http://auth.dashboard.com/realms/bookme-realm/protocol/openid-connect/certs"
```
- Thanks to my CoreDNS override in minikube, pods can still reach that host internally without hitting the Ingress.

### External URLs for React App:
```
VITE_KEYCLOAK_URL=http://auth.dashboard.com
```

#### CoreDNS (minikube) mapping for internal resolution:
```
hosts {
    10.106.129.56 auth.dashboard.com
    fallthrough
}
```
- Note that the 10.106.129.56 is the ClusterIP of the keycloak service

### Benefits of This Approach
- External apps can authenticate via standard ingress URLs without exposing internal services.
- Internal services can validate JWTs directly using the ClusterIP service, bypassing ingress.
- No 401 Unauthorized issues due to unreachable issuer URLs from internal pods.
- Keeps internal and external traffic separated for better security and flexibility.