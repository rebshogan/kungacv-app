# KungaCV — Authentic Mobile Career Studio & AI Resume Builder

KungaCV is a native Android adaptation of the `vend-pixel/kungacv` career document studio—combining its **Calm Editorial SaaS** design system, authentic KungaCV logo mark, template-first workflow, structured CV profile editor, synced Cover Letter Studio, and Job Tracker with a multi-turn Gemini Career Coach.

## User Review & Critical Decisions

> [!IMPORTANT]
> We inspected the `vend-pixel/kungacv` repository directly (`Logo.jsx`, `tailwind.config.js`, `globals.css`, `plan.md`, `redux/resume/features.js`, `helper.js`, `templatePresets.js`, `app/resume/page.jsx`, `app/resume/create/page.jsx`, `app/cover-letter/page.jsx`, and `app/job-tracker/page.jsx`) so the Android app faithfully mirrors your web product's visual identity, data model, and mobile UX without generic AI-slop patterns.

- **Confirmed Brand & Design System (from `vend-pixel/kungacv`)**:
  - **Movement**: *Calm Editorial SaaS* — warm paper surfaces (`#FAF9F5` / `#F5F4F0`), near-black ink navigation (`#0A0A0B`), electric-blue actions (`#0070F0` with `#1080F0 → #0040D0` gradient), crisp `#DEDDD6` warm borders, and `Poppins` UI typography paired with `Lora` / document serif & sans template fonts.
  - **Authentic KungaCV Logo**: Recreated directly from `components/Logo.jsx`—the signature blue gradient `"K"` (`#38A0FF → #0040D0`) paired with the folded document and person/lines glyph, plus the `KungaCV` wordmark (`Kunga` in ink/white + `CV` in brand blue).
- **Confirmed Kotlin CV Profile Data Classes**:
  - Dedicated, strongly-typed Kotlin data classes in `CvProfileModels.kt` modeling `CvProfile`, `PersonalInfo` (with `ProfileLinks`), `WorkExperience` (with structured `CvDate` start/end ranges, city/country, employer, jobTitle, description, highlights, and visibility flag), `Education`, `Skill` (with `SkillLevel` proficiency: *Beginner*, *Skillful*, *Experienced*, *Expert*), plus supporting sections (`LanguageItem`, `ProjectItem`, `CertificateItem`, `InterestItem`, `ReferenceItem`) and `CvCustomization`.
- **Confirmed Mobile-First Workspace Architecture**:
  - Adapts the web repository's `/resume` dashboard, `/resume/create` editor + customizer + export-check modal, `/cover-letter` numbered studio (`01 / Details`, `02 / Your message`, `03 / Match your resume`), and `/job-tracker` pipeline into an ergonomic mobile experience, augmented by the multi-turn **Gemini Career Coach**.

---

## 1. Overview & Core Concept

