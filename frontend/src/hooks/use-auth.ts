import { useState, useEffect, useCallback } from "react";
import { auth } from "@/lib/auth";
import type { AuthData } from "@/types/api";

export function useAuth() {
    const [user, setUser] = useState<AuthData | null>(auth.getUser());

    // Only update state when the underlying data actually changes so that
    // re-dispatching "auth-change" with the same payload doesn't produce a
    // new object reference and retrigger dependent effects (e.g. ProtectedRoute).
    const syncUser = useCallback(() => {
        const next = auth.getUser();
        setUser(prev => {
            if (JSON.stringify(prev) === JSON.stringify(next)) return prev;
            return next;
        });
    }, []);

    useEffect(() => {
        window.addEventListener("auth-change", syncUser);
        return () => window.removeEventListener("auth-change", syncUser);
    }, [syncUser]);

    return {
        user,
        isAuthenticated: !!user,
        isAdmin: user?.role === "admin",
        logout: auth.logout,
    };
}
