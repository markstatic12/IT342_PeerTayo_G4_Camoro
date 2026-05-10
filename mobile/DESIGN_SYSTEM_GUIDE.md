# Mobile App - Visual Design Guide

## Design System Overview

### Color Palette

#### Primary Colors
```
Primary Background:  #080b14 (Dark Navy)
Secondary BG:        #0f1623 (Lighter Navy)
Surface/Cards:       #111827 (Input fields, containers)
```

#### Accent Colors
```
Blue Primary:    #3b82f6 (Main accent for CTA buttons)
Blue Light:      #60a5fa (Hover states)
Blue Dark:       #1e40af (Active states)
```

#### Text Colors
```
Primary Text:      #f8fafc (Bright white - headlines, body)
Secondary Text:    #94a3b8 (Muted gray - secondary content)
Muted Text:        #64748b (Darker gray - tertiary)
Dim Text:          #475569 (Subtlest gray - placeholders, hints)
```

#### Border Colors
```
Subtle Border:     #1e2d3d
Normal Border:     #2a3f54
```

#### Status Colors
```
Success:  #22c55e (Green)
Error:    #ef4444 (Red)
Warning:  #f97316 (Orange)
```

---

## Component Styles

### Buttons

#### Primary Button (Sign In / Sign Up / Get Started)
```
Background:     #3b82f6 (Blue)
Text Color:     #ffffff (White)
Height:         52dp (mobile optimized)
Padding:        Top/Bottom 12dp, Left/Right 16dp
Border Radius:  10dp
Shadow:         0 4px 20px rgba(59,130,246,0.3)
Font Weight:    Bold (700)
Font Size:      13sp
```

**States:**
- Default: Blue background, white text
- Hover: Slightly darker blue, elevated shadow
- Active: Pressed state, reduced shadow
- Disabled: 50% opacity

#### Secondary Button (Outlined)
```
Background:     #111827 (Dark surface)
Border:         1px solid #2a3f54 (Normal border)
Text Color:     #94a3b8 (Secondary text)
Height:         50dp
```

**States:**
- Hover: Blue-tinted background, blue border
- Active: Full blue background transition

### Form Inputs

#### Text Input Fields
```
Background:     #111827 (Dark surface)
Border:         1px solid #1e2d3d (Subtle)
Height:         50dp
Padding:        Horizontal 13dp, Vertical 10dp
Text Color:     #f8fafc (Primary)
Hint Color:     #475569 (Dim)
Font Size:      13sp
```

**Focus State:**
```
Background:     #06131f (Slightly darker)
Border Color:   #3b82f6 (Blue)
Box Shadow:     0 0 0 3px rgba(59,130,246,0.1)
Glow:           0 0 20px rgba(59,130,246,0.07)
```

#### Input Labels
```
Font Size:      11sp
Font Weight:    700 (Bold)
Color:          #64748b (Muted)
Text Transform: None
Letter Spacing: 0
```

#### Icons
```
Color:          #475569 (Dim)
Size:           20dp (standard Material icon)
Tint:           Matches input text color on focus
```

#### Eye Toggle (Password)
```
Normal Color:   #334155
Hover Color:    #3b82f6
Size:           20dp
```

### Dividers

#### Form Divider (or/and separator)
```
Line Height:    1px
Color:          #1e2d3d (Subtle border)
Spacing:        18px above/below
Text:           "or" at 11sp, bold, #475569
```

### Error Messages

#### Error Display
```
Background:     #1a1a1a (Very dark)
Text Color:     #fca5a5 (Light red)
Font Size:      12sp
Font Weight:    600 (Semibold)
Padding:        10dp all sides
Border:         Optional: 1px solid rgba(239,68,68,0.2)
Margin:         24dp top, 2dp sides
Animation:      Fade in on error
Visibility:     Auto-hidden when input changes
```

---

## Page Layouts

### Landing Page Layout

