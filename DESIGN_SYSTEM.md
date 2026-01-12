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

