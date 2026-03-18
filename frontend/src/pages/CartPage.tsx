import { useState } from "react";
import { useCart } from "@/hooks/use-cart";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";
import {
    Empty,
    EmptyContent,
    EmptyDescription,
    EmptyHeader,
    EmptyMedia,
    EmptyTitle,
} from "@/components/ui/empty";
import { Separator } from "@/components/ui/separator";
import { Trash2, LogIn, ShieldAlert, CircleCheck, ShoppingCart, Loader2 } from "lucide-react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { toast } from "sonner";
import { auth } from "@/lib/auth";

export default function CartPage() {
    const location = useLocation();
    const navigate = useNavigate();
    const { cartItems, authError, removeFromCart, updateQuantity, clearCart } = useCart();
    const [checkoutLoading, setCheckoutLoading] = useState(false);

    const total = cartItems.reduce((acc, item) => acc + item.price * item.quantity, 0);

    const handleCheckout = async () => {
        const user = auth.getUser();
        if (!user) {
            toast.error("You must be logged in to checkout");
            return;
        }

        if (cartItems.length === 0) {
            toast.error("Your cart is empty");
            return;
        }

        setCheckoutLoading(true);

        try {
            // The backend derives order items from the user's cart; do not send cart items in the request body.
            // Send an empty payload to satisfy the controller's @RequestBody without duplicating cart data.
            const response = await fetch("/api/order-items", {
                method: "POST",
                credentials: "include",
                headers: {
                    "Content-Type": "application/json",
                    ...auth.authHeaders(),
                },
            });

            if (!response.ok) {
                throw new Error("Failed to complete checkout");
            }

            // Step 2: Clear the cart
            await clearCart();

            // Step 3: Show success message and redirect
            toast.success("Order placed successfully!", {
                icon: <CircleCheck className="h-5 w-5 text-green-500" />,
            });

            // Redirect to orders page
            navigate("/orders");
        } catch (error) {
            console.error("Checkout error:", error);
            toast.error("Failed to complete checkout. Please try again.");
        } finally {
            setCheckoutLoading(false);
        }
    };



    // Show error screens for auth issues
    if (authError === 'unauthorized') {
        const loginUrl = `/login?next=${encodeURIComponent(location.pathname)}`;
        return (
            <div className="flex flex-col items-center justify-center h-[70vh] gap-4">
                <LogIn className="h-16 w-16 text-muted-foreground" />
                <h2 className="text-2xl font-bold">Unauthorized</h2>
                <p className="text-muted-foreground">You need to be logged in to view your cart.</p>
                <Button asChild>
                    <Link to={loginUrl}>Go to Login</Link>
                </Button>
            </div>
        );
    }

    if (authError === 'forbidden') {
        return (
            <div className="flex flex-col items-center justify-center h-[70vh] gap-4">
                <ShieldAlert className="h-16 w-16 text-destructive" />
                <h2 className="text-2xl font-bold">Access Forbidden</h2>
                <p className="text-muted-foreground">You don't have permission to access this cart.</p>
                <p className="text-sm text-muted-foreground">Please contact an administrator if you believe this is an error.</p>
            </div>
        );
    }

    if (cartItems.length === 0) {
        return (
            <div className="container mx-auto max-w-3xl py-10">
                <Empty className="min-h-[50vh] border-none">
                    <EmptyHeader>
                        <EmptyMedia variant="icon">
                            <ShoppingCart />
                        </EmptyMedia>
                        <EmptyTitle>Your Cart is Empty</EmptyTitle>
                        <EmptyDescription>
                            Looks like you haven&apos;t added anything yet.
                        </EmptyDescription>
                    </EmptyHeader>
                    <EmptyContent className="flex-row justify-center">
                        <Button asChild>
                            <Link to="/products">Browse Products</Link>
                        </Button>
                    </EmptyContent>
                </Empty>
            </div>
        );
    }

    return (
        <div className="container mx-auto max-w-4xl py-6 space-y-8">
            <h1 className="text-3xl font-bold">My Cart</h1>

            <div className="grid gap-8 md:grid-cols-3">
                <div className="md:col-span-2 space-y-4">
                    {cartItems.map((item) => (
                        <Card key={item.id} className="flex flex-row items-center p-4 gap-4">
                            {/* Image placeholder or actual image if available */}
                            <div className="h-24 w-24 shrink-0 overflow-hidden rounded-md border">
                                {item.image ? (
                                    <img src={item.image} alt={item.name} className="h-full w-full object-cover" />
                                ) : (
                                    <div className="h-full w-full bg-muted flex items-center justify-center text-xs">No Image</div>
                                )}
                            </div>

                            <div className="flex-1 space-y-1">
                                <h3 className="font-semibold">{item.name}</h3>
                                <p className="text-sm text-muted-foreground">${item.price.toFixed(2)}</p>
                            </div>

                            <div className="flex items-center gap-2">
                                <Button
                                    variant="outline"
                                    size="icon"
                                    className="h-8 w-8"
                                    onClick={() => updateQuantity(item.id, item.quantity - 1)}
                                    disabled={item.quantity <= 1}
                                >
                                    -
                                </Button>
                                <span className="w-8 text-center">{item.quantity}</span>
                                <Button
                                    variant="outline"
                                    size="icon"
                                    className="h-8 w-8"
                                    onClick={() => updateQuantity(item.id, item.quantity + 1)}
                                >
                                    +
                                </Button>
                            </div>

                            <Button
                                variant="ghost"
                                size="icon"
                                className="text-destructive hover:text-destructive/90 hover:bg-destructive/10"
                                onClick={() => removeFromCart(item.id)}
                            >
                                <Trash2 className="h-4 w-4" />
                            </Button>
                        </Card>
                    ))}
                    <Button variant="outline" onClick={clearCart} className="w-full">
                        Clear Cart
                    </Button>
                </div>

                <div>
                    <Card>
                        <CardHeader>
                            <CardTitle>Order Summary</CardTitle>
                        </CardHeader>
                        <CardContent className="space-y-4">
                            <div className="flex justify-between">
                                <span className="text-muted-foreground">Subtotal</span>
                                <span>${total.toFixed(2)}</span>
                            </div>
                            <Separator />
                            <div className="flex justify-between font-bold text-lg">
                                <span>Total</span>
                                <span>${total.toFixed(2)}</span>
                            </div>
                        </CardContent>
                        <CardFooter>
                            <Button
                                className="w-full"
                                size="lg"
                                onClick={handleCheckout}
                                disabled={checkoutLoading}
                            >
                                {checkoutLoading ? (
                                    <>
                                        <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                                        Processing...
                                    </>
                                ) : (
                                    "Checkout"
                                )}
                            </Button>
                        </CardFooter>
                    </Card>
                </div>
            </div>
        </div>
    );
}
