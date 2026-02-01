import { useState, useEffect } from "react";
import { ProductCard } from "./ProductCard";
import { Input } from "@/components/ui/input";

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

  function handleBlur() {
    // Timeout to allow click on dropdown item
    setTimeout(() => setShowDropdown(false), 200);
  }

  useEffect(() => {
    setLoading(true);
    fetch("/api/products")
      .then((res) => {
        if (!res.ok) throw new Error("Failed to fetch products");
        return res.json();
      })
      .then((payload) => {
        // payload: { status, message, data: { content: [...] } }
        const products = (payload.data.content || []).map((item: any) => ({
          id: item.id,
          name: item.name ?? "Unnamed Product",
          description: item.description,
          price: item.price,
          categoryId: item.categoryId,
          image: item.imageUrl,
        }));
        setProducts(products);
        setError(null);
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, []);

  const matches = search
    ? products.filter(
      (product) =>
        product.name?.toLowerCase().includes(search.toLowerCase()) ||
        product.description?.toLowerCase().includes(search.toLowerCase()),
    )
    : [];

  return (
    <div className="space-y-6">
      <div className="flex flex-col items-center gap-6">
        <h1 className="text-3xl font-bold">Products</h1>
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
      )}
    </div>
  );
}
