/**
 * K6 Load Test for Sample Istio Services
 *
 * Este script testa a resiliência do sistema sob diferentes cenários de carga.
 *
 * Uso:
 *   k6 run load-test.js                              # Cenário padrão (ramping)
 *   k6 run --env SCENARIO=spike load-test.js         # Spike test
 *   k6 run --env SCENARIO=stress load-test.js        # Stress test
 *   k6 run --env BASE_URL=http://localhost:30000 load-test.js
 *
 * Cenários:
 *   - default: Rampa gradual de 0 a 50 VUs
 *   - spike: Pico repentino de 0 a 200 VUs
 *   - stress: Carga sustentada de 100 VUs por 5 minutos
 *   - soak: Carga moderada por longo período (30 min)
 */

import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Rate, Trend, Counter } from 'k6/metrics';

// Custom metrics
const errorRate = new Rate('errors');
const pingDuration = new Trend('ping_duration');
const delayDuration = new Trend('delay_duration');
const successfulRequests = new Counter('successful_requests');
const failedRequests = new Counter('failed_requests');

// Configuração base
const BASE_URL = __ENV.BASE_URL || 'http://localhost:30000';
const SCENARIO = __ENV.SCENARIO || 'default';

// Cenários de teste
const scenarios = {
    // Cenário padrão: rampa gradual
    default: {
        executor: 'ramping-vus',
        startVUs: 0,
        stages: [
            { duration: '30s', target: 10 },   // Warm-up
            { duration: '1m', target: 25 },    // Ramp-up
            { duration: '2m', target: 50 },    // Carga máxima
            { duration: '30s', target: 25 },   // Ramp-down
            { duration: '30s', target: 0 },    // Cool-down
        ],
        gracefulRampDown: '30s',
    },

    // Spike test: pico repentino de carga
    spike: {
        executor: 'ramping-vus',
        startVUs: 0,
        stages: [
            { duration: '10s', target: 10 },   // Carga inicial
            { duration: '5s', target: 200 },   // SPIKE!
            { duration: '30s', target: 200 },  // Sustenta o spike
            { duration: '10s', target: 10 },   // Retorna ao normal
            { duration: '30s', target: 10 },   // Estabiliza
            { duration: '10s', target: 0 },    // Cool-down
        ],
        gracefulRampDown: '10s',
    },

    // Stress test: carga alta sustentada
    stress: {
        executor: 'constant-vus',
        vus: 100,
        duration: '5m',
    },

    // Soak test: carga moderada por longo período
    soak: {
        executor: 'constant-vus',
        vus: 30,
        duration: '30m',
    },

    // Breakpoint test: encontrar o ponto de quebra
    breakpoint: {
        executor: 'ramping-arrival-rate',
        startRate: 10,
        timeUnit: '1s',
        preAllocatedVUs: 50,
        maxVUs: 500,
        stages: [
            { duration: '2m', target: 50 },
            { duration: '2m', target: 100 },
            { duration: '2m', target: 200 },
            { duration: '2m', target: 300 },
        ],
    },
};

export const options = {
    scenarios: {
        [SCENARIO]: scenarios[SCENARIO] || scenarios.default,
    },
    thresholds: {
        http_req_duration: ['p(95)<2000', 'p(99)<5000'],  // 95% < 2s, 99% < 5s
        errors: ['rate<0.1'],                              // Error rate < 10%
        http_req_failed: ['rate<0.1'],                     // HTTP failures < 10%
    },
    // Configurações de saída
    summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'p(99)'],
};

// Função principal de teste
export default function () {
    group('Service Chain Test', function () {
        // Test 1: Ping simples através da cadeia
        const pingResponse = http.get(`${BASE_URL}/first/ping`, {
            tags: { name: 'ping' },
            timeout: '10s',
        });

        const pingSuccess = check(pingResponse, {
            'ping: status 200': (r) => r.status === 200,
            'ping: contains first-service': (r) => r.body && r.body.includes('first-service'),
            'ping: contains caller-service': (r) => r.body && r.body.includes('caller-service'),
            'ping: contains callme-service': (r) => r.body && r.body.includes('callme-service'),
            'ping: response time < 2s': (r) => r.timings.duration < 2000,
        });

        if (pingSuccess) {
            successfulRequests.add(1);
        } else {
            failedRequests.add(1);
        }

        errorRate.add(!pingSuccess);
        pingDuration.add(pingResponse.timings.duration);
    });

    group('Resilience Test', function () {
        // Test 2: Endpoint com delay aleatório (testa timeout handling)
        const delayResponse = http.get(`${BASE_URL}/first/ping-with-random-delay`, {
            tags: { name: 'ping-delay' },
            timeout: '10s',
        });

        check(delayResponse, {
            'delay: status 200': (r) => r.status === 200,
            'delay: response time < 5s': (r) => r.timings.duration < 5000,
        });

        delayDuration.add(delayResponse.timings.duration);
    });

    // Pausa entre iterações (simula tempo de "think")
    sleep(Math.random() * 2 + 0.5); // 0.5s a 2.5s
}

