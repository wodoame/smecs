
import * as React from "react"
import { toast } from "sonner"
import { Button } from "@/components/ui/button"
import { Checkbox } from "@/components/ui/checkbox"
import {
    DropdownMenu,
    DropdownMenuCheckboxItem,
    DropdownMenuContent,
    DropdownMenuGroup,
    DropdownMenuItem,
    DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { Input } from "@/components/ui/input"
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from "@/components/ui/table"
import {
    flexRender,
    getCoreRowModel,
    getFilteredRowModel,
    getPaginationRowModel,
    getSortedRowModel,
    useReactTable,
    type ColumnDef,
    type ColumnFiltersState,
    type SortingState,
    type VisibilityState,
} from "@tanstack/react-table"
import { ArrowUpDown, ChevronDown, CircleCheck, MoreHorizontal, Plus, Search, Trash2 } from "lucide-react"
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
    DialogTrigger,
} from "@/components/ui/dialog"
import {
    AlertDialog,
    AlertDialogAction,
    AlertDialogCancel,
    AlertDialogContent,
    AlertDialogDescription,
    AlertDialogFooter,
    AlertDialogHeader,
    AlertDialogTitle,
    AlertDialogTrigger,
} from "@/components/ui/alert-dialog"
import {
    Pagination,
    PaginationContent,
    PaginationItem,
    PaginationNext,
    PaginationPrevious,
} from "@/components/ui/pagination"
import type { ApiResponse } from "@/types/api"

export interface Category {
    categoryId: number;
    categoryName: string;
    description: string;
    imageUrl: string;
    relatedImageUrls: string[] | null;
}

