# CSRF vs CORS in SMECS

## Why this guide exists
SMECS uses stateless JWT auth for API calls and supports OAuth2 login. This guide explains what CSRF and CORS protect against, how they differ, and how to verify behavior using Postman and the browser.

## CSRF vs CORS (short version)
- CSRF is a server-side defense against cross-site requests that reuse a logged-in browser session cookie.
- CORS is a browser-enforced policy that controls which origins can call your API with credentials.
- CSRF protects server state when cookies are used for auth; CORS controls which browsers are allowed to make requests.

## SMECS-specific context
- JWT API requests are stateless and do not rely on session cookies, so CSRF checks are ignored for API paths.
- See `src/main/java/com/smecs/config/SecurityConfig.java` where CSRF ignores `/api/**`, `/graphql`, and OAuth2 callback paths.
- OAuth2 login uses a short-lived server session only for the login redirect flow (PKCE state/nonce); normal API calls still use JWT.

## Practical demonstrations

### Demo A: Postman vs browser (CORS)
Postman does not enforce CORS, browsers do.

1) In Postman, call a public endpoint:
   - `GET http://localhost:8080/api/products`
   - Expected: `200 OK` with data.

2) In the browser console (any page running in the browser):
   - `fetch("http://localhost:8080/api/products")`
   - Expected:
     - If CORS is allowlisted for the browser origin, the request succeeds.
     - If CORS is not allowlisted, the browser blocks the response with a CORS error in the console.

Notes:
- For frontend dev, the common origin is `http://localhost:5173`.
- If CORS is not yet configured, add it in Spring Security or a `WebMvcConfigurer`, then re-run the demo.

### Demo B: CSRF mechanism (illustration endpoint)
SMECS includes a dedicated form endpoint to demonstrate CSRF token flow.

1) Open `GET http://localhost:8080/security/csrf-demo` in your browser.
2) Submit the form normally.
3) Expected: success message appears (hidden CSRF input is sent).
4) Remove the hidden `_csrf` field in browser dev tools and resubmit.
5) Expected: request is rejected with `403 Forbidden`.

## Quick reference
- Use CSRF protection when your API relies on session cookies in browsers.
- Use CORS configuration to limit which browser origins may call your API.
- JWT + stateless APIs: ignore CSRF for API paths, but CORS still matters for browsers.
