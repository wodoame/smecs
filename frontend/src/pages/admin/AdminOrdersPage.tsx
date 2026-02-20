import * as React from "react"
import { toast } from "sonner"
import { Button } from "@/components/ui/button"
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
import { ArrowUpDown, ChevronDown, MoreHorizontal, Search, ListRestart, LogIn, ShieldAlert, Check } from "lucide-react"
import {
    Dialog,
    DialogContent,
    DialogHeader,
    DialogTitle,
} from "@/components/ui/dialog"
import {
    Pagination,
    PaginationContent,
    PaginationItem,
    PaginationNext,
    PaginationPrevious,
} from "@/components/ui/pagination"
import { auth } from "@/lib/auth"
import { Link, useLocation } from "react-router-dom"

export interface Order {
    id: number;
    userId: number;
    totalAmount: number;
    status: string;
    createdAt: string;
}

export default function AdminOrdersPage() {
    const location = useLocation();
    const [data, setData] = React.useState<Order[]>([])
    const [loading, setLoading] = React.useState(true)
    const [sorting, setSorting] = React.useState<SortingState>([])
    const [columnFilters, setColumnFilters] = React.useState<ColumnFiltersState>(
        []
    )
    const [columnVisibility, setColumnVisibility] =
        React.useState<VisibilityState>({})
    const [rowSelection, setRowSelection] = React.useState({})

    // View Order State
    const [isViewDialogOpen, setIsViewDialogOpen] = React.useState(false);
    const [viewingOrder, setViewingOrder] = React.useState<Order | null>(null);

    // Search and Pagination State
    const [searchTerm, setSearchTerm] = React.useState("")
    const [globalFilter, setGlobalFilter] = React.useState("")

    // Error State
    const [authError, setAuthError] = React.useState<'unauthorized' | 'forbidden' | null>(null)

    const fetchOrders = () => {
        setLoading(true);
        const user = auth.getUser();

        if (!user) {
            setAuthError('unauthorized');
            setLoading(false);
            return;
        }

        fetch(`/api/orders?size=100`, {
            headers: {
                "Authorization": `Bearer ${user.token}`,
            },
        })
            .then((res) => {
                if (res.status === 401) {
                    setAuthError('unauthorized');
                    throw new Error("Unauthorized - Please log in");
                }
                if (res.status === 403) {
                    setAuthError('forbidden');
                    throw new Error("Forbidden - You don't have permission to access this resource");
                }
                if (!res.ok) throw new Error("Failed to fetch orders");
                return res.json();
            })
            .then((payload) => {
                // The API returns { content: [...], page: {...} } directly
                // not wrapped in a data property like ApiResponse<T>
                setData(payload.content || []);
            })
            .catch((err) => {
                console.error("Error fetching orders:", err);
                if (!authError) {
                    toast.error("Failed to load orders");
                }
            })
            .finally(() => setLoading(false));
    };

    const handleUpdateStatus = async (orderId: number, newStatus: string) => {
        const user = auth.getUser();
        if (!user) {
            toast.error("You must be logged in to perform this action");
            return;
        }

        try {
            const res = await fetch(`/api/orders/${orderId}`, {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${user.token}`,
                },
                body: JSON.stringify({ status: newStatus }),
            });

            if (!res.ok) {
                let errorMsg = "Failed to update status";
                try {
                    const data = await res.json();
                    errorMsg = data.message || errorMsg;
                } catch (e) { }
                throw new Error(errorMsg);
            }

            toast.success("Order status updated successfully");
            // Refresh the list after update
            fetchOrders();
        } catch (error: any) {
            toast.error(error.message || "An error occurred while updating status");
        }
    };

    React.useEffect(() => {
        fetchOrders();
    }, []);

    const handleViewOrder = (order: Order) => {
        setViewingOrder(order);
        setIsViewDialogOpen(true);
    };

    const columns: ColumnDef<Order>[] = React.useMemo(
        () => [
            {
                accessorKey: "id",
                header: ({ column }) => {
                    return (
                        <Button
                            variant="ghost"
                            onClick={() => column.toggleSorting(column.getIsSorted() === "asc")}
                            className="justify-start !px-0 text-left"
                        >
                            Order ID
                            <ArrowUpDown className="ml-2 h-4 w-4" />
                        </Button>
                    )
                },
                cell: ({ row }) => <div className="font-medium">#{row.getValue("id")}</div>,
            },
            {
                accessorKey: "userId",
                header: ({ column }) => {
                    return (
                        <Button
                            variant="ghost"
                            onClick={() => column.toggleSorting(column.getIsSorted() === "asc")}
                            className="justify-start !px-0 text-left"
                        >
                            User ID
                            <ArrowUpDown className="ml-2 h-4 w-4" />
                        </Button>
                    )
                },
                cell: ({ row }) => <div className="font-medium">{row.getValue("userId")}</div>,
            },
            {
                accessorKey: "totalAmount",
                header: () => <div className="text-left">Total Amount</div>,
                cell: ({ row }) => {
                    const amount = parseFloat(row.getValue("totalAmount"))

                    const formatted = new Intl.NumberFormat("en-US", {
                        style: "currency",
                        currency: "USD",
                    }).format(amount)

                    return <div className="text-left font-medium">{formatted}</div>
                },
            },
            {
                accessorKey: "status",
                header: "Status",
                cell: ({ row }) => {
                    const status = row.getValue("status") as string;
                    const orderId = row.original.id;
                    const statusColors: Record<string, string> = {
                        pending: "bg-yellow-100 text-yellow-800",
                        shipped: "bg-purple-100 text-purple-800",
                        delivered: "bg-green-100 text-green-800",
                        cancelled: "bg-red-100 text-red-800",
                    };
                    const colorClass = statusColors[status.toLowerCase()] || "bg-gray-100 text-gray-800";

                    return (
                        <DropdownMenu>
                            <DropdownMenuTrigger className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium cursor-pointer ${colorClass}`}>
                                {status}
                                <ChevronDown className="ml-1 h-3 w-3" />
                            </DropdownMenuTrigger>
                            <DropdownMenuContent align="start">
                                <DropdownMenuItem onClick={() => handleUpdateStatus(orderId, "pending")}>
                                    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${statusColors["pending"]}`}>
                                        pending
                                    </span>
                                    {status.toLowerCase() === "pending" && <Check className="ml-auto h-4 w-4" />}
                                </DropdownMenuItem>
                                <DropdownMenuItem onClick={() => handleUpdateStatus(orderId, "shipped")}>
                                    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${statusColors["shipped"]}`}>
                                        shipped
                                    </span>
                                    {status.toLowerCase() === "shipped" && <Check className="ml-auto h-4 w-4" />}
                                </DropdownMenuItem>
                                <DropdownMenuItem onClick={() => handleUpdateStatus(orderId, "delivered")}>
                                    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${statusColors["delivered"]}`}>
                                        delivered
                                    </span>
                                    {status.toLowerCase() === "delivered" && <Check className="ml-auto h-4 w-4" />}
                                </DropdownMenuItem>
                                <DropdownMenuItem onClick={() => handleUpdateStatus(orderId, "cancelled")}>
                                    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${statusColors["cancelled"]}`}>
                                        cancelled
                                    </span>
                                    {status.toLowerCase() === "cancelled" && <Check className="ml-auto h-4 w-4" />}
                                </DropdownMenuItem>
                            </DropdownMenuContent>
                        </DropdownMenu>
                    );
                },
            },
            {
                accessorKey: "createdAt",
                header: ({ column }) => {
                    return (
                        <Button
                            variant="ghost"
                            onClick={() => column.toggleSorting(column.getIsSorted() === "asc")}
                            className="justify-start !px-0 text-left"
                        >
                            Order Date
                            <ArrowUpDown className="ml-2 h-4 w-4" />
                        </Button>
                    )
                },
                cell: ({ row }) => {
                    const date = new Date(row.getValue("createdAt"));
                    return <div>{date.toLocaleDateString()}</div>;
                },
            },
            {
                id: "actions",
                enableHiding: false,
                cell: ({ row }) => {
                    const order = row.original

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
                                    <DropdownMenuItem onClick={() => handleViewOrder(order)}>
                                        View details
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
        onGlobalFilterChange: setGlobalFilter,
        getCoreRowModel: getCoreRowModel(),
        getPaginationRowModel: getPaginationRowModel(),
        getSortedRowModel: getSortedRowModel(),
        getFilteredRowModel: getFilteredRowModel(),
        onColumnVisibilityChange: setColumnVisibility,
        onRowSelectionChange: setRowSelection,
        globalFilterFn: (row, _columnId, filterValue) => {
            const searchValue = filterValue.toLowerCase();
            const id = row.getValue<number>("id")?.toString() || "";
            const userId = row.getValue<number>("userId")?.toString() || "";
            const status = row.getValue<string>("status")?.toLowerCase() || "";

            return id.includes(searchValue) || userId.includes(searchValue) || status.includes(searchValue);
        },
        state: {
            sorting,
            columnFilters,
            columnVisibility,
            rowSelection,
            globalFilter,
        },
    })

    // Show error screens for auth issues
    if (authError === 'unauthorized') {
        const loginUrl = `/login?next=${encodeURIComponent(location.pathname)}`;
        return (
            <div className="flex flex-col items-center justify-center h-[70vh] gap-4">
                <LogIn className="h-16 w-16 text-muted-foreground" />
                <h2 className="text-2xl font-bold">Unauthorized</h2>
                <p className="text-muted-foreground">You need to be logged in to view your orders.</p>
                <Button asChild>
                    <Link to={loginUrl}>Go to Login</Link>
                </Button>
            </div>
        );
    }

    if (authError === 'forbidden') {
        return (
            <div className="flex flex-col items-center justify-center h-[70vh] gap-4">
                <ShieldAlert className="h-16 w-16 text-destructive" />
                <h2 className="text-2xl font-bold">Access Forbidden</h2>
                <p className="text-muted-foreground">You don't have permission to access this page.</p>
                <p className="text-sm text-muted-foreground">Please contact an administrator if you believe this is an error.</p>
            </div>
        );
    }

    return (
        <div className="w-full p-6">
            <div className="mb-6">
                <h1 className="text-3xl font-bold">All Orders</h1>
                <p className="text-muted-foreground">Manage all user orders across the system</p>
            </div>

            <div className="flex items-center py-4">
                <div className="flex w-full max-w-sm items-center space-x-2">
                    <Input
                        placeholder="Filter orders..."
                        value={searchTerm}
                        onChange={(event) => {
                            const value = event.target.value;
                            setSearchTerm(value);
                            setGlobalFilter(value);
                        }}
                    />
                    <Button type="submit" size="icon" onClick={() => setGlobalFilter(searchTerm)}>
                        <Search className="h-4 w-4" />
                    </Button>
                    <Button variant="outline" onClick={() => {
                        setSearchTerm("");
                        setGlobalFilter("");
                    }}>
                        <ListRestart className="h-4 w-4 mr-2" />
                        Show All
                    </Button>
                </div>

                <Dialog open={isViewDialogOpen} onOpenChange={setIsViewDialogOpen}>
                    <DialogContent className="sm:max-w-[425px]">
                        <DialogHeader>
                            <DialogTitle>Order Details</DialogTitle>
                        </DialogHeader>
                        <div className="grid gap-4 py-4">
                            <div className="flex flex-col gap-2">
                                <label className="text-sm font-medium">Order ID</label>
                                <div className="text-sm text-muted-foreground">#{viewingOrder?.id}</div>
                            </div>
                            <div className="flex flex-col gap-2">
                                <label className="text-sm font-medium">User ID</label>
                                <div className="text-sm text-muted-foreground">{viewingOrder?.userId}</div>
                            </div>
                            <div className="flex flex-col gap-2">
                                <label className="text-sm font-medium">Total Amount</label>
                                <div className="text-sm text-muted-foreground">
                                    {viewingOrder?.totalAmount && new Intl.NumberFormat("en-US", {
                                        style: "currency",
                                        currency: "USD",
                                    }).format(viewingOrder.totalAmount)}
                                </div>
                            </div>
                            <div className="flex flex-col gap-2">
                                <label className="text-sm font-medium">Status</label>
                                <div className="text-sm text-muted-foreground">{viewingOrder?.status}</div>
                            </div>
                            <div className="flex flex-col gap-2">
                                <label className="text-sm font-medium">Order Date</label>
                                <div className="text-sm text-muted-foreground">
                                    {viewingOrder?.createdAt && new Date(viewingOrder.createdAt).toLocaleString()}
                                </div>
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
                                    No orders found.
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
                                onClick={() => table.previousPage()}
                                className={!table.getCanPreviousPage() ? "pointer-events-none opacity-50" : "cursor-pointer"}
                            />
                        </PaginationItem>
                        <PaginationItem>
                            <PaginationNext
                                onClick={() => table.nextPage()}
                                className={!table.getCanNextPage() ? "pointer-events-none opacity-50" : "cursor-pointer"}
                            />
                        </PaginationItem>
                    </PaginationContent>
                </Pagination>
            </div>
        </div>
    )
}
