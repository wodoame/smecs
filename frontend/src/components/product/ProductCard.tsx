import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardAction,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";

export interface ProductCardProps {
  image: string;
  title: string;
  description: string;
  price: string;
  featured?: boolean;
  onView?: () => void;
}

export function ProductCard({
  image,
  title,
  description,
  price,
  featured = false,
  onView,
}: ProductCardProps) {
  return (
    <Card className="relative mx-auto w-full max-w-sm pt-0 flex flex-col h-full shadow-none border-zinc-200 dark:border-zinc-800">
      <img
        src={image}
        alt={title}
        className="relative z-20 aspect-video w-full object-cover"
      />
      <div className="flex-1 flex flex-col">
        <CardHeader className="pb-2">
          <CardAction>
            {featured && <Badge variant="secondary">Featured</Badge>}
          </CardAction>
          <div className="space-y-1">
            <CardTitle>{title}</CardTitle>
            <CardDescription>{description}</CardDescription>
            <div className="text-lg font-semibold text-left">{price}</div>
          </div>
        </CardHeader>
        <div className="flex-1" />
      </div>
      <CardFooter className="w-full pt-2 pb-4 px-4 mt-auto">
        <Button className="w-full rounded-b-md" onClick={onView}>
          View Product
        </Button>
      </CardFooter>
    </Card>
  );
}
