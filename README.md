# <img src="https://res.cloudinary.com/do2guqnvl/image/upload/v1708365000/rivo/logo_main.png" width="40" height="40" /> Rivo — The Rhythm of Ethiopia

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-purple.svg?style=flat-square&logo=kotlin)](https://kotlinlang.org/)
[![Compose](https://img.shields.io/badge/Jetpack_Compose-Premium_UI-pink.svg?style=flat-square&logo=jetpack-compose)](https://developer.android.com/jetpack/compose)
[![Backend](https://img.shields.io/badge/Node.js-Express-green.svg?style=flat-square&logo=node.js)](https://nodejs.org/)
[![Database](https://img.shields.io/badge/MongoDB-Atlas-darkgreen.svg?style=flat-square&logo=mongodb)](https://www.mongodb.com/atlas)
[![Cloud](https://img.shields.io/badge/Cloudinary-Media_Management-blue.svg?style=flat-square&logo=cloudinary)](https://cloudinary.com/)

**Rivo** (*Ri*hythm + *Vo*ice) represents the ultimate "Music Flow." It is a state-of-the-art Ethiopian music streaming platform built to empower artists and delight listeners with a premium, immersive experience.

---

## 🎨 Immersive Design Philosophy
Rivo isn't just an app; it's a visual journey. We've implemented a **Premium Design System** that features:
- ✨ **Glassmorphism UI**: High-gloss, translucent components that give the app a modern, airy feel.
- 🌈 **Mesh Gradients**: Dynamic, animated background glows that react and shift, creating a living interface.
- 🏔️ **Parallax Effects**: Immersive artist profiles with scrolling depth and smooth transitions.
- 🖤 **Obsidian Dark Mode**: A curated dark-theme palette optimized for visual comfort and high-contrast aesthetics.

---

## ⚡ Key Core Pillars

### 🎵 1. Advanced Music Management (Real Cloud-Sync)
Designed for creators. Rivo uses a robust Cloudinary-integrated pipeline to ensure your art is delivered in high fidelity to listeners everywhere.

*   **Artist Power**: 
    *   **Direct Upload**: Seamless multipart uploads of audio and cover art directly to Cloudinary.
    *   **Creative Metadata**: Full control over genre tagging, album organization, and title management.
    *   **Live Updates**: Edit your tracks in real-time with instant backend synchronization.
*   **Curated Approval**: Admins maintain a high-quality ecosystem by reviewing and moderating all content before it goes global.

### 🎧 2. Personalization & Discovery
For the listener, Rivo is a gateway to the heart of Ethiopian sound.

*   **Dynamic Playlists**: Create, edit, and reorder your own collections with custom cover art support.
*   **The Watchlist**: Never lose a track. Heart any song to save it to your premium "Liked Songs" center.
*   **Smart Explore**: Mesh-gradient-powered discovery engine featuring Trending Now, Fresh Hits, and Featured Artists.
*   **Real-time Analytics**: Artists get insights into their growth with monthly listener stats and play counts.

---

## 🛠️ The Rivo Tech Stack

### **Frontend (The Experience)**
- **Language**: Kotlin 1.9+
- **Architecture**: MVVM (Model-View-ViewModel) + Clean Architecture
- **UI Framework**: Jetpack Compose (Modern Declarative UI)
- **Networking**: Retrofit 2 + OkHttp (with Multipart support)
- **Media**: ExoPlayer (High-performance audio streaming)
- **DI**: Hilt (Dependency Injection)

### **Backend (The Engine)**
- **Runtime**: Node.js & Express
- **Database**: MongoDB Atlas (Scalable NoSQL)
- **File Storage**: Cloudinary (Cloud-native media management)
- **Security**: JWT (Authentication) & bcrypt (Password Hashing)
- **Deployment**: Render / Vercel ready

---

## 🚀 Vision
Our goal with **Rivo** is to digitize the rich musical heritage of Ethiopia while providing local artists with the tools they need to succeed in the modern era. From the first beat of a *Tizita* to the latest *Habesha* heat, Rivo is the platform for your voice.

---

## � Installation & Setup

### Backend
1. `cd backend`
2. `npm install`
3. Create `.env` with `MONGODB_URI`, `CLOUDINARY_CLOUD_NAME`, `API_KEY`, `API_SECRET`, and `JWT_SECRET`.
4. `npm run dev`

### Frontend
1. Open the project in **Android Studio (Hedgehog or newer)**.
2. Sync Gradle files.
3. Update the `BASE_URL` in `NetworkModule.kt` to point to your backend.
4. Run on a physical device or emulator.

---

*Rivo — Where Rhythm Meets Voice.*
