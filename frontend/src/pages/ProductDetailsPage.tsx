import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { useCart } from "@/hooks/use-cart";
import { toast } from "sonner";
import { ArrowLeft, LogIn, MessageSquareText, Sparkles, Star } from "lucide-react";
import { ReviewList } from "@/components/ReviewList";
import { ReviewForm } from "@/components/ReviewForm";
import { auth } from "@/lib/auth";
import { Separator } from "@/components/ui/separator";

interface Product {
    id: number;
    name: string;
    description: string;
    price: number;
    image: string;
}

interface Review {
    id: number;
    userId: number;
    productId: number;
    rating: number;
    comment: string;
    createdAt: string;
    user?: {
        username: string;
    };
}

export default function ProductDetailsPage() {
    const { productId } = useParams<{ productId: string }>();
    const navigate = useNavigate();
    const { addToCart } = useCart();

    const [product, setProduct] = useState<Product | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [reviews, setReviews] = useState<Review[]>([]);
    const [isLoadingReviews, setIsLoadingReviews] = useState(true);
    const [isAuthenticated, setIsAuthenticated] = useState(false);

    const totalReviews = reviews.length;
    const averageRating = totalReviews > 0
        ? reviews.reduce((sum, review) => sum + review.rating, 0) / totalReviews
        : 0;

    useEffect(() => {
        // Check authentication status
        setIsAuthenticated(auth.isAuthenticated());

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

        const fetchReviews = async () => {
            if (!productId) return;
            try {
                const query = `
                    query GetReviews($productId: ID!, $page: Int, $size: Int) {
                        reviewsByProduct(productId: $productId, page: $page, size: $size) {
                            content {
                                id
                                userId
                                productId
                                rating
                                comment
                                createdAt
                                user {
                                    username
                                }
                            }
                            page {
                                totalElements
                                totalPages
                            }
                        }
                    }
                `;

                const response = await fetch("/graphql", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                    },
                    body: JSON.stringify({
                        query,
                        variables: {
                            productId,
                            page: 1,
                            size: 10
                        }
                    })
                });

                if (response.ok) {
                    const data = await response.json();
                    const reviewData = data.data?.reviewsByProduct?.content || [];
                    setReviews(reviewData);
                } else {
                    console.error("Failed to load reviews");
                }
            } catch (error) {
                console.error("Error loading reviews", error);
            } finally {
                setIsLoadingReviews(false);
            }
        };

        fetchProduct();
        fetchReviews();
    }, [productId]);

    const handleReviewSubmit = async () => {
        // Refetch reviews after successful submission
        setIsLoadingReviews(true);
        try {
            const query = `
                query GetReviews($productId: ID!, $page: Int, $size: Int) {
                    reviewsByProduct(productId: $productId, page: $page, size: $size) {
                        content {
                            id
                            userId
                            productId
                            rating
                            comment
                            createdAt
                            user {
                                username
                            }
                        }
                    }
                }
            `;

            const response = await fetch("/graphql", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({
                    query,
                    variables: {
                        productId,
                        page: 1,
                        size: 10
                    }
                })
            });

            if (response.ok) {
                const data = await response.json();
                const reviewData = data.data?.reviewsByProduct?.content || [];
                setReviews(reviewData);
                toast.success("Review submitted successfully!");
            }
        } catch (error) {
            console.error("Error refreshing reviews", error);
        } finally {
            setIsLoadingReviews(false);
        }
    };

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

            {/* Reviews Section */}
            <Separator className="my-12" />

            <section className="max-w-6xl mx-auto rounded-2xl border border-zinc-200/80 dark:border-zinc-800/80 bg-gradient-to-b from-zinc-50 to-white dark:from-zinc-950 dark:to-zinc-900 p-6 md:p-8 space-y-8">
                <div className="flex flex-col gap-5 md:flex-row md:items-end md:justify-between">
                    <div className="space-y-2">
                        <div className="inline-flex items-center gap-2 rounded-full border border-zinc-200 dark:border-zinc-700 px-3 py-1 text-xs font-medium uppercase tracking-wide text-zinc-600 dark:text-zinc-300">
                            <MessageSquareText className="h-3.5 w-3.5" />
                            Community Feedback
                        </div>
                        <h2 className="text-3xl font-bold tracking-tight">Customer Reviews</h2>
                        <p className="text-sm text-zinc-600 dark:text-zinc-400">
                            See what shoppers are saying and share your own experience.
                        </p>
                    </div>

                    <div className="grid grid-cols-2 gap-3 w-full md:w-auto">
                        <div className="rounded-xl border border-zinc-200 dark:border-zinc-800 bg-white/90 dark:bg-zinc-900/60 px-4 py-3">
                            <p className="text-xs uppercase tracking-wide text-zinc-500 dark:text-zinc-400">Average</p>
                            <p className="mt-1 flex items-center gap-1.5 text-xl font-semibold">
                                <Star className="h-4 w-4 fill-yellow-400 text-yellow-500" />
                                {totalReviews > 0 ? averageRating.toFixed(1) : "N/A"}
                            </p>
                        </div>
                        <div className="rounded-xl border border-zinc-200 dark:border-zinc-800 bg-white/90 dark:bg-zinc-900/60 px-4 py-3">
                            <p className="text-xs uppercase tracking-wide text-zinc-500 dark:text-zinc-400">Reviews</p>
                            <p className="mt-1 text-xl font-semibold">{totalReviews}</p>
                        </div>
                    </div>
                </div>

                <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
                    <div className="lg:col-span-8 rounded-xl border border-zinc-200 dark:border-zinc-800 bg-white/70 dark:bg-zinc-900/60 p-4 md:p-5">
                        <ReviewList reviews={reviews} isLoading={isLoadingReviews} />
                    </div>

                    <div className="lg:col-span-4">
                        {isAuthenticated ? (
                            <div className="md:sticky md:top-20">
                                <ReviewForm
                                    productId={parseInt(productId!)}
                                    onSubmitSuccess={handleReviewSubmit}
                                />
                            </div>
                        ) : (
                            <div className="rounded-xl border border-zinc-200 dark:border-zinc-800 bg-white dark:bg-zinc-900 p-6 text-center space-y-4">
                                <div className="mx-auto flex h-12 w-12 items-center justify-center rounded-full bg-yellow-100 text-yellow-700 dark:bg-yellow-900/40 dark:text-yellow-300">
                                    <Sparkles className="h-5 w-5" />
                                </div>
                                <div>
                                    <p className="font-semibold">Share your thoughts</p>
                                    <p className="text-sm text-zinc-600 dark:text-zinc-400 mt-1">
                                        Sign in to rate this product and leave a detailed review.
                                    </p>
                                </div>
                                <Button
                                    onClick={() => navigate(`/login?next=${encodeURIComponent(window.location.pathname)}`)}
                                    className="bg-yellow-500 hover:bg-yellow-600 text-black font-medium"
                                >
                                    <LogIn className="mr-2 h-4 w-4" />
                                    Sign In
                                </Button>
                            </div>
                        )}
                    </div>
                </div>
            </section>
        </div>
    );
}
