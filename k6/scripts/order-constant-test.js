import http from "k6/http";
import { check, sleep } from "k6";

export const options = {
    scenarios: {
        constant_load: {
            executor: "constant-arrival-rate",
            rate: parseInt(__ENV.RATE || "700", 10), // 목표 RPS (기본 700)
            timeUnit: "1s",
            duration: __ENV.DURATION || "3m", // 테스트 지속 시간 (기본 3분)
            preAllocatedVUs: parseInt(__ENV.PRE_VUS || "200", 10),
            maxVUs: parseInt(__ENV.MAX_VUS || "1000", 10),
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

const USER_COUNT = parseInt(__ENV.USER_COUNT || "1000", 10);
const PASSWORD = __ENV.PASSWORD || "testpassword";

const PRODUCT_ID = parseInt(__ENV.PRODUCT_ID || "2", 10);
const QTY = parseInt(__ENV.QTY || "1", 10);
const ITEMS_FIELD = __ENV.ITEMS_FIELD || "orderProducts";

const tokenByVu = new Map();

function getUserIndexForVu() {
    return ((__VU - 1) % USER_COUNT) + 1;
}

function loginAndGetToken(userIndex) {
    const username = `test${userIndex}`;

    const res = http.post(
        `${BASE_URL}${LOGIN_PATH}`,
        JSON.stringify({ loginId: username, password: PASSWORD }),
        { headers: { "Content-Type": "application/json" }, tags: { name: "login" } }
    );

    const ok = check(res, {
        "login status is 200": (r) => r.status === 200,
        "login has accessToken": (r) => !!r.json("accessToken"),
    });

    if (!ok) return null;
    return res.json("accessToken");
}

function getTokenForThisVu() {
    const vu = __VU;
    if (tokenByVu.has(vu)) return tokenByVu.get(vu);

    const userIndex = getUserIndexForVu();
    const token = loginAndGetToken(userIndex);
    if (token) tokenByVu.set(vu, token);
    return token;
}

export default function () {
    const userIndex = getUserIndexForVu();
    const addressId = userIndex;

    const token = getTokenForThisVu();
    if (!token) {
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
