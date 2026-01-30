import { useState, useEffect } from "react";
import { ProductCard } from "./ProductCard";
import { Navbar } from "../layout/Navbar";

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

  useEffect(() => {
    setLoading(true);
    fetch("/api/products")
      .then((res) => {
        if (!res.ok) throw new Error("Failed to fetch products");
        return res.json();
      })
      .then((data: Product[]) => {
        setProducts(data);
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
    <>
      <Navbar search={search} onSearchChange={setSearch} matches={matches} />
      <div className="container mx-auto py-8 px-4 pt-40">
        <h1 className="text-3xl font-bold mb-8 text-center">Products</h1>
        {loading ? (
          <div className="text-center">Loading...</div>
        ) : error ? (
          <div className="text-center text-red-500">{error}</div>
        ) : (
          <div className="grid gap-8 grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4">
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
    </>
  );
}
