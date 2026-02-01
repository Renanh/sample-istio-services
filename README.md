# Sample Istio Services

> **Demonstrando resiliência em microservices com Istio Service Mesh no Kubernetes**

[![CI](https://github.com/Renanh/sample-istio-services/actions/workflows/ci.yml/badge.svg)](https://github.com/Renanh/sample-istio-services/actions/workflows/ci.yml)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.2-green.svg)](https://spring.io/projects/spring-boot)
[![Istio](https://img.shields.io/badge/Istio-1.28-blue.svg)](https://istio.io/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

Este projeto demonstra como implementar padrões de resiliência em microservices utilizando **Istio Service Mesh**, incluindo **Circuit Breaker**, **Retries**, **Timeouts** e **Fault Injection** - tudo configurado na camada de infraestrutura, sem alterações no código da aplicação.

---

## Sumário

- [Por que Istio?](#por-que-istio)
- [Arquitetura](#arquitetura)
- [Service Mesh Fundamentals](#service-mesh-fundamentals)
- [Traffic Management](#traffic-management)
- [Resiliência com Istio](#resiliência-com-istio)
- [Chaos Engineering](#chaos-engineering)
- [Observability](#observability)
- [Quick Start](#quick-start)
- [Testes de Carga](#testes-de-carga)
- [Referências](#referências)

---

## Por que Istio?

Em arquiteturas de microservices, a resiliência é crítica. Tradicionalmente, implementamos Circuit Breakers e Retries diretamente no código usando bibliotecas como Resilience4j ou Hystrix. **Com Istio, movemos essa responsabilidade para a camada de infraestrutura**.

### Vantagens

| Aspecto | Sem Istio | Com Istio |
| ------- | --------- | --------- |
| Circuit Breaker | Código (Resilience4j) | DestinationRule YAML |
| Retries | Código (Spring Retry) | VirtualService YAML |
| Timeouts | Código (RestClient) | VirtualService YAML |
| mTLS | Configuração manual | Automático |
| Tracing | Instrumentação | Automático |
| Traffic Split | Deployment complexo | VirtualService |

> **"O objetivo é aumentar a resiliência diretamente no Istio, sem que nenhuma das aplicações tenha circuit breaker ou retry flows implementados programaticamente."**
> — Matheus Fidelis

---

## Arquitetura

```
                         ┌─────────────────────────────────────────────────────┐
                         │                    Istio Control Plane               │
                         │  ┌─────────┐  ┌──────────┐  ┌────────────────────┐  │
                         │  │  Pilot  │  │  Citadel │  │      Galley        │  │
                         │  └─────────┘  └──────────┘  └────────────────────┘  │
                         └─────────────────────────────────────────────────────┘
                                                    │
                         ┌──────────────────────────┼──────────────────────────┐
                         │                Data Plane│(Envoy Sidecars)          │
                         │                          ▼                          │
┌──────────┐    ┌────────┴────────┐    ┌────────────────────┐    ┌────────────┴────────┐
│  Client  │───▶│  Istio Gateway  │───▶│   first-service    │───▶│   caller-service    │
│          │    │   (NodePort)    │    │  ┌──────┬───────┐  │    │  ┌──────┬───────┐   │
└──────────┘    └─────────────────┘    │  │ App  │ Envoy │  │    │  │ App  │ Envoy │   │
                                       │  └──────┴───────┘  │    │  └──────┴───────┘   │
                                       │     v1 / v2        │    │        v1           │
                                       └────────────────────┘    └──────────┬──────────┘
                                                                            │
                                                                            ▼
                                                                 ┌────────────────────┐
                                                                 │   callme-service   │
                                                                 │  ┌──────┬───────┐  │
                                                                 │  │ App  │ Envoy │  │
                                                                 │  └──────┴───────┘  │
                                                                 │  80% v2 / 20% v1   │
                                                                 └────────────────────┘
```

### Fluxo de Chamadas

```
first-service(v1) → caller-service(v1) → callme-service(v1/v2)
```

Cada serviço possui um **sidecar Envoy** injetado automaticamente pelo Istio, que intercepta todo o tráfego de rede e aplica as políticas configuradas.

---

## Service Mesh Fundamentals

### Componentes Principais do Istio

| Componente | Função |
| ---------- | ------ |
| **Gateway** | Entry point para tráfego externo |
| **VirtualService** | Regras de roteamento (retries, timeouts, fault injection) |
| **DestinationRule** | Políticas de destino (circuit breaker, load balancing) |
| **ServiceEntry** | Registro de serviços externos |
| **PeerAuthentication** | Políticas de mTLS |

### Sidecar Injection

O Istio injeta automaticamente um container **Envoy Proxy** em cada Pod quando o namespace tem o label:

```yaml
kubectl label namespace <namespace> istio-injection=enabled
```

---

## Traffic Management

### Gateway

O Gateway define o ponto de entrada para tráfego externo:

```yaml
apiVersion: networking.istio.io/v1beta1
kind: Gateway
metadata:
  name: sample-gateway
spec:
  selector:
    istio: ingressgateway
  servers:
    - port:
        number: 80
        name: http
        protocol: HTTP
      hosts:
        - "*"
```

### VirtualService - Roteamento

Roteamento com **traffic splitting** (Canary Deployment):

```yaml
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: callme-service-route
spec:
  hosts:
    - callme-service
  http:
    - route:
        - destination:
            host: callme-service
            subset: v2
          weight: 80
        - destination:
            host: callme-service
            subset: v1
          weight: 20
```

### DestinationRule - Subsets

Define os subsets para diferentes versões:

```yaml
apiVersion: networking.istio.io/v1beta1
kind: DestinationRule
metadata:
  name: callme-service-destination
spec:
  host: callme-service
  subsets:
    - name: v1
      labels:
        version: v1
    - name: v2
      labels:
        version: v2
```

---

## Resiliência com Istio

### Circuit Breaker

O Circuit Breaker no Istio é configurado através de `outlierDetection` no DestinationRule:

```yaml
apiVersion: networking.istio.io/v1beta1
kind: DestinationRule
metadata:
  name: callme-service-destination
spec:
  host: callme-service
  trafficPolicy:
    connectionPool:
      tcp:
        maxConnections: 100
      http:
        http2MaxRequests: 1000
        maxRequestsPerConnection: 10
    outlierDetection:
      consecutive5xxErrors: 5      # Erros 5xx antes de ejetar
      interval: 10s                # Intervalo de análise
      baseEjectionTime: 30s        # Tempo de ejeção base
      maxEjectionPercent: 50       # Máximo de hosts ejetados
```

#### Parâmetros do Circuit Breaker

| Parâmetro | Descrição | Recomendação |
| --------- | --------- | ------------ |
| `consecutive5xxErrors` | Erros consecutivos antes da ejeção | 3-5 para críticos, 5-10 para background |
| `interval` | Frequência de análise | 5s-30s dependendo da criticidade |
| `baseEjectionTime` | Duração da ejeção | 15s-60s |
| `maxEjectionPercent` | % máximo de pods ejetados | 50-100% |

#### Configurações por Tier de Serviço

**Serviços Críticos (pagamentos, auth):**
```yaml
outlierDetection:
  consecutive5xxErrors: 3
  interval: 5s
  baseEjectionTime: 15s
  maxEjectionPercent: 100
```

**Serviços Padrão:**
```yaml
outlierDetection:
  consecutive5xxErrors: 5
  interval: 10s
  baseEjectionTime: 30s
  maxEjectionPercent: 50
```

**Serviços Background/Async:**
```yaml
outlierDetection:
  consecutive5xxErrors: 10
  interval: 30s
  baseEjectionTime: 60s
  maxEjectionPercent: 30
```

### Retries

Configuração de retries no VirtualService:

```yaml
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: callme-service-route
spec:
  hosts:
    - callme-service
  http:
    - route:
        - destination:
            host: callme-service
      retries:
        attempts: 3
        perTryTimeout: 2s
        retryOn: gateway-error,connect-failure,refused-stream,5xx
```

#### Condições de Retry

| Condição | Descrição |
| -------- | --------- |
| `5xx` | Qualquer erro 5xx |
| `gateway-error` | 502, 503, 504 |
| `connect-failure` | Falha de conexão |
| `refused-stream` | Stream recusado |
| `retriable-4xx` | Erros 4xx retriáveis |
| `reset` | Conexão resetada |

### Timeouts

```yaml
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: callme-service-route
spec:
  hosts:
    - callme-service
  http:
    - route:
        - destination:
            host: callme-service
      timeout: 5s
      retries:
        attempts: 3
        perTryTimeout: 2s
```

> **Importante:** O `timeout` é para a requisição completa (incluindo retries). O `perTryTimeout` é para cada tentativa individual.

---

## Chaos Engineering

O Istio permite injetar falhas para testar a resiliência do sistema sem modificar o código das aplicações.

### Fault Injection - Delay

Injeta latência artificial nas requisições:

```yaml
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: callme-service-route
spec:
  hosts:
    - callme-service
  http:
    - fault:
        delay:
          percentage:
            value: 50
          fixedDelay: 5s
      route:
        - destination:
            host: callme-service
```

### Fault Injection - Abort

Injeta erros HTTP nas requisições:

```yaml
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: callme-service-route
spec:
  hosts:
    - callme-service
  http:
    - fault:
        abort:
          percentage:
            value: 30
          httpStatus: 500
      route:
        - destination:
            host: callme-service
```

### Cenários de Teste

#### 1. Pod Failure sob Carga

Simula a morte súbita de pods durante carga alta:

```bash
# Terminal 1: Inicia carga
k6 run --vus 50 --duration 60s load-test.js

# Terminal 2: Mata pods aleatoriamente
kubectl delete pod -l app=callme-service -n istio-test
```

**Resultado esperado:** Com circuit breaker e retries configurados, as requisições são redirecionadas para pods saudáveis com mínimo impacto no throughput.

#### 2. Availability Zone Failure

Em clusters multi-AZ, simula a perda de uma zona inteira:

```bash
# Drena todos os nodes de uma AZ específica
kubectl drain node-az-1a --ignore-daemonsets --delete-emptydir-data
```

**Resultado esperado:** O tráfego é automaticamente roteado para pods em outras AZs.

#### 3. Cascade Failure

Testa o comportamento quando um serviço downstream falha:

```yaml
# Injeta 100% de erro no serviço mais interno
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: callme-service-chaos
spec:
  hosts:
    - callme-service
  http:
    - fault:
        abort:
          percentage:
            value: 100
          httpStatus: 503
      route:
        - destination:
            host: callme-service
```

---

## Observability

### Kiali - Service Mesh Dashboard

```bash
kubectl apply -f https://raw.githubusercontent.com/istio/istio/release-1.28/samples/addons/kiali.yaml
istioctl dashboard kiali
```

Kiali oferece:
- Visualização do service graph
- Métricas de tráfego
- Validação de configurações Istio
- Distributed tracing

### Grafana - Métricas

```bash
kubectl apply -f https://raw.githubusercontent.com/istio/istio/release-1.28/samples/addons/grafana.yaml
istioctl dashboard grafana
```

Dashboards incluídos:
- Istio Mesh Dashboard
- Istio Service Dashboard
- Istio Workload Dashboard

### Jaeger - Distributed Tracing

```bash
kubectl apply -f https://raw.githubusercontent.com/istio/istio/release-1.28/samples/addons/jaeger.yaml
istioctl dashboard jaeger
```

### Prometheus - Métricas

```bash
kubectl apply -f https://raw.githubusercontent.com/istio/istio/release-1.28/samples/addons/prometheus.yaml
```

Métricas importantes:
- `istio_requests_total` - Total de requisições
- `istio_request_duration_milliseconds` - Latência
- `istio_tcp_connections_opened_total` - Conexões TCP

---

## Quick Start

### Pré-requisitos

- Java 21+
- Maven 3.9+
- Docker ou Podman
- kubectl
- Kind ou Minikube
- Istio CLI (`istioctl`)

### 1. Clone o repositório

```bash
git clone https://github.com/Renanh/sample-istio-services.git
cd sample-istio-services
```

### 2. Build do projeto

```bash
mvn clean package -DskipTests
```

### 3. Crie o cluster Kind

```bash
kind create cluster --config k8s/kind-cluster-test.yaml --name istio-test
```

### 4. Instale o Istio

```bash
istioctl install --set profile=demo -y

# Habilita sidecar injection
kubectl create namespace istio-test
kubectl label namespace istio-test istio-injection=enabled
```

### 5. Build e carregue as imagens

```bash
# Build com Jib
mvn compile jib:buildTar

# Carrega no Kind
kind load image-archive first-service/target/jib-image.tar --name istio-test
kind load image-archive caller-service/target/jib-image.tar --name istio-test
kind load image-archive callme-service/target/jib-image.tar --name istio-test
```

### 6. Deploy dos serviços

```bash
kubectl apply -f first-service/k8s/deployment.yaml -n istio-test
kubectl apply -f caller-service/k8s/deployment.yaml -n istio-test
kubectl apply -f callme-service/k8s/deployment.yaml -n istio-test
```

### 7. Aplique as regras do Istio

```bash
kubectl apply -f first-service/k8s/istio-rules.yaml -n istio-test
kubectl apply -f caller-service/k8s/istio-rules.yaml -n istio-test
kubectl apply -f callme-service/k8s/istio-rules.yaml -n istio-test
```

### 8. Teste

```bash
curl http://localhost:30000/first/ping
# Resposta: first-service(v1) -> caller-service(v1) -> callme-service(v1)
```

---

## Testes de Carga

### K6 Load Test

```bash
# Instale o K6
# Windows: winget install grafana.k6
# Mac: brew install k6
# Linux: sudo apt install k6

# Execute o teste
k6 run first-service/src/test/resources/k6/load-test.js
```

### Cenários de Teste

```javascript
export const options = {
  scenarios: {
    // Rampa de carga gradual
    ramp_up: {
      executor: 'ramping-vus',
      startVUs: 1,
      stages: [
        { duration: '30s', target: 50 },
        { duration: '1m', target: 100 },
        { duration: '30s', target: 0 },
      ],
    },
    // Carga constante
    constant_load: {
      executor: 'constant-vus',
      vus: 50,
      duration: '2m',
    },
    // Spike test
    spike: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '10s', target: 200 },
        { duration: '30s', target: 200 },
        { duration: '10s', target: 0 },
      ],
    },
  },
};
```

---

## Endpoints

| Service | Path | Descrição |
| ------- | ---- | --------- |
| first-service | `/first/ping` | Ping através da cadeia completa |
| first-service | `/first/ping-with-random-error` | Testa retries (30% de erro) |
| first-service | `/first/ping-with-random-delay` | Testa timeouts (0-2s delay) |
| caller-service | `/caller/ping` | Chama callme-service |
| callme-service | `/callme/ping` | Resposta simples |
| callme-service | `/callme/ping-with-random-delay` | Delay aleatório |
| Todos | `/actuator/health` | Health check |
| Todos | `/actuator/health/liveness` | Liveness probe |
| Todos | `/actuator/health/readiness` | Readiness probe |
| Todos | `/swagger-ui.html` | API documentation |

---

## Estrutura do Projeto

```
sample-istio-services/
├── .github/
│   ├── workflows/
│   │   └── ci.yml                # GitHub Actions CI/CD
│   └── dependabot.yml            # Atualizações automáticas
├── first-service/                 # Serviço de entrada
│   ├── src/main/java/
│   │   └── com/github/renanh/first/
│   │       ├── api/
│   │       │   ├── dto/          # DTOs (Records)
│   │       │   └── resource/     # REST endpoints
│   │       ├── domain/service/   # Lógica de negócio
│   │       └── infrastructure/
│   │           ├── client/       # HTTP clients (RestClient)
│   │           └── config/       # Configurações (@ConfigurationProperties)
│   ├── k8s/
│   │   ├── deployment.yaml       # K8s Deployment + Service
│   │   └── istio-rules.yaml      # Gateway, VirtualService, DestinationRule
│   └── skaffold.yaml
├── caller-service/                # Serviço intermediário
│   └── (mesma estrutura)
├── callme-service/                # Serviço backend
│   └── src/main/java/.../
│       └── domain/event/         # JFR Events para observabilidade
├── k8s/
│   ├── kind-cluster-test.yaml    # Configuração do Kind
│   └── chaos/                    # Cenários de Chaos Engineering
│       ├── fault-delay.yaml      # Injeção de latência
│       ├── fault-abort.yaml      # Injeção de erros HTTP
│       ├── fault-cascade.yaml    # Simulação de falha em cascata
│       └── circuit-breaker-strict.yaml
├── pom.xml                        # Parent POM (Maven)
├── renovate.json                  # Renovate config
└── README.md
```

---

## CI/CD

Este projeto usa **GitHub Actions** para CI/CD:

### Workflow de CI

- **Build & Test**: Compila e executa testes
- **Security Scan**: Trivy para vulnerabilidades
- **Docker Build**: Constrói imagens com Jib
- **Integration Test**: Deploy em Kind + testes K6

### Secrets Necessários

| Secret | Descrição |
| ------ | --------- |
| `DOCKER_USERNAME` | Docker Hub username |
| `DOCKER_PASSWORD` | Docker Hub password/token |
| `SONAR_TOKEN` | SonarCloud token (opcional) |

---

## Tecnologias

| Tecnologia | Versão | Propósito |
| ---------- | ------ | --------- |
| Java | 21 | Runtime |
| Spring Boot | 4.0.2 | Framework |
| Lombok | 1.18.34 | Redução de boilerplate |
| Apache Commons Lang 3 | 3.17.0 | Utilitários e validação |
| Istio | 1.20+ | Service Mesh |
| Kubernetes | 1.29+ | Container Orchestration |
| Kind | 0.20+ | Local K8s |
| Skaffold | 2.x | Development workflow |
| K6 | 0.50+ | Load testing |
| Jib | 3.5.1 | Container builds |

---

## Práticas de Código

Este projeto segue as melhores práticas de desenvolvimento Java moderno:

### Lombok

Utilizado para eliminar boilerplate:

```java
@Slf4j                      // Logger automático
@RequiredArgsConstructor    // Construtor para campos final
@Getter @Setter             // Getters e Setters
```

### Type-Safe Configuration

Configurações tipadas com `@ConfigurationProperties`:

```java
@Component
@ConfigurationProperties(prefix = "services.caller")
@Getter @Setter
public class CallerClientProperties {
    private String url = "http://caller-service:8080";
    private int timeout = 5000;
}
```

### RestClient (Spring 6+)

HTTP client moderno substituindo o deprecated `RestTemplate`:

```java
RestClient.builder()
    .baseUrl(properties.getUrl())
    .requestFactory(requestFactory)
    .build();
```

### Java Records

DTOs imutáveis com validação:

```java
public record PingResponse(String message, String version, Instant timestamp) {
    public PingResponse {
        Validate.notBlank(message, "message must not be blank");
    }
}
```

### JFR Events

Eventos do Java Flight Recorder para observabilidade:

```java
@Name("ProcessingEvent")
@Label("Processing Event")
public class ProcessingEvent extends Event {
    // Métricas customizadas
}
```

---

## Referências

Este projeto foi inspirado e utiliza conceitos dos seguintes artigos:

- [Spring Boot Library for integration with Istio](https://piotrminkowski.com/2020/06/10/spring-boot-library-for-integration-with-istio/) - Piotr Minkowski
- [Circuit breaker and retries on Kubernetes with Istio and Spring Boot](https://piotrminkowski.com/2020/06/03/circuit-breaker-and-retries-on-kubernetes-with-istio-and-spring-boot/) - Piotr Minkowski
- [Sobrevivendo a cenários de caos no Kubernetes com Istio e Amazon EKS](https://medium.com/@fidelissauro/sobrevivendo-a-cenários-de-caos-no-kubernetes-com-istio-e-amazon-eks-4fb8469a73da) - Matheus Fidelis
- [Istio Official Documentation](https://istio.io/latest/docs/)
- [Enhancing Network Resilience with Istio on Amazon EKS](https://aws.amazon.com/blogs/opensource/enhancing-network-resilience-with-istio-on-amazon-eks/) - AWS

---

## Autor

**Renan H. Silva**

- GitHub: [@Renanh](https://github.com/Renanh)

---

## Licença

MIT License - veja [LICENSE](LICENSE) para detalhes.
