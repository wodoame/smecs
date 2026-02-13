import { Star } from "lucide-react";
import { cn } from "@/lib/utils";

interface StarRatingProps {
    rating: number;
    maxRating?: number;
    size?: number;
    interactive?: boolean;
    onRatingChange?: (rating: number) => void;
    className?: string;
}

export function StarRating({
    rating,
    maxRating = 5,
    size = 20,
    interactive = false,
    onRatingChange,
    className
}: StarRatingProps) {
    const handleClick = (selectedRating: number) => {
        if (interactive && onRatingChange) {
            onRatingChange(selectedRating);
        }
    };

    return (
        <div className={cn("flex items-center gap-0.5", className)}>
            {Array.from({ length: maxRating }, (_, index) => {
                const starValue = index + 1;
                const isFilled = starValue <= rating;
                const isPartial = starValue > rating && starValue - 1 < rating;
                const partialPercentage = isPartial ? ((rating % 1) * 100) : 0;

                return (
                    <button
                        key={index}
                        type="button"
                        onClick={() => handleClick(starValue)}
                        disabled={!interactive}
                        className={cn(
                            "relative transition-transform",
                            interactive && "cursor-pointer hover:scale-110 active:scale-95",
                            !interactive && "cursor-default"
                        )}
                        aria-label={`${starValue} star${starValue !== 1 ? 's' : ''}`}
                    >
                        {isPartial ? (
                            <div className="relative" style={{ width: size, height: size }}>
                                <Star
                                    size={size}
                                    className="absolute inset-0 text-zinc-300 dark:text-zinc-700"
                                    fill="currentColor"
                                />
                                <div
                                    className="absolute inset-0 overflow-hidden"
                                    style={{ width: `${partialPercentage}%` }}
                                >
                                    <Star
                                        size={size}
                                        className="text-yellow-500"
                                        fill="currentColor"
                                    />
                                </div>
                            </div>
                        ) : (
                            <Star
                                size={size}
                                className={cn(
                                    isFilled ? "text-yellow-500" : "text-zinc-300 dark:text-zinc-700"
                                )}
                                fill={isFilled ? "currentColor" : "none"}
                                strokeWidth={isFilled ? 0 : 2}
                            />
                        )}
                    </button>
                );
            })}
        </div>
    );
}
