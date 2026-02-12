import { useNavigate } from "react-router-dom";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

export interface Category {
    categoryId: number;
    categoryName: string;
    description: string;
    imageUrl: string;
    relatedImageUrls: string[];
}

interface CategoryCardProps {
    category: Category;
}

export function CategoryCard({ category }: CategoryCardProps) {
    const navigate = useNavigate();
    const allImages = [category.imageUrl, ...category.relatedImageUrls];

    const handleCardClick = () => {
        navigate(`/categories/${category.categoryId}`);
    };

    return (
        <Card
            className="w-full shadow-none cursor-pointer transition-all hover:shadow-md hover:border-blue-400/60"
            onClick={handleCardClick}
        >
            <CardHeader>
                <CardTitle>{category.categoryName}</CardTitle>
                <p className="text-sm text-muted-foreground">{category.description}</p>
            </CardHeader>
            <CardContent>
                <div className="w-full overflow-x-auto whitespace-nowrap rounded-md border p-4">
                    <div className="flex w-max space-x-4">
                        {allImages.map((image, index) => (
                            <figure key={index} className="shrink-0">
                                <div className="overflow-hidden rounded-md">
                                    <img
                                        src={image}
                                        alt={`${category.categoryName} image ${index + 1}`}
                                        className="aspect-[3/4] h-fit w-fit object-cover model-card-image"
                                        style={{ maxHeight: '200px' }}
                                    />
                                </div>
                            </figure>
                        ))}
                    </div>
                </div>
            </CardContent>
        </Card>
    );
}
