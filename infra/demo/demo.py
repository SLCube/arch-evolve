"""Local demo initializer and API smoke test; never targets the host databases."""
import json
import sys
import time
import urllib.error
import urllib.request
from pathlib import Path

import psycopg2
import redis

BASE = "http://api-gateway:8080"
LOGIN = "demo-reviewer"
PASSWORD = "demo1234"
PRODUCT_NAME = "ArchEvolve demo product"
STATE = Path("/state/demo.json")


def api(method, route, body=None, token=None):
    headers = {"Content-Type": "application/json"}
    if token:
        headers["Authorization"] = "Bearer " + token
    request = urllib.request.Request(
        BASE + route,
        data=json.dumps(body).encode() if body is not None else None,
        headers=headers,
        method=method,
    )
    try:
        with urllib.request.urlopen(request, timeout=15) as response:
            text = response.read().decode()
            return json.loads(text) if text else None
    except urllib.error.HTTPError as error:
        raise RuntimeError(f"{method} {route}: HTTP {error.code}: {error.read().decode()}") from error


def login():
    return api("POST", "/users/login", {"loginId": LOGIN, "password": PASSWORD})["accessToken"]


def seed():
    with psycopg2.connect(host="monolith-db", dbname="playground", user="playground", password="playground") as db:
        with db.cursor() as cursor:
            cursor.execute("SELECT user_id FROM users WHERE login_id = %s", (LOGIN,))
            user = cursor.fetchone()
            if user is None:
                api("POST", "/users/sign-up", {"loginId": LOGIN, "password": PASSWORD, "nickname": "reviewer"})
            # No public role-change/address-list API exists. Only this demo user is modified.
            cursor.execute("UPDATE users SET role = 'ADMIN' WHERE login_id = %s RETURNING user_id", (LOGIN,))
            user_id = cursor.fetchone()[0]
            db.commit()
            token = login()
            cursor.execute("SELECT user_address_id FROM user_address WHERE user_id = %s ORDER BY user_address_id LIMIT 1", (user_id,))
            address = cursor.fetchone()
            if address is None:
                # Existing address query uses an inner join and excludes users without addresses.
                # Provision the demo address without changing application behavior in this Docker task.
                cursor.execute("""
                    INSERT INTO user_address(user_id, receiver_name, receiver_phone_number,
                        zip_code, base_address, detail_address, is_default)
                    VALUES (%s, %s, %s, %s, %s, %s, true) RETURNING user_address_id
                """, (user_id, "Demo Reviewer", "01000000000", "12345", "Demo address", "Local demo only"))
                address = cursor.fetchone()
                db.commit()
            products = api("GET", "/products")
            product = next((p for p in products if p["name"] == PRODUCT_NAME), None)
            if product is None:
                product = api("POST", "/products", {"name": PRODUCT_NAME, "stock": 10000, "price": 1000}, token)
            # Product creation currently initializes no Redis stock; the app runner only runs at startup.
            # Set missing demo keys only, never refill stock or clear reservations on repeat initialization.
            cache = redis.Redis(host="redis", decode_responses=True)
            for suffix, value in (("available", product["stock"]), ("reserved", 0), ("confirmed", 0)):
                cache.set(f"product:stock:{product['id']}:{suffix}", value, nx=True)
            methods = api("GET", "/payment/payment-methods", token=token)
            if not any(p.get("isDefault", p.get("default", False)) for p in methods):
                if methods:
                    raise RuntimeError("Demo payment method has no default; reset only the demo data or restore its default method.")
                api("POST", "/payment/payment-methods", {
                    "authKey": "demo-auth-key", "cardCompany": "DEMO", "cardNumberMasked": "0000-****-****-0000", "setAsDefault": True,
                }, token)
            STATE.parent.mkdir(parents=True, exist_ok=True)
            STATE.write_text(json.dumps({"userId": user_id, "productId": product["id"], "addressId": address[0]}))
    print(f"DEMO READY: loginId={LOGIN}, password={PASSWORD}")
    print(f"Demo IDs: {STATE.read_text()}")
    print("Gateway: http://localhost:18080 | Verify: docker compose run --rm smoke-test")


def smoke():
    if not STATE.exists():
        raise RuntimeError("Demo initialization has not completed. Check: docker compose logs demo-init")
    state = json.loads(STATE.read_text())
    token = login()
    print("[1/4] Login OK")
    order = api("POST", "/orders", {
        "addressId": state["addressId"], "orderProducts": [{"productId": state["productId"], "quantity": 1}],
    }, token)
    order_id = order["id"]
    print(f"[2/4] Order accepted: orderId={order_id}")
    deadline = time.monotonic() + 90
    detail = None
    while time.monotonic() < deadline:
        detail = api("GET", f"/orders/{order_id}", token=token)
        if detail["status"] == "FAILED":
            raise RuntimeError(f"Payment/order failed: orderId={order_id}, detail={detail}")
        if detail["status"] == "COMPLETED" and detail.get("delivery"):
            delivery = api("GET", f"/delivery/deliveries/orders/{order_id}", token=token)
            if delivery["orderId"] != order_id:
                raise RuntimeError(f"Delivery does not match the order: {delivery}")
            print("[3/4] Payment processed, order COMPLETED")
            print(f"[4/4] Delivery created: deliveryId={delivery['deliveryId']}, orderId={delivery['orderId']}")
            print("SMOKE TEST PASSED (happy path only; no real payment)")
            return
        time.sleep(1)
    raise RuntimeError(f"Timed out waiting for payment/delivery: orderId={order_id}, last response={detail}. Check app/Kafka logs.")


if __name__ == "__main__":
    try:
        mode = sys.argv[1] if len(sys.argv) > 1 else "smoke"
        if mode == "seed":
            seed()
        elif mode == "smoke":
            smoke()
        else:
            raise ValueError("Expected seed or smoke")
    except Exception as error:
        print(f"DEMO FAILED: {error}", file=sys.stderr)
        sys.exit(1)
