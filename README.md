# CBOR Wrangler

A Burp Suite extension that automatically detects and displays CBOR (Concise Binary Object Representation) data in a human-readable format.

> **Disclaimer:** This extension was created on personal time without the use of any company resources.

## Installation

**From Release:**
1. Download `CBOR_Wrangler.jar` from [releases](https://github.com/nedpfeiffer/cbor-wrangler/releases)
2. In Burp Suite: **Extensions** → **Add** → Select JAR

**Build from Source:**
```bash
./gradlew jar
# Output: build/libs/CBOR_Wrangler.jar
```

## Usage

Once installed, a "CBOR Wrangler" tab appears in the HTTP message editor when CBOR content is detected (via Content-Type header or automatic parsing). The tab shows headers and decoded CBOR body in JSON-like format.

## Requirements

- Burp Suite (Professional or Community)
- Java 21+
