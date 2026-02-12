import { useState, useEffect } from "react";
import { useParams } from "react-router-dom";
import { ProductCard } from "@/components/product/ProductCard";
import { useCart } from "@/hooks/use-cart";
import {
    Pagination,
    PaginationContent,
    PaginationEllipsis,
    PaginationItem,
    PaginationLink,
    PaginationNext,
    PaginationPrevious,
} from "@/components/ui/pagination";

interface Product {
    id: number;
    name: string;
    description: string;
    price: number;
    categoryId: number;
    image: string;
}

export default function CategoryProductsPage() {
    const { categoryId } = useParams<{ categoryId: string }>();
    const [products, setProducts] = useState<Product[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [page, setPage] = useState(1);
    const [totalPages, setTotalPages] = useState(0);
    const [categoryName, setCategoryName] = useState<string>("");
    const { addToCart } = useCart();

    useEffect(() => {
        if (!categoryId) return;

        setLoading(true);

        const query = `
            query Products($categoryId: ID, $page: Int, $size: Int) {
                products(categoryId: $categoryId, page: $page, size: $size) {
                    content {
                        id
                        name
                        description
                        price
                        imageUrl
                        category {
                            id
                            name
                        }
                    }
                    page {
                        totalPages
                    }
                }
            }
        `;

        fetch("/graphql", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({
                query,
                variables: {
                    categoryId: categoryId,
                    page: page,
                    size: 10
                }
            }),
        })
            .then((res) => {
                if (!res.ok) throw new Error("Failed to fetch products");
                return res.json();
            })
            .then((result) => {
                if (result.data?.products) {
                    const data = result.data.products;
                    const products = (data.content || []).map((item: any) => ({
                        id: parseInt(item.id),
                        name: item.name ?? "Unnamed Product",
                        description: item.description,
                        price: item.price,
                        categoryId: item.category?.id ? parseInt(item.category.id) : 0,
                        image: item.imageUrl,
                    }));
                    setProducts(products);
                    setTotalPages(data.page?.totalPages ?? 0);

                    // Set category name from the first product's category
                    if (products.length > 0 && data.content[0]?.category?.name) {
                        setCategoryName(data.content[0].category.name);
                    }

                    setError(null);
                }
            })
            .catch((err) => setError(err.message))
            .finally(() => setLoading(false));
    }, [categoryId, page]);

    const renderPaginationItems = () => {
        const items = [];
        const maxVisiblePages = 5;

        if (totalPages <= maxVisiblePages) {
            for (let i = 1; i <= totalPages; i++) {
                items.push(
                    <PaginationItem key={i}>
                        <PaginationLink
                            href="#"
                            isActive={page === i}
                            onClick={(e) => {
                                e.preventDefault();
                                setPage(i);
                            }}
                        >
                            {i}
                        </PaginationLink>
                    </PaginationItem>
                );
            }
        } else {
            // Always show first page
            items.push(
                <PaginationItem key={1}>
                    <PaginationLink
                        href="#"
                        isActive={page === 1}
                        onClick={(e) => {
                            e.preventDefault();
                            setPage(1);
                        }}
                    >
                        1
                    </PaginationLink>
                </PaginationItem>
            );

            // Determine range around current page
            let start = Math.max(2, page - 1);
            let end = Math.min(totalPages - 1, page + 1);

            if (page < 4) {
                start = 2;
                end = Math.min(totalPages - 1, 4);
            } else if (page > totalPages - 3) {
                start = Math.max(2, totalPages - 3);
                end = totalPages - 1;
            }

            if (start > 2) {
                items.push(
                    <PaginationItem key="ellipsis-start">
                        <PaginationEllipsis />
                    </PaginationItem>
                );
            }

            for (let i = start; i <= end; i++) {
                items.push(
                    <PaginationItem key={i}>
                        <PaginationLink
                            href="#"
                            isActive={page === i}
                            onClick={(e) => {
                                e.preventDefault();
                                setPage(i);
                            }}
                        >
                            {i}
                        </PaginationLink>
                    </PaginationItem>
                );
            }

            if (end < totalPages - 1) {
                items.push(
                    <PaginationItem key="ellipsis-end">
                        <PaginationEllipsis />
                    </PaginationItem>
                );
            }

            // Always show last page
            items.push(
                <PaginationItem key={totalPages}>
                    <PaginationLink
                        href="#"
                        isActive={page === totalPages}
                        onClick={(e) => {
                            e.preventDefault();
                            setPage(totalPages);
                        }}
                    >
                        {totalPages}
                    </PaginationLink>
                </PaginationItem>
            );
        }
        return items;
    };

    if (loading) {
        return <div className="text-center p-8">Loading products...</div>;
    }

    if (error) {
        return <div className="text-center text-red-500 p-8">Error: {error}</div>;
    }

    return (
        <div className="space-y-6">
            <div className="flex items-center justify-between">
                <h1 className="text-3xl font-bold">
                    {categoryName || "Category Products"}
                </h1>
            </div>

            {products.length === 0 ? (
                <div className="text-center text-muted-foreground p-8">
                    No products found in this category.
                </div>
            ) : (
                <>
                    <div className="grid gap-6 grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 2xl:grid-cols-7">
                        {products.map((product) => (
                            <ProductCard
                                key={product.id}
                                id={product.id}
                                image={product.image}
                                title={product.name}
                                description={product.description}
                                price={`$${product.price}`}
                                onAddToCart={() => {
                                    addToCart({
                                        id: product.id,
                                        name: product.name,
                                        price: product.price,
                                        image: product.image,
                                    });
                                }}
                            />
                        ))}
                    </div>

                    {totalPages > 1 && (
                        <Pagination className="mt-8 justify-end">
                            <PaginationContent>
                                <PaginationItem>
                                    <PaginationPrevious
                                        href="#"
                                        onClick={(e) => {
                                            e.preventDefault();
                                            if (page > 1) setPage(page - 1);
                                        }}
                                        className={page <= 1 ? "pointer-events-none opacity-50" : "cursor-pointer"}
                                    />
                                </PaginationItem>

                                {renderPaginationItems()}

                                <PaginationItem>
                                    <PaginationNext
                                        href="#"
                                        onClick={(e) => {
                                            e.preventDefault();
                                            if (page < totalPages) setPage(page + 1);
                                        }}
                                        className={page >= totalPages ? "pointer-events-none opacity-50" : "cursor-pointer"}
                                    />
                                </PaginationItem>
                            </PaginationContent>
                        </Pagination>
                    )}
                </>
            )}
        </div>
    );
}
