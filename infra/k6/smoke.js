import http from 'k6/http';
import { check } from 'k6';

export const options = {
    vus: 1,
    duration: '10s'
}

export default function () {
    const res = http.get('http://host.docker.internal:8080/actuator/health')
    check(res, { 'status was ok': (r) => r.status === 200 })
};