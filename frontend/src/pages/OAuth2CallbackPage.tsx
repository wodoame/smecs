import { useEffect, useRef } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { auth } from "@/lib/auth";
import type { AuthData } from "@/types/api";

/**
 * Landing page for the Google OAuth2 redirect.
 *
 * Spring Security redirects here after a successful Google login:
 *   /oauth2/callback?token=<jwt>&cartId=<id>&next=<path>
 *
 * This page reads those params, stores the session via auth.login(),
 * then navigates the user to their intended destination.
 */
export default function OAuth2CallbackPage() {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const handled = useRef(false);

    useEffect(() => {
        // Guard against React StrictMode double-invocation
        if (handled.current) return;
        handled.current = true;

        const token  = searchParams.get("token");
        const cartId = searchParams.get("cartId");
        const next   = searchParams.get("next") || "/";

        if (!token) {
            // Something went wrong on the backend — send to login with an error flag
            navigate("/login?error=oauth_failed", { replace: true });
            return;
        }

        // Decode the JWT payload to extract user claims (no signature check needed
        // here — the server already validated the token before issuing it)
        try {
            const payload = JSON.parse(atob(token.split(".")[1]));

            const authData: AuthData = {
                token,
                id:     payload.id,
                username: payload.sub,
                email:  payload.email,
                role:   payload.role,
                cartId: cartId ? Number(cartId) : 0,
            };

            auth.login(authData);
            navigate(next, { replace: true });
        } catch {
            navigate("/login?error=oauth_failed", { replace: true });
        }
    }, [navigate, searchParams]);

    return (
        <div className="flex items-center justify-center h-svh">
            <div className="text-center">
                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto mb-4" />
                <p className="text-muted-foreground">Completing sign-in…</p>
            </div>
        </div>
    );
}

