import { Navigate, useLocation } from "react-router-dom";
import { useAuth } from "@/hooks/use-auth";
import { useEffect, useState } from "react";
import { auth } from "@/lib/auth";

interface ProtectedRouteProps {
    children: React.ReactNode;
}

export function ProtectedRoute({ children }: ProtectedRouteProps) {
    const { user } = useAuth();
    const location = useLocation();
    const [isVerifying, setIsVerifying] = useState(true);
    const [isValid, setIsValid] = useState(false);

    useEffect(() => {
        // If no user in localStorage, skip verification
        if (!user) {
            setIsVerifying(false);
            return;
        }

        // Verify token with server
        const verifyToken = async () => {
            try {
                const response = await fetch("/api/auth/verify", {
                    method: "GET",
                    headers: {
                        "Authorization": `Bearer ${user.token}`,
                        "Content-Type": "application/json",
                    },
                });

                if (response.ok) {
                    setIsValid(true);
                } else {
                    // Token is invalid, clear localStorage
                    auth.logout();
                    setIsValid(false);
                }
            } catch (error) {
                console.error("Token verification failed:", error);
                auth.logout();
                setIsValid(false);
            } finally {
                setIsVerifying(false);
            }
        };

        verifyToken();
    }, [user]);

    // Show loading state while verifying
    if (isVerifying) {
        return (
            <div className="flex items-center justify-center h-[70vh]">
                <div className="text-center">
                    <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto mb-4"></div>
                    <p className="text-muted-foreground">Verifying authentication...</p>
                </div>
            </div>
        );
    }

    // Redirect to login if not authenticated or token is invalid
    if (!user || !isValid) {
        return <Navigate to={`/login?next=${encodeURIComponent(location.pathname)}`} replace />;
    }

    // Render children if authenticated and verified
    return <>{children}</>;
}
