# Nuvoco Risk Management App

## 🚀 Features
- Firebase Authentication (Login/Signup)
- Job creation and risk assignment
- Manager approval/reject workflow
- Status tracking (Open, Approved, Closed)
- Web dashboard with real-time Firestore updates

## 📂 Folder Structure
- `app/` → Android app code (build with Android Studio)
- `dashboard/` → Web dashboard (host with Firebase Hosting)

## 🔧 Setup
1. Clone repo:  
   `git clone https://github.com/your-username/nuvoco-risk-management-app.git`
2. Add Firebase config:
   - Put `google-services.json` in `app/`.
   - Update `dashboard/script.js` with your Firebase config.
3. Build APK:
   - Open in Android Studio → Build → Build Bundle/APK → Debug/Release APK.
4. Host dashboard:
   - `cd dashboard`
   - `firebase init hosting`
   - `firebase deploy`
