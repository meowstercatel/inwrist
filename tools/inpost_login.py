#!/usr/bin/env python3
import base64
import hashlib
import json
import secrets
import subprocess
import sys
import urllib.parse
import urllib.request
from pathlib import Path
from xml.sax.saxutils import escape

CLIENT_ID = "inpost-mobile"
REDIRECT_URI = "https://account.inpost-group.com/callback"
AUTHORIZE_URL = "https://account.inpost-group.com/oauth2/authorize"
TOKEN_URL = "https://api-inmobile-pl.easypack24.net/global/oauth2/token"
PACKAGE = "com.inwrist.app"
STATE_FILE = Path.home() / ".inwrist-login.json"


def b64(data: bytes) -> str:
    return base64.urlsafe_b64encode(data).rstrip(b"=").decode()


def start() -> None:
    verifier = b64(secrets.token_bytes(32))
    state = b64(secrets.token_bytes(12))
    STATE_FILE.write_text(json.dumps({"verifier": verifier, "state": state}))
    params = {
        "response_type": "code",
        "client_id": CLIENT_ID,
        "redirect_uri": REDIRECT_URI,
        "scope": "openid",
        "code_challenge": b64(hashlib.sha256(verifier.encode()).digest()),
        "code_challenge_method": "S256",
        "state": state,
        "nonce": b64(secrets.token_bytes(12)),
        "response_mode": "query",
        "lang": "pl",
        "prompt": "login",
    }
    print(AUTHORIZE_URL + "?" + urllib.parse.urlencode(params))


def exchange(callback_url: str) -> None:
    saved = json.loads(STATE_FILE.read_text())
    query = urllib.parse.parse_qs(urllib.parse.urlparse(callback_url).query)
    if query.get("state", [None])[0] != saved["state"]:
        sys.exit("state mismatch, run `start` again")
    form = urllib.parse.urlencode({
        "client_id": CLIENT_ID,
        "grant_type": "authorization_code",
        "redirect_uri": REDIRECT_URI,
        "code_verifier": saved["verifier"],
        "code": query["code"][0],
    }).encode()
    request = urllib.request.Request(TOKEN_URL, data=form, headers={"Accept": "application/json"})
    with urllib.request.urlopen(request) as response:
        tokens = json.load(response)
    saved["tokens"] = tokens
    STATE_FILE.write_text(json.dumps(saved))
    print("ok, expires_in=%s" % tokens.get("expires_in"))


def push() -> None:
    tokens = json.loads(STATE_FILE.read_text())["tokens"]
    xml = (
        "<?xml version='1.0' encoding='utf-8' standalone='yes' ?>\n<map>\n"
        f'    <string name="access_token">{escape(tokens["access_token"])}</string>\n'
        f'    <string name="refresh_token">{escape(tokens["refresh_token"])}</string>\n'
        "</map>\n"
    )
    subprocess.run(["adb", "shell", "am", "force-stop", PACKAGE], check=True)
    subprocess.run(
        ["adb", "shell", f"run-as {PACKAGE} sh -c 'mkdir -p shared_prefs && cat > shared_prefs/auth.xml'"],
        input=xml.encode(), check=True)
    print("pushed tokens to watch")


if __name__ == "__main__":
    commands = {"start": start, "push": push}
    if len(sys.argv) == 3 and sys.argv[1] == "exchange":
        exchange(sys.argv[2])
    elif len(sys.argv) == 2 and sys.argv[1] in commands:
        commands[sys.argv[1]]()
    else:
        sys.exit("usage: inpost_login.py start | exchange <callback-url> | push")
