import http from "k6/http";
import { check, sleep } from "k6";

export const options = {
    scenarios: {
        order_ramp_rate: {
            executor: "ramping-arrival-rate",
            startRate: parseInt(__ENV.START_RATE || "100", 10), // 시작 RPS
            timeUnit: "1s",
            stages: [
                { target: parseInt(__ENV.RATE1 || "250", 10), duration: __ENV.DUR1 || "1m" },
                { target: parseInt(__ENV.RATE2 || "400", 10), duration: __ENV.DUR2 || "1m" },
                { target: parseInt(__ENV.RATE3 || "650", 10), duration: __ENV.DUR3 || "1m" },
                { target: 0, duration: __ENV.DUR4 || "30s" }, // 그레이스풀 다운
            ],
            preAllocatedVUs: parseInt(__ENV.PRE_VUS || "200", 10),
            maxVUs: parseInt(__ENV.MAX_VUS || "700", 10),
            gracefulStop: "10s",
        },
    },
    thresholds: {
        http_req_failed: ["rate<0.05"],
        http_req_duration: ["p(95)<1000", "p(99)<2000"],
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

    // ramping-arrival-rate는 iteration sleep이 RPS 제어에 거의 영향 없음
    sleep(0.01);
}
