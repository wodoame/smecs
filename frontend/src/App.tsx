import * as React from "react";
import { AppSidebar } from "@/components/app-sidebar";
import { Outlet, useNavigate } from "react-router-dom";
import { Separator } from "@/components/ui/separator";
import {
  SidebarInset,
  SidebarProvider,
  SidebarTrigger,
} from "@/components/ui/sidebar";
import { Input } from "@/components/ui/input";
import { Search } from "lucide-react";
import { Toaster } from "@/components/ui/sonner";

export default function App() {
  const navigate = useNavigate();
  const [searchTerm, setSearchTerm] = React.useState("");

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    navigate(`/products?query=${encodeURIComponent(searchTerm)}`);
  };

  return (
    <SidebarProvider>
      <Toaster />
      <AppSidebar />
      <SidebarInset>
        <header className="flex h-16 shrink-0 items-center gap-2 transition-[width,height] ease-linear group-has-data-[collapsible=icon]/sidebar-wrapper:h-12 border-b px-4">
          <div className="flex items-center gap-2 w-full">
            <SidebarTrigger className="-ml-1" />
            <Separator
              orientation="vertical"
              className="mr-2 data-[orientation=vertical]:h-4"
            />
            <form onSubmit={handleSearch} className="w-full max-w-2xl mx-auto">
              <div className="relative">
                <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
                <Input
                  type="search"
                  placeholder="Search products..."
                  className="pl-8 w-full"
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                />
              </div>
            </form>
          </div>
        </header>
        <div className="flex flex-1 flex-col gap-4 p-4">
          <Outlet />
        </div>
      </SidebarInset>
    </SidebarProvider>
  );
}
