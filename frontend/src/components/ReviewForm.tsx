import { useState } from "react";
import { StarRating } from "@/components/ui/star-rating";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Label } from "@/components/ui/label";
import { useAuth } from "@/hooks/use-auth";

interface ReviewFormProps {
    productId: number;
    onSubmitSuccess: () => void;
}

export function ReviewForm({ productId, onSubmitSuccess }: ReviewFormProps) {
    const { user } = useAuth();
    const [rating, setRating] = useState(0);
    const [comment, setComment] = useState("");
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError(null);

        if (rating === 0) {
            setError("Please select a rating");
            return;
        }

        if (!comment.trim()) {
            setError("Please write a comment");
            return;
        }

        setIsSubmitting(true);

        try {
            // Check if user is authenticated
            if (!user) {
                setError("You must be logged in to submit a review");
                setIsSubmitting(false);
                return;
            }

            const response = await fetch("/api/reviews", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${user.token}`
                },
                body: JSON.stringify({
                    userId: user.id,
                    productId,
                    rating,
                    comment: comment.trim()
                })
            });

            if (response.ok) {
                // Reset form
                setRating(0);
                setComment("");
                onSubmitSuccess();
            } else {
                const errorData = await response.json();
                console.log(errorData);
                setError(errorData.message || "Failed to submit review");
            }
        } catch (err) {
            console.error("Error submitting review:", err);
            setError("An error occurred while submitting your review");
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <Card className="border-zinc-200 dark:border-zinc-800">
            <CardHeader>
                <CardTitle className="text-xl">Write a Review</CardTitle>
            </CardHeader>
            <CardContent>
                <form onSubmit={handleSubmit} className="space-y-4">
                    <div>
                        <Label htmlFor="rating" className="mb-2 block">
                            Your Rating
                        </Label>
                        <StarRating
                            rating={rating}
                            interactive
                            onRatingChange={setRating}
                            size={28}
                            className="mb-1"
                        />
                        {rating > 0 && (
                            <p className="text-sm text-zinc-500 dark:text-zinc-400 mt-1">
                                {rating} star{rating !== 1 ? 's' : ''}
                            </p>
                        )}
                    </div>

                    <div>
                        <Label htmlFor="comment" className="mb-2 block">
                            Your Review
                        </Label>
                        <Textarea
                            id="comment"
                            value={comment}
                            onChange={(e) => setComment(e.target.value)}
                            placeholder="Share your experience with this product..."
                            rows={4}
                            className="resize-none focus-visible:border-blue-600 focus-visible:ring-1 focus-visible:ring-blue-600"
                        />
                    </div>

                    {error && (
                        <p className="text-sm text-red-600 dark:text-red-400">
                            {error}
                        </p>
                    )}

                    <Button
                        type="submit"
                        disabled={isSubmitting}
                        className="w-full bg-yellow-500 hover:bg-yellow-600 text-black font-medium"
                    >
                        {isSubmitting ? "Submitting..." : "Submit Review"}
                    </Button>
                </form>
            </CardContent>
        </Card>
    );
}
