"use client"

import * as React from "react"
import { useLocation } from "react-router-dom"
import {
  AudioWaveform,
  BookOpen,
  Bot,
  Command,
  Frame,
  GalleryVerticalEnd,
  LayoutList,
  Map,
  PieChart,
  Settings2,
  Shield,
  SquareTerminal,
} from "lucide-react"

import { NavMain } from "@/components/nav-main"
import { NavProjects } from "@/components/nav-projects"
import { NavUser } from "@/components/nav-user"
import { TeamSwitcher } from "@/components/team-switcher"
import {
  Sidebar,
  SidebarContent,
  SidebarFooter,
  SidebarHeader,
  SidebarRail,
} from "@/components/ui/sidebar"

// This is sample data.
const data = {
  user: {
    name: "shadcn",
    email: "m@example.com",
    avatar: "/avatars/shadcn.jpg",
  },
  teams: [
    {
      name: "Acme Inc",
      logo: GalleryVerticalEnd,
      plan: "Enterprise",
    },
    {
      name: "Acme Corp.",
      logo: AudioWaveform,
      plan: "Startup",
    },
    {
      name: "Evil Corp.",
      logo: Command,
      plan: "Free",
    },
  ],
  navMain: [
    {
      title: "Playground",
      url: "#",
      icon: SquareTerminal,
      isActive: true,
      items: [
        {
          title: "History",
          url: "#",
        },
        {
          title: "Starred",
          url: "#",
        },
        {
          title: "Settings",
          url: "#",
        },
      ],
    },
    {
      title: "Models",
      url: "#",
      icon: Bot,
      items: [
        {
          title: "Genesis",
          url: "#",
        },
        {
          title: "Explorer",
          url: "#",
        },
        {
          title: "Quantum",
          url: "#",
        },
      ],
    },
    {
      title: "Documentation",
      url: "#",
      icon: BookOpen,
      items: [
        {
          title: "Introduction",
          url: "#",
        },
        {
          title: "Get Started",
          url: "#",
        },
        {
          title: "Tutorials",
          url: "#",
        },
        {
          title: "Changelog",
          url: "#",
        },
      ],
    },
    {
      title: "Admin",
      url: "#",
      icon: Shield,
      items: [
        {
          title: "Inventories",
          url: "/admin/inventories",
        },
        {
          title: "Categories",
          url: "/admin/categories",
        },
      ],
    },
    {
      title: "Settings",
      url: "#",
      icon: Settings2,
      items: [
        {
          title: "General",
          url: "#",
        },
        {
          title: "Team",
          url: "#",
        },
        {
          title: "Billing",
          url: "#",
        },
        {
          title: "Limits",
          url: "#",
        },
      ],
    },
  ],
  projects: [
    {
      name: "Design Engineering",
      url: "#",
      icon: Frame,
    },
    {
      name: "Sales & Marketing",
      url: "#",
      icon: PieChart,
    },
    {
      name: "Travel",
      url: "#",
      icon: Map,
    },
  ],
}

interface Category {
  categoryId: number
  categoryName: string
  description: string
  imageUrl: string
  relatedImageUrls: string[] | null
}

export function AppSidebar({ ...props }: React.ComponentProps<typeof Sidebar>) {
  const { pathname } = useLocation()
  const dataWithActiveState = {
    ...data,
    navMain: data.navMain.map((item) => ({
      ...item,
      isActive:
        item.title === "Admin"
          ? pathname.startsWith("/admin")
          : item.isActive,
    })),
  }

  const [categories, setCategories] = React.useState<Category[]>([])

  React.useEffect(() => {
    fetch("/api/categories")
      .then((res) => res.json())
      .then((data) => {
        // Handle paginated response if necessary, assuming data.content is the array
        // based on AdminCategoriesPage logic: payload.data.content
        if (data.data?.content) {
          setCategories(data.data.content)
        } else if (Array.isArray(data)) {
          setCategories(data)
        }
      })
      .catch((err) => console.error("Failed to fetch categories", err))
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
          url: `/categories/${cat.categoryId}`, // Assuming a route exists or just a placeholder
        })),
        {
          title: "See more",
          url: "/categories",
        },
      ],
    },
  ]

  return (
    <Sidebar collapsible="icon" {...props}>
      <SidebarHeader>
        <TeamSwitcher teams={data.teams} />
      </SidebarHeader>
      <SidebarContent>
        <NavMain items={dataWithActiveState.navMain} />
        <NavMain items={navCatalog} label="Catalog" />
        <NavProjects projects={data.projects} />
      </SidebarContent>
      <SidebarFooter>
        <NavUser user={data.user} />
      </SidebarFooter>
      <SidebarRail />
    </Sidebar>
  )
}
