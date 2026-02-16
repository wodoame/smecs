import { useState, useEffect, useCallback } from "react";
import { toast } from "sonner";
import { auth } from "@/lib/auth";

export interface CartItem {
    id: number;
    cartItemId?: number;
    name: string;
    price: number;
    image: string;
    quantity: number;
}

interface CartOperations {
    cartItems: CartItem[];
    authError: 'unauthorized' | 'forbidden' | null;
    addToCart: (product: { id: number; name: string; price: number; image: string }) => Promise<void> | void;
    removeFromCart: (id: number) => Promise<void> | void;
    updateQuantity: (id: number, quantity: number) => Promise<void> | void;
    clearCart: () => Promise<void> | void;
}

export function useCart(): CartOperations {
    const [cartItems, setCartItems] = useState<CartItem[]>([]);
    const [cartId, setCartId] = useState<number | null>(null);
    const [authError, setAuthError] = useState<'unauthorized' | 'forbidden' | null>(null);

    const fetchCartItems = useCallback(async (id: number) => {
        const user = auth.getUser();

        try {
            const response = await fetch(`/api/cart-items/cart/${id}`, {
                headers: {
                    ...(user?.token && { "Authorization": `Bearer ${user.token}` })
                }
            });

            if (response.status === 401) {
                setAuthError('unauthorized');
                setCartItems([]);
                return;
            }

            if (response.status === 403) {
                setAuthError('forbidden');
                setCartItems([]);
                return;
            }

            if (response.ok) {
                setAuthError(null);
                const json = await response.json();
                const items = json.data || [];
                const mappedItems: CartItem[] = items.map((item: any) => ({
                    id: item.productId,
                    cartItemId: item.cartItemId,
                    name: item.productName,
                    price: item.price,
                    image: item.productImage,
                    quantity: item.quantity
                }));
                setCartItems(mappedItems);
            }
        } catch (error) {
            console.error("Failed to fetch cart items", error);
        }
    }, []);

    const initCart = useCallback(async () => {
        const user = auth.getUser();

        // If no user is logged in, show unauthorized error
        if (!user) {
            setAuthError('unauthorized');
            setCartItems([]);
            setCartId(null);
            return;
        }

        if (user.cartId) {
            setCartId(user.cartId);
            setAuthError(null);
            await fetchCartItems(user.cartId);
        } else {
            setCartItems([]);
            setCartId(null);
            setAuthError(null);
        }
    }, [fetchCartItems]);

    useEffect(() => {
        initCart();

        // Listen for auth changes to re-initialize or clear cart
        const handleAuthChange = () => {
            initCart();
        };

        window.addEventListener("auth-change", handleAuthChange);
        return () => window.removeEventListener("auth-change", handleAuthChange);
    }, [initCart]);

    const refreshCart = async () => {
        if (!cartId) return;
        await fetchCartItems(cartId);
    };

    const addToCart = async (product: { id: number; name: string; price: number; image: string }) => {
        const user = auth.getUser();
        if (!user) {
            toast.error("You must be logged in to add items to cart");
            return;
        }

        const currentCartId = cartId || user.cartId;

        if (!currentCartId) {
            toast.error("Cart not available");
            return;
        }

        if (!cartId) setCartId(currentCartId);

        try {
            const response = await fetch("/api/cart-items", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${user.token}`
                },
                body: JSON.stringify({
                    cartId: currentCartId,
                    productId: product.id,
                    quantity: 1
                })
            });

            if (response.ok) {
                await refreshCart();
                toast.success(`Added ${product.name} to cart`);
            } else {
                toast.error("Failed to add to cart");
            }
        } catch (error) {
            toast.error("Failed to add to cart");
        }
    };

    const removeFromCart = async (id: number) => {
        const user = auth.getUser();
        if (!user) return;

        const itemToRemove = cartItems.find(item => item.id === id);
        const targetId = itemToRemove?.cartItemId;

        if (!targetId) return;

        try {
            const response = await fetch(`/api/cart-items/${targetId}`, {
                method: "DELETE",
                headers: {
                    "Authorization": `Bearer ${user.token}`
                }
            });
            if (response.ok) await refreshCart();
            else toast.error("Failed to remove item");
        } catch (error) {
            toast.error("Failed to remove item");
        }
    };

    const updateQuantity = async (id: number, quantity: number) => {
        if (quantity < 1) return;
        const user = auth.getUser();
        if (!user) return;

        const itemToUpdate = cartItems.find(item => item.id === id);
        const targetId = itemToUpdate?.cartItemId;
        if (!targetId) return;

        try {
            const response = await fetch(`/api/cart-items/${targetId}`, {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${user.token}`
                },
                body: JSON.stringify({
                    quantity
                })
            });
            if (response.ok) await refreshCart();
        } catch (error) {
            console.error("Failed to update quantity");
        }
    };

    const clearCart = async () => {
        if (!cartId) return;
        const user = auth.getUser();
        if (!user) return;

        try {
            const response = await fetch(`/api/carts/${cartId}/clear`, {
                method: "DELETE",
                headers: {
                    "Authorization": `Bearer ${user.token}`
                }
            });
            if (response.ok) {
                setCartItems([]);
                toast.success("Cart cleared successfully");
            } else {
                toast.error("Failed to clear cart");
            }
        } catch (e) {
            console.error("Failed to clear cart", e);
            toast.error("An error occurred while clearing the cart");
        }
    };

    return { cartItems, authError, addToCart, removeFromCart, updateQuantity, clearCart };
}
