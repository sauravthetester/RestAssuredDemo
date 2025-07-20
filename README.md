
# 📘 Rest Assured API Testing Framework

This is a Java-based REST API automation framework built using **Rest Assured**, **TestNG**, **Maven**, and **WireMock**. It supports real-time and mock API testing with reusable utilities, schema validation, security tests, rate-limiting simulations, and more.

---

## 🏗️ Project Structure

```
src
├── main
│   └── java
│       └── utils/                    # Config reader, status codes, JSON reader
│       ├── mockutils/                # WireMock server and stubs
├── test
│   ├── java
│   │   ├── base/                     # Test base (env & mock setup)
│   │   ├── requests/                 # API request methods
│   │   └── tests/                    # All test classes (mock and real)
│   └── resources
│       ├── suites/                  # TestNG XML suite files
│       ├── requestdata/             # JSON payloads for requests
│       ├── responsedata/            # Mock response JSONs
│       ├── schema/                  # JSON Schemas for contract validation
│       └── config.properties        # Environment configuration
```

---

## 🔧 Key Utilities

| Class/File                  | Description                                     |
|----------------------------|-------------------------------------------------|
| `ConfigReader.java`        | Loads environment-based config from `.properties` |
| `ReadJsonFile.java`        | Reads JSON from `resources` as string           |
| `StatusCodesUtil.java`     | Reusable HTTP status code constants             |
| `WireMockSetup.java`       | Starts/stops mock server                        |
| `StubUtils.java`           | Configures stub responses using WireMock        |

---

## ✅ Test Scenarios Covered

- **Functional Tests:** Login, Create, Fetch, Delete
- **Negative Tests:** Unauthorized, Bad Request, Not Found
- **Security Tests:** SQL Injection, HTML Injection (XSS)
- **Contract Validation:** JSON Schema match
- **Idempotency:** Repeated POSTs give same result
- **Rate Limiting:** Simulate burst requests (429)
- **Caching Behavior:** Validate 304 Not Modified with ETag
- **Mock Testing:** With WireMock for offline test simulation

---

## 📦 Maven Profiles & Commands

### 🧪 Run full test suite (default)
```bash
mvn clean test -Denvr=dev -Dmock=false
```

### 🔁 Run only smoke tests
```bash
mvn test -Psmoke -Denvr=stage -Dmock=false
```

### ❌ Run negative test cases
```bash
mvn test -Pnegative -Denvr=dev -Dmock=false
```

### 🎭 Run mock test suite with WireMock
```bash
mvn test -Pmock -Denvr=dev -Dmock=true
```

### 🧪 Run regression only
```bash
mvn test -Pregression -Denvr=dev -Dmock=false
```

### 🔄 Run tests with rate limiting simulation
```bash
mvn test -Pregression -Denvr=dev -DrateLimitingThreads=100
```

---

## 🧪 Sample JSON Schema Validation

**Schema** (`schema/successfulLoginSchema.json`)
```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "title": "LoginResponse",
  "type": "object",
  "properties": {
    "token": { "type": "string" }
  },
  "required": ["token"],
  "additionalProperties": false
}
```

**Usage**
```java
response.then().body(matchesJsonSchemaInClasspath("schema/successfulLoginSchema.json"));
```

---

## 🛡️ Sample Security Test: HTML Injection

```json
{
  "name": "<script>alert('xss')</script>",
  "job": "<script>alert('xss')</script>"
}
```

```java
Assert.assertFalse(response.body().toString().contains("<script>"),
    "Potential XSS vulnerability: script tag echoed in response!");
```

---

## 🧊 Caching Test (ETag Validation)

```java
String etag = response.getHeader("ETag");
Response cachedResponse = ReqResAPI.getSingleUser("2", "token", true, etag);
cachedResponse.then().statusCode(304);
```

---

## 🧪 Mock Stubbing Example

```java
stubFor(get(urlEqualTo("/api/singleusers"))
  .withRequestBody(matchingJsonPath("$.id", matching("\d{3}")))
  .willReturn(aResponse()
    .withStatus(200)
    .withBody("{ "id": "123", "name": "John Doe" }")));
```

---

## ✅ Example Assertions

```java
response.then().statusCode(200);
Assert.assertTrue(response.getTime() < 2500, "Slow response");
Assert.assertEquals(response.jsonPath().getString("token"), "expected-token");
response.then().body("data.size()", equalTo(6));
```

---

## 🔌 Dependencies Used

- [Rest Assured](https://rest-assured.io/)
- [TestNG](https://testng.org/)
- [WireMock](https://wiremock.org/)
- [JSON Schema Validator](https://github.com/rest-assured/rest-assured/wiki/Usage#json-schema-validation)

---

## 🧠 Tips

- Keep mock and real APIs separate via TestNG groups
- Use `@Parameters` for environment and thread count control
- Write custom assertions and validators under `utils/` for reusability

---

## 🤝 Contributing

Feel free to fork this repository and add enhancements for:

- OAuth2 token testing
- Role-based access validation
- GraphQL APIs
- Dynamic data generation

---

## 🏁 Final Note

This framework is designed for **high coverage**, **security-conscious**, and **real-world** API testing including mocking and integration validation. Use it to run in CI/CD, validate microservices contracts, or scale with threads for load-style checks.

---
