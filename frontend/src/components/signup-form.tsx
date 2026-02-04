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
    FieldDescription,
    FieldGroup,
    FieldLabel,
} from "@/components/ui/field"
import { Input } from "@/components/ui/input"
import type { RegisterPayload, SingleApiResponse, AuthData } from "@/types/api"
import { toast } from "sonner"

export function SignupForm({
    className,
    ...props
}: React.ComponentProps<"div">) {
    const navigate = useNavigate()
    const [isLoading, setIsLoading] = useState(false)
    const [formData, setFormData] = useState({
        name: "", // mapped to username as per payload requirement if name is username, or we adjust
        email: "",
        password: "",
        confirmPassword: "",
    })

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { id, value } = e.target
        setFormData((prev) => ({ ...prev, [id]: value }))
    }

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault()

        if (formData.password !== formData.confirmPassword) {
            toast.error("Passwords do not match")
            return
        }

        setIsLoading(true)

        try {
            const payload: RegisterPayload = {
                username: formData.name, // Using 'name' input as username
                email: formData.email,
                password: formData.password,
            }

            const response = await fetch("/api/auth/register", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify(payload),
            })

            const data: SingleApiResponse<AuthData> = await response.json()

            if (!response.ok) {
                throw new Error(data.message || "Registration failed")
            }

            toast.success("Registration successful! Please login.")
            navigate("/login")
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
                    <CardTitle className="text-xl">Create your account</CardTitle>
                    <CardDescription>
                        Enter your email below to create your account
                    </CardDescription>
                </CardHeader>
                <CardContent>
                    <form onSubmit={handleSubmit}>
                        <FieldGroup>
                            <Field>
                                <FieldLabel htmlFor="name">Username</FieldLabel>
                                <Input
                                    id="name"
                                    type="text"
                                    placeholder="janedoe"
                                    required
                                    value={formData.name}
                                    onChange={handleChange}
                                    disabled={isLoading}
                                />
                            </Field>
                            <Field>
                                <FieldLabel htmlFor="email">Email</FieldLabel>
                                <Input
                                    id="email"
                                    type="email"
                                    placeholder="janedoe@example.com"
                                    required
                                    value={formData.email}
                                    onChange={handleChange}
                                    disabled={isLoading}
                                />
                            </Field>
                            <Field>
                                <Field className="grid grid-cols-2 gap-4">
                                    <Field>
                                        <FieldLabel htmlFor="password">Password</FieldLabel>
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
                                        <FieldLabel htmlFor="confirmPassword">
                                            Confirm Password
                                        </FieldLabel>
                                        <Input
                                            id="confirmPassword" // Changed ID to match state key
                                            type="password"
                                            required
                                            value={formData.confirmPassword}
                                            onChange={handleChange}
                                            disabled={isLoading}
                                        />
                                    </Field>
                                </Field>
                                <FieldDescription>
                                    Must be at least 8 characters long.
                                </FieldDescription>
                            </Field>
                            <Field>
                                <Button type="submit" className="w-full" disabled={isLoading}>
                                    {isLoading ? "Creating Account..." : "Create Account"}
                                </Button>
                                <FieldDescription className="text-center">
                                    Already have an account? <a href="/login">Sign in</a>
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
