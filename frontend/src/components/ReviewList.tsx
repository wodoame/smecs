import { StarRating } from "@/components/ui/star-rating";
import { Card, CardContent } from "@/components/ui/card";
import { Separator } from "@/components/ui/separator";
import { formatDistanceToNow } from "@/lib/date-utils";

interface Review {
    id: number;
    userId: number;
    productId: number;
    rating: number;
    comment: string;
    createdAt: string;
    user?: {
        username: string;
    };
}

interface ReviewListProps {
    reviews: Review[];
    isLoading?: boolean;
}

export function ReviewList({ reviews, isLoading }: ReviewListProps) {
    if (isLoading) {
        return (
            <div className="space-y-4">
                {[1, 2, 3].map((i) => (
                    <Card key={i} className="animate-pulse">
                        <CardContent className="p-6">
                            <div className="h-4 bg-zinc-200 dark:bg-zinc-800 rounded w-1/4 mb-3"></div>
                            <div className="h-3 bg-zinc-200 dark:bg-zinc-800 rounded w-3/4 mb-2"></div>
                            <div className="h-3 bg-zinc-200 dark:bg-zinc-800 rounded w-1/2"></div>
                        </CardContent>
                    </Card>
                ))}
            </div>
        );
    }

    if (reviews.length === 0) {
        return (
            <div className="text-center py-12 text-zinc-500 dark:text-zinc-400">
                <p className="text-lg">No reviews yet</p>
                <p className="text-sm mt-2">Be the first to review this product!</p>
            </div>
        );
    }

    return (
        <div className="space-y-4">
            {reviews.map((review) => (
                <Card key={review.id} className="border-zinc-200 dark:border-zinc-800">
                    <CardContent className="p-6">
                        <div className="flex items-start justify-between mb-3">
                            <div className="flex items-center gap-3">
                                <div className="w-10 h-10 rounded-full bg-gradient-to-br from-yellow-400 to-yellow-600 flex items-center justify-center text-white font-semibold">
                                    {review.user?.username.charAt(0).toUpperCase() || review.userId.toString().slice(-2)}
                                </div>
                                <div>
                                    <p className="font-medium text-sm">{review.user?.username || `User #${review.userId}`}</p>
                                    <p className="text-xs text-zinc-500 dark:text-zinc-400">
                                        {formatDistanceToNow(review.createdAt)}
                                    </p>
                                </div>
                            </div>
                            <StarRating rating={review.rating} size={18} />
                        </div>
                        <Separator className="mb-3" />
                        <p className="text-sm text-zinc-700 dark:text-zinc-300 leading-relaxed">
                            {review.comment}
                        </p>
                    </CardContent>
                </Card>
            ))}
        </div>
    );
}
