import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { useState } from "react";

export interface ProductMatch {
  title: string;
  description: string;
}

export function Navbar({
  search,
  onSearchChange,
  matches = [],
}: {
  search: string;
  onSearchChange: (value: string) => void;
  matches?: ProductMatch[];
}) {
  const [showDropdown, setShowDropdown] = useState(false);

  function handleBlur() {
    // Timeout to allow click on dropdown item
    setTimeout(() => setShowDropdown(false), 200);
  }

  return (
    <nav className="fixed top-0 left-0 right-0 z-50 bg-white shadow-sm dark:bg-zinc-900">
      <div className="w-full max-w-7xl mx-auto flex items-center justify-between gap-4 py-3 px-4">
        <div className="text-2xl font-bold tracking-tight text-primary shrink-0 cursor-pointer">
          SMECS
        </div>
        <div className="flex-1 max-w-2xl mx-auto relative">
          <Input
            type="search"
            placeholder="Search products..."
            className="h-12 text-base px-4 py-2 border-2 border-primary/30 focus:border-primary bg-white dark:bg-zinc-800 transition-all w-full"
            value={search}
            onChange={(e) => {
              onSearchChange(e.target.value);
              setShowDropdown(true);
            }}
            onFocus={() => setShowDropdown(true)}
            onBlur={handleBlur}
            aria-label="Search products"
          />
          {showDropdown && matches.length > 0 && (
            <div
              className="absolute left-0 right-0 z-50 w-full bg-white dark:bg-zinc-900 border border-zinc-200 dark:border-zinc-700 rounded-lg shadow-lg mt-2"
              style={{ top: "100%" }}
            >
              <ul>
                {matches.map((product, idx) => (
                  <li
                    key={idx}
                    className="px-6 py-3 cursor-pointer hover:bg-zinc-100 dark:hover:bg-zinc-800 border-b last:border-b-0 border-zinc-100 dark:border-zinc-800"
                    onMouseDown={() => {
                      onSearchChange(product.title);
                      setShowDropdown(false);
                    }}
                  >
                    <div className="font-semibold">{product.title}</div>
                    <div className="text-xs text-zinc-500">
                      {product.description}
                    </div>
                  </li>
                ))}
              </ul>
            </div>
          )}
        </div>
        <div className="shrink-0">
          <Button>Sign In</Button>
        </div>
      </div>
    </nav>
  );
}
