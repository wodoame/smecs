import { useCart } from "@/hooks/use-cart";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";
import { Separator } from "@/components/ui/separator";
import { Trash2 } from "lucide-react";

export default function CartPage() {
    const { cartItems, removeFromCart, updateQuantity, clearCart } = useCart();

    const total = cartItems.reduce((acc, item) => acc + item.price * item.quantity, 0);

    if (cartItems.length === 0) {
        return (
            <div className="flex flex-col items-center justify-center h-[50vh] gap-4">
                <h2 className="text-2xl font-bold">Your Cart is Empty</h2>
                <p className="text-muted-foreground">Looks like you haven't added anything yet.</p>
                <Button onClick={() => window.location.href = '/products'}>Browse Products</Button>
            </div>
        );
    }

    return (
        <div className="container mx-auto max-w-4xl py-6 space-y-8">
            <h1 className="text-3xl font-bold">Shopping Cart</h1>

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
                            <Button className="w-full" size="lg">Checkout</Button>
                        </CardFooter>
                    </Card>
                </div>
            </div>
        </div>
    );
}
