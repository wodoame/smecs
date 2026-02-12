import { useAuth } from "@/hooks/use-auth";
import { ShieldAlert } from "lucide-react";
import { ProtectedRoute } from "./ProtectedRoute";

interface AdminRouteProps {
    children: React.ReactNode;
}

export function AdminRoute({ children }: AdminRouteProps) {
    const { isAdmin } = useAuth();

    return (
        <ProtectedRoute>
            {isAdmin ? (
                <>{children}</>
            ) : (
                <div className="flex flex-col items-center justify-center h-[70vh] gap-4">
                    <ShieldAlert className="h-16 w-16 text-destructive" />
                    <h2 className="text-2xl font-bold">Access Forbidden</h2>
                    <p className="text-muted-foreground">You don't have permission to access this page.</p>
                    <p className="text-sm text-muted-foreground">Please contact an administrator if you believe this is an error.</p>
                </div>
            )}
        </ProtectedRoute>
    );
}