```
┌─────────────────────────────────┐
│  PeerTayo    [Sign In] [Sign Up] │ ← Navigation Bar (64dp)
├─────────────────────────────────┤
│                                  │
│  • Structured · Private ...      │ ← Hero Badge
│                                  │
│  Peer Evaluation.                │
│  Reimagined for Real Teams.      │ ← Hero Title (32sp bold)
│                                  │
│  PeerTayo transforms how teams   │
│  assess each other...            │ ← Hero Subtitle (14sp muted)
│                                  │
│  [Get Started Free]              │ ← Primary CTA
│  [See How It Works]              │ ← Secondary CTA
│                                  │
├─────────────────────────────────┤
│  Why Choose PeerTayo?            │ ← Section Title
│  ┌─────────────────────────────┐ │
│  │ 🔲 Structured Feedback      │ │ ← Feature Cards
│  │    Capture meaningful...    │ │
│  └─────────────────────────────┘ │
│  ┌─────────────────────────────┐ │
│  │ 🔒 Complete Privacy         │ │
│  │    Anonymous evaluations... │ │
│  └─────────────────────────────┘ │
│  ┌─────────────────────────────┐ │
│  │ 📊 Actionable Insights      │ │
│  │    Get comprehensive...     │ │
│  └─────────────────────────────┘ │
│                                  │
├─────────────────────────────────┤
│  Ready to Transform...?          │ ← CTA Section
│  Join thousands of teams...      │
│  [Create Free Account]           │
│                                  │
├─────────────────────────────────┤
│  PeerTayo                        │ ← Footer
│  © 2024 PeerTayo                 │
└─────────────────────────────────┘
```

### Login Page Layout

```
┌─────────────────────────────────┐
│                                  │ ← 40dp spacing
│        PeerTayo                  │ ← Brand (28sp bold)
│                                  │
│     Welcome back                 │ ← Heading (24sp bold)
│  Sign in to your PeerTayo...     │ ← Subtitle (13sp dim)
│                                  │
│  [Error message if any]          │ ← Error display (hidden by default)
│                                  │
│  Email address                   │ ← Label (11sp muted)
│  ┌──────────────────────────────┐│
│  │✉️ you@example.com            ││ ← Input field (50dp height)
│  └──────────────────────────────┘│
│                                  │
│  Password                        │
│  ┌──────────────────────────────┐│
│  │🔒 ••••••••••••••• 👁️         ││ ← Password toggle
│  └──────────────────────────────┘│
│                                  │
│       ⏳ [Signing in...]         │ ← Progress bar (hidden/shown on load)
│                                  │
│     [Sign In Button]             │ ← Primary button (52dp)
│                                  │
│   ━━━━━ or ━━━━━                 │ ← Divider
│                                  │
│  [📧 Continue with Google]       │ ← Secondary button (50dp)
│                                  │
│  Don't have an account?          │ ← Footer text (12sp dim)
│  Create one (link)               │
│                                  │ ← 40dp spacing
└─────────────────────────────────┘
```

### Register Page Layout

```
┌─────────────────────────────────┐
│                                  │ ← 40dp spacing
│        PeerTayo                  │ ← Brand (28sp bold)
│                                  │
│    Create an account             │ ← Heading (24sp bold)
│  Get started with PeerTayo...    │ ← Subtitle (13sp dim)
│                                  │
│  [Error message if any]          │ ← Error display
│                                  │
│  First name           Last name  │
│  ┌─────────────┐ ┌─────────────┐│
│  │👤 Juan      │ │👤 Dela Cruz ││ ← Two-column on wider screens
│  └─────────────┘ └─────────────┘│
│                                  │
│  Email address                   │
│  ┌──────────────────────────────┐│
│  │✉️ you@example.com            ││
│  └──────────────────────────────┘│
│                                  │
│  Password                        │
│  ┌──────────────────────────────┐│
│  │🔒 At least 6 characters  👁️  ││
│  └──────────────────────────────┘│
│                                  │
│  Confirm password                │
│  ┌──────────────────────────────┐│
│  │🔒 Re-enter your password  👁️  ││
│  └──────────────────────────────┘│
│                                  │
│       ⏳ [Creating account...]  │ ← Progress bar
│                                  │
│   [Create Account Button]        │ ← Primary button (52dp)
│                                  │
│   ━━━━━ or ━━━━━                 │ ← Divider
│                                  │
│  [📧 Continue with Google]       │ ← Secondary button (50dp)
│                                  │
│  Already have an account?        │ ← Footer text (12sp dim)
│  Sign in (link)                  │
│                                  │ ← 40dp spacing
└─────────────────────────────────┘
```

---

## Typography Scale

```
Headlines:
  Hero Title:      32sp, Bold (800), Leading 1.16
  Page Title:      24sp, Bold (800), Leading 1.2
  Section Title:   22sp, Bold (800)

Body:
  Regular Body:    14sp, Regular (400), Leading 1.6
  Form Label:      13sp, Regular (400)
  Description:     13sp, Regular (400), Muted color
  Caption:         12sp, Regular (400), Dim color

UI Elements:
  Button Text:     13sp, Bold (700)
  Form Label:      11sp, Bold (700), Uppercase
  Helper Text:     11sp, Regular (400), Dim color
```

