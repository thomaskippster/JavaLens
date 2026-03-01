# 📱 JavaLens - Pixel 9 Exclusive Edition

**JavaLens** is a powerful Android utility designed exclusively for the **Google Pixel 9**. It leverages on-device AI (Gemini Nano) and the Tensor G4 NPU to extract, repair, and manage Java source code directly from camera feeds, videos, or static images.

## 🚀 Key Features

- **Live OCR Scan**: Real-time Java code extraction via CameraX and ML Kit.
- **Smart Stitching Engine**: Seamlessly merges overlapping code blocks during scrolling (Suffix-Prefix Matching).
- **On-Device AI (Gemini Nano)**: Automatically repairs OCR syntax errors and categorizes code snippets locally.
- **Snippet Vault**: A high-contrast "Cyber-Dark" library to store and search your scanned Java classes.
- **Video Import**: Parse MP4 files frame-by-frame using the NPU for offline code extraction.
- **Shared Clipboard**: One-tap copy to the Android system clipboard for cross-device usage.
- **GitHub Integration**: Direct dashboard to push your local snippets to a GitHub repository.

## 🛠 Tech Stack

- **Framework**: Expo (SDK 54) / React Native
- **UI**: Custom "Cyber-Dark" Design System with Neon Accents
- **Icons**: Lucide React Native
- **AI/ML**: Google ML Kit (OCR) & Gemini Nano Integration simulation
- **Haptics**: Pixel 9 Haptics Engine integration via `expo-haptics`

## 📦 Installation & Setup

1. **Clone the repository**:
   ```bash
   git clone https://github.com/thomaskippster/javalens.git
   cd javalens
   ```

2. **Install dependencies**:
   ```bash
   npm install --legacy-peer-deps
   ```

3. **Start the development server**:
   ```bash
   npx expo start --clear
   ```

4. **Run on Pixel 9**:
   Open the **Expo Go** app on your Pixel 9 and scan the QR code displayed in your terminal.

## 🎨 Design System

- **Background**: `#000000` (True Black)
- **Primary**: `#6366F1` (Neon Indigo)
- **Accent**: `#10B981` (Neon Emerald)
- **Surface**: `#0D1117` (Cyber Slate)

---
Developed by [thomaskippster](https://github.com/thomaskippster)
