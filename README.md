# Book Me - Gateway Service
BookMe manages bookings and this service is the api-gateway service for the BookMe application.

---

## 🔧 Local Dev Testing Setup (Local + Port-Forwarding) for Keycloak

This guide helps you run the API Gateway and access Keycloak locally for token-based authentication using `kubectl port-forward`.

---

### 📦 Prerequisites

- A running Kubernetes cluster (e.g., Minikube or Docker Desktop)
- `kubectl` installed and configured
- Keycloak running as a Kubernetes service named: `keycloak-service`
- API Gateway deployed to the cluster

---

### ✅ Step-by-Step Setup

#### 1. 📌 Port-forward Keycloak to your local machine

```bash
kubectl port-forward svc/keycloak-service 8181:8080
```
Keycloak will now be accessible at:
http://localhost:8181

#### 2. ⚙️ Set the Keycloak Frontend URL
In the Keycloak Admin Console:

- Navigate to: Realm Settings → General
- Set the Frontend URL to: 
 ```http
http://keycloak-service:8080
```

This ensures tokens have the correct iss (issuer) claim expected by the API Gateway. 
Hence, you will have the iss claim in JWT as "http://keycloak-service:8080/realms/bookme-realm"

#### 3. ⚙️ Verify API Gateway JWT Configuration
Ensure your API Gateway's configuration includes the correct issuer URI. 
For example, in application.yml or your deployment environment variables:
```KEYCLOAK_JWT_ISSUER_URI=http://keycloak-service:8080/realms/bookme-realm```
>>**Important**: Use the internal service URL here (keycloak-service:8080), not localhost. The gateway runs inside the cluster and resolves the service name internally.


#### 4. 🧪 Get an Access Token via Postman
In Postman, configure the OAuth2 token request with:
- Access Token URL:
  ```http://localhost:8181/realms/bookme-realm/protocol/openid-connect/token```
- Provide:
  - client_id
  - client_secret
  - Grant type (i.e. client_credentials) - Use client_credentials grant for service-to-service flow.

    The above steps will obtain a valid JWT token for testing.


#### 5. 📬 Port-forward the API Gateway (if not already done)
```bash
kubectl port-forward svc/gateway-service 9000:9000
```
The API Gateway will now be accessible at:
```http://localhost:9000```


#### 6. ✅ Make API Requests with Bearer Token
Use Postman or curl to call your API through the gateway with the token:
```bash
curl -H "Authorization: Bearer <access-token>" http://localhost:9000/bookings
```
Or in Postman:

- URL: http://localhost:9000/bookings
- Add header: Authorization: Bearer <access-token>

>ℹ️ This setup is intended for local development and testing only. For production, consider exposing Keycloak via an Ingress or external DNS endpoint.