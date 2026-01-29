import { useState } from "react";
import { ProductCard } from "./ProductCard";
import { Navbar } from "../layout/Navbar";

const sampleProducts = [
  {
    image:
      "https://images.unsplash.com/photo-1546435770-a3e426bf472b?q=80&w=865&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
    title: "Wireless Headphones",
    description: "High-quality wireless headphones with noise cancellation.",
    price: "$99.99",
    featured: true,
  },
  {
    image:
      "https://images.unsplash.com/photo-1660844817855-3ecc7ef21f12?q=80&w=486&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
    title: "Smart Watch",
    description: "Track your fitness and notifications with this smart watch.",
    price: "$149.99",
    featured: false,
  },
  {
    image:
      "https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?q=80&w=1031&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
    title: "Bluetooth Speaker",
    description: "Portable speaker with deep bass and long battery life.",
    price: "$59.99",
    featured: false,
  },
  {
    image:
      "https://images.unsplash.com/photo-1616423640778-28d1b53229bd?q=80&w=870&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
    title: "DSLR Camera",
    description: "Capture stunning photos with this professional DSLR camera.",
    price: "$499.99",
    featured: true,
  },
];

export function ProductList() {
  const [search, setSearch] = useState("");

  const matches = search
    ? sampleProducts.filter(
        (product) =>
          product.title.toLowerCase().includes(search.toLowerCase()) ||
          product.description.toLowerCase().includes(search.toLowerCase()),
      )
    : [];

  return (
    <>
      <Navbar search={search} onSearchChange={setSearch} matches={matches} />
      <div className="container mx-auto py-8 px-4 pt-40">
        <h1 className="text-3xl font-bold mb-8 text-center">Products</h1>
        <div className="grid gap-8 grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4">
          {sampleProducts.map((product, idx) => (
            <ProductCard
              key={idx}
              image={product.image}
              title={product.title}
              description={product.description}
              price={product.price}
              featured={product.featured}
              onView={() => {
                // Placeholder for view action
                alert(`Viewing: ${product.title}`);
              }}
            />
          ))}
        </div>
      </div>
    </>
  );
}
