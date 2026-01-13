# Design System

This document outlines the visual design language for the Smart E-Commerce System (SMECS), inspired by Tailwind CSS.

## Color Palette

### Primary (Blue)
- **Base**: #3b82f6 (blue-500)
- **Hover**: #2563eb (blue-600)
- **Active**: #1d4ed8 (blue-700)
- **Text**: #ffffff

### Secondary (Gray)
- **Base**: #6b7280 (gray-500)
- **Hover**: #4b5563 (gray-600)
- **Active**: #374151 (gray-700)
- **Text**: #ffffff

### Danger (Red)
- **Base**: #ef4444 (red-500)
- **Hover**: #dc2626 (red-600)
- **Active**: #b91c1c (red-700)
- **Text**: #ffffff

### Neutral / Surface
- **Background**: #f3f4f6 (gray-100)
- **Surface**: #ffffff (white)
- **Border**: #e5e7eb (gray-200)

### Text
- **Headings**: #111827 (gray-900)
- **Body**: #374151 (gray-700)
- **Muted**: #6b7280 (gray-500)

## Components

### Buttons

Buttons should have a consistent padding, rounded corners, and clear focus states.

#### Base Button Style (Common)
- **Padding**: 8px 16px (0.5rem 1rem)
- **Border Radius**: 6px (0.375rem)
- **Font Size**: 14px
- **Font Weight**: Bold
- **Cursor**: Hand/Pointer
- **Effect**: Subtle shadow on hover, slight scale on press (if possible in JavaFX)

#### Primary Button (`.button-primary`)
- Background: Primary Base
- Text: White

#### Secondary Button (`.button-secondary`)
- Background: Secondary Base
- Text: White

#### Danger Button (`.button-danger`)
- Background: Danger Base
- Text: White

### Input Fields

Input fields should have comfortable padding and a clean border.

#### Base Input Style (TextField, ComboBox)
- **Padding**: 10px 12px (0.625rem 0.75rem)
- **Border Radius**: 6px (0.375rem)
- **Border**: 1px solid #e5e7eb (gray-200)
- **Background**: #ffffff (white)
- **Font Size**: 14px
- **Focus Ring**: 2px solid #3b82f6 (blue-500)

#### Text Area
- **Container**: Similar border and radius
- **Content Padding**: 10px 12px (applied to content, not scroll container)

### Tables

Table headers should be aligned to the start of the column for better readability.

#### Table Headers
- **Alignment**: Center-Left (Start)
- **Selector**: `.table-view .column-header .label`

#### Table Rows (`.table-row-cell`)
- **Row Height**: 40px

#### Table Cells (`.table-cell`)
- **Padding**: 8px 10px (vertical horizontal)
- **Alignment**: Center-Left

### Badges

Badges are small, pill-shaped labels used to display status information. They are commonly used for stock status indicators.

#### Base Badge Style (`.badge`)
- **Padding**: 4px 10px (0.25rem 0.625rem)
- **Border Radius**: 12px (pill shape)
- **Font Size**: 12px
- **Font Weight**: Bold
- **Alignment**: Center

#### Success Badge (`.badge-success`) - "In Stock"
- **Background**: #d1fae5 (green-100)
- **Text**: #065f46 (green-800)

#### Warning Badge (`.badge-warning`) - "Low Stock"
- **Background**: #fef3c7 (amber-100)
- **Text**: #92400e (amber-800)

#### Danger Badge (`.badge-danger`) - "Out of Stock"
- **Background**: #fee2e2 (red-100)
- **Text**: #991b1b (red-800)

#### Stock Status Logic
- **In Stock**: quantity > 10
- **Low Stock**: quantity > 0 and quantity <= 10
- **Out of Stock**: quantity <= 0

