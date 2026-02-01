# Chaos Engineering Scenarios

> Cenários de Chaos Engineering para testar a resiliência do sistema usando Istio

Este diretório contém configurações de fault injection do Istio para simular falhas e validar a resiliência da arquitetura de microservices.

---

## Cenários Disponíveis

| Arquivo | Cenário | Descrição | Impacto |
| ------- | ------- | --------- | ------- |
| `fault-delay.yaml` | Latência | Injeta delay de 5s em 50% das requisições | Testa timeouts e retries |
| `fault-abort.yaml` | Erro HTTP | Injeta erro 500 em 30% das requisições | Testa circuit breaker |
| `fault-cascade.yaml` | Falha em Cascata | Simula falha total do serviço backend | Testa graceful degradation |
| `circuit-breaker-strict.yaml` | Circuit Breaker Agressivo | Configuração mais restritiva | Testa ejeção rápida |

---

## Pré-requisitos

- Cluster Kubernetes com Istio instalado
- Namespace `istio-test` com `istio-injection=enabled`
- Serviços deployados e funcionando
- K6 instalado para testes de carga

---

## Como Usar

### 1. Aplicar cenário de teste

```bash
# Injeta latência (50% das requisições terão 5s de delay)
kubectl apply -f k8s/chaos/fault-delay.yaml -n istio-test

# OU injeta erros (30% das requisições retornarão HTTP 500)
kubectl apply -f k8s/chaos/fault-abort.yaml -n istio-test

# OU simula falha total do backend
kubectl apply -f k8s/chaos/fault-cascade.yaml -n istio-test
```

### 2. Executar carga

```bash
# Teste básico
k6 run first-service/src/test/resources/k6/load-test.js

# Com mais usuários virtuais
k6 run --vus 50 --duration 60s first-service/src/test/resources/k6/load-test.js
```

### 3. Observar métricas

```bash
# Kiali - Visualização do service mesh
istioctl dashboard kiali

# Grafana - Métricas detalhadas
istioctl dashboard grafana

# Jaeger - Distributed tracing
istioctl dashboard jaeger
```

### 4. Remover cenário e restaurar

```bash
# Remove o cenário de chaos
kubectl delete -f k8s/chaos/fault-delay.yaml -n istio-test

# Restaura a configuração normal do Istio
kubectl apply -f callme-service/k8s/istio-rules.yaml -n istio-test
```

---

## Cenários de Teste Recomendados

### Teste 1: Resiliência a Latência

**Objetivo:** Validar que timeouts e retries protegem o sistema contra alta latência.

1. Aplique `fault-delay.yaml`
2. Execute carga com K6
3. **Observe:** Retries compensam requests lentos
4. **Verifique no Kiali:** Aumento de latência P99

**Resultado esperado:** Taxa de sucesso > 95% apesar dos delays.

### Teste 2: Resiliência a Erros

**Objetivo:** Validar que o circuit breaker ejeta instâncias com falha.

1. Aplique `fault-abort.yaml`
2. Execute carga com K6
3. **Observe:** Circuit breaker ejetando pods com erro
4. **Verifique no Grafana:** Taxa de sucesso e ejeções

**Resultado esperado:** Sistema estabiliza após ejeção dos pods problemáticos.

### Teste 3: Falha em Cascata

**Objetivo:** Validar graceful degradation quando um serviço downstream falha completamente.

1. Aplique `fault-cascade.yaml` (100% de erro)
2. Execute carga com K6
3. **Observe:** Sistema degrada graciosamente
4. **Verifique:** Circuit breaker evita sobrecarga upstream

**Resultado esperado:** Erros são contidos no serviço afetado.

### Teste 4: Pod Failure (Chaos Monkey)

**Objetivo:** Validar recuperação automática quando pods morrem.

```bash
# Em um terminal, execute carga
k6 run --vus 50 --duration 120s first-service/src/test/resources/k6/load-test.js

# Em outro terminal, mate pods aleatoriamente
kubectl delete pod -l app=callme-service -n istio-test --grace-period=0
```

**Resultado esperado:** Kubernetes recria pods, Istio redireciona tráfego, impacto mínimo.

---

## Métricas para Monitorar

| Métrica | O que observar |
| ------- | -------------- |
| `istio_requests_total` | Taxa de requisições com sucesso vs falha |
| `istio_request_duration_milliseconds_bucket` | Distribuição de latência (P50, P90, P99) |
| `envoy_cluster_upstream_cx_active` | Conexões ativas |
| `envoy_cluster_outlier_detection_ejections_active` | Pods ejetados pelo circuit breaker |

---

## Dicas

- **Sempre** remova os cenários de chaos após os testes
- Execute testes em **ambientes não-produtivos**
- Monitore as métricas **durante** a execução
- Documente os **resultados** de cada teste
- Ajuste os parâmetros do circuit breaker conforme necessário
