import { useState, useEffect } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Separator } from "@/components/ui/separator";
import { LogIn, ShieldAlert, PackageOpen, ArrowLeft } from "lucide-react";
import { Link, useLocation, useParams, useNavigate } from "react-router-dom";
import { toast } from "sonner";
import { auth } from "@/lib/auth";
import { Button } from "@/components/ui/button";

interface OrderItem {
    id: number;
    orderId: number;
    productId: number;
    quantity: number;
    price: number;
    productName?: string;
    productImage?: string;
}

export default function OrderItemsPage() {
    const { orderId } = useParams();
    const location = useLocation();
    const navigate = useNavigate();
    const [items, setItems] = useState<OrderItem[]>([]);
    const [loading, setLoading] = useState(true);
    const [authError, setAuthError] = useState<'unauthorized' | 'forbidden' | null>(null);

    const total = items.reduce((acc, item) => acc + (item.price * item.quantity), 0);
    const isAdminRoute = location.pathname.startsWith('/admin');

    useEffect(() => {
        const fetchOrderItems = async () => {
            const user = auth.getUser();
            if (!user) {
                setAuthError('unauthorized');
                setLoading(false);
                return;
            }

            try {
                // Fetch the order items
                const res = await fetch(`/api/orderitems/order/${orderId}`, {
                    headers: {
                        "Authorization": `Bearer ${user.token}`,
                    }
                });

                if (res.status === 401) {
                    setAuthError('unauthorized');
                    throw new Error("Unauthorized - Please log in");
                }
                if (res.status === 403) {
                    setAuthError('forbidden');
                    throw new Error("Forbidden - You don't have permission to access this resource");
                }
                if (!res.ok) throw new Error("Failed to fetch order items");

                const payload = await res.json();
                let fetchedItems: OrderItem[] = payload.content || payload.data || [];

                // Fetch product details for each item to get names and images
                const itemsWithDetails = await Promise.all(
                    fetchedItems.map(async (item) => {
                        try {
                            const productRes = await fetch(`/api/products/${item.productId}`);
                            if (productRes.ok) {
                                const pData = await productRes.json();
                                const product = pData.data;
                                return {
                                    ...item,
                                    productName: product.name,
                                    productImage: product.imageUrl || product.image
                                };
                            }
                        } catch (e) {
                            console.error(`Failed to fetch product details for ${item.productId}`, e);
                        }
                        return { ...item, productName: `Product #${item.productId}` };
                    })
                );

                setItems(itemsWithDetails);
            } catch (err) {
                console.error("Error fetching order items:", err);
                if (!authError) {
                    toast.error("Failed to load order items");
                }
            } finally {
                setLoading(false);
            }
        };

        if (orderId) {
            fetchOrderItems();
        }
    }, [orderId]);


    if (authError === 'unauthorized') {
        const loginUrl = `/login?next=${encodeURIComponent(location.pathname)}`;
        return (
            <div className="flex flex-col items-center justify-center h-[70vh] gap-4">
                <LogIn className="h-16 w-16 text-muted-foreground" />
                <h2 className="text-2xl font-bold">Unauthorized</h2>
                <p className="text-muted-foreground">You need to be logged in to view order details.</p>
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
                <p className="text-muted-foreground">You don't have permission to view these order details.</p>
            </div>
        );
    }

    if (loading) {
        return <div className="flex justify-center items-center h-[50vh]">Loading order items...</div>;
    }

    if (items.length === 0) {
        return (
            <div className="flex flex-col items-center justify-center h-[50vh] gap-4">
                <PackageOpen className="h-16 w-16 text-muted-foreground" />
                <h2 className="text-2xl font-bold">No Items Found</h2>
                <p className="text-muted-foreground">There are no items associated with this order.</p>
                <Button onClick={() => navigate(isAdminRoute ? '/admin/orders' : '/orders')}>Return to Orders</Button>
            </div>
        );
    }

    return (
        <div className="container mx-auto max-w-4xl py-6 space-y-8">
            <div className="flex items-center gap-4">
                <Button variant="ghost" size="icon" onClick={() => navigate(isAdminRoute ? '/admin/orders' : '/orders')}>
                    <ArrowLeft className="h-5 w-5" />
                </Button>
                <div>
                    <h1 className="text-3xl font-bold">Order #{orderId} Items</h1>
                    <p className="text-muted-foreground">Review the items purchased in this order.</p>
                </div>
            </div>


            <div className="grid gap-8 md:grid-cols-3">
                <div className="md:col-span-2 space-y-4">
                    {items.map((item) => (
                        <Card key={item.id} className="flex flex-row items-center p-4 gap-4">
                            <div className="h-24 w-24 shrink-0 overflow-hidden rounded-md border">
                                {item.productImage ? (
                                    <img src={item.productImage} alt={item.productName} className="h-full w-full object-cover" />
                                ) : (
                                    <div className="h-full w-full bg-muted flex items-center justify-center text-xs">No Image</div>
                                )}
                            </div>

                            <div className="flex-1 space-y-1">
                                <h3 className="font-semibold">{item.productName}</h3>
                                <p className="text-sm text-muted-foreground">${item.price.toFixed(2)}</p>
                            </div>

                            <div className="flex items-center gap-2 pr-4">
                                <span className="text-muted-foreground text-sm">Qty:</span>
                                <span className="font-medium text-lg">{item.quantity}</span>
                            </div>

                            <div className="flex flex-col items-end min-w-24">
                                <span className="text-muted-foreground text-xs uppercase tracking-wider mb-1">Subtotal</span>
                                <span className="font-semibold">${(item.price * item.quantity).toFixed(2)}</span>
                            </div>

                        </Card>
                    ))}
                </div>

                <div>
                    <Card className="sticky top-6">
                        <CardHeader>
                            <CardTitle>Order Summary</CardTitle>
                        </CardHeader>
                        <CardContent className="space-y-4">
                            <div className="flex justify-between">
                                <span className="text-muted-foreground">Items Subtotal</span>
                                <span>${total.toFixed(2)}</span>
                            </div>
                            <Separator />
                            <div className="flex justify-between font-bold text-lg">
                                <span>Total Paid</span>
                                <span>${total.toFixed(2)}</span>
                            </div>
                        </CardContent>
                    </Card>
                </div>
            </div>
        </div>
    );
}
