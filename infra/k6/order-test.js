import http from "k6/http";
import { check, sleep } from "k6";

// ---- options ----
export const options = {
    scenarios: {
        lock_contention: {
            executor: "ramping-vus",
            startVUs: 0,
            stages: [
                { duration: "10s", target: 20 },
                { duration: "20s", target: 50 },
                { duration: "20s", target: 100 },
                { duration: "10s", target: 0 },
            ],
            gracefulRampDown: "5s",
        },
    },
    thresholds: {
        http_req_failed: ["rate<0.05"],
        http_req_duration: ["p(95)<2000", "p(99)<5000"],
    },
};

// ---- constants / env ----
const BASE_URL = __ENV.BASE_URL || "http://localhost:8080";

const LOGIN_PATH = __ENV.LOGIN_PATH || "/users/login";
const ORDER_PATH = __ENV.ORDER_PATH || "/orders";

const USER_COUNT = parseInt(__ENV.USER_COUNT || "1000", 10);
const PASSWORD = __ENV.PASSWORD || "testpassword";

const PRODUCT_ID = parseInt(__ENV.PRODUCT_ID || "2", 10);
const QTY = parseInt(__ENV.QTY || "1", 10);

// 주문 바디에서 "아이템 배열" 필드명이 확실치 않아서 옵션화
// 예: items / orderItems / products ...
const ITEMS_FIELD = __ENV.ITEMS_FIELD || "orderProducts";

// ---- per-VU token cache ----
const tokenByVu = new Map();

function getUserIndexForVu() {
    // VU를 1..1000 계정으로 매핑
    return ((__VU - 1) % USER_COUNT) + 1;
}

function loginAndGetToken(userIndex) {
    const username = `test${userIndex}`;

    const res = http.post(
        `${BASE_URL}${LOGIN_PATH}`,
        JSON.stringify({ loginId:username, password: PASSWORD }),
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

    // 너의 데이터 조건: addressId = user_id - 2
    // userId가 testN -> (N+2) 라고 보면 addressId는 결국 N
    const addressId = userIndex;

    const token = getTokenForThisVu();
    if (!token) {
        // 로그인 실패면 주문 스킵
        sleep(0.2);
        return;
    }

    const itemsArray = [{ productId: PRODUCT_ID, quantity: QTY }];

    // items 필드명이 다를 수 있으니 동적으로 넣기
    const payload = { addressId };
    payload[ITEMS_FIELD] = itemsArray;

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

    sleep(0.1);
}
