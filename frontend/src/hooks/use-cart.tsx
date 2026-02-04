import { useState, useEffect } from "react";
import { toast } from "sonner";

export interface CartItem {
    id: number;
    name: string;
    price: number;
    image: string;
    quantity: number;
}

export function useCart() {
    const [cartItems, setCartItems] = useState<CartItem[]>([]);

    // Load cart from localStorage on mount
    useEffect(() => {
        const storedCart = localStorage.getItem("cart");
        if (storedCart) {
            try {
                setCartItems(JSON.parse(storedCart));
            } catch (error) {
                console.error("Failed to parse cart from localStorage", error);
            }
        }
    }, []);

    // Save cart to localStorage whenever it changes
    useEffect(() => {
        // We only save if cartItems has been initialized or changed. 
        // However, to avoid overwriting with empty array on initial mount before read, 
        // we should be careful. But since the read happens on mount, and this effect runs on cartItems change,
        // we might need a ref to track if initial load happened? 
        // simpler: just save. React state updates are batched. 
        // Better: write a separate function to save, and manual updates.
        // For simplicity with this hook pattern:
        if (cartItems.length > 0) {
            localStorage.setItem("cart", JSON.stringify(cartItems));
        } else {
            // If we clear the cart, we want to update localStorage too.
            // Check if we have read from LS yet? 
            // Let's rely on the fact that if it's empty initially, we overwrite with empty. 
            // Actually, the first useEffect runs after render.
        }
        // Let's do explicit save in the modifier functions to be safer and avoid sync issues.
    }, [cartItems]);

    const saveToLocalStorage = (items: CartItem[]) => {
        localStorage.setItem("cart", JSON.stringify(items));
    };

    const addToCart = (product: { id: number; name: string; price: number; image: string }) => {
        setCartItems((prev) => {
            const existingItem = prev.find((item) => item.id === product.id);
            let newItems;
            if (existingItem) {
                newItems = prev.map((item) =>
                    item.id === product.id ? { ...item, quantity: item.quantity + 1 } : item
                );
            } else {
                newItems = [...prev, { ...product, quantity: 1 }];
            }
            saveToLocalStorage(newItems);
            toast.success(`Added ${product.name} to cart`);
            return newItems;
        });
    };

    const removeFromCart = (id: number) => {
        setCartItems((prev) => {
            const newItems = prev.filter((item) => item.id !== id);
            saveToLocalStorage(newItems);
            return newItems;
        });
    };

    const updateQuantity = (id: number, quantity: number) => {
        if (quantity < 1) return;
        setCartItems((prev) => {
            const newItems = prev.map(item => item.id === id ? { ...item, quantity } : item);
            saveToLocalStorage(newItems);
            return newItems;
        });
    }

    const clearCart = () => {
        setCartItems([]);
        saveToLocalStorage([]);
    };

    return {
        cartItems,
        addToCart,
        removeFromCart,
        updateQuantity,
        clearCart,
    };
}
