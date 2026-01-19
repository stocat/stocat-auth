import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  vus: 10,
  duration: '1m',
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<500'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8085';
const PASSWORD = __ENV.PASSWORD || 'P@ssw0rd!';

function signupPayload(unique) {
  return JSON.stringify({
    nickname: `user${unique}`,
    email: `user${unique}@example.com`,
    password: PASSWORD,
  });
}

function loginPayload(unique) {
  return JSON.stringify({
    email: `user${unique}@example.com`,
    password: PASSWORD,
  });
}

export default function () {
  const unique = `${__VU}-${__ITER}`;
  const headers = { 'Content-Type': 'application/json' };

  const signupRes = http.post(`${BASE_URL}/auth/signup`, signupPayload(unique), { headers });
  check(signupRes, {
    'signup: 200': (r) => r.status === 200,
  });

  const loginRes = http.post(`${BASE_URL}/auth/login`, loginPayload(unique), { headers });
  check(loginRes, {
    'login: 200': (r) => r.status === 200,
  });

  const errorsRes = http.get(`${BASE_URL}/auth/errors`);
  check(errorsRes, {
    'errors: 200': (r) => r.status === 200,
  });

  sleep(1);
}
