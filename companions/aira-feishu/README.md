# Aira Feishu connector

An optional computer-side connector that receives private messages from a paired Feishu user and submits tasks to Aira on an Android phone. It uses the public `/api/v1` task API and the official Feishu SDK, without modifying Android code or importing the Aira CLI's internals.

See [the English guide](../../docs/feishu.en.md) or [简体中文指南](../../docs/feishu.md) for QR creation, linking an existing app, manual setup and troubleshooting.

Requires Python 3.10+, a Feishu enterprise custom app, and a Aira phone exposing task API v1 (APK v0.1.23+). Use matching current phone/CLI versions when possible. The computer and phone must be on the same LAN or a trusted private VPN such as Tailscale. The computer must remain online to receive events.

```bash
# From a repository checkout; pipx keeps the optional SDK dependencies isolated.
pipx install ./companions/aira-feishu
aira-feishu setup
aira-feishu doctor
aira-feishu run
```

`setup` offers scan-to-create, link-existing and manual credential entry. `run` prints a ten-minute one-time code when no owner is paired; send it privately to the bot. This is separate from the Feishu application authorization and from the phone's own access token.

## Development

```bash
python3 -m venv companions/aira-feishu/.venv
companions/aira-feishu/.venv/bin/python -m pip install -e ./companions/aira-feishu
companions/aira-feishu/.venv/bin/python -m unittest discover -s companions/aira-feishu/tests -v
```

On Windows, use `tools\aira-feishu\.venv\Scripts\python.exe`. Tests use fake phone HTTP endpoints and fake registration responses. They do not register real applications, send Feishu messages or execute phone tasks. A CI matrix covers Linux, macOS and Windows.
