import { useState } from "react"
import { useNavigate } from "react-router-dom"
import { cn } from "@/lib/utils"
import { Button } from "@/components/ui/button"
import {
    Card,
    CardContent,
    CardDescription,
    CardHeader,
    CardTitle,
} from "@/components/ui/card"
import {
    Field,
    FieldGroup,
    FieldLabel,
    FieldDescription,
} from "@/components/ui/field"
import { Input } from "@/components/ui/input"
import type { SingleApiResponse, AuthData } from "@/types/api"
import { auth } from "@/lib/auth"
import { toast } from "sonner"

export function LoginForm({
    className,
    ...props
}: React.ComponentProps<"div">) {
    const navigate = useNavigate()
    const [isLoading, setIsLoading] = useState(false)
    const [formData, setFormData] = useState({
        username: "",
        password: "",
    })

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { id, value } = e.target
        setFormData((prev) => ({ ...prev, [id]: value }))
    }

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault()
        setIsLoading(true)

        try {
            const response = await fetch("/api/auth/login", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify(formData),
            })

            const data: SingleApiResponse<AuthData> = await response.json()

            if (!response.ok) {
                throw new Error(data.message || "Login failed")
            }

            auth.login(data.data)
            toast.success("Login successful!")
            navigate("/")
        } catch (error) {
            toast.error(error instanceof Error ? error.message : "Something went wrong")
        } finally {
            setIsLoading(false)
        }
    }

    return (
        <div className={cn("flex flex-col gap-6", className)} {...props}>
            <Card>
                <CardHeader className="text-center">
                    <CardTitle className="text-xl">Welcome back</CardTitle>
                    <CardDescription>
                        Enter your username below to login to your account
                    </CardDescription>
                </CardHeader>
                <CardContent>
                    <form onSubmit={handleSubmit}>
                        <FieldGroup>
                            <Field>
                                <FieldLabel htmlFor="username">Username</FieldLabel>
                                <Input
                                    id="username"
                                    type="text"
                                    placeholder="janedoe"
                                    required
                                    value={formData.username}
                                    onChange={handleChange}
                                    disabled={isLoading}
                                />
                            </Field>
                            <Field>
                                <div className="flex items-center">
                                    <FieldLabel htmlFor="password">Password</FieldLabel>
                                    <a
                                        href="#"
                                        className="ml-auto text-sm underline-offset-4 hover:underline"
                                    >
                                        Forgot your password?
                                    </a>
                                </div>
                                <Input
                                    id="password"
                                    type="password"
                                    required
                                    value={formData.password}
                                    onChange={handleChange}
                                    disabled={isLoading}
                                />
                            </Field>
                            <Field>
                                <Button type="submit" className="w-full" disabled={isLoading}>
                                    {isLoading ? "Logging in..." : "Login"}
                                </Button>
                                <FieldDescription className="text-center">
                                    Don't have an account? <a href="/signup">Sign up</a>
                                </FieldDescription>
                            </Field>
                        </FieldGroup>
                    </form>
                </CardContent>
            </Card>
            <FieldDescription className="px-6 text-center">
                By clicking continue, you agree to our <a href="#">Terms of Service</a>{" "}
                and <a href="#">Privacy Policy</a>.
            </FieldDescription>
        </div>
    )
}
