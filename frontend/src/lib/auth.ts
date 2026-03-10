import type { AuthData } from "@/types/api";

const AUTH_KEY = "smecs_auth";

export const auth = {
    login: (data: AuthData) => {
        localStorage.setItem(AUTH_KEY, JSON.stringify(data));
        window.dispatchEvent(new Event("auth-change"));
    },

    logout: async () => {
        const user = auth.getUser();
        try {
            await fetch("/api/auth/logout", {
                method: "POST",
                credentials: "include",
                headers: {
                    ...(user?.token ? { Authorization: `Bearer ${user.token}` } : {}),
                },
            });
        } catch {
            // Local cleanup still happens even if logout revocation fails.
        }
        localStorage.removeItem(AUTH_KEY);
        window.dispatchEvent(new Event("auth-change"));
    },

    getUser: (): AuthData | null => {
        const data = localStorage.getItem(AUTH_KEY);
        if (!data) return null;
        try {
            return JSON.parse(data) as AuthData;
        } catch {
            return null;
        }
    },

    isAuthenticated: (): boolean => {
        const user = auth.getUser();
        if (!user) return false;

        // If authExpiry is present, check if it's still valid
        if (user.authExpiry) {
            return Date.now() < user.authExpiry;
        }

        // If no expiry, user is authenticated if data exists
        return true;
    },

    authHeaders: (): Record<string, string> => {
        const user = auth.getUser();
        return user?.token ? { Authorization: `Bearer ${user.token}` } : {};
    }
};
