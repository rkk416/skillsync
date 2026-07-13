# SkillSync UI Style Guide

## Foundation

| Property | Standard |
| --- | --- |
| Window size | 1200 x 800 pixels |
| Theme | Light |
| Primary color | `#2563EB` |
| Accent color | `#0EA5E9` |
| Secondary color | `#FFFFFF` |
| Background color | `#F8FAFC` |
| Border color | `#E5E7EB` |
| Font family | Segoe UI |

## Layout and Components

- Use a left sidebar for primary navigation.
- Use a top utility bar for page title, search, notifications, and profile actions.
- Use rounded corners for buttons.
- Use rounded corners for cards.
- Apply consistent spacing between related components and layout regions.
- Use the shared values in `skillsync.utils.UIConstants` when building JavaFX views.
- Use `skillsync.utils.ViewFactory` for shared cards, metric cards, progress cards, badges, tags, empty states, search fields, and page shells.

## Enterprise UI Rules

- Dashboard, Analytics, and Recommendation Center should use full, purposeful layouts with no blank pages.
- Prefer reusable cards for statistics, summaries, insights, quick actions, and recommendation details.
- Use preview data only inside the view layer when live repository data is unavailable.
- Keep controllers lightweight and avoid adding business logic to JavaFX views.
- Maintain a quiet professional palette: deep blue primary, sky blue accent, white cards, light-gray background, and semantic status colors.