- **What It Does**: Lets job seekers pick a curated KungaCV template, author a structured CV profile with real-time completion progress and section ordering, customize layout/accent/skill visualization styles, run pre-export quality checks before downloading an A4 PDF, craft resume-synced cover letters, track job applications by stage, and collaborate with a multi-turn Gemini Career Coach.
- **Target Audience / Persona**: Mobile-first job seekers, graduates, and professionals (preserving KungaCV's locally-aware sample profiles such as *Thoko Banda*, *Mphatso Chirwa*, *Lina Phiri*, and *Tapiwa Mwale*) who want a fast, encouraging, print-accurate career document studio on Android.
- **Key Value**: Combines the exact workflow and visual craft of the KungaCV web studio with native Android ergonomics, offline-capable A4 PDF rendering, Firestore cloud sync, and context-aware Gemini AI assistance.

---

## 2. User Experience & Visual Design

### Key User Flows

1. **Career Studio Workspace (`/resume` Mobile Adaptation)**:
   - Top `#0A0A0B` (`ink-950`) KungaCV header bar displaying the authentic vector `KungaCV` logo mark, cloud save status pill (*Saved* / *Local draft*), and account menu.
   - Prominent **"Continue where you left off / Pick a look, then make it yours"** next-step banner card (`workspace-next-step`).
   - Category filter chips (*All*, *Single Column*, *Two Column*, *Serif*, *Compact*) filtering KungaCV's template presets (*Classic Blue*, *Sage Sidebar*, *Warm Dots*, *Editorial Frame*, *Lavender Split*, *Quiet Serif*, *Grid Skills*, *Language Bubbles*, *Uppercase Studio*, *Modern Neutral*), plus a *"Start from scratch"* card and *"Your resumes"* saved work list with Duplicate and Delete actions.
2. **Structured CV Profile Editor & Customizer (`/resume/create` Mobile Adaptation)**:
   - Top document bar with inline resume title rename, `Content` vs `Customize` mode switcher, and a floating **"Preview & PDF"** action bar.
   - **Editor Progress Card (`EditorProgress`)**: Displays completion percentage with a `#0070F0` progress bar and quick-jump step pills for *Personal Info*, *Work Experience*, *Education*, *Skills*, and *+ Add Content* (*Projects*, *Languages*, *Certificates*, *Interests*, *References*).
   - **Section Organizer (`SectionOrganizer`)**: Lets users reorder sections and toggle section visibility on the printed CV.
   - **AI-Assisted Bullet & Summary Refinement**: Inside *Personal Info* and *Work Experience*, tapping **"Polish with Gemini"** refines raw notes into clear, measurable achievement statements.
   - **Export Check Dialog**: Mirrors the repo's `exportWarnings` pre-flight check—alerting users if name, email, or experience is missing while still offering *"Keep editing"* or *"Download anyway"*.
3. **Cover Letter Studio (`/cover-letter` Mobile Adaptation)**:
   - Structured into the exact three numbered cards from the repo:
     - `01 / Details — Tailor this letter`: Document name, Recipient name, Company, Role, Subject, and a one-tap **Sync from resume** button.
     - `02 / Your message`: Greeting, Letter body, Closing, **"Refresh from resume experience"**, and **"Tailor with Gemini AI"** (using a pasted job description).
     - `03 / Match your resume`: Style selector cards (*Classic Blue*, *Editorial Serif*, *Warm Accent*) that sync the cover letter's rule line and typography with the active CV.
   - Includes a live A4 `cover-letter-paper` preview with the *"Prepared with KungaCV"* footer and native PDF export.
4. **Job Tracker & Multi-Turn Gemini Career Coach (`/job-tracker` + AI Coach)**:
   - **Job Tracker**: Track applications across stages (*Saved*, *Applied*, *Interviewing*, *Offer*) with company, role, follow-up date, and linked resume/cover letter, plus stage-count summary cards.
   - **Gemini Career Coach**: Scrollable multi-turn chat thread grounded in the active `CvProfile`, with model complexity routing (*Fast* `gemini-3.5-flash-lite`, *General* `gemini-3.8-flash`, *Deep Strategy* `gemini-4-argon`) and one-tap application of suggested rewrites directly into the CV profile.

### Visual Identity & Theme (Anti-Slop Precision)

- **Aesthetic Direction**: *Calm Editorial SaaS* — high-contrast `#0A0A0B` ink chrome, `#F5F4F0` / `#FAF9F5` warm paper surfaces, `#DEDDD6` stone hairline borders, `#0070F0` electric blue interactive accents, and zero generic purple gradients or unstyled gray cards.
- **Color Palette & Tokens (from `tailwind.config.js` & `globals.css`)**:
  - `--background`: `#F5F4F0` (Warm Workspace Paper) / `#F9F9FD` (Canvas) / Dark Mode: `#0A0A0B` (`ink-950`)
  - `--surface`: `#FFFFFF` (Card Surface) & `#FAF9F5` (Field Input Surface) / Dark Mode: `#17171A` (`ink-850`)
  - `--primary`: `#0070F0` (`brand-600` Signature Electric Blue) / Hover & Active: `#0050E0` (`brand-700`)
  - `--primary-gradient`: `#1080F0` (`brand-500`) → `#0040D0` (`brand-800`)
  - `--secondary`: `#0A0A0B` (`ink-950` Near-Black Ink for headers, primary pills, and active navigation tabs)
  - `--border`: `#DEDDD6` (Warm Paper Hairline) & `#D5E4F8` (Soft Brand Info Border on `#EEF6FF` tint)
  - `--text-primary`: `#0A0A0B` (`ink-950`) & `--text-secondary`: `#6B6B76` (`ink-500`)
- **Typography & Hierarchy**:
  - **UI & Controls**: `Poppins` (bundled locally in `res/font/`) with bold/extrabold (`700`/`800`) tracking-tight headings and `0.16em` uppercase `#0070F0` eyebrows (`01 / DETAILS`, `YOUR CAREER STUDIO`).
  - **Document Previews**: Switchable template typography (`Poppins` clean sans, `Lora` / `Playfair Display` editorial serif, and `Plus Jakarta Sans` compact grid).

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Dedicated Kotlin CV Profile Data Classes Mirroring `vend-pixel/kungacv`**
  - *Chosen Approach*: Define clean, immutable Kotlin data classes (`CvProfile`, `PersonalInfo`, `ProfileLinks`, `WorkExperience`, `Education`, `Skill`, `SkillLevel`, `CvDate`, `LanguageItem`, `ProjectItem`, `CertificateItem`, `InterestItem`, `ReferenceItem`, `CvCustomization`) with lossless Firestore map serialization (`toMap()` / `fromMap()`).
  - *Why*: Provides type-safe state management for Jetpack Compose forms while remaining 100% compatible with the repo's nested CV document schema and Firestore security rules.
  - *Alternatives Considered*: Flat string fields, which cannot express structured date ranges (`month`, `year`, `presentYear`), skill proficiency levels, or per-entry visibility toggles.

- **Decision 2: Mobile-First Adaptation of the Desktop Split Workspace**
  - *Chosen Approach*: On phones, adapt the repo's left workspace rail + right canvas into a crisp `#0A0A0B` top header, a pill-segmented workspace switcher (`Workspace`, `CV Editor`, `Cover Letter`, `Tracker & Coach`), and an instant full-sheet **Live Paper Preview & PDF Export** modal with pinch-to-zoom and pre-export warnings. On tablets/foldables, automatically expand into the repo's side-by-side editor and live A4 preview stage.
  - *Why*: Preserves the repo's exact visual language (`workspace-mobile-nav`, `editor-progress`, `section-organizer`, `cover-letter-paper`) without cramming a two-column desktop split onto a narrow screen.

---

## 4. Technical Architecture & Data Strategy *(Technical Reference)*

### Architecture & Component Diagram

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                     UI Layer (Jetpack Compose — KungaCV)                     │
│                                                                              │
│  ┌────────────────────────────────────────────────────────────────────────┐  │
│  │ KungaCvTopHeader (#0A0A0B Ink, Authentic Vector Logo, Save Status Pill)│  │
│  └───────────────────────────────────┬────────────────────────────────────┘  │
│                                      │                                       │
│  ┌────────────────┬──────────────────┼──────────────────┬─────────────────┐  │
│  │ WorkspaceScreen│  CvEditorScreen  │CoverLetterStudio │  Tracker & AI   │  │
│  │ • NextStep Card│ • EditorProgress │ • 01 / Details   │  Coach Screen   │  │
│  │ • Preset Chips │ • Personal/Exp/  │ • 02 / Message   │ • Job Pipeline  │  │
│  │ • 12 Presets   │   Edu/Skills     │ • 03 / Match CV  │ • Multi-Turn    │  │
│  │ • Saved Resumes│ • Customize Mode │ • Live A4 Paper  │   Gemini Chat   │  │
│  └───────┬────────┴────────┬─────────┴────────┬─────────┴────────┬────────┘  │
└──────────┼─────────────────┼──────────────────┼──────────────────┼───────────┘
           │                 │                  │                  │
           ▼                 ▼                  ▼                  ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                     Presentation Layer (MVVM ViewModels)                     │
│                                                                              │
│  ┌──────────────────────────────────────┐  ┌──────────────────────────────┐  │
│  │        ResumeStudioViewModel         │  │     CareerCoachViewModel     │  │
│  │ • Active CvProfile & Section Editors │  │ • Multi-Turn Chat Stream     │  │
│  │ • Template Presets & CvCustomization │  │ • Grounded in Active Profile │  │
│  │ • Export Warnings & A4 PDF Trigger   │  │ • Model Tier Selector        │  │
│  │ • Cover Letters & Job Tracker State  │  │   (Flash-Lite / Flash /Argon)│  │
│  └──────────────────┬───────────────────┘  └──────────────┬───────────────┘  │
└─────────────────────┼─────────────────────────────────────┼──────────────────┘
                      │                                     │
                      ▼                                     ▼
┌─────────────────────────────────────────┐  ┌─────────────────────────────────┐
│     Data & Cloud Persistence Layer      │  │      AI & Native PDF Engine     │
│                                         │  │                                 │
│  ┌───────────────────────────────────┐  │  │  ┌───────────────────────────┐  │
│  │      CvProfileModels.kt &         │  │  │  │   GeminiCareerService     │  │
│  │        KungaCvRepository          │  │  │  │ • Bullet/Summary Polish   │  │
│  │ • /users/{uid}/resumes            │  │  │  │ • Cover Letter Tailoring  │  │
│  │ • /users/{uid}/coverLetters       │  │  │  │ • Streaming Career Coach  │  │
│  │ • /users/{uid}/chatMessages       │  │  │  └───────────────────────────┘  │
│  └───────────────────────────────────┘  │  │  ┌───────────────────────────┐  │
│                                         │  │  │    CvPdfExportEngine      │  │
│                                         │  │  │ • Native A4 PdfDocument   │  │
│                                         │  │  └───────────────────────────┘  │
└─────────────────────────────────────────┘  └─────────────────────────────────┘
```

### Data Model & State (`CvProfileModels.kt`)

- **`CvProfile`** (backed by `ResumeDocument` in `/users/{uid}/resumes/{resumeId}`):
  - **`personalInfo: PersonalInfo`**: `fullName`, `jobTitle`, `email`, `phone`, `address`, `summary`, `links: ProfileLinks` (`website`, `linkedin`, `github`, `portfolio`).
  - **`workExperience: List<WorkExperience>`**: `id`, `jobTitle`, `employer`, `city`, `country`, `startDate: CvDate`, `endDate: CvDate`, `link`, `description`, `highlights: List<String>`, `visible: Boolean`.
  - **`education: List<Education>`**: `id`, `degree`, `school`, `city`, `country`, `startDate: CvDate`, `endDate: CvDate`, `link`, `description`, `visible: Boolean`.
  - **`skills: List<Skill>`**: `id`, `skill` (name), `skillLevel: SkillLevel` (`BEGINNER`, `SKILLFUL`, `EXPERIENCED`, `EXPERT`), `description`, `visible: Boolean`.
  - **Extended Sections**: `languages: List<LanguageItem>`, `projects: List<ProjectItem>`, `certificates: List<CertificateItem>`, `interests: List<InterestItem>`, `references: List<ReferenceItem>`.
  - **`customization: CvCustomization`**: `presetId`, `accentColorHex`, `columns` (`1` or `2`), `layoutDirection` (`"top"`, `"left"`, `"right"`), `skillDisplayStyle` (`"dots"`, `"bar"`, `"bubble"`, `"grid"`), `headingStyle`, `fontFamily`, `sectionOrder: List<String>`.
- **`CoverLetterDocument`**: `id`, `userId`, `resumeId`, `title`, `recipientName`, `companyName`, `targetRole`, `subject`, `greeting`, `body`, `closing`, `templateStyle` (`"classic"`, `"editorial"`, `"warm"`), `jobDescription`, `tone`.
- **`JobApplicationEntry`**: `id`, `company`, `role`, `stage` (`"Saved"`, `"Applied"`, `"Interviewing"`, `"Offer"`), `nextFollowUp`, `linkedResumeTitle`, `linkedCoverLetterTitle`.

### Interactive Component & State Mapping

- **Workspace & Template Presets**:
  - Selecting any of the KungaCV presets (*Classic Blue*, *Sage Sidebar*, *Warm Dots*, *Editorial Frame*, *Lavender Split*, *Quiet Serif*, *Grid Skills*, *Language Bubbles*, *Uppercase Studio*, *Modern Neutral*) either creates a new resume pre-populated with that preset's `CvCustomization` (and optional Malawi starter profile) or applies the style to the active `CvProfile`.
- **Structured Editor (`PersonalInfo`, `WorkExperience`, `Education`, `Skills`)**:
  - Each section form mutates the typed `CvProfile` state in `ResumeStudioViewModel`, recalculates the `EditorProgress` completion percentage (`0%–100%`), and autosaves to Firestore with a live status indicator (*Saving…* → *Saved*).
  - In `Skills`, users add skills with a 4-level proficiency selector (*Beginner*, *Skillful*, *Experienced*, *Expert*) rendered in the live preview as dots, progress bars, bubbles, or a grid according to `CvCustomization.skillDisplayStyle`.
- **Pre-Export Quality Check & Native A4 PDF**:
  - Tapping **Download PDF** runs the repo's pre-export check (`exportWarnings` for missing name, email, or experience/projects). Users can fix the missing fields immediately or proceed with **Download anyway** to generate and share a crisp A4 PDF via `CvPdfExportEngine`.