---

## Spacing Scale

```
Minimal:          4dp
Small:            8dp
Base:             12dp
Medium:           16dp
Large:            20dp
XL:               24dp
2XL:              28dp
3XL:              32dp
4XL:              40dp
```

### Typical Spacing in Forms
```
Section Title to First Field:    28-32dp
Field to Field:                  16dp
Button to Divider:               20dp
Divider to Next Button:          16dp
Buttons to Footer Link:          20dp
Page Horizontal Padding:         24dp
Page Vertical Padding (top):     40dp
Page Vertical Padding (bottom):  40dp
```

---

## Interaction States

### Button States

#### Primary Button
```
Idle:      #3b82f6, shadow: 0 4px 20px rgba(59,130,246,0.3)
Hover:     #3085f0 (darker), shadow: 0 6px 28px rgba(59,130,246,0.46)
           Transform: translateY(-1px)
Active:    #276ABC (even darker), shadow: 0 2px 12px rgba(59,130,246,0.28)
           Transform: translateY(0)
Disabled:  #3b82f6 @ 50% opacity
Loading:   Text changes, button disabled, show progress bar
```

#### Form Input Focus
```
On Focus:
  - Border color changes to #3b82f6
  - Background lightens slightly
  - Box shadow appears (0 0 0 3px rgba(59,130,246,0.1))
  - Glow effect (0 0 20px rgba(59,130,246,0.07))
```

### Error States

#### Input with Error
```
Border:        1px solid #ef4444 (Red)
Background:    #111827 (unchanged)
Error Text:    Display below/above input
Animation:     Fade in (150ms)
Clear Trigger: User starts typing new input
```

---

## Animations & Transitions

### Page Transitions
```
Enter:  Fade up + slight scale (100-150ms)
Exit:   Fade down (75-100ms)
Easing: ease-out cubic-bezier(0.22, 1, 0.36, 1)
```

### Button Interactions
```
Shimmer Effect:  Left-to-right light reflection (550ms)
Ripple Effect:   Material Design ripple on tap
Transform:       translateY(±1px) on hover/active
Duration:        150ms (transitions), 550ms (shimmer)
```

### Form Interactions
```
Error Appearance: Fade in (150ms ease)
Error Clear:      Fade out on input change
Placeholder:      Dim gray color (#475569)
Focus Highlight:  Smooth border/shadow transition (220ms)
```

---

## Accessibility Considerations

### Touch Targets
- Minimum height: 48dp (recommended to 50-52dp)
- Minimum width: 48dp
- Spacing between targets: minimum 8dp

### Color Contrast
- Text on background: WCAG AA minimum (4.5:1)
- Primary text: #f8fafc on #080b14 = 14.2:1 ✓
- Secondary text: #94a3b8 on #080b14 = 6.1:1 ✓
- Error text: #fca5a5 on #1a1a1a = 5.8:1 ✓

### Typography
- Minimum font size: 11sp (for captions)
- Readable line spacing: 1.5-1.8
- Maximum line length: ~60 characters (mobile)

---

## Dark Mode

The entire app is designed in **dark mode first**:
- All colors are optimized for dark backgrounds
- No light theme toggle needed (provides better power savings on OLED)
- If light theme support is added, use Material 3 color tokens for automatic conversion

---

## Implementation Notes

### Material Design 3 Integration
- Uses Material Design 3 components
- Follows Material Design guidelines for spacing/typography
- Implements Material You color system where applicable
- Uses AndroidX Material library components

### ViewBinding Support
- All views use ViewBinding (no findViewById)
- Auto-generated binding classes from layout XML
- Type-safe and null-safe

### Responsive Design
- Layouts are responsive and work on various screen sizes
- Form fields stack vertically on narrow screens
- Touch targets remain 50+ dp height
- Side-by-side fields reduce to single column on small devices

---

## Future Enhancements

### Possible Improvements
1. Add haptic feedback on button presses
2. Implement Loading skeleton screens
3. Add swipe gestures for form navigation
4. Support biometric authentication
5. Add animations for form entry/exit
6. Implement onboarding sequences
7. Add accessibility service for screen readers
8. Support RTL languages

---

## Design System Files

- **colors.xml**: Color definitions
- **themes.xml**: Theme and component styles
- **Layout XML Files**:
  - activity_landing.xml
  - activity_login.xml
  - activity_register.xml
  - activity_main.xml

All color values, dimensions, and styles are centralized for easy theming.
