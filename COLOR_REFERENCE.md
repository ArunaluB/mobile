# EV Hub App - Quick Color Reference

## 🎨 Color Palette Cheat Sheet

### Base Colors (Backgrounds)
```
#000000  ████  Primary Background (Black)
#0E0D0A  ████  Deep Black Tint
#201F19  ████  Dark Overlay / Surface
#343432  ████  Dark Panel
#515353  ████  Medium Gray (Secondary Text)
```

### Primary Accents (Neon Green Theme)
```
#4AF13C  ████  Neon Green (Primary)
#00FF66  ████  Bright Green (Success)
#8CA59F  ████  Muted Green-Gray
```

### Secondary Accents
```
#FFFFFF  ████  White (Text/Icons)
#00AFFF  ████  Electric Blue
#FF176A  ████  Vivid Pink
#836DFD  ████  Purple-Blue
```

---

## 🎯 Usage Examples

### Buttons
- **Primary Action**: Neon Green (#4AF13C) background
- **Outlined**: Transparent with Neon Green border
- **Danger**: Pink stroke (#FF176A)

### Cards
- **Background**: Dark Surface (#201F19)
- **Elevated**: Dark Panel (#343432)
- **Borders**: Accent colors or Muted Gray (#8CA59F)

### Text
- **Primary**: White (#FFFFFF)
- **Secondary**: Medium Gray (#515353)
- **Accent**: Neon Green (#4AF13C)

### Gradients
1. **Green Gradient**: #4AF13C → #00FF66
2. **Blue Gradient**: #00AFFF → #006699
3. **Purple Gradient**: #836DFD → #5347A1
4. **Header Gradient**: #343432 → #0E0D0A

---

## 📱 Quick Implementation Tips

### XML Color Reference
```xml
<!-- Primary -->
<item name="colorPrimary">@color/primary</item>  <!-- #4AF13C -->

<!-- Background -->
android:background="@color/background_primary"  <!-- #000000 -->

<!-- Card Surface -->
app:cardBackgroundColor="@color/surface"  <!-- #201F19 -->

<!-- Text -->
android:textColor="@color/text_primary"  <!-- #FFFFFF -->
```

### Gradient Usage
```xml
<!-- Neon Green Gradient -->
android:background="@drawable/gradient_green_card"

<!-- Blue Gradient -->
android:background="@drawable/gradient_blue_card"

<!-- Purple Gradient -->
android:background="@drawable/gradient_purple_card"
```

### Border/Stroke
```xml
<!-- Neon Green Border -->
app:strokeColor="@color/primary"
app:strokeWidth="2dp"

<!-- Subtle Border -->
app:strokeColor="@color/accent_muted"
app:strokeWidth="1dp"
```

---

## ⚡ EV Hub Theme Identity

**Color Philosophy**: 
- Dark backgrounds for premium feel and battery efficiency
- Neon green accents for electric/charging theme
- High contrast for readability
- Modern gradients for depth and dimension

**Visual Language**:
- ⚡ Lightning bolt = Energy/Charging
- Neon Green = Active/Live/Charging
- Blue = Information/Navigation
- Pink = Alerts/Important
- Purple = Special/Premium

---

*Use this as a quick reference when styling new components!*
