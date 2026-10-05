import json
import unittest
from unittest.mock import MagicMock, patch

import demo


class SmokeTest(unittest.TestCase):
    def setUp(self):
        self.state = MagicMock()
        self.state.exists.return_value = True
        self.state.read_text.return_value = json.dumps({"userId": 1, "productId": 2, "addressId": 3})

    def test_happy_path(self):
        responses = [
            {"accessToken": "test-token"}, {"id": 7},
            {"status": "PENDING", "delivery": None},
            {"status": "COMPLETED", "delivery": {"deliveryId": 8}},
            {"deliveryId": 8, "orderId": 7},
        ]
        with patch.object(demo, "STATE", self.state), patch.object(demo, "api", side_effect=responses) as api, patch.object(demo.time, "sleep"):
            demo.smoke()
        self.assertEqual(api.call_count, 5)
        self.assertEqual(api.call_args.args[1], "/delivery/deliveries/orders/7")

    def test_payment_failure(self):
        with patch.object(demo, "STATE", self.state), patch.object(demo, "api", side_effect=[
            {"accessToken": "test-token"}, {"id": 7}, {"status": "FAILED"},
        ]), self.assertRaisesRegex(RuntimeError, "Payment/order failed"):
            demo.smoke()

    def test_wrong_delivery(self):
        with patch.object(demo, "STATE", self.state), patch.object(demo, "api", side_effect=[
            {"accessToken": "test-token"}, {"id": 7},
            {"status": "COMPLETED", "delivery": {"deliveryId": 8}}, {"orderId": 999},
        ]), self.assertRaisesRegex(RuntimeError, "does not match"):
            demo.smoke()

    def test_timeout(self):
        with patch.object(demo, "STATE", self.state), patch.object(demo, "api", side_effect=[
            {"accessToken": "test-token"}, {"id": 7},
        ]), patch.object(demo.time, "monotonic", side_effect=[0, 91]), self.assertRaisesRegex(RuntimeError, "Timed out"):
            demo.smoke()

    def test_missing_initialization(self):
        self.state.exists.return_value = False
        with patch.object(demo, "STATE", self.state), self.assertRaisesRegex(RuntimeError, "initialization has not completed"):
            demo.smoke()


if __name__ == "__main__":
    unittest.main()
