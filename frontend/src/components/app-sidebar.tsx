"use client"

import * as React from "react"
import { Link, useLocation } from "react-router-dom"
import {
  GalleryVerticalEnd,
  LayoutList,
  LogIn,
  Package,
  Shield,
  ShoppingBag,
} from "lucide-react"

import { NavMain } from "@/components/nav-main"
import { NavUser } from "@/components/nav-user"
import {
  Sidebar,
  SidebarContent,
  SidebarFooter,
  SidebarHeader,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  SidebarRail,
} from "@/components/ui/sidebar"
import { useAuth } from "@/hooks/use-auth"

interface Category {
  categoryId: number
  categoryName: string
}

export function AppSidebar({ ...props }: React.ComponentProps<typeof Sidebar>) {
  const { pathname } = useLocation()
  const { user, isAdmin } = useAuth()
  const [categories, setCategories] = React.useState<Category[]>([])

  React.useEffect(() => {
    // Using GraphQL to demonstrate coexistence with REST
    const query = `
      query {
        categories {
          content {
            id
            name
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
      body: JSON.stringify({ query }),
    })
      .then((res) => res.json())
      .then((result) => {
        if (result.data?.categories?.content) {
          // Map GraphQL response to match the expected Category interface
          const mappedCategories = result.data.categories.content.map((cat: any) => ({
            categoryId: parseInt(cat.id),
            categoryName: cat.name,
          }));
          setCategories(mappedCategories);
        }
      })
      .catch((err) => console.error("Failed to fetch categories via GraphQL", err));
  }, [])

  const navCatalog = [
    {
      title: "Categories",
      url: "#",
      icon: LayoutList,
      isActive: true,
      items: [
        ...categories.map((cat) => ({
          title: cat.categoryName,
          url: `/categories/${cat.categoryId}`,
        })),
        {
          title: "See more",
          url: "/categories",
        },
      ],
    },
  ]

  const navMainItems = [
    {
      title: "Cart",
      url: "/cart",
      icon: ShoppingBag,
      items: [],
      // Always show cart
      visible: true
    },
    {
      title: "Orders",
      url: "/orders",
      icon: Package,
      items: [],
      // Show orders for authenticated users
      visible: !!user
    },
    {
      title: "Admin",
      url: "#",
      icon: Shield,
      visible: isAdmin, // Only show if admin
      items: [
        {
          title: "Inventories",
          url: "/admin/inventories",
        },
        {
          title: "Categories",
          url: "/admin/categories",
        },
        {
          title: "Orders",
          url: "/admin/orders",
        },
      ],
    }
  ]

  const filteredNavMain = navMainItems
    .filter(item => item.visible)
    .map(({ visible, ...item }) => ({
      ...item,
      isActive:
        item.title === "Admin"
          ? pathname.startsWith("/admin")
          : false, // simplified isActive logic
    }))

  const userData = user ? {
    name: user.username,
    email: user.email,
    avatar: "", // Placeholder or add to AuthData if available
  } : null

  return (
    <Sidebar collapsible="icon" {...props}>
      <SidebarHeader>
        <SidebarMenu>
          <SidebarMenuItem>
            <SidebarMenuButton size="lg" asChild>
              <Link to="/products">
                <div className="bg-sidebar-primary text-sidebar-primary-foreground flex aspect-square size-8 items-center justify-center rounded-lg">
                  <GalleryVerticalEnd className="size-4" />
                </div>
                <div className="flex flex-col gap-0.5 leading-none">
                  <span className="font-semibold">SMECS</span>
                  <span className="">v1.0.0</span>
                </div>
              </Link>
            </SidebarMenuButton>
          </SidebarMenuItem>
        </SidebarMenu>
      </SidebarHeader>
      <SidebarContent>
        <NavMain items={filteredNavMain} />
        <NavMain items={navCatalog} label="Catalog" />
      </SidebarContent>
      <SidebarFooter>
        {userData ? (
          <NavUser user={userData} />
        ) : (
          <SidebarMenu>
            <SidebarMenuItem>
              <SidebarMenuButton asChild>
                <Link to="/login">
                  <LogIn className="h-4 w-4" />
                  <span>Log In</span>
                </Link>
              </SidebarMenuButton>
            </SidebarMenuItem>
          </SidebarMenu>
        )}
      </SidebarFooter>
      <SidebarRail />
    </Sidebar>
  )
}
