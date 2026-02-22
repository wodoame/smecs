import { LoginForm } from "@/components/login-form";
import { GalleryVerticalEnd } from "lucide-react";
import { Toaster } from "sonner";
import { Link } from "react-router-dom";

export default function LoginPage() {
    return (
        <div className="bg-muted flex min-h-svh flex-col items-center justify-center p-6 md:p-10">
            <Toaster />
            <div className="flex w-full max-w-sm flex-col gap-6">
                <Link to="/products" className="flex items-center gap-2 self-center font-medium">
                    <div className="bg-primary text-primary-foreground flex size-6 items-center justify-center rounded-md">
                        <GalleryVerticalEnd className="size-4" />
                    </div>
                    SMECS
                </Link>
                <LoginForm />
            </div>
        </div>
    );
}
