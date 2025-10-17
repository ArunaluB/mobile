# ReChargeMe Design System & Color Palette

## 🎨 Color Palette Overview

This design system is inspired by the modern, sleek UI of the ReChargeMe EV Charging application, featuring a dark theme with vibrant teal accents.

### Primary Brand Colors
```
Primary Teal:        #26D0CE (Main brand color, used for CTAs, highlights)
Primary Dark:        #1DB9B0 (Darker variant for pressed states)
Primary Variant:     #00BFA5 (Alternative teal shade)
Accent:              #26D0CE (Same as primary for consistency)
Accent Light:        #5DE3E1 (Lighter teal for highlights)
```

### Background Colors (Dark Theme)
```
Background:          #0A0E27 (Main app background - deep navy)
Background Light:    #121826 (Slightly lighter background)
Surface:             #1E2337 (Card backgrounds, elevated elements)
Surface Elevated:    #2A3142 (Higher elevation surfaces)
Surface Variant:     #252B3D (Alternative surface color)
```

### Card & Container Colors
```
Card Background:     #1E2337 (Standard card background)
Card Background Dark:#161A2E (Darker card variant)
Card Stroke:         #2D3548 (Border color for cards)
```

### Text Colors
```
Text Primary:        #FFFFFF (Main text - white)
Text Secondary:      #9CA3AF (Secondary text - light gray)
Text Tertiary:       #6B7280 (Tertiary text - gray)
Text Hint:           #4B5563 (Hint text - dark gray)
Text on Primary:     #FFFFFF (Text on colored backgrounds)
```

### Accent & Highlight Colors
```
Teal Accent:         #26D0CE (Main teal accent)
Teal Light:          #5DE3E1 (Light teal)
Teal Dark:           #1DB9B0 (Dark teal)
Electric Blue:       #4A90E2 (Blue accent for stats)
Electric Purple:     #7B68EE (Purple accent for variety)
```

### Gradient Colors
```
Gradient Start:      #26D0CE (Teal start)
Gradient Mid:        #1DB9B0 (Teal middle)
Gradient End:        #00A896 (Teal-green end)
Gradient Purple Start:#7B68EE (Purple gradient start)
Gradient Purple End:  #5A4FCF (Purple gradient end)
```

### Status Colors
```
Success:             #00E676 (Approved, success states)
In Progress:         #FFB300 (Pending, warning states)
Completed:           #26D0CE (Completed tasks - teal)
Cancelled:           #FF5252 (Error, cancelled states)
Pending:             #78909C (Waiting states)
```

### Semantic Colors
```
Success Light:       #69F0AE (Light green variant)
Error:               #FF5252 (Red for errors)
Error Light:         #FF8A80 (Light red variant)
Warning:             #FFB300 (Amber for warnings)
Warning Light:       #FFD54F (Light amber variant)
Info:                #4A90E2 (Blue for information)
Info Light:          #82B1FF (Light blue variant)
```

### UI Element Colors
```
Divider:             #2D3548 (Standard divider)
Divider Light:       #374151 (Lighter divider)
Overlay:             #80000000 (50% black overlay)
Overlay Dark:        #CC000000 (80% black overlay)
Ripple:              #4026D0CE (25% teal ripple effect)
```

### Button Colors
```
Button Primary:      #26D0CE (Primary button)
Button Pressed:      #1DB9B0 (Pressed state)
Button Secondary:    #2A3142 (Secondary button)
Button Disabled:     #374151 (Disabled state)
```

### Icon Colors
```
Icon Primary:        #26D0CE (Primary icons)
Icon Secondary:      #9CA3AF (Secondary icons)
Icon on Surface:     #FFFFFF (Icons on colored backgrounds)
```

### Progress & Battery Colors
```
Battery Full:        #00E676 (Green - full charge)
Battery Medium:      #FFB300 (Amber - medium charge)
Battery Low:         #FF5252 (Red - low charge)
Progress Bar:        #26D0CE (Teal progress)
Progress Background: #2A3142 (Progress track)
```

## 🎯 Design Principles

