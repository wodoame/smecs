import { useEffect, useRef } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { auth } from "@/lib/auth";
import type { AuthData } from "@/types/api";

/**
 * Landing page for the Google OAuth2 redirect.
 *
 * Spring Security redirects here after a successful Google login:
 *   /oauth2/callback?next=<path>
 *
 * The JWT is delivered in an HttpOnly cookie, so the SPA fetches the
 * authenticated user profile and then restores the original route.
 */
export default function OAuth2CallbackPage() {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const handled = useRef(false);

    useEffect(() => {
        // Guard against React StrictMode double-invocation
        if (handled.current) return;
        handled.current = true;

        const completeOAuthLogin = async () => {
            try {
                const next = searchParams.get("next") || "/";
                const response = await fetch("/api/auth/me", {
                    credentials: "include",
                });

                if (!response.ok) {
                    navigate("/login?error=oauth_failed", { replace: true });
                    return;
                }

                const payload = await response.json();
                const user = payload.data;
                const authData: AuthData = {
                    id: user.id,
                    username: user.username,
                    email: user.email,
                    role: user.role,
                };

                auth.login(authData);
                navigate(next, { replace: true });
            } catch {
                navigate("/login?error=oauth_failed", { replace: true });
            }
        };

        void completeOAuthLogin();
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