// Teste de setup (executado uma vez no início)
export function setup() {
    console.log(`\n========================================`);
    console.log(`  K6 Load Test - Sample Istio Services`);
    console.log(`========================================`);
    console.log(`  Base URL: ${BASE_URL}`);
    console.log(`  Scenario: ${SCENARIO}`);
    console.log(`========================================\n`);

    // Verifica se o serviço está acessível
    const healthCheck = http.get(`${BASE_URL}/actuator/health`);
    if (healthCheck.status !== 200) {
        console.error(`Service not healthy! Status: ${healthCheck.status}`);
    }

    return { startTime: Date.now() };
}

// Teste de teardown (executado uma vez no final)
export function teardown(data) {
    const duration = (Date.now() - data.startTime) / 1000;
    console.log(`\nTest completed in ${duration.toFixed(2)} seconds`);
}

// Formatação do sumário
export function handleSummary(data) {
    const summary = generateSummary(data);

    return {
        'stdout': summary,
        'summary.json': JSON.stringify(data, null, 2),
    };
}

function generateSummary(data) {
    const metrics = data.metrics;
    const indent = '  ';

    let summary = '\n';
    summary += '╔══════════════════════════════════════════════════════════════╗\n';
    summary += '║              LOAD TEST SUMMARY                               ║\n';
    summary += '╠══════════════════════════════════════════════════════════════╣\n';

    // Requests
    summary += '║ REQUESTS                                                     ║\n';
    summary += `║${indent}Total:        ${formatNumber(metrics.http_reqs?.values?.count || 0).padStart(10)}                             ║\n`;
    summary += `║${indent}Rate:         ${formatNumber(metrics.http_reqs?.values?.rate?.toFixed(2) || 0).padStart(10)}/s                           ║\n`;
    summary += `║${indent}Failed:       ${formatNumber(metrics.http_req_failed?.values?.passes || 0).padStart(10)}                             ║\n`;

    // Durations
    summary += '╠══════════════════════════════════════════════════════════════╣\n';
    summary += '║ RESPONSE TIMES                                               ║\n';
    const dur = metrics.http_req_duration?.values || {};
    summary += `║${indent}Average:      ${formatMs(dur.avg).padStart(10)}                             ║\n`;
    summary += `║${indent}Median:       ${formatMs(dur.med).padStart(10)}                             ║\n`;
    summary += `║${indent}P90:          ${formatMs(dur['p(90)']).padStart(10)}                             ║\n`;
    summary += `║${indent}P95:          ${formatMs(dur['p(95)']).padStart(10)}                             ║\n`;
    summary += `║${indent}P99:          ${formatMs(dur['p(99)']).padStart(10)}                             ║\n`;
    summary += `║${indent}Max:          ${formatMs(dur.max).padStart(10)}                             ║\n`;

    // Error rate
    summary += '╠══════════════════════════════════════════════════════════════╣\n';
    summary += '║ ERROR RATE                                                   ║\n';
    const errRate = (metrics.errors?.values?.rate || 0) * 100;
    summary += `║${indent}Rate:         ${errRate.toFixed(2).padStart(10)}%                            ║\n`;

    // Thresholds
    summary += '╠══════════════════════════════════════════════════════════════╣\n';
    summary += '║ THRESHOLDS                                                   ║\n';
    const thresholds = data.thresholds || {};
    for (const [name, result] of Object.entries(thresholds)) {
        const status = result.ok ? '✓ PASS' : '✗ FAIL';
        summary += `║${indent}${name.substring(0, 35).padEnd(35)} ${status.padStart(8)}       ║\n`;
    }

    summary += '╚══════════════════════════════════════════════════════════════╝\n';

    return summary;
}

function formatNumber(num) {
    return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',');
}

function formatMs(ms) {
    if (ms === undefined || ms === null) return 'N/A';
    if (ms < 1000) return `${ms.toFixed(2)}ms`;
    return `${(ms / 1000).toFixed(2)}s`;
}