### 1. **Modern Dark Theme**
- Primary dark background (#0A0E27) creates a sophisticated, energy-efficient interface
- Reduces eye strain for users
- Perfect for automotive/EV applications

### 2. **Vibrant Teal Accent**
- Teal (#26D0CE) represents electricity, energy, and eco-friendliness
- Creates strong visual hierarchy
- Makes interactive elements immediately recognizable

### 3. **Layered Elevation**
- Multiple surface levels create depth
- Cards float above the background
- Gradients add visual interest

### 4. **High Contrast Text**
- White text on dark backgrounds ensures readability
- Secondary text uses sufficient contrast ratios (WCAG AA compliant)
- Hierarchy through color and weight

### 5. **Gradient Accents**
- Smooth teal-to-teal-green gradients for primary elements
- Purple gradients for variety and stats
- Creates modern, premium feel

## 📐 Component Styling

### Cards
- **Corner Radius**: 16-24dp (rounded, modern)
- **Elevation**: 4-12dp (creates depth)
- **Background**: Surface colors with subtle borders
- **Stroke**: 1dp when needed for definition

### Buttons
- **Primary**: Gradient teal background, white text
- **Corner Radius**: 12dp
- **Height**: 56-60dp
- **Elevation**: 4dp
- **Text**: Bold, uppercase with letter spacing

### Text Fields
- **Style**: Outlined boxes
- **Corner Radius**: 12dp
- **Stroke**: 2dp when focused
- **Icons**: Teal colored start/end icons
- **Background**: Surface color

### Icons
- **Size**: 24-64dp based on usage
- **Color**: Teal for primary, gray for secondary
- **Style**: Material Design icons

## 🎨 Gradient Applications

### Header Gradients
```xml
<gradient
    android:angle="135"
    android:startColor="#26D0CE"
    android:centerColor="#1DB9B0"
    android:endColor="#00A896" />
```

### Button Gradients
```xml
<gradient
    android:angle="135"
    android:startColor="#26D0CE"
    android:endColor="#1DB9B0" />
```

### Stats Card Gradients
- **Blue Card**: #4A90E2 → #2A5FAE
- **Purple Card**: #7B68EE → #5A4FCF
- **Orange Card**: #FF6B35 → #F7931E
- **Green Card**: #00E676 → #00C853

## 💡 Usage Guidelines

### When to Use Teal (#26D0CE)
- Primary CTAs (Sign In, Book, Submit)
- Active states (selected tabs, checkboxes)
- Progress indicators
- Icons requiring attention
- Links and interactive text

### When to Use White (#FFFFFF)
- Primary headings
- Body text
- Text on colored backgrounds
- Icons on dark backgrounds

### When to Use Gray Variants
- **#9CA3AF**: Subtitles, descriptions, labels
- **#6B7280**: Helper text, timestamps
- **#4B5563**: Placeholder text, disabled states

### When to Use Status Colors
- **Green (#00E676)**: Confirmed bookings, success messages
- **Amber (#FFB300)**: Pending actions, warnings
- **Red (#FF5252)**: Errors, cancellations, critical alerts
- **Blue (#4A90E2)**: Information, stats

## 🔧 Implementation Files

### Core Files
1. **colors.xml** - All color definitions
2. **themes.xml** - Material theme configuration
3. **styles.xml** - Reusable component styles
4. **Drawable Resources** - Gradients, backgrounds, buttons

### Key Drawables
- `bg_gradient_primary.xml` - Primary teal gradient
- `bg_gradient_card.xml` - Card background gradient
- `bg_button_primary.xml` - Primary button with states
- `bg_input_field.xml` - Input field background
- `bg_circle_accent.xml` - Circular teal accent
- `bg_progress_bar.xml` - Progress bar styling

## 🌙 Dark Theme Benefits

1. **Battery Efficiency**: OLED screens consume less power
2. **Reduced Eye Strain**: Better for night-time usage
3. **Premium Feel**: Modern, sophisticated aesthetic
4. **Focus**: Dark backgrounds make content pop
5. **EV Context**: Aligns with eco-friendly, tech-forward brand

## 📱 Application Across Screens

### Login Screen
- Dark background with teal accents
- Gradient logo circle
- Elevated card for form
- Teal primary button

### Home Screen
- Gradient header with teal
- Dark card backgrounds
- Colorful stat cards with gradients
- Quick action cards with subtle colors

### Profile & Settings
- Consistent dark theme
- Teal highlights for active states
- Sectioned cards for organization

## ✨ Special Effects

### Ripple Effects
- Color: `#4026D0CE` (25% opacity teal)
- Applied to all clickable elements

### Elevation Shadows
- Light shadows for depth
- Not too heavy to maintain modern feel

### Transitions
- Smooth color transitions on state changes
- Fade animations for text/images

## 🎯 Accessibility

- **Contrast Ratio**: All text meets WCAG AA standards
- **Touch Targets**: Minimum 48dp for buttons
- **Color Blind Safe**: Uses multiple indicators (not just color)
- **Dark Mode**: Native dark theme implementation

---

**Design System Version**: 1.0  
**Last Updated**: October 2025  
**Designer**: Arunalu Bamunusinghe  
**App**: ReChargeMe - EV Charging Management
