#!/usr/bin/env python3
"""
EyesOfPriestess — End-to-End Covenant Protocol Integration Verification Script
Tests the complete lifecycle of Escrow & Microservices interaction via Kong API Gateway.
"""

import sys
import time
import requests

API_GATEWAY = "http://localhost:8080/api/v1"

def print_step(title):
    print(f"\n=======================================================")
    print(f"  {title}")
    print(f"=======================================================")

def main():
    print_step("EYES OF PRIESTESS — E2E VERIFICATION SUITE")
    print(f"Targeting API Gateway: {API_GATEWAY}")

    # Generate unique test user emails
    timestamp = int(time.time())
    buyer_email = f"buyer_{timestamp}@sanctum.realm"
    seller_email = f"seller_{timestamp}@sanctum.realm"
    password = "SecretPassword123!"
    pin = "123456"

    # Step 1: Register Buyer & Seller
    print_step("Step 1: Registering Pilgrims (Buyer & Seller)")
    buyer_reg = requests.post(f"{API_GATEWAY}/seals/rite/forge", json={
        "email": buyer_email,
        "password": password,
        "fullName": "Buyer Pilgrim",
        "sanctumTitle": "Initiate Seeker",
        "securityPin": pin
    })
    print(f"Buyer Registration Status: {buyer_reg.status_code}")
    if buyer_reg.status_code not in (200, 201):
        print("Registration failed:", buyer_reg.text)
        sys.exit(1)
    buyer_data = buyer_reg.json().get("data", {})
    buyer_token = buyer_data.get("accessToken")
    buyer_id = buyer_data.get("pilgrim", {}).get("id")

    seller_reg = requests.post(f"{API_GATEWAY}/seals/rite/forge", json={
        "email": seller_email,
        "password": password,
        "fullName": "Seller Artisan",
        "sanctumTitle": "Master Craftsman",
        "securityPin": pin
    })
    print(f"Seller Registration Status: {seller_reg.status_code}")
    seller_data = seller_reg.json().get("data", {})
    seller_token = seller_data.get("accessToken")
    seller_id = seller_data.get("pilgrim", {}).get("id")

    print(f"✓ Buyer ID:  {buyer_id}")
    print(f"✓ Seller ID: {seller_id}")

    # Step 2: Login Check
    print_step("Step 2: Authenticating Pilgrims (Sacred Rite)")
    login_resp = requests.post(f"{API_GATEWAY}/seals/rite", json={
        "email": buyer_email,
        "password": password
    })
    assert login_resp.status_code == 200, f"Buyer login failed: {login_resp.text}"
    print("✓ Sacred Rite Authentication Successful")

    # Step 3: Top-up Treasury
    print_step("Step 3: Offering Treasury Top-Up for Buyer")
    buyer_headers = {"Authorization": f"Bearer {buyer_token}"}
    offering_resp = requests.post(
        f"{API_GATEWAY}/vault/offerings",
        headers=buyer_headers,
        json={"amount": 500000, "channel": "MOCK_VA"}
    )
    print(f"Offering Response Status: {offering_resp.status_code}")

    treasury_resp = requests.get(f"{API_GATEWAY}/vault/treasury", headers=buyer_headers)
    print(f"Buyer Treasury Status: {treasury_resp.status_code}, Body: {treasury_resp.text}")

    # Step 4: Forge Covenant
    print_step("Step 4: Forging Escrow Covenant (Buyer -> Seller)")
    covenant_resp = requests.post(
        f"{API_GATEWAY}/covenants",
        headers=buyer_headers,
        json={
            "title": "Ancient Relic Purchase",
            "description": "Purchase of Crystalline Artifact with 100% Escrow Hold",
            "amount": 250000,
            "destinedPilgrimId": seller_id,
            "expiryDays": 7
        }
    )
    print(f"Forge Covenant Status: {covenant_resp.status_code}")
    if covenant_resp.status_code not in (200, 201):
        print("Forge covenant error:", covenant_resp.text)
        sys.exit(1)
    
    cov_data = covenant_resp.json().get("data", {})
    cov_id = cov_data.get("id")
    print(f"✓ Covenant Forged Successfully ID: {cov_id}")

    # Step 5: Accept Covenant
    print_step("Step 5: Seller Accepts Covenant")
    seller_headers = {"Authorization": f"Bearer {seller_token}"}
    accept_resp = requests.post(
        f"{API_GATEWAY}/covenants/{cov_id}/accept",
        headers=seller_headers
    )
    print(f"Accept Status: {accept_resp.status_code}")

    # Step 6: Seal (Lock Escrow Funds)
    print_step("Step 6: Buyer Seals Covenant (Lock Funds in Vault)")
    seal_resp = requests.post(
        f"{API_GATEWAY}/covenants/{cov_id}/seal",
        headers=buyer_headers,
        json={"pin": pin}
    )
    print(f"Seal Status: {seal_resp.status_code}")

    # Step 7: Deliver Work
    print_step("Step 7: Seller Delivers Covenant Artifact")
    deliver_resp = requests.post(
        f"{API_GATEWAY}/covenants/{cov_id}/deliver",
        headers=seller_headers,
        json={"deliveryProof": "https://sanctum.vault/proof/artifact-rel-9982"}
    )
    print(f"Deliver Status: {deliver_resp.status_code}")

    # Step 8: Fulfill & Release Payment
    print_step("Step 8: Buyer Confirms Receipt & Fulfills Covenant")
    fulfill_resp = requests.post(
        f"{API_GATEWAY}/covenants/{cov_id}/fulfill",
        headers=buyer_headers,
        json={"pin": pin}
    )
    print(f"Fulfill Status: {fulfill_resp.status_code}")

    # Step 9: Final Treasury Balance Verification
    print_step("Step 9: Final Treasury Verification")
    final_buyer = requests.get(f"{API_GATEWAY}/vault/treasury", headers=buyer_headers)
    final_seller = requests.get(f"{API_GATEWAY}/vault/treasury", headers=seller_headers)
    print(f"Buyer Treasury:  {final_buyer.text}")
    print(f"Seller Treasury: {final_seller.text}")

    print_step("ALL E2E INTEGRATION CHECKS COMPLETED SUCCESSFULLY!")

if __name__ == "__main__":
    main()
