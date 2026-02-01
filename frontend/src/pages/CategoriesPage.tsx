
import { useEffect, useState } from "react";
import { type Category, CategoryCard } from "@/components/category/CategoryCard";

export default function CategoriesPage() {
    const [categories, setCategories] = useState<Category[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        setLoading(true);
        fetch("/api/categories?includeRelatedImages=true")
            .then((res) => {
                if (!res.ok) throw new Error("Failed to fetch categories");
                return res.json();
            })
            .then((payload) => {
                // payload: { status, message, data: { content: [...] } }
                setCategories(payload.data.content || []);
                setError(null);
            })
            .catch((err) => setError(err.message))
            .finally(() => setLoading(false));
    }, []);

    if (loading) return <div className="p-4 text-center">Loading categories...</div>;
    if (error) return <div className="p-4 text-center text-red-500">Error: {error}</div>;

    return (
        <div className="space-y-6 container mx-auto p-4">
            <h1 className="text-3xl font-bold mb-6">Categories</h1>
            <div className="grid grid-cols-1 gap-6">
                {categories.map((category) => (
                    <CategoryCard key={category.categoryId} category={category} />
                ))}
            </div>
        </div>
    );
}
