import { useState, useEffect } from "react";
import { ProductCard } from "./ProductCard";
import { Input } from "@/components/ui/input";
import {
  Pagination,
  PaginationContent,
  PaginationEllipsis,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious,
} from "@/components/ui/pagination";

export interface Product {
  id: number;
  name: string;
  description: string;
  price: number;
  categoryId: number;
  image: string;
}

export function ProductList() {
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [search, setSearch] = useState("");
  const [showDropdown, setShowDropdown] = useState(false);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  function handleBlur() {
    // Timeout to allow click on dropdown item
    setTimeout(() => setShowDropdown(false), 200);
  }

  useEffect(() => {
    setLoading(true);
    fetch(`/api/products?page=${page}`)
      .then((res) => {
        if (!res.ok) throw new Error("Failed to fetch products");
        return res.json();
      })
      .then((payload) => {
        // payload: { status, message, data: { content: [...], page: { totalPages: ... } } }
        const products = (payload.data.content || []).map((item: any) => ({
          id: item.id,
          name: item.name ?? "Unnamed Product",
          description: item.description,
          price: item.price,
          categoryId: item.categoryId,
          image: item.imageUrl,
        }));
        setProducts(products);
        setTotalPages(payload.data.page?.totalPages ?? 0);
        setError(null);
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, [page]);

  const matches = search
    ? products.filter(
      (product) =>
        product.name?.toLowerCase().includes(search.toLowerCase()) ||
        product.description?.toLowerCase().includes(search.toLowerCase()),
    )
    : [];

  const renderPaginationItems = () => {
    const items = [];
    const maxVisiblePages = 5;

    if (totalPages <= maxVisiblePages) {
      for (let i = 0; i < totalPages; i++) {
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
              {i + 1}
            </PaginationLink>
          </PaginationItem>
        );
      }
    } else {
      // Always show first page
      items.push(
        <PaginationItem key={0}>
          <PaginationLink
            href="#"
            isActive={page === 0}
            onClick={(e) => {
              e.preventDefault();
              setPage(0);
            }}
          >
            1
          </PaginationLink>
        </PaginationItem>
      );

      // Determine range around current page
      let start = Math.max(1, page - 1);
      let end = Math.min(totalPages - 2, page + 1);

      if (page < 3) {
        start = 1;
        end = Math.min(totalPages - 2, 3);
      } else if (page > totalPages - 4) {
        start = Math.max(1, totalPages - 4);
        end = totalPages - 2;
      }

      if (start > 1) {
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
              {i + 1}
            </PaginationLink>
          </PaginationItem>
        );
      }

      if (end < totalPages - 2) {
        items.push(
          <PaginationItem key="ellipsis-end">
            <PaginationEllipsis />
          </PaginationItem>
        );
      }

      // Always show last page
      items.push(
        <PaginationItem key={totalPages - 1}>
          <PaginationLink
            href="#"
            isActive={page === totalPages - 1}
            onClick={(e) => {
              e.preventDefault();
              setPage(totalPages - 1);
            }}
          >
            {totalPages}
          </PaginationLink>
        </PaginationItem>
      );
    }
    return items;
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col items-center gap-6">
        {/* <h1 className="text-3xl font-bold">Products</h1> */}
        <div className="relative w-full max-w-2xl">
          <Input
            type="search"
            placeholder="Search products..."
            className="w-full p-2"
            value={search}
            onChange={(e) => {
              setSearch(e.target.value);
              setShowDropdown(true);
            }}
            onFocus={() => setShowDropdown(true)}
            onBlur={handleBlur}
          />
          {showDropdown && matches.length > 0 && (
            <div className="absolute left-0 right-0 z-50 w-full bg-white dark:bg-zinc-900 border border-zinc-200 dark:border-zinc-700 rounded-lg shadow-lg mt-2 max-h-[60vh] overflow-y-auto">
              <ul>
                {matches.map((product) => (
                  <li
                    key={product.id}
                    className="px-4 py-2 cursor-pointer hover:bg-zinc-100 dark:hover:bg-zinc-800 border-b last:border-b-0 border-zinc-100 dark:border-zinc-800"
                    onMouseDown={() => {
                      setSearch(product.name);
                      setShowDropdown(false);
                    }}
                  >
                    <div className="font-semibold text-sm">{product.name}</div>
                    <div className="text-xs text-zinc-500 truncate">
                      {product.description}
                    </div>
                  </li>
                ))}
              </ul>
            </div>
          )}
        </div>
      </div>

      {loading ? (
        <div className="text-center">Loading...</div>
      ) : error ? (
        <div className="text-center text-red-500">{error}</div>
      ) : (
        <>
          <div className="grid gap-6 grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 2xl:grid-cols-7">
            {products.map((product) => (
              <ProductCard
                key={product.id}
                image={product.image}
                title={product.name}
                description={product.description}
                price={`$${product.price}`}
                onView={() => {
                  alert(`Viewing: ${product.name}`);
                }}
              />
            ))}
          </div>

          <Pagination className="mt-8">
            <PaginationContent>
              <PaginationItem>
                <PaginationPrevious
                  href="#"
                  onClick={(e) => {
                    e.preventDefault();
                    if (page > 0) setPage(page - 1);
                  }}
                  className={page === 0 ? "pointer-events-none opacity-50" : "cursor-pointer"}
                />
              </PaginationItem>

              {renderPaginationItems()}

              <PaginationItem>
                <PaginationNext
                  href="#"
                  onClick={(e) => {
                    e.preventDefault();
                    if (page < totalPages - 1) setPage(page + 1);
                  }}
                  className={page >= totalPages - 1 ? "pointer-events-none opacity-50" : "cursor-pointer"}
                />
              </PaginationItem>
            </PaginationContent>
          </Pagination>
        </>
      )}
    </div>
  );
}
