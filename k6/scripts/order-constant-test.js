import http from "k6/http";
import { check, sleep } from "k6";

export const options = {
    setupTimeout: "5m", // setup 타임아웃 5분으로 증가
    scenarios: {
        constant_load: {
            executor: "constant-arrival-rate",
            rate: parseInt(__ENV.RATE || "100", 10), // 목표 RPS (기본 100)
            timeUnit: "1s",
            duration: __ENV.DURATION || "3m", // 테스트 지속 시간 (기본 3분)
            preAllocatedVUs: parseInt(__ENV.PRE_VUS || "100", 10),
            maxVUs: parseInt(__ENV.MAX_VUS || "100", 10),
        },
    },
    thresholds: {
        http_req_failed: ["rate<0.05"], // 실패율 5% 미만
        http_req_duration: ["p(95)<1000", "p(99)<2000"], // P95 < 1초, P99 < 2초
    },
};

const BASE_URL = __ENV.BASE_URL || "http://localhost:8080";
const LOGIN_PATH = __ENV.LOGIN_PATH || "/users/login";
const ORDER_PATH = __ENV.ORDER_PATH || "/orders";

const USER_COUNT = parseInt(__ENV.USER_COUNT || "100", 10);
const PASSWORD = __ENV.PASSWORD || "password1234";

const PRODUCT_ID = parseInt(__ENV.PRODUCT_ID || "2", 10);
const QTY = parseInt(__ENV.QTY || "1", 10);
const ITEMS_FIELD = __ENV.ITEMS_FIELD || "orderProducts";

function getUserIndexForVu() {
    return ((__VU - 1) % USER_COUNT) + 1;
}

export function setup() {
    console.log(`Setup: Logging in ${USER_COUNT} users...`);
    const tokens = {};
    const BATCH_SIZE = 50; // 50개씩 병렬 처리

    for (let start = 1; start <= USER_COUNT; start += BATCH_SIZE) {
        const end = Math.min(start + BATCH_SIZE - 1, USER_COUNT);
        const requests = {};

        // 배치 요청 준비
        for (let i = start; i <= end; i++) {
            const username = `test${i}`;
            requests[`user_${i}`] = {
                method: "POST",
                url: `${BASE_URL}${LOGIN_PATH}`,
                body: JSON.stringify({ loginId: username, password: PASSWORD }),
                params: { headers: { "Content-Type": "application/json" } },
            };
        }

        // 병렬 요청 실행
        const responses = http.batch(requests);

        // 응답 처리
        for (let i = start; i <= end; i++) {
            const res = responses[`user_${i}`];
            const ok = check(res, {
                "login status is 200": (r) => r.status === 200,
                "login has accessToken": (r) => !!r.json("accessToken"),
            });

            if (ok) {
                tokens[i] = res.json("accessToken");
            } else {
                console.error(`Failed to login user: test${i}`);
            }
        }

        console.log(`Logged in ${end}/${USER_COUNT} users`);
        sleep(0.1); // 배치 간 짧은 딜레이
    }

    console.log(`Setup complete: ${Object.keys(tokens).length}/${USER_COUNT} users logged in`);
    return { tokens };
}

export default function (data) {
    const userIndex = getUserIndexForVu();
    const addressId = userIndex;

    const token = data.tokens[userIndex];
    if (!token) {
        console.error(`No token for user ${userIndex}`);
        sleep(0.1);
        return;
    }

    const payload = { addressId };
    payload[ITEMS_FIELD] = [{ productId: PRODUCT_ID, quantity: QTY }];

    const res = http.post(`${BASE_URL}${ORDER_PATH}`, JSON.stringify(payload), {
        headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
        },
        tags: { name: "order" },
    });

    check(res, {
        "order status 200/201": (r) => r.status === 200 || r.status === 201,
    });

    // constant-arrival-rate는 자동으로 RPS 제어하므로 sleep 최소화
    sleep(0.01);
}
