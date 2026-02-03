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
import { ArrowUpDown, ChevronDown, CircleCheck, MoreHorizontal, Plus, Search } from "lucide-react"
import {
    DialogTrigger,
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
} from "@/components/ui/dialog"
import {
    Select,
    SelectContent,
    SelectGroup,
    SelectItem,
    SelectLabel,
    SelectTrigger,
    SelectValue,
} from "@/components/ui/select"
import {
    Pagination,
    PaginationContent,
    PaginationItem,
    PaginationNext,
    PaginationPrevious,
} from "@/components/ui/pagination"
import type { ApiResponse } from "@/types/api"

export interface InventoryItem {
    id: number;
    productId: number;
    name: string;
    description: string;
    price: number;
    category: {
        categoryId: number;
        categoryName: string;
        description?: string;
        imageUrl?: string;
    };
    image: string;
    quantity: number;
}



export default function AdminProductsPage() {
    const [data, setData] = React.useState<InventoryItem[]>([])
    const [loading, setLoading] = React.useState(true)
    const [sorting, setSorting] = React.useState<SortingState>([])
    const [columnFilters, setColumnFilters] = React.useState<ColumnFiltersState>(
        []
    )
    const [columnVisibility, setColumnVisibility] =
        React.useState<VisibilityState>({})
    const [rowSelection, setRowSelection] = React.useState({})

    // Add Inventory State
    const [isAddDialogOpen, setIsAddDialogOpen] = React.useState(false);
    const [categories, setCategories] = React.useState<{ categoryId: number; categoryName: string }[]>([]);
    const [newInventory, setNewInventory] = React.useState({
        name: "",
        description: "",
        price: "",
        categoryId: "",
        imageUrl: "",
        quantity: "",
    });

    // Edit Inventory State
    const [isEditDialogOpen, setIsEditDialogOpen] = React.useState(false);
    const [editingItem, setEditingItem] = React.useState<InventoryItem | null>(null);

    // View Inventory State
    const [isViewDialogOpen, setIsViewDialogOpen] = React.useState(false);
    const [viewingItem, setViewingItem] = React.useState<InventoryItem | null>(null);

    // Search and Pagination State
    const [searchTerm, setSearchTerm] = React.useState("")
    const [page, setPage] = React.useState(1)
    const [totalPages, setTotalPages] = React.useState(0)

    const fetchInventory = (query = "", pageIndex = 1) => {
        setLoading(true);
        fetch(`/api/inventories?query=${encodeURIComponent(query)}&page=${pageIndex}`)
            .then((res) => {
                if (!res.ok) throw new Error("Failed to fetch inventory");
                return res.json();
            })
            .then((payload: ApiResponse<any>) => {
                const inventory = (payload.data.content || []).map((item: any) => ({
                    id: item.id,
                    productId: item.product.id,
                    name: item.product.name ?? "Unnamed Product",
                    description: item.product.description,
                    price: item.product.price,
                    category: item.product.category,
                    image: item.product.imageUrl,
                    quantity: item.quantity,
                }));
                setData(inventory);
                // Access totalPages from the nested page object
                setTotalPages(payload.data.page?.totalPages || 0);
            })
            .catch((err) => console.error("Error fetching inventory:", err))
            .finally(() => setLoading(false));
    };

    const handleSearch = () => {
        setPage(1);
        fetchInventory(searchTerm, 1);
    };

    const handlePageChange = (newPage: number) => {
        if (newPage >= 1 && newPage <= totalPages) {
            setPage(newPage);
            fetchInventory(searchTerm, newPage);
        }
    };

    const fetchCategories = () => {
        fetch("/api/categories")
            .then((res) => res.json())
            .then((payload) => {
                setCategories(payload.data.content || []);
            })
            .catch((err) => console.error("Error fetching categories:", err));
    };

    React.useEffect(() => {
        fetchInventory();
        fetchCategories();
    }, []);

    const handleCreateInventory = () => {
        const payload = {
            quantity: parseInt(newInventory.quantity),
            product: {
                name: newInventory.name,
                description: newInventory.description,
                price: parseFloat(newInventory.price),
                categoryId: parseInt(newInventory.categoryId),
                imageUrl: newInventory.imageUrl,
            }
        };

        fetch("/api/inventories", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload),
        })
            .then((res) => {
                if (!res.ok) throw new Error("Failed to create inventory");
                return res.json();
            })
            .then(() => {
                setIsAddDialogOpen(false);
                setNewInventory({
                    name: "",
                    description: "",
                    price: "",
                    categoryId: "",
                    imageUrl: "",
                    quantity: "",
                });
                fetchInventory(searchTerm, page);
            })
            .catch((err) => alert(err.message));
    };

    const handleEditItem = (item: InventoryItem) => {
        setEditingItem(item);
        setIsEditDialogOpen(true);
    };

    const handleUpdateItem = () => {
        if (!editingItem) return;

        const payload = {
            quantity: editingItem.quantity,
            product: {
                name: editingItem.name,
                description: editingItem.description,
                price: editingItem.price,
                categoryId: editingItem.category.categoryId,
                imageUrl: editingItem.image,
            }
        };

        fetch(`/api/inventories/${editingItem.id}`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload),
        })
            .then((res) => {
                if (!res.ok) throw new Error("Failed to update inventory");
                return res.json();
            })
            .then(() => {
                setIsEditDialogOpen(false);
                setEditingItem(null);
                fetchInventory();
                toast.success("Inventory updated successfully", {
                    icon: <CircleCheck className="h-5 w-5 text-green-500" />,
                });
            })
            .catch((err) => toast.error(err.message));
    };

    const handleViewItem = (item: InventoryItem) => {
        setViewingItem(item);
        setIsViewDialogOpen(true);
    };

    const columns: ColumnDef<InventoryItem>[] = React.useMemo(
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
                accessorKey: "image",
                header: "Image",
                cell: ({ row }) => (
                    <img
                        src={row.getValue("image")}
                        alt={row.getValue("name")}
                        className="h-10 w-10 object-cover rounded-md"
                    />
                ),
            },
            {
                accessorKey: "name",
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
                cell: ({ row }) => <div className="lowercase">{row.getValue("name")}</div>,
            },
            {
                accessorKey: "description",
                header: "Description",
                cell: ({ row }) => <div className="truncate max-w-[200px]">{row.getValue("description")}</div>,
            },
            {
                accessorKey: "category",
                header: "Category",
                cell: ({ row }) => {
                    const category = row.getValue("category") as InventoryItem["category"];
                    return <div className="text-left">{category?.categoryName || "N/A"}</div>;
                },
            },
            {
                accessorKey: "quantity",
                header: ({ column }) => {
                    return (
                        <Button
                            variant="ghost"
                            onClick={() => column.toggleSorting(column.getIsSorted() === "asc")}
                            className="justify-start !px-0 text-left"
                        >
                            Quantity
                            <ArrowUpDown className="ml-2 h-4 w-4" />
                        </Button>
                    )
                },
                cell: ({ row }) => <div className="text-left font-medium">{row.getValue("quantity")}</div>,
            },
            {
                accessorKey: "price",
                header: () => <div className="text-left">Price</div>,
                cell: ({ row }) => {
                    const amount = parseFloat(row.getValue("price"))

                    const formatted = new Intl.NumberFormat("en-US", {
                        style: "currency",
                        currency: "USD",
                    }).format(amount)

                    return <div className="text-left font-medium">{formatted}</div>
                },
            },
            {
                id: "actions",
                enableHiding: false,
                cell: ({ row }) => {
                    const item = row.original

                    return (
                        <DropdownMenu>
                            <DropdownMenuTrigger asChild>
                                <Button variant="ghost" className="h-8 w-8 p-0">
                                    <span className="sr-only">Open menu</span>
                                    <MoreHorizontal className="h-4 w-4" />
                                </Button>
                            </DropdownMenuTrigger>
                            <DropdownMenuContent align="end">
                                <DropdownMenuGroup>
                                    <DropdownMenuItem onClick={() => handleViewItem(item)}>
                                        View details
                                    </DropdownMenuItem>
                                    <DropdownMenuItem onClick={() => handleEditItem(item)}>
                                        Edit product
                                    </DropdownMenuItem>
                                </DropdownMenuGroup>
                            </DropdownMenuContent>
                        </DropdownMenu>
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
                            table.getColumn("name")?.setFilterValue(event.target.value);
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
                            <Plus className="mr-2 h-4 w-4" /> Add Inventory
                        </Button>
                    </DialogTrigger>
                    <DialogContent className="sm:max-w-[425px] overflow-y-auto max-h-[90vh]">
                        <DialogHeader>
                            <DialogTitle>Add New Inventory</DialogTitle>
                            <DialogDescription>
                                Create a new product and add it to inventory.
                            </DialogDescription>
                        </DialogHeader>
                        <div className="grid gap-4 py-4">
                            <div className="flex flex-col gap-2">
                                <label htmlFor="name" className="text-sm font-medium">
                                    Name
                                </label>
                                <Input
                                    id="name"
                                    value={newInventory.name}
                                    onChange={(e) => setNewInventory({ ...newInventory, name: e.target.value })}
                                />
                            </div>
                            <div className="flex flex-col gap-2">
                                <label htmlFor="description" className="text-sm font-medium">
                                    Description
                                </label>
                                <Input
                                    id="description"
                                    value={newInventory.description}
                                    onChange={(e) => setNewInventory({ ...newInventory, description: e.target.value })}
                                />
                            </div>
                            <div className="flex flex-col gap-2">
                                <label htmlFor="price" className="text-sm font-medium">
                                    Price
                                </label>
                                <Input
                                    id="price"
                                    type="number"
                                    value={newInventory.price}
                                    onChange={(e) => setNewInventory({ ...newInventory, price: e.target.value })}
                                />
                            </div>
                            <div className="flex flex-col gap-2">
                                <label htmlFor="quantity" className="text-sm font-medium">
                                    Quantity
                                </label>
                                <Input
                                    id="quantity"
                                    type="number"
                                    value={newInventory.quantity}
                                    onChange={(e) => setNewInventory({ ...newInventory, quantity: e.target.value })}
                                />
                            </div>
                            <div className="flex flex-col gap-2">
                                <label htmlFor="category" className="text-sm font-medium">
                                    Category
                                </label>
                                <Select
                                    value={newInventory.categoryId ? String(newInventory.categoryId) : undefined}
                                    onValueChange={(value) => setNewInventory({ ...newInventory, categoryId: value })}
                                >
                                    <SelectTrigger className="w-full">
                                        <SelectValue placeholder="Select a category" />
                                    </SelectTrigger>
                                    <SelectContent>
                                        <SelectGroup>
                                            <SelectLabel>Categories</SelectLabel>
                                            {categories.map((cat) => (
                                                <SelectItem key={cat.categoryId} value={String(cat.categoryId)}>
                                                    {cat.categoryName}
                                                </SelectItem>
                                            ))}
                                        </SelectGroup>
                                    </SelectContent>
                                </Select>
                            </div>
                            <div className="flex flex-col gap-2">
                                <label htmlFor="image" className="text-sm font-medium">
                                    Image URL
                                </label>
                                <Input
                                    id="image"
                                    value={newInventory.imageUrl}
                                    onChange={(e) => setNewInventory({ ...newInventory, imageUrl: e.target.value })}
                                />
                            </div>
                        </div>
                        <DialogFooter>
                            <Button type="submit" className="w-full" onClick={handleCreateInventory}>Add Inventory</Button>
                        </DialogFooter>
                    </DialogContent>
                </Dialog>

                <Dialog open={isEditDialogOpen} onOpenChange={setIsEditDialogOpen}>
                    <DialogContent className="sm:max-w-[425px] overflow-y-auto max-h-[90vh]">
                        <DialogHeader>
                            <DialogTitle>Edit Inventory</DialogTitle>
                            <DialogDescription>
                                Update product and inventory details.
                            </DialogDescription>
                        </DialogHeader>
                        <div className="grid gap-4 py-4">
                            <div className="flex flex-col gap-2">
                                <label htmlFor="editName" className="text-sm font-medium">
                                    Name
                                </label>
                                <Input
                                    id="editName"
                                    value={editingItem?.name || ""}
                                    onChange={(e) =>
                                        setEditingItem((prev) =>
                                            prev ? { ...prev, name: e.target.value } : null
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
                                    value={editingItem?.description || ""}
                                    onChange={(e) =>
                                        setEditingItem((prev) =>
                                            prev ? { ...prev, description: e.target.value } : null
                                        )
                                    }
                                />
                            </div>
                            <div className="flex flex-col gap-2">
                                <label htmlFor="editPrice" className="text-sm font-medium">
                                    Price
                                </label>
                                <Input
                                    id="editPrice"
                                    type="number"
                                    value={editingItem?.price || ""}
                                    onChange={(e) =>
                                        setEditingItem((prev) =>
                                            prev ? { ...prev, price: parseFloat(e.target.value) } : null
                                        )
                                    }
                                />
                            </div>
                            <div className="flex flex-col gap-2">
                                <label htmlFor="editQuantity" className="text-sm font-medium">
                                    Quantity
                                </label>
                                <Input
                                    id="editQuantity"
                                    type="number"
                                    value={editingItem?.quantity || ""}
                                    onChange={(e) =>
                                        setEditingItem((prev) =>
                                            prev ? { ...prev, quantity: parseInt(e.target.value) } : null
                                        )
                                    }
                                />
                            </div>
                            <div className="flex flex-col gap-2">
                                <label htmlFor="editCategory" className="text-sm font-medium">
                                    Category
                                </label>
                                <Select
                                    value={editingItem?.category?.categoryId ? String(editingItem.category.categoryId) : undefined}
                                    onValueChange={(value) => {
                                        const selectedCategory = categories.find(c => String(c.categoryId) === value);
                                        if (selectedCategory) {
                                            setEditingItem((prev) =>
                                                prev ? { ...prev, category: { ...prev.category, categoryId: selectedCategory.categoryId, categoryName: selectedCategory.categoryName } } : null
                                            )
                                        }
                                    }}
                                >
                                    <SelectTrigger className="w-full">
                                        <SelectValue placeholder="Select a category" />
                                    </SelectTrigger>
                                    <SelectContent>
                                        <SelectGroup>
                                            <SelectLabel>Categories</SelectLabel>
                                            {categories.map((cat) => (
                                                <SelectItem key={cat.categoryId} value={String(cat.categoryId)}>
                                                    {cat.categoryName}
                                                </SelectItem>
                                            ))}
                                        </SelectGroup>
                                    </SelectContent>
                                </Select>
                            </div>
                            <div className="flex flex-col gap-2">
                                <label htmlFor="editImage" className="text-sm font-medium">
                                    Image URL
                                </label>
                                <Input
                                    id="editImage"
                                    value={editingItem?.image || ""}
                                    onChange={(e) =>
                                        setEditingItem((prev) =>
                                            prev ? { ...prev, image: e.target.value } : null
                                        )
                                    }
                                />
                            </div>
                        </div>
                        <DialogFooter>
                            <Button type="submit" className="w-full" onClick={handleUpdateItem}>
                                Save Changes
                            </Button>
                        </DialogFooter>
                    </DialogContent>
                </Dialog>

                <Dialog open={isViewDialogOpen} onOpenChange={setIsViewDialogOpen}>
                    <DialogContent className="sm:max-w-[425px]">
                        <DialogHeader>
                            <DialogTitle>Product Details</DialogTitle>
                        </DialogHeader>
                        <div className="grid gap-4 py-4">
                            <div className="flex flex-col gap-2">
                                <label className="text-sm font-medium">Name</label>
                                <div className="text-sm text-muted-foreground">{viewingItem?.name}</div>
                            </div>
                            <div className="flex flex-col gap-2">
                                <label className="text-sm font-medium">Description</label>
                                <div className="text-sm text-muted-foreground">{viewingItem?.description}</div>
                            </div>
                            <div className="flex flex-col gap-2">
                                <label className="text-sm font-medium">Price</label>
                                <div className="text-sm text-muted-foreground">
                                    {new Intl.NumberFormat("en-US", { style: "currency", currency: "USD" }).format(viewingItem?.price || 0)}
                                </div>
                            </div>
                            <div className="flex flex-col gap-2">
                                <label className="text-sm font-medium">Quantity</label>
                                <div className="text-sm text-muted-foreground">{viewingItem?.quantity}</div>
                            </div>
                            <div className="flex flex-col gap-2">
                                <label className="text-sm font-medium">Category</label>
                                <div className="text-sm text-muted-foreground">{viewingItem?.category?.categoryName}</div>
                            </div>
                            <div className="flex flex-col gap-2">
                                <label className="text-sm font-medium">Image</label>
                                {viewingItem?.image && (
                                    <img
                                        src={viewingItem.image}
                                        alt={viewingItem.name}
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
                        {/* Optionally add page numbers here if needed, keeping it simple for now as requested */}
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
