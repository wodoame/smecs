import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { useNavigate } from "react-router-dom";
import {
  Card,
  CardAction,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";

export interface ProductCardProps {
  id: number;
  image: string;
  title: string;
  description: string;
  price: string;
  featured?: boolean;
  onAddToCart?: () => void;
}

export function ProductCard({
  id,
  image,
  title,
  description,
  price,
  featured = false,
  onAddToCart,
}: ProductCardProps) {
  const navigate = useNavigate();

  const handleNavigate = () => {
    navigate(`/products/${id}`);
  };

  return (
    <Card className="relative mx-auto w-full max-w-sm pt-0 flex flex-col h-full shadow-none border-zinc-200 dark:border-zinc-800">
      <div onClick={handleNavigate} className="cursor-pointer">
        <img
          src={image}
          alt={title}
          className="relative z-20 aspect-video w-full object-cover"
        />
      </div>
      <div className="flex-1 flex flex-col">
        <CardHeader className="pb-2">
          <CardAction>
            {featured && <Badge variant="secondary">Featured</Badge>}
          </CardAction>
          <div className="space-y-1 min-w-0 cursor-pointer" onClick={handleNavigate}>
            <CardTitle className="truncate hover:underline">{title}</CardTitle>
            <CardDescription className="truncate">{description}</CardDescription>
            <div className="text-lg font-semibold text-left">{price}</div>
          </div>
        </CardHeader>
        <div className="flex-1" />
      </div>
      <CardFooter className="w-full pt-2 pb-4 px-4 mt-auto">
        <Button
          className="w-full rounded-b-md bg-yellow-500 hover:bg-yellow-600 text-black"
          onClick={(e) => {
            e.stopPropagation();
            onAddToCart?.();
          }}
        >
          Add to Cart
        </Button>
      </CardFooter>
    </Card>
  );
}