export default function AdminCategoriesPage() {
    const [data, setData] = React.useState<Category[]>([])
    const [loading, setLoading] = React.useState(true)
    const [sorting, setSorting] = React.useState<SortingState>([])
    const [columnFilters, setColumnFilters] = React.useState<ColumnFiltersState>(
        []
    )
    const [columnVisibility, setColumnVisibility] =
        React.useState<VisibilityState>({})
    const [rowSelection, setRowSelection] = React.useState({})

    // Add Category State
    const [isAddDialogOpen, setIsAddDialogOpen] = React.useState(false);
    const [newCategory, setNewCategory] = React.useState({
        categoryName: "",
        description: "",
        imageUrl: "",
    });

    // Edit Category State
    const [isEditDialogOpen, setIsEditDialogOpen] = React.useState(false);
    const [editingCategory, setEditingCategory] = React.useState<Category | null>(null);

    // View Category State
    const [isViewDialogOpen, setIsViewDialogOpen] = React.useState(false);
    const [viewingCategory, setViewingCategory] = React.useState<Category | null>(null);

    // Search and Pagination State
    const [searchTerm, setSearchTerm] = React.useState("")
    const [page, setPage] = React.useState(1)
    const [totalPages, setTotalPages] = React.useState(0)


    const fetchCategories = (query = "", pageIndex = 1) => {
        setLoading(true);
        fetch(`/api/categories?query=${encodeURIComponent(query)}&page=${pageIndex}`)
            .then((res) => {
                if (!res.ok) throw new Error("Failed to fetch categories");
                return res.json();
            })
            .then((payload: ApiResponse<Category>) => {
                setData(payload.data.content || []);
                setTotalPages(payload.data.page?.totalPages || 0);
            })
            .catch((err) => console.error("Error fetching categories:", err))
            .finally(() => setLoading(false));
    };

    const handleSearch = () => {
        setPage(1);
        fetchCategories(searchTerm, 1);
    };

    const handlePageChange = (newPage: number) => {
        if (newPage >= 1 && newPage <= totalPages) {
            setPage(newPage);
            fetchCategories(searchTerm, newPage);
        }
    };

    React.useEffect(() => {
        fetchCategories();
    }, []);

    const handleCreateCategory = () => {
        fetch("/api/categories", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(newCategory),
        })
            .then((res) => {
                if (!res.ok) throw new Error("Failed to create category");
                return res.json();
            })
            .then(() => {
                setIsAddDialogOpen(false);
                setNewCategory({
                    categoryName: "",
                    description: "",
                    imageUrl: "",
                });
                fetchCategories(searchTerm, page);
                toast.success("Category created successfully", {
                    icon: <CircleCheck className="h-5 w-5 text-green-500" />,
                })
            })
            .catch((err) => alert(err.message));
    };

    const handleDeleteCategory = (id: number) => {
        fetch(`/api/categories/${id}`, {
            method: "DELETE",
        })
            .then((res) => {
                if (!res.ok) throw new Error("Failed to delete category");
                toast.success("Category deleted successfully", {
                    icon: <CircleCheck className="h-5 w-5 text-green-500" />,
                });
                fetchCategories(searchTerm, page);
            })
            .catch((err) => toast.error(err.message));
    };

    const handleEditCategory = (category: Category) => {
        setEditingCategory(category);
        setIsEditDialogOpen(true);
    };

    const handleUpdateCategory = () => {
        if (!editingCategory) return;

        fetch(`/api/categories/${editingCategory.categoryId}`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                categoryName: editingCategory.categoryName,
                description: editingCategory.description,
                imageUrl: editingCategory.imageUrl,
            }),
        })
            .then((res) => {
                if (!res.ok) throw new Error("Failed to update category");
                return res.json();
            })
            .then(() => {
                setIsEditDialogOpen(false);
                setEditingCategory(null);
                fetchCategories();
                toast.success("Category updated successfully", {
                    icon: <CircleCheck className="h-5 w-5 text-green-500" />,
                });
            })
            .catch((err) => toast.error(err.message));
    };

    const handleViewCategory = (category: Category) => {
        setViewingCategory(category);
        setIsViewDialogOpen(true);
    };

    const columns: ColumnDef<Category>[] = React.useMemo(
        () => [
            {
                id: "select",
                header: ({ table }) => (
                    <Checkbox
                        checked={
                            table.getIsAllPageRowsSelected() ||
                            (table.getIsSomePageRowsSelected() && "indeterminate")
                        }
                        onCheckedChange={(value) => table.toggleAllPageRowsSelected(!!value)}
                        aria-label="Select all"
                    />
                ),
                cell: ({ row }) => (
                    <Checkbox
                        checked={row.getIsSelected()}
                        onCheckedChange={(value) => row.toggleSelected(!!value)}
                        aria-label="Select row"
                    />
                ),
                enableSorting: false,
                enableHiding: false,
            },
            {
                accessorKey: "imageUrl",
                header: "Image",
                cell: ({ row }) => (
                    <img
                        src={row.getValue("imageUrl")}
                        alt={row.getValue("categoryName")}
                        className="h-10 w-10 object-cover rounded-md"
                    />
                ),
            },
            {
                accessorKey: "categoryName",
                header: ({ column }) => {
                    return (
                        <Button
                            variant="ghost"
                            onClick={() => column.toggleSorting(column.getIsSorted() === "asc")}
                            className="justify-start !px-0 text-left"
                        >
                            Name
                            <ArrowUpDown className="ml-2 h-4 w-4" />
                        </Button>
                    )
                },
                cell: ({ row }) => <div>{row.getValue("categoryName")}</div>,
            },
            {
                accessorKey: "description",
                header: "Description",
                cell: ({ row }) => <div className="truncate max-w-[200px]">{row.getValue("description")}</div>,
            },
            {
                id: "actions",
                enableHiding: false,
                cell: ({ row }) => {
                    const category = row.original

                    return (
                        <AlertDialog>
                            <DropdownMenu>
                                <DropdownMenuTrigger asChild>
                                    <Button variant="ghost" className="h-8 w-8 p-0">
                                        <span className="sr-only">Open menu</span>
                                        <MoreHorizontal className="h-4 w-4" />
                                    </Button>
                                </DropdownMenuTrigger>
                                <DropdownMenuContent align="end">
                                    <DropdownMenuGroup>
                                        <DropdownMenuItem onClick={() => handleViewCategory(category)}>
                                            View details
                                        </DropdownMenuItem>
                                        <DropdownMenuItem
                                            onClick={() => handleEditCategory(category)}
                                        >
                                            Edit category
                                        </DropdownMenuItem>
                                        <AlertDialogTrigger asChild>
                                            <DropdownMenuItem className="text-red-600 focus:text-red-600 focus:bg-red-50 cursor-pointer">
                                                <Trash2 className="mr-2 h-4 w-4" />
                                                Delete category
                                            </DropdownMenuItem>
                                        </AlertDialogTrigger>
                                    </DropdownMenuGroup>
                                </DropdownMenuContent>
                            </DropdownMenu>
                            <AlertDialogContent>
                                <AlertDialogHeader>
                                    <AlertDialogTitle>Are you absolutely sure?</AlertDialogTitle>
                                    <AlertDialogDescription>
                                        This action cannot be undone. This will permanently delete the category
                                        and remove it from our servers.
                                    </AlertDialogDescription>
                                </AlertDialogHeader>
                                <AlertDialogFooter>
                                    <AlertDialogCancel>Cancel</AlertDialogCancel>
                                    <AlertDialogAction
                                        onClick={() => handleDeleteCategory(category.categoryId)}
                                        className="bg-red-600 hover:bg-red-700"
                                    >
                                        Delete
                                    </AlertDialogAction>
                                </AlertDialogFooter>
                            </AlertDialogContent>
                        </AlertDialog>
                    )
                },
            },
        ],
        []
    );

    const table = useReactTable({
        data,
        columns,
        onSortingChange: setSorting,
        onColumnFiltersChange: setColumnFilters,
        getCoreRowModel: getCoreRowModel(),
        getPaginationRowModel: getPaginationRowModel(),
        getSortedRowModel: getSortedRowModel(),
        getFilteredRowModel: getFilteredRowModel(),
        onColumnVisibilityChange: setColumnVisibility,
        onRowSelectionChange: setRowSelection,
        state: {
            sorting,
            columnFilters,
            columnVisibility,
            rowSelection,
        },
    })

    return (
        <div className="w-full p-6">
            <div className="flex items-center py-4">
                <div className="flex w-full max-w-sm items-center space-x-2">
                    <Input
                        placeholder="Filter products..."
                        value={searchTerm}
                        onChange={(event) => {
                            setSearchTerm(event.target.value);
                            table.getColumn("categoryName")?.setFilterValue(event.target.value);
                        }}
                        onKeyDown={(e) => {
                            if (e.key === "Enter") {
                                handleSearch();
                            }
                        }}
                    />
                    <Button type="submit" size="icon" onClick={handleSearch}>
                        <Search className="h-4 w-4" />
                    </Button>
                </div>

                <Dialog open={isAddDialogOpen} onOpenChange={setIsAddDialogOpen}>
                    <DialogTrigger asChild>
                        <Button className="ml-2">
                            <Plus className="mr-2 h-4 w-4" /> Add Category
                        </Button>
                    </DialogTrigger>
                    <DialogContent className="sm:max-w-[425px]">
                        <DialogHeader>
                            <DialogTitle>Add New Category</DialogTitle>
                            <DialogDescription>
                                Create a new category for products.
                            </DialogDescription>
                        </DialogHeader>
                        <div className="grid gap-4 py-4">
                            <div className="flex flex-col gap-2">
                                <label htmlFor="categoryName" className="text-sm font-medium">
                                    Name
                                </label>
                                <Input
                                    id="categoryName"
                                    value={newCategory.categoryName}
                                    onChange={(e) => setNewCategory({ ...newCategory, categoryName: e.target.value })}
                                />
                            </div>
                            <div className="flex flex-col gap-2">
                                <label htmlFor="description" className="text-sm font-medium">
                                    Description
                                </label>
                                <Input
                                    id="description"
                                    value={newCategory.description}
                                    onChange={(e) => setNewCategory({ ...newCategory, description: e.target.value })}
                                />
                            </div>
                            <div className="flex flex-col gap-2">
                                <label htmlFor="imageUrl" className="text-sm font-medium">
                                    Image URL
                                </label>
                                <Input
                                    id="imageUrl"
                                    value={newCategory.imageUrl}
                                    onChange={(e) => setNewCategory({ ...newCategory, imageUrl: e.target.value })}
                                />
                            </div>
                        </div>
                        <DialogFooter>
                            <Button type="submit" className="w-full" onClick={handleCreateCategory}>Add Category</Button>
                        </DialogFooter>
                    </DialogContent>
                </Dialog>

                <Dialog open={isEditDialogOpen} onOpenChange={setIsEditDialogOpen}>
                    <DialogContent className="sm:max-w-[425px]">
                        <DialogHeader>
                            <DialogTitle>Edit Category</DialogTitle>
                            <DialogDescription>
                                Update category information.
                            </DialogDescription>
                        </DialogHeader>
                        <div className="grid gap-4 py-4">
                            <div className="flex flex-col gap-2">
                                <label htmlFor="editCategoryName" className="text-sm font-medium">
                                    Name
                                </label>
                                <Input
                                    id="editCategoryName"
                                    value={editingCategory?.categoryName || ""}
                                    onChange={(e) =>
                                        setEditingCategory((prev) =>
                                            prev ? { ...prev, categoryName: e.target.value } : null
                                        )
                                    }
                                />
                            </div>
                            <div className="flex flex-col gap-2">
                                <label htmlFor="editDescription" className="text-sm font-medium">
                                    Description
                                </label>
                                <Input
                                    id="editDescription"
                                    value={editingCategory?.description || ""}
                                    onChange={(e) =>
                                        setEditingCategory((prev) =>
                                            prev ? { ...prev, description: e.target.value } : null
                                        )
                                    }
                                />
                            </div>
                            <div className="flex flex-col gap-2">
                                <label htmlFor="editImageUrl" className="text-sm font-medium">
                                    Image URL
                                </label>
                                <Input
                                    id="editImageUrl"
                                    value={editingCategory?.imageUrl || ""}
                                    onChange={(e) =>
                                        setEditingCategory((prev) =>
                                            prev ? { ...prev, imageUrl: e.target.value } : null
                                        )
                                    }
                                />
                            </div>
                        </div>
                        <DialogFooter>
                            <Button type="submit" className="w-full" onClick={handleUpdateCategory}>
                                Save Changes
                            </Button>
                        </DialogFooter>
                    </DialogContent>
                </Dialog>

                <Dialog open={isViewDialogOpen} onOpenChange={setIsViewDialogOpen}>
                    <DialogContent className="sm:max-w-[425px]">
                        <DialogHeader>
                            <DialogTitle>Category Details</DialogTitle>
                        </DialogHeader>
                        <div className="grid gap-4 py-4">
                            <div className="flex flex-col gap-2">
                                <label className="text-sm font-medium">Name</label>
                                <div className="text-sm text-muted-foreground">{viewingCategory?.categoryName}</div>
                            </div>
                            <div className="flex flex-col gap-2">
                                <label className="text-sm font-medium">Description</label>
                                <div className="text-sm text-muted-foreground">{viewingCategory?.description}</div>
                            </div>
                            <div className="flex flex-col gap-2">
                                <label className="text-sm font-medium">Image</label>
                                {viewingCategory?.imageUrl && (
                                    <img
                                        src={viewingCategory.imageUrl}
                                        alt={viewingCategory.categoryName}
                                        className="h-40 w-full object-cover rounded-md"
                                    />
                                )}
                            </div>
                        </div>
                    </DialogContent>
                </Dialog>

                <DropdownMenu>
                    <DropdownMenuTrigger asChild>
                        <Button variant="outline" className="ml-auto">
                            Columns <ChevronDown className="ml-2 h-4 w-4" />
                        </Button>
                    </DropdownMenuTrigger>
                    <DropdownMenuContent align="end">
                        <DropdownMenuGroup>
                            {table
                                .getAllColumns()
                                .filter((column) => column.getCanHide())
                                .map((column) => {
                                    return (
                                        <DropdownMenuCheckboxItem
                                            key={column.id}
                                            className="capitalize"
                                            checked={column.getIsVisible()}
                                            onCheckedChange={(value) =>
                                                column.toggleVisibility(!!value)
                                            }
                                        >
                                            {column.id}
                                        </DropdownMenuCheckboxItem>
                                    )
                                })}
                        </DropdownMenuGroup>
                    </DropdownMenuContent>
                </DropdownMenu>
            </div>
            <div className="rounded-md border">
                <Table>
                    <TableHeader>
                        {table.getHeaderGroups().map((headerGroup) => (
                            <TableRow key={headerGroup.id}>
                                {headerGroup.headers.map((header) => {
                                    return (
                                        <TableHead key={header.id}>
                                            {header.isPlaceholder
                                                ? null
                                                : flexRender(
                                                    header.column.columnDef.header,
                                                    header.getContext()
                                                )}
                                        </TableHead>
                                    )
                                })}
                            </TableRow>
                        ))}
                    </TableHeader>
                    <TableBody>
                        {loading ? (
                            <TableRow>
                                <TableCell colSpan={columns.length} className="h-24 text-center">
                                    Loading...
                                </TableCell>
                            </TableRow>
                        ) : table.getRowModel().rows?.length ? (
                            table.getRowModel().rows.map((row) => (
                                <TableRow
                                    key={row.id}
                                    data-state={row.getIsSelected() && "selected"}
                                >
                                    {row.getVisibleCells().map((cell) => (
                                        <TableCell key={cell.id}>
                                            {flexRender(
                                                cell.column.columnDef.cell,
                                                cell.getContext()
                                            )}
                                        </TableCell>
                                    ))}
                                </TableRow>
                            ))
                        ) : (
                            <TableRow>
                                <TableCell
                                    colSpan={columns.length}
                                    className="h-24 text-center"
                                >
                                    No results.
                                </TableCell>
                            </TableRow>
                        )}
                    </TableBody>
                </Table>
            </div>
            <div className="flex items-center justify-end space-x-2 py-4">
                <div className="text-muted-foreground flex-1 text-sm">
                    {table.getFilteredSelectedRowModel().rows.length} of{" "}
                    {table.getFilteredRowModel().rows.length} row(s) selected.
                </div>
                <Pagination className="w-auto h-auto shrink-0 space-x-2 justify-end">
                    <PaginationContent>
                        <PaginationItem>
                            <PaginationPrevious
                                onClick={() => handlePageChange(page - 1)}
                                className={page <= 1 ? "pointer-events-none opacity-50" : "cursor-pointer"}
                            />
                        </PaginationItem>
                        <PaginationItem>
                            <PaginationNext
                                onClick={() => handlePageChange(page + 1)}
                                className={page >= totalPages ? "pointer-events-none opacity-50" : "cursor-pointer"}
                            />
                        </PaginationItem>
                    </PaginationContent>
                </Pagination>
            </div>
        </div>
    )
}
