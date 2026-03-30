# Design System Specification: The Academic Curator

## 1. Overview & Creative North Star
This design system moves beyond the "utility-first" aesthetic of standard educational tools to embrace the **Creative North Star: The Academic Curator.** 

The goal is to transform the student experience from a chaotic to-do list into a refined, editorial environment. By leveraging intentional asymmetry, high-contrast typographic scales, and tonal depth, we create a space that feels authoritative yet breathable. We reject the "boxed-in" feel of traditional apps; instead, we treat the Android canvas as a series of layered, high-end stationery sheets. The system is grounded in Material Design 3 logic but elevated through custom surface treatments that prioritize focus and cognitive ease.

---

## 2. Colors: Tonal Architecture
The palette is rooted in deep professional blues and sophisticated slate greys, accented by a "high-energy" tertiary amber for priority tasks. 

### The "No-Line" Rule
**Explicit Instruction:** Designers are prohibited from using 1px solid borders for sectioning or containment. Boundaries must be defined solely through background color shifts.
*   **Implementation:** Use `surface-container-low` for secondary sections sitting on a `surface` background. The transition between these two tokens is the only "line" allowed.

### Surface Hierarchy & Nesting
Treat the UI as a physical stack of materials. Use the surface-container tiers to create nested depth:
*   **Base Layer:** `surface` (#f7f9fe)
*   **Secondary Content:** `surface_container_low` (#f1f4f9)
*   **Active Cards:** `surface_container_lowest` (#ffffff) for maximum "pop."
*   **Navigation Elements:** `surface_dim` (#d7dadf) for distinct peripheral grounding.

### The "Glass & Gradient" Rule
To break the flat Android mold, use Glassmorphism for floating action buttons (FABs) and overlay menus. Apply `surface_variant` at 80% opacity with a `16px` backdrop-blur. 
*   **Signature Textures:** For primary CTA backgrounds or Hero headers, use a subtle linear gradient from `primary` (#003b5a) to `primary_container` (#1a5276) at a 135-degree angle to add a "soulful" depth that flat hex codes cannot replicate.

---

## 3. Typography: The Editorial Edge
We use a dual-typeface system to balance character with legibility.

*   **Display & Headlines (Manrope):** Chosen for its geometric modernism. Use `display-lg` (3.5rem) with tight tracking (-0.02em) for empty states or dashboard greetings to create a "magazine" feel.
*   **Body & Labels (Inter):** Chosen for its exceptional readability on mobile screens. 
*   **Hierarchy as Brand:** Use `headline-sm` (Manrope, 1.5rem) in `primary` for section titles, paired with `label-md` (Inter, 0.75rem) in `secondary` for metadata. This high-contrast pairing (Serif-like intent vs. Swiss-style utility) establishes an authoritative voice.

---

## 4. Elevation & Depth: Tonal Layering
Traditional drop shadows are largely replaced by **Tonal Layering.**

*   **The Layering Principle:** Place a `surface_container_lowest` card on a `surface_container_low` background. The slight delta in brightness creates a "Soft Lift" that feels native to the screen, not "pasted on."
*   **Ambient Shadows:** If a floating effect is required (e.g., a critical FAB), use an extra-diffused shadow: `Blur: 24px, Y: 8px, Opacity: 6%`. The shadow color must be a tinted version of `on_surface` (#181c20), never pure black.
*   **The "Ghost Border" Fallback:** If a container requires more definition for accessibility, use the "Ghost Border": `outline_variant` at 15% opacity. Standard 100% opaque borders are strictly forbidden.

---

## 5. Components: Refined MD3 Primitives

### Buttons
*   **Primary:** Gradient-filled (Primary to Primary Container), `xl` (1.5rem) roundedness. 
*   **Secondary:** `surface_container_high` fill with `on_surface` text. No border.
*   **Tertiary:** Ghost style using `tertiary` (#4b3200) text for high-priority alerts.

### Input Fields
*   **Style:** Filled containers using `surface_container_highest` with a `2px` bottom-indicator in `primary` on focus. 
*   **Roundedness:** `md` (0.75rem) top corners to maintain the student-friendly approachability.

### Cards & Lists
*   **Rule:** Forbid the use of divider lines. 
*   **Implementation:** Separate list items using `spacing-4` (1rem) vertical gaps or by alternating backgrounds between `surface` and `surface_container_low`.
*   **Student Feature:** Use `tertiary_container` for "Urgent" task cards to provide a warm, amber warmth that signals priority without the anxiety of "Error Red."

### Bottom Navigation
*   **Design:** Use `surface_container_lowest` with a blur-effect background. Active states should use the `primary_fixed_dim` pill shape around icons to follow MD3 standards while feeling premium.

---

## 6. Do’s and Don’ts

### Do:
*   **Do** embrace white space. Use `spacing-12` (3rem) for top-level page margins to let content breathe.
*   **Do** use asymmetrical layouts. For example, left-align headlines while right-aligning action chips to create a sophisticated, non-template look.
*   **Do** use `tertiary` (#4b3200) sparingly as a "highlighter" for key dates or deadlines.

### Don’t:
*   **Don’t** use pure black (#000000) for text. Always use `on_surface` (#181c20) to maintain the slate-blue professional tone.
*   **Don’t** use standard `0.5rem` rounding for everything. Use the scale: `xl` for large cards/buttons, `sm` for small tags. Variety creates visual rhythm.
*   **Don’t** use more than one "floating" element per screen. If a FAB is present, other actions must be anchored to the surface.