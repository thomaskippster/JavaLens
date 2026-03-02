# 📱 JavaLens - Pixel 9 Exclusive Edition

**JavaLens** is a powerful Native Android utility designed exclusively for the **Google Pixel 9**. It leverages on-device AI (Gemini Nano) and the Tensor G4 NPU to extract, repair, and manage Java source code directly from camera feeds, videos, or static images.

## 🚀 Key Features

- **Live OCR Scan**: Real-time Java code extraction via CameraX and ML Kit.
- **Smart Stitching Engine**: Seamlessly merges overlapping code blocks during scrolling (Suffix-Prefix Matching).
- **On-Device AI (Gemini Nano)**: Automatically repairs OCR syntax errors and categorizes code snippets locally.
- **Snippet Vault**: A high-contrast "Cyber-Dark" library to store and search your scanned Java classes.
- **Video Import**: Parse MP4 files frame-by-frame using the NPU for offline code extraction.
- **Shared Clipboard**: One-tap copy to the Android system clipboard for cross-device usage.
- **GitHub Integration**: Direct dashboard to push your local snippets to a GitHub repository.

## 🛠 Tech Stack

- **Platform**: Native Android (Kotlin)
- **UI**: Jetpack Compose with Custom "Cyber-Dark" Design System
- **Architecture**: MVVM + Clean Architecture
- **AI/ML**: Google ML Kit (OCR) & Gemini Nano Integration (AICore)
- **Database**: Room Persistence Library

## 📦 Installation & Setup

1. **Clone the repository**:
   ```bash
   git clone https://github.com/thomaskippster/javalens.git
   ```

2. **Open in Android Studio**:
   Import the project as a Gradle project.

3. **Build & Run**:
   Connect your **Pixel 9** (or compatible device) and click "Run" in Android Studio to build and start the app.

## 🎨 Design System

- **Background**: `#000000` (True Black)
- **Primary**: `#6366F1` (Neon Indigo)
- **Accent**: `#10B981` (Neon Emerald)
- **Surface**: `#0D1117` (Cyber Slate)

---
Developed by [thomaskippster](https://github.com/thomaskippster)
