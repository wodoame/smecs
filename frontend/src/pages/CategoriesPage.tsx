
import { useEffect, useMemo, useState } from "react";
import { type Category, CategoryCard } from "@/components/category/CategoryCard";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { ListRestart, Search } from "lucide-react";
import {
    Pagination,
    PaginationContent,
    PaginationItem,
    PaginationNext,
    PaginationPrevious,
} from "@/components/ui/pagination";

export default function CategoriesPage() {
    const [categories, setCategories] = useState<Category[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [page, setPage] = useState(1);
    const [totalPages, setTotalPages] = useState(0);
    const [searchTerm, setSearchTerm] = useState("");
    const [activeQuery, setActiveQuery] = useState("");

    useEffect(() => {
        setLoading(true);
        fetch(`/api/categories?relatedImages=true&page=${page}&query=${encodeURIComponent(activeQuery)}`)
            .then((res) => {
                if (!res.ok) throw new Error("Failed to fetch categories");
                return res.json();
            })
            .then((payload) => {
                // payload: { status, message, data: { content: [...], page: { ... } } }
                setCategories(payload.data.content || []);
                setTotalPages(payload.data.page?.totalPages || 0);
                setError(null);
            })
            .catch((err) => setError(err.message))
            .finally(() => setLoading(false));
    }, [page, activeQuery]);

    const filteredCategories = useMemo(() => {
        const normalizedSearchTerm = searchTerm.trim().toLowerCase();
        if (!normalizedSearchTerm) return categories;

        return categories.filter((category) => {
            const name = category.categoryName?.toLowerCase() || "";
            const description = category.description?.toLowerCase() || "";
            return name.includes(normalizedSearchTerm) || description.includes(normalizedSearchTerm);
        });
    }, [categories, searchTerm]);

    const handleSearch = () => {
        setPage(1);
        setActiveQuery(searchTerm.trim());
    };

    const handleShowAll = () => {
        setSearchTerm("");
        setActiveQuery("");
        setPage(1);
    };

    const handlePageChange = (newPage: number) => {
        if (newPage >= 1 && newPage <= totalPages) {
            setPage(newPage);
        }
    };

    if (loading) return <div className="p-4 text-center">Loading categories...</div>;
    if (error) return <div className="p-4 text-center text-red-500">Error: {error}</div>;

    return (
        <div className="space-y-6 container mx-auto p-4">
            <h1 className="text-3xl font-bold mb-6">Categories</h1>

            <div className="flex w-full max-w-xl items-center gap-2">
                <Input
                    placeholder="Filter categories..."
                    value={searchTerm}
                    onChange={(event) => setSearchTerm(event.target.value)}
                    onKeyDown={(event) => {
                        if (event.key === "Enter") {
                            handleSearch();
                        }
                    }}
                />
                <Button type="button" size="icon" onClick={handleSearch}>
                    <Search className="h-4 w-4" />
                </Button>
                <Button type="button" variant="outline" onClick={handleShowAll}>
                    <ListRestart className="h-4 w-4 mr-2" />
                    Show All
                </Button>
            </div>

            <div className="grid grid-cols-1 gap-6">
                {filteredCategories.map((category) => (
                    <CategoryCard key={category.categoryId} category={category} />
                ))}
            </div>

            {totalPages > 1 && (
                <Pagination className="w-auto h-auto shrink-0 space-x-2 justify-end mt-6">
                    <PaginationContent>
                        <PaginationItem>
                            <PaginationPrevious
                                onClick={() => handlePageChange(page - 1)}
                                className={page <= 1 ? "pointer-events-none opacity-50" : "cursor-pointer"}
                            />
                        </PaginationItem>
                        <div className="flex items-center justify-center text-sm font-medium">
                            Page {page} of {totalPages}
                        </div>
                        <PaginationItem>
                            <PaginationNext
                                onClick={() => handlePageChange(page + 1)}
                                className={page >= totalPages ? "pointer-events-none opacity-50" : "cursor-pointer"}
                            />
                        </PaginationItem>
                    </PaginationContent>
                </Pagination>
            )}
        </div>
    );
}
