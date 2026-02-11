import { useState, useEffect } from "react";
import { auth } from "@/lib/auth";
import type { AuthData } from "@/types/api";

export function useAuth() {
    const [user, setUser] = useState<AuthData | null>(auth.getUser());

    useEffect(() => {
        const handleAuthChange = () => {
            setUser(auth.getUser());
        };

        window.addEventListener("auth-change", handleAuthChange);
        return () => window.removeEventListener("auth-change", handleAuthChange);
    }, []);

    return {
        user,
        isAuthenticated: !!user,
        isAdmin: user?.role === "admin",
        logout: auth.logout,
    };
}
