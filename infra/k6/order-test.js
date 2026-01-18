import http from "k6/http";
import { check, sleep } from "k6";

export const options = {
    scenarios: {
        order_rate_vu1: {
            executor: "constant-arrival-rate",
            rate: parseInt(__ENV.RATE || "5", 10),     // 초당 iteration(=주문 1회) 목표
            timeUnit: "1s",
            duration: __ENV.DURATION || "90s",
            preAllocatedVUs: 1,
            maxVUs: 1,
            gracefulStop: "10s",
        },
    },
    thresholds: {
        http_req_failed: ["rate<0.05"],
        http_req_duration: ["p(95)<500", "p(99)<1000"],
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
    return ((__VU - 1) % USER_COUNT) + 1; // VU=1이면 userIndex=1 고정
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

    // 여기 sleep은 "요청률 조절"이 아니라 iteration 내부 처리 시간만 늘림
    sleep(0.01);
}
