import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { useCart } from "@/hooks/use-cart";
import { toast } from "sonner";
import { ArrowLeft } from "lucide-react";

interface Product {
    id: number;
    name: string;
    description: string;
    price: number;
    image: string;
}

export default function ProductDetailsPage() {
    const { productId } = useParams<{ productId: string }>();
    const navigate = useNavigate();
    const { addToCart } = useCart();

    const [product, setProduct] = useState<Product | null>(null);
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        const fetchProduct = async () => {
            if (!productId) return;
            try {
                // Assuming standard endpoint pattern based on plan
                const response = await fetch(`/api/products/${productId}`);
                if (response.ok) {
                    const data = await response.json();
                    // Handle wrapped response if api uses standard wrapper, or direct data
                    // Based on previous files, response structure is typically { data: ... }
                    const apiData = data.data || data;
                    setProduct({
                        ...apiData,
                        image: apiData.imageUrl || apiData.image,
                        // Ensure price is number if needed, though most likely number from API
                        // If api returns "name" as null, handle it? ProductList does `name ?? "Unnamed"`
                        name: apiData.name ?? "Unnamed Product"
                    });
                } else {
                    toast.error("Failed to load product");
                }
            } catch (error) {
                console.error("Error loading product", error);
                toast.error("Error loading product");
            } finally {
                setIsLoading(false);
            }
        };

        fetchProduct();
    }, [productId]);

    if (isLoading) {
        return <div className="p-8 text-center">Loading...</div>;
    }

    if (!product) {
        return (
            <div className="p-8 text-center">
                <h2 className="text-xl font-bold">Product not found</h2>
                <Button onClick={() => navigate("/products")} className="mt-4">
                    Back to Products
                </Button>
            </div>
        );
    }

    return (
        <div className="container mx-auto px-4 py-8">
            <Button
                variant="ghost"
                className="mb-6 pl-0 hover:pl-2 transition-all"
                onClick={() => navigate("/products")}
            >
                <ArrowLeft className="mr-2 h-4 w-4" />
                Back to Products
            </Button>

            <div className="grid md:grid-cols-2 gap-8 lg:gap-12">
                <div className="rounded-lg overflow-hidden bg-white dark:bg-zinc-900 border border-zinc-200 dark:border-zinc-800">
                    <img
                        src={product.image}
                        alt={product.name}
                        className="w-full h-auto object-cover aspect-video md:aspect-square"
                    />
                </div>

                <div className="flex flex-col">
                    <h1 className="text-3xl font-bold tracking-tight mb-2">{product.name}</h1>
                    <div className="text-2xl font-semibold mb-6">${typeof product.price === 'number' ? product.price.toFixed(2) : product.price}</div>

                    <div className="prose dark:prose-invert mb-8">
                        <p>{product.description}</p>
                    </div>

                    <div className="mt-auto">
                        <Button
                            size="lg"
                            className="w-full md:w-auto min-w-[200px] bg-yellow-500 hover:bg-yellow-600 text-black font-medium"
                            onClick={() => addToCart({
                                id: product.id,
                                name: product.name,
                                price: typeof product.price === 'string' ? parseFloat(product.price) : product.price,
                                image: product.image
                            })}
                        >
                            Add to Cart
                        </Button>
                    </div>
                </div>
            </div>
        </div>
    );
}
